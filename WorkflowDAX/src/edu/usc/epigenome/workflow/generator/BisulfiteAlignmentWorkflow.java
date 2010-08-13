package edu.usc.epigenome.workflow.generator;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.Job;

import edu.usc.epigenome.workflow.DAX.ECDax;
import edu.usc.epigenome.workflow.ECWorkflowParams.specialized.GAParams;
import edu.usc.epigenome.workflow.job.ECJob;
import edu.usc.epigenome.workflow.job.ecjob.CountAdapterTrimJob;
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
import edu.usc.epigenome.workflow.job.ecjob.WigToTdfJob;

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
					boolean isPE = workFlowParams.getLaneInput(i).contains(",");
				
					List<Sol2SangerJob> fastqJobs = new LinkedList<Sol2SangerJob>();
					List<FastQ2BFQJob> bfqJobs = new LinkedList<FastQ2BFQJob>();
					List<ECJob> mapJobs = new LinkedList<ECJob>();
					List<String> filterTrimCountFiles = new LinkedList<String>();
					
					String laneInputFileNameR1 = pbsMode ? new File(workFlowParams.getLaneInput(i).split(",")[0]).getAbsolutePath() : new File(workFlowParams.getLaneInput(i).split(",")[0]).getName();
					String laneInputFileNameR2 =  null;
					
					//split Fastq Job. handle paired end and non pbs
					int splitSize = Integer.parseInt(workFlowParams.getSetting("ClusterSize")) / 8;
					FastQConstantSplitJob fastqSplitJob = null;
					if(isPE)
					{
						laneInputFileNameR2 = pbsMode ? new File(workFlowParams.getLaneInput(i).split(",")[1]).getAbsolutePath() : new File(workFlowParams.getLaneInput(i).split(",")[1]).getName();
						fastqSplitJob = new FastQConstantSplitJob(laneInputFileNameR1, laneInputFileNameR2, splitSize);
						System.out.println("Creating bisulfite PE Processing workflow for lane " + i + ": " + laneInputFileNameR1 + " " + laneInputFileNameR2 );
					}
					else
					{
						fastqSplitJob = new FastQConstantSplitJob(laneInputFileNameR1, splitSize);
						System.out.println("Creating bisulfite SR Processing workflow for lane " + i + ": " + laneInputFileNameR1);
					}						
					dax.addJob(fastqSplitJob);
	
		
					// iterate through the output files of fastQsplit jobs to create pipeline
					for (Filename f : fastqSplitJob.getOutputFiles())
					{
						String splitFileName = f.getFilename();
						FilterContamsJob filterContamJob = null;
						if(!isPE)
						{
							//filter contam job, cant do with PE since it messes up order
							String splitFastqOutputFile = f.getFilename();
							filterContamJob = new FilterContamsJob(splitFastqOutputFile);
							dax.addJob(filterContamJob);
							dax.addChild(filterContamJob.getID(), fastqSplitJob.getID());
							filterTrimCountFiles.add(filterContamJob.getContamAdapterTrimCountsOutputFileName());
							splitFileName = filterContamJob.getNoContamOutputFileName();
						}
						
						// sol2sanger job
						Sol2SangerJob sol2sangerJob = new Sol2SangerJob(splitFileName);
						dax.addJob(sol2sangerJob);
						dax.addChild(sol2sangerJob.getID(), isPE ? fastqSplitJob.getID() : filterContamJob.getID());						
						fastqJobs.add(sol2sangerJob);
						
						// fastq2bfq job
						FastQ2BFQJob fastq2bfqJob = new FastQ2BFQJob(sol2sangerJob.getSingleOutputFile().getFilename());
						dax.addJob(fastq2bfqJob);
						dax.addChild(fastq2bfqJob.getID(), sol2sangerJob.getID());
						bfqJobs.add(fastq2bfqJob);
	
						
							
					}
					
					// map job. needs genome. PE and SE are processed diff due to extra file and args
					if(isPE)
					{
						for(int h = 0; h < bfqJobs.size(); h+=2)
						{
							FastQ2BFQJob bfqJobR1 = bfqJobs.get(h);
							FastQ2BFQJob bfqJobR2 = bfqJobs.get(h+1);
							MapJob mapJob = new MapJob(bfqJobR1.getSingleOutputFile().getFilename(), bfqJobR2.getSingleOutputFile().getFilename(), workFlowParams.getSetting("Lane." + i + ".ReferenceBFA"),  Integer.parseInt(workFlowParams.getSetting("MinMismatches")),
									workFlowParams.getSetting("Lane." + i + ".AlignmentType"), Integer.parseInt(workFlowParams.getSetting("MaqTrimEnd1")), Integer.parseInt(workFlowParams.getSetting("MaqTrimEnd2")));
							dax.addJob(mapJob);
							dax.addChild(mapJob.getID(), bfqJobR1.getID());
							dax.addChild(mapJob.getID(), bfqJobR2.getID());
							mapJobs.add(mapJob);
						}						
					}
					else
					{
						for(FastQ2BFQJob bfqJob : bfqJobs)
						{
							MapJob mapJob = new MapJob(bfqJob.getSingleOutputFile().getFilename(), workFlowParams.getSetting("Lane." + i + ".ReferenceBFA"),  Integer.parseInt(workFlowParams.getSetting("MinMismatches")),
									workFlowParams.getSetting("Lane." + i + ".AlignmentType"), Integer.parseInt(workFlowParams.getSetting("MaqTrimEnd1")), Integer.parseInt(workFlowParams.getSetting("MaqTrimEnd2")));
							dax.addJob(mapJob);
							dax.addChild(mapJob.getID(), bfqJob.getID());
							mapJobs.add(mapJob);
						}
					}
					
					//create qcmetrics job 
					QCMetricsJob qcjob = new QCMetricsJob(workFlowParams.getSetting("tmpDir") + "/" + workFlowParams.getSetting("FlowCellName"), workFlowParams.getSetting("FlowCellName"));
					dax.addJob(qcjob);
					
					//try to align a single bfqJob to multiple organisms to test for contam
					String[] organisms = {"/home/uec-00/shared/production/genomes/hg18_unmasked/hg18_unmasked.plusContam.bfa", 
										  "/home/uec-00/shared/production/genomes/sacCer1/sacCer1.bfa",
										  "/home/uec-00/shared/production/genomes/phi-X174/phi_plus_SNPs.bfa",
										  "/home/uec-00/shared/production/genomes/arabidopsis/tair8.pluscontam.bfa",
										  "/home/uec-00/shared/production/genomes/mm9_unmasked/mm9_unmasked.bfa",
										  "/home/uec-00/shared/production/genomes/Ecoli/EcoliIHE3034.bfa",
										  "/home/uec-00/shared/production/genomes/rn4_unmasked/rn4.bfa"};
					
					for(String bfa : organisms)
					{
						String bfaBase = new File(bfa).getName().replace(".bfa", "");
						MapJob sampleMapJob = new MapJob(bfqJobs.get(bfqJobs.size() / 2).getSingleOutputFile().getFilename(), bfa,  Integer.parseInt(workFlowParams.getSetting("MinMismatches")),
								"regular", Integer.parseInt(workFlowParams.getSetting("MaqTrimEnd1")),Integer.parseInt(workFlowParams.getSetting("MaqTrimEnd2")), "aligntest_s_" + i + "_" + bfaBase + ".map");						
						dax.addJob(sampleMapJob);
						dax.addChild(sampleMapJob.getID(), bfqJobs.get(bfqJobs.size() / 2).getID());		
						if(bfa.contains("hg"))
							dax.addChild(qcjob.getID(), sampleMapJob.getID());
					}
					
					
					//for each lane create a countfastq job
					CountFastQJob countFastQJob = new CountFastQJob(fastqJobs, workFlowParams.getSetting("FlowCellName"), i, false);
					dax.addJob(countFastQJob);
					// mapmerge is child to all the map jobs
					for (Job fastqjob : fastqJobs)
						dax.addChild(countFastQJob.getID(), fastqjob.getID());
					
					//countAdapterTrimJob needs all the adapterCount filenames from FilterContamsJob
					if(!isPE)
					{
						CountAdapterTrimJob countAdapterTrim = new CountAdapterTrimJob(filterTrimCountFiles,  workFlowParams.getSetting("FlowCellName"), i);
						dax.addJob(countAdapterTrim);
						for (Job bfqjob : bfqJobs)
							dax.addChild(countAdapterTrim.getID(), bfqjob.getID());
					}
					
					//create nmercount for 3
					CountNmerJob count3mer = new CountNmerJob(fastqJobs, workFlowParams.getSetting("FlowCellName"), i, 3);
					dax.addJob(count3mer);
					// mapmerge is child to all the map jobs
					for (Job fastqjob : fastqJobs)
						dax.addChild(count3mer.getID(), fastqjob.getID());
					
					//create nmercount for 5
					CountNmerJob count5mer = new CountNmerJob(fastqJobs, workFlowParams.getSetting("FlowCellName"), i, 5);
					dax.addJob(count5mer);
					// mapmerge is child to all the map jobs
					for (Job fastqjob : fastqJobs)
						dax.addChild(count5mer.getID(), fastqjob.getID());
					
					//create nmercount for 10
					CountNmerJob count10mer = new CountNmerJob(fastqJobs, workFlowParams.getSetting("FlowCellName"), i, 10);
					dax.addJob(count10mer);
					// mapmerge is child to all the map jobs
					for (Job fastqjob : fastqJobs)
						dax.addChild(count10mer.getID(), fastqjob.getID());
					
					// for each lane create a map merge job
					MapMergeJob mapMergeJob = new MapMergeJob(mapJobs, workFlowParams.getSetting("FlowCellName"), i);
					dax.addJob(mapMergeJob);
					// mapmerge is child to all the map jobs
					for (Job map : mapJobs)
						dax.addChild(mapMergeJob.getID(), map.getID());
					//mapMergeJobs.add(mapMergeJob);
					
						
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
					else if(workFlowParams.getSetting("Lane." + i + ".ReferenceBFA").contains("hg19")) { genome = "hg19";}
					else if(workFlowParams.getSetting("Lane." + i + ".ReferenceBFA").contains("tair8")) { genome = "tair8";}
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
					PileupToWigJob pilewig = new PileupToWigJob(pileupJob.getSingleOutputFile().getFilename(), workFlowParams.getSetting("FlowCellName"), i, Integer.parseInt(workFlowParams.getSetting("WigWindSize")), 50, 1, 0, 2);
					dax.addJob(pilewig);
					dax.addChild(pilewig.getID(), pileupJob.getID());
					
					//wig to tdf (IGVTOOLS )job child of pileup to wig
					WigToTdfJob wigtotdf = new WigToTdfJob(pilewig.getSingleOutputFile().getFilename(),genome);
					dax.addJob(wigtotdf);
					dax.addChild(wigtotdf.getID(),pilewig.getID());
				}

			}
			if(dax.getChildCount() > 0)
			{
				dax.saveAsDot("bisulfite_dax.dot");
				dax.saveAsSimpleDot("bisulfite_dax_simple.dot");
				if(pbsMode)
					dax.runWorkflow(dryrun);
				dax.saveAsXML("bisulfite_dax.xml");
			}
			
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
