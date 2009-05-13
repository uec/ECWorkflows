
	package edu.usc.epigenome.workflow;

	import java.io.File;
	import java.util.LinkedList;
	import java.util.List;
	import org.griphyn.vdl.dax.Filename;
	import org.griphyn.vdl.dax.Job;

import edu.usc.epigenome.workflow.DAX.ECDax;
import edu.usc.epigenome.workflow.job.ECJob;
	import edu.usc.epigenome.workflow.job.ecjob.CountFastQJob;
	import edu.usc.epigenome.workflow.job.ecjob.CountPileupJob;
	import edu.usc.epigenome.workflow.job.ecjob.FastQ2BFQJob;
	import edu.usc.epigenome.workflow.job.ecjob.FastQSplitJob;
	import edu.usc.epigenome.workflow.job.ecjob.FilterContamsJob;
	import edu.usc.epigenome.workflow.job.ecjob.GzipJob;
	import edu.usc.epigenome.workflow.job.ecjob.MapJob;
	import edu.usc.epigenome.workflow.job.ecjob.MapMergeJob;
	import edu.usc.epigenome.workflow.job.ecjob.MapViewJob;
	import edu.usc.epigenome.workflow.job.ecjob.PileupJob;
	import edu.usc.epigenome.workflow.job.ecjob.ReadCountJob;
	import edu.usc.epigenome.workflow.job.ecjob.ReadDepthJob;
	import edu.usc.epigenome.workflow.job.ecjob.Sol2SangerJob;
import edu.usc.epigenome.workflow.metadata.GAMetaData;

	public class AlignPileupSimpleDot
	{
		public static void createWorkFlow(ECDax dax)	
		{
			try
			{
				// construct a dax object
				// For every requested lane in this flowcell..
				GAMetaData workFlowParams = dax.getWorkFlowParams();
				
				List<ECJob> mapMergeJobs = new LinkedList<ECJob>();
				for (int i : workFlowParams.getAvailableLanes())
				{
					List<ECJob> mapJobs = new LinkedList<ECJob>();
					List<Sol2SangerJob> fastqJobs = new LinkedList<Sol2SangerJob>();
					
					String laneInputFileName = new File(workFlowParams.getLaneInput(i)).getAbsolutePath();
					System.out.println("Creating processing pipeline for lane " + i + ": " + laneInputFileName);

					// create a fastSplit job
					int splitSize = 0;
					if (workFlowParams.laneIsBisulfite(i))
						splitSize = Integer.parseInt(workFlowParams.getSetting("BisulfiteSplitFactor"));
					else
						splitSize = Integer.parseInt(workFlowParams.getSetting("RegularSplitFactor"));
					FastQSplitJob fastqSplitJob = new FastQSplitJob(laneInputFileName, splitSize);
					dax.addJob(fastqSplitJob);

					// iterate through the output files of fastQsplit jobs to create pipeline
					for (Filename f : fastqSplitJob.getOutputFiles())
					{
						//filter contam job
						String splitFastqOutputFile = f.getFilename();
						FilterContamsJob filterContamJob = new FilterContamsJob(splitFastqOutputFile);
						dax.addJob(filterContamJob);
						dax.addChild(filterContamJob.getID(), fastqSplitJob.getID());

						// sol2sanger job
						
						//get the nocontam file
						String nocontamFile = "";
						for(Filename s : filterContamJob.getOutputFiles())
						{
							if(s.getFilename().contains(".nocontam"))
							{
								nocontamFile = s.getFilename();
							}
						}
						Sol2SangerJob sol2sangerJob = new Sol2SangerJob(nocontamFile);
						dax.addJob(sol2sangerJob);
						dax.addChild(sol2sangerJob.getID(), filterContamJob.getID());
						fastqJobs.add(sol2sangerJob);
						
						// fastq2bfq job
						FastQ2BFQJob fastq2bfqJob = new FastQ2BFQJob(sol2sangerJob.getSingleOutputFile().getFilename());
						dax.addJob(fastq2bfqJob);
						dax.addChild(fastq2bfqJob.getID(), sol2sangerJob.getID());

						// map job. additional input grabbed from hg18.BS.bfa
						MapJob mapJob = new MapJob(fastq2bfqJob.getSingleOutputFile().getFilename(), workFlowParams.getSetting("Lane." + i + ".ReferenceBFA"),  Integer.parseInt(workFlowParams.getSetting("MinMismatches")),
								workFlowParams.laneIsBisulfite(i));
						dax.addJob(mapJob);
						dax.addChild(mapJob.getID(), fastq2bfqJob.getID());
						mapJobs.add(mapJob);

					}
					//for each lane create a countfastq job
					CountFastQJob countFastQJob = new CountFastQJob(fastqJobs, workFlowParams.getSetting("FlowCellName"), i);
					dax.addJob(countFastQJob);
					// mapmerge is child to all the map jobs
					for (Job fastqjob : fastqJobs)
					{
						dax.addChild(countFastQJob.getID(), fastqjob.getID());
					}
					
					// for each lane create a map merge job
					MapMergeJob mapMergeJob = new MapMergeJob(mapJobs, workFlowParams.getSetting("FlowCellName"), i);
					dax.addJob(mapMergeJob);
					// mapmerge is child to all the map jobs
					for (Job map : mapJobs)
					{
						dax.addChild(mapMergeJob.getID(), map.getID());
					}
					mapMergeJobs.add(mapMergeJob);
					
					// crate mapview job, child of mapmerge
					MapViewJob mapViewJob =  new MapViewJob(mapMergeJob.getSingleOutputFile().getFilename(), Integer.parseInt(workFlowParams.getSetting("MaqPileupQ")));;
					dax.addJob(mapViewJob);
					dax.addChild(mapViewJob.getID(), mapMergeJob.getID());
	
					//create pileup job, child of mapview
					PileupJob pileupJob = new PileupJob(mapMergeJob.getSingleOutputFile().getFilename(), workFlowParams.getSetting("Lane." + i +".ReferenceBFA"), Integer.parseInt(workFlowParams
							.getSetting("MaqPileupQ")));;
					dax.addJob(pileupJob);
					dax.addChild(pileupJob.getID(), mapMergeJob.getID());
					
					//create gzip job, child of pileup
					GzipJob gzipjob = new GzipJob(pileupJob.getSingleOutputFile().getFilename());
					dax.addJob(gzipjob);
					dax.addChild(gzipjob.getID(),pileupJob.getID());
									
					//create countPileupJob, child of gziped pileupJob
					CountPileupJob countMonoPileupJob = new CountPileupJob(pileupJob.getSingleOutputFile().getFilename() + ".gz",CountPileupJob.Mononucleotide);
					dax.addJob(countMonoPileupJob);
					dax.addChild(countMonoPileupJob.getID(), gzipjob.getID());
					
					//create countPileupJob, child of gzipped pileupJob
					CountPileupJob countCGPileupJob = new CountPileupJob(pileupJob.getSingleOutputFile().getFilename() + ".gz",CountPileupJob.CGdinucleotide);
					dax.addJob(countCGPileupJob);
					dax.addChild(countCGPileupJob.getID(), gzipjob.getID());
					
					//create countPileupJob, child of gzipped pileupJob
					CountPileupJob countCHPileupJob = new CountPileupJob(pileupJob.getSingleOutputFile().getFilename() + ".gz",CountPileupJob.CHdinucleotide);
					dax.addJob(countCHPileupJob);
					dax.addChild(countCHPileupJob.getID(), gzipjob.getID());
					
					//create countPileupJob, child of gzipped pileupJob
					CountPileupJob countGenomePileupJob = new CountPileupJob(pileupJob.getSingleOutputFile().getFilename() + ".gz",CountPileupJob.RefComposition);
					dax.addJob(countGenomePileupJob);
					dax.addChild(countGenomePileupJob.getID(), gzipjob.getID());
					
					//create readdepth:0, child of gzipped pileupJob
					String genome;
					if(workFlowParams.getSetting("Lane." + i + ".ReferenceBFA").contains("phi")) { genome = "phiX";}
					if(workFlowParams.getSetting("Lane." + i + ".ReferenceBFA").contains("hg18")) { genome = "hg18";}
					else {genome = "hg18";}
					
					ReadDepthJob readdepthJob0 = new ReadDepthJob(pileupJob.getSingleOutputFile().getFilename() + ".gz", workFlowParams.getSetting("FlowCellName"), i,genome, 5000, 0);
					dax.addJob(readdepthJob0);
					dax.addChild(readdepthJob0.getID(), gzipjob.getID());

					//create readdepth:1, child of gzipped pileupJob
					ReadDepthJob readdepthJob1 = new ReadDepthJob(pileupJob.getSingleOutputFile().getFilename() + ".gz", workFlowParams.getSetting("FlowCellName"), i,genome, 5000, 1);
					dax.addJob(readdepthJob1); 
					dax.addChild(readdepthJob1.getID(), gzipjob.getID());
					
					//create readcount, child of gzipped pileupJob
					ReadCountJob readcountJob = new ReadCountJob(pileupJob.getSingleOutputFile().getFilename() + ".gz", workFlowParams.getSetting("FlowCellName"), i, 1000000, 100);
					dax.addJob(readcountJob);
					dax.addChild(readcountJob.getID(), gzipjob.getID());
				}
				MapMergeJob globalMerge1 = new MapMergeJob(mapMergeJobs.subList(0, 4),workFlowParams.getSetting("FlowCellName"), 9);
				MapMergeJob globalMerge2 = new MapMergeJob(mapMergeJobs.subList(5, 7),workFlowParams.getSetting("FlowCellName"), 10);
				dax.addJob(globalMerge1);
				dax.addJob(globalMerge2);
				
				List<MapMergeJob> splitList = new LinkedList<MapMergeJob>();
				splitList.add(globalMerge2);
				splitList.add(globalMerge1);
				int ti=9;
				for(MapMergeJob j : splitList)
				{
					MapMergeJob globalMerge = j;
					for (ECJob map : mapMergeJobs)
					{
						dax.addChild(globalMerge.getID(), map.getID());
					}
					
					
					
					// crate mapview job, child of mapmerge
					MapViewJob mapViewJob =  new MapViewJob(globalMerge.getSingleOutputFile().getFilename(), Integer.parseInt(workFlowParams.getSetting("MaqPileupQ")));;
					dax.addJob(mapViewJob);
					dax.addChild(mapViewJob.getID(), globalMerge.getID());
	
					//create pileup job, child of mapview
					PileupJob pileupJob = new PileupJob(globalMerge.getSingleOutputFile().getFilename(), workFlowParams.getSetting("Lane." + ti +".ReferenceBFA"), Integer.parseInt(workFlowParams
							.getSetting("MaqPileupQ")));;
					dax.addJob(pileupJob);
					dax.addChild(pileupJob.getID(), globalMerge.getID());
					
					//create gzip job, child of pileup
					GzipJob gzipjob = new GzipJob(pileupJob.getSingleOutputFile().getFilename());
					dax.addJob(gzipjob);
					dax.addChild(gzipjob.getID(),pileupJob.getID());
									
					//create countPileupJob, child of gziped pileupJob
					CountPileupJob countMonoPileupJob = new CountPileupJob(pileupJob.getSingleOutputFile().getFilename() + ".gz",CountPileupJob.Mononucleotide);
					dax.addJob(countMonoPileupJob);
					dax.addChild(countMonoPileupJob.getID(), gzipjob.getID());
					ti++;
				}
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}	
		
		public static void usage()
		{
			System.out.println("Usage: program [-dryrun] workflowParameterFile.txt");
			System.out.println("workflowParameterFile.txt: contains all parameters");
			System.out.println("-dryrun: display pbs output, do not run");
			System.exit(0);
		}
		public static void main(String[] args)
		{
			String paramFile = "";
			Boolean dryrun = false;
			//create a dax from the passed in param obj
			if(args.length == 1)
			{
				if((new File(args[0])).exists())
					paramFile = args[0];
				else
					usage();
			}
			else if(args.length==2)
			{
				if(args[0].equals("-dryrun") && (new File(args[1])).exists())
				{
					paramFile = args[1];
					dryrun = true;
				}
				else
					usage();
			}
			else
				usage();
					
			ECDax dax = new ECDax(new GAMetaData(paramFile));
			createWorkFlow(dax);
			dax.saveAsSimpleDot("alignpileup_dax.dot");
			dax.runWorkflow(dryrun);
			dax.saveAsXML("alignpileup_dax.xml");
		}

	}
