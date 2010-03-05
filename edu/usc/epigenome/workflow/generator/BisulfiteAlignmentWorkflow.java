package edu.usc.epigenome.workflow.generator;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.Job;

import edu.usc.epigenome.workflow.DAX.ECDax;
import edu.usc.epigenome.workflow.ECWorkflowParams.specialized.GAParams;
import edu.usc.epigenome.workflow.job.ECJob;
import edu.usc.epigenome.workflow.job.ecjob.CountFastQJob;
import edu.usc.epigenome.workflow.job.ecjob.CountNmerJob;
import edu.usc.epigenome.workflow.job.ecjob.CountPileupJob;
import edu.usc.epigenome.workflow.job.ecjob.FastQ2BFQJob;
import edu.usc.epigenome.workflow.job.ecjob.FastQConstantSplitJob;
import edu.usc.epigenome.workflow.job.ecjob.FilterContamsJob;
import edu.usc.epigenome.workflow.job.ecjob.MapJob;
import edu.usc.epigenome.workflow.job.ecjob.MapMergeJob;
import edu.usc.epigenome.workflow.job.ecjob.Maq2BamJob;
import edu.usc.epigenome.workflow.job.ecjob.PileupJob;
import edu.usc.epigenome.workflow.job.ecjob.PileupToWigJob;
import edu.usc.epigenome.workflow.job.ecjob.QCMetricsJob;
import edu.usc.epigenome.workflow.job.ecjob.ReadCountJob;
import edu.usc.epigenome.workflow.job.ecjob.ReadDepthJob;
import edu.usc.epigenome.workflow.job.ecjob.Sol2SangerJob;

public class BisulfiteAlignmentWorkflow
{
	/**
	 * Creates an AlignPileUp workflow from an empty dax object 
	 * @param dax The ECDAX to which processing jobs will be added
	 */	
	public static void createWorkFlow(GAParams par,Boolean pbsMode, Boolean dryrun)	
	{
		try
		{
			// construct a dax object
			// For every requested lane in this flowcell..
			ECDax dax = new ECDax(par);
			
			//get the params so that we have the input parameters
			GAParams workFlowParams = (GAParams) dax.getWorkFlowParams();
			
			for (int i : workFlowParams.getAvailableLanes())
			{
				if(workFlowParams.getSetting("Lane." + i + ".AlignmentType").toLowerCase().contains("bisulfite"))
				{
					List<ECJob> mapJobs = new LinkedList<ECJob>();
					List<Sol2SangerJob> fastqJobs = new LinkedList<Sol2SangerJob>();
					
					String laneInputFileName;
					if(pbsMode = true)
						laneInputFileName = new File(workFlowParams.getLaneInput(i)).getAbsolutePath();
					else
						laneInputFileName = new File(workFlowParams.getLaneInput(i)).getName();
					
						
					System.out.println("Creating Bisulfite workflow for lane " + i + ": " + laneInputFileName);
	
		
					int splitSize = Integer.parseInt(workFlowParams.getSetting("ClusterSize")) / 8;
					FastQConstantSplitJob fastqSplitJob = new FastQConstantSplitJob(laneInputFileName, splitSize);
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
								workFlowParams.getSetting("Lane." + i + ".AlignmentType"), Integer.parseInt(workFlowParams.getSetting("MaqTrimEnd1")), Integer.parseInt(workFlowParams.getSetting("MaqTrimEnd2")));
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
					
					
					//create nmercount for 3
					CountNmerJob count3mer = new CountNmerJob(fastqJobs, workFlowParams.getSetting("FlowCellName"), i, 3);
					dax.addJob(count3mer);
					// mapmerge is child to all the map jobs
					for (Job fastqjob : fastqJobs)
					{
						dax.addChild(count3mer.getID(), fastqjob.getID());
					}
					
					//create nmercount for 5
					CountNmerJob count5mer = new CountNmerJob(fastqJobs, workFlowParams.getSetting("FlowCellName"), i, 5);
					dax.addJob(count5mer);
					// mapmerge is child to all the map jobs
					for (Job fastqjob : fastqJobs)
					{
						dax.addChild(count5mer.getID(), fastqjob.getID());
					}
					
					//create nmercount for 10
					CountNmerJob count10mer = new CountNmerJob(fastqJobs, workFlowParams.getSetting("FlowCellName"), i, 10);
					dax.addJob(count10mer);
					// mapmerge is child to all the map jobs
					for (Job fastqjob : fastqJobs)
					{
						dax.addChild(count10mer.getID(), fastqjob.getID());
					}
					
					// for each lane create a map merge job
					MapMergeJob mapMergeJob = new MapMergeJob(mapJobs, workFlowParams.getSetting("FlowCellName"), i);
					dax.addJob(mapMergeJob);
					// mapmerge is child to all the map jobs
					for (Job map : mapJobs)
					{
						dax.addChild(mapMergeJob.getID(), map.getID());
					}
					//mapMergeJobs.add(mapMergeJob);
					
					//create qcmetrics job 
					QCMetricsJob qcjob = new QCMetricsJob(workFlowParams.getSetting("tmpDir") + "/" + workFlowParams.getSetting("FlowCellName"), workFlowParams.getSetting("FlowCellName"));
					dax.addJob(qcjob);
	
	
					//create pileup.gz job, child of mapmerge
					PileupJob pileupJob = new PileupJob(mapMergeJob.getSingleOutputFile().getFilename(), workFlowParams.getSetting("Lane." + i +".ReferenceBFA"), Integer.parseInt(workFlowParams
							.getSetting("MaqPileupQ")));;
					dax.addJob(pileupJob);
					dax.addChild(pileupJob.getID(), mapMergeJob.getID());				
				
					//create maq2bamjob, child of mapMerge
					Maq2BamJob maq2bamJob = new Maq2BamJob(mapMergeJob.getSingleOutputFile().getFilename(),workFlowParams.getSetting("Lane." + i +".ReferenceBFA").replace(".bfa", ".fa"));
					dax.addJob(maq2bamJob);
					dax.addChild(maq2bamJob.getID(), mapMergeJob.getID());
					
					//create countPileupJob, child of gziped pileupJob
					CountPileupJob countMonoPileupJob = new CountPileupJob(pileupJob.getSingleOutputFile().getFilename() ,CountPileupJob.Mononucleotide);
					dax.addJob(countMonoPileupJob);
					dax.addChild(countMonoPileupJob.getID(), pileupJob.getID());
					dax.addChild(qcjob.getID(), countMonoPileupJob.getID());
					
					//create countPileupJob, child of gzipped pileupJob
					CountPileupJob countCGPileupJob = new CountPileupJob(pileupJob.getSingleOutputFile().getFilename(),CountPileupJob.CGdinucleotide);
					dax.addJob(countCGPileupJob);
					dax.addChild(countCGPileupJob.getID(), pileupJob.getID());
					dax.addChild(qcjob.getID(), countCGPileupJob.getID());
					
					//create countPileupJob, child of gzipped pileupJob
					CountPileupJob countCHPileupJob = new CountPileupJob(pileupJob.getSingleOutputFile().getFilename(),CountPileupJob.CHdinucleotide);
					dax.addJob(countCHPileupJob);
					dax.addChild(countCHPileupJob.getID(), pileupJob.getID());
					dax.addChild(qcjob.getID(), countCHPileupJob.getID());
					
					//create countPileupJob, child of gzipped pileupJob
					CountPileupJob countGenomePileupJob = new CountPileupJob(pileupJob.getSingleOutputFile().getFilename(),CountPileupJob.RefComposition);
					dax.addJob(countGenomePileupJob);
					dax.addChild(countGenomePileupJob.getID(), pileupJob.getID());
					dax.addChild(qcjob.getID(), countGenomePileupJob.getID());
					
					//create readdepth:0, child of gzipped pileupJob
					String genome;
					if(workFlowParams.getSetting("Lane." + i + ".ReferenceBFA").contains("phi")) { genome = "phiX";}
					else if(workFlowParams.getSetting("Lane." + i + ".ReferenceBFA").contains("hg18")) { genome = "hg18";}
					else if(workFlowParams.getSetting("Lane." + i + ".ReferenceBFA").contains("sacCer")) { genome = "sacCer1";}
					else if(workFlowParams.getSetting("Lane." + i + ".ReferenceBFA").contains("mm")) { genome = "mm9";}
					else {genome = "hg18";}
					
					ReadDepthJob readdepthJob0 = new ReadDepthJob(pileupJob.getSingleOutputFile().getFilename(), workFlowParams.getSetting("FlowCellName"), i,genome, 5000, 0);
					dax.addJob(readdepthJob0);
					dax.addChild(readdepthJob0.getID(), pileupJob.getID());
					dax.addChild(qcjob.getID(), readdepthJob0.getID());
	
					//create readdepth:1, child of gzipped pileupJob
					ReadDepthJob readdepthJob1 = new ReadDepthJob(pileupJob.getSingleOutputFile().getFilename(), workFlowParams.getSetting("FlowCellName"), i,genome, 5000, 1);
					dax.addJob(readdepthJob1); 
					dax.addChild(readdepthJob1.getID(), pileupJob.getID());
					dax.addChild(qcjob.getID(), readdepthJob1.getID());
					
					//create readcount, child of gzipped pileupJob
					ReadCountJob readcountJob = new ReadCountJob(pileupJob.getSingleOutputFile().getFilename(), workFlowParams.getSetting("FlowCellName"), i, Integer.parseInt(workFlowParams.getSetting("randomSubset")), 20);
					dax.addJob(readcountJob);
					dax.addChild(readcountJob.getID(), pileupJob.getID());
					dax.addChild(qcjob.getID(), readcountJob.getID());
					
					
					//pileup to wig job child of gzipped pileupjob
					PileupToWigJob pilewig = new PileupToWigJob(pileupJob.getSingleOutputFile().getFilename(), workFlowParams.getSetting("FlowCellName"), i, 600, 50, 1, 0, 2);
					dax.addJob(pilewig);
					dax.addChild(pilewig.getID(), pileupJob.getID());
				}

			}
			dax.saveAsDot("bisulfite_dax.dot");
			dax.saveAsSimpleDot("bisulfite_dax_simple.dot");
			if(pbsMode)
				dax.runWorkflow(dryrun);
			dax.saveAsXML("bisulfite_dax.xml");
			
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}