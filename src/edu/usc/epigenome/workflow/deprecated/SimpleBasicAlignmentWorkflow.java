package edu.usc.epigenome.workflow.deprecated;

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
import edu.usc.epigenome.workflow.job.ecjob.WigToTdfJob;

import edu.usc.epigenome.workflow.job.ecjob.Sol2SangerJob;

public class SimpleBasicAlignmentWorkflow
{
	/**
	 * Creates an AlignPileUp workflow from an empty dax object 
	 * @param dax The ECDAX to which processing jobs will be added
	 */
	public static String WorkflowName = "simple";
	
	public static void createWorkFlow(String sample, GAParams par,Boolean pbsMode, Boolean dryrun)
	{
		try
		{
			// construct a dax object
			// For every requested lane in this flowcell..
			ECDax dax = new ECDax(par);
			
			//get the params so that we have the input parameters
			GAParams workFlowParams = (GAParams) dax.getWorkFlowParams();
			
			String sampleName = par.getSamples().get(sample).get("SampleID");
			String laneNumber = par.getSamples().get(sample).get("Lane");
			String fileInput = par.getSamples().get(sample).get("Input");
			String referenceGenome = par.getSamples().get(sample).get("Reference");
			String sampleWorkflow = par.getSamples().get(sample).get("Workflow");
			String label = workFlowParams.getSetting("FlowCellName") + "_" + laneNumber + "_" + sampleName;
			
			boolean isPE = fileInput.contains(",");
			List<Sol2SangerJob> fastqJobs = new LinkedList<Sol2SangerJob>();
			List<FastQ2BFQJob> bfqJobs = new LinkedList<FastQ2BFQJob>();
			List<ECJob> mapJobs = new LinkedList<ECJob>();
			List<String> filterTrimCountFiles = new LinkedList<String>();
			
			String laneInputFileNameR1 = pbsMode ? new File(fileInput.split(",")[0]).getAbsolutePath() : new File(fileInput.split(",")[0]).getName();
			
			//split Fastq Job. handle paired end and non pbs
			int splitSize = Integer.parseInt(workFlowParams.getSetting("ClusterSize"));
			FastQConstantSplitJob fastqSplitJob = null;
			if(isPE)
			{
				String laneInputFileNameR2 = pbsMode ? new File(fileInput.split(",")[1]).getAbsolutePath() : new File(fileInput.split(",")[1]).getName();
				fastqSplitJob = new FastQConstantSplitJob(laneInputFileNameR1, laneInputFileNameR2, splitSize);
				System.out.println("Creating bisulfite PE Processing workflow for lane " + label + ": " + laneInputFileNameR1 + " " + laneInputFileNameR2 );
			}
			else
			{
				fastqSplitJob = new FastQConstantSplitJob(laneInputFileNameR1, splitSize);
				System.out.println("Creating bislfite SR Processing workflow for lane " + label + ": " + laneInputFileNameR1);
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
					MapJob mapJob = new MapJob(bfqJobR1.getSingleOutputFile().getFilename(), bfqJobR2.getSingleOutputFile().getFilename(), referenceGenome,  Integer.parseInt(workFlowParams.getSetting("MinMismatches")),
							sampleWorkflow, Integer.parseInt(workFlowParams.getSetting("MaqTrimEnd1")), Integer.parseInt(workFlowParams.getSetting("MaqTrimEnd2")));
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
					MapJob mapJob = new MapJob(bfqJob.getSingleOutputFile().getFilename(), referenceGenome,  Integer.parseInt(workFlowParams.getSetting("MinMismatches")),
							sampleWorkflow, Integer.parseInt(workFlowParams.getSetting("MaqTrimEnd1")), Integer.parseInt(workFlowParams.getSetting("MaqTrimEnd2")));
					dax.addJob(mapJob);
					dax.addChild(mapJob.getID(), bfqJob.getID());
					mapJobs.add(mapJob);
				}
			}
			
	
			
			// for each lane create a map merge job
			MapMergeJob mapMergeJob = new MapMergeJob(mapJobs, workFlowParams.getSetting("FlowCellName"), Integer.parseInt(laneNumber));
			dax.addJob(mapMergeJob);
			// mapmerge is child to all the map jobs
			for (Job map : mapJobs)
				dax.addChild(mapMergeJob.getID(), map.getID());
			//mapMergeJobs.add(mapMergeJob);
			
		
			
			//countAdapterTrimJob needs all the adapterCount filenames from FilterContamsJob, , child of mapmerge
			if(!isPE)
			{
				CountAdapterTrimJob countAdapterTrim = new CountAdapterTrimJob(filterTrimCountFiles,  workFlowParams.getSetting("FlowCellName"), Integer.parseInt(laneNumber));
				dax.addJob(countAdapterTrim);
				dax.addChild(countAdapterTrim.getID(), mapMergeJob.getID());
			}

			//create maq2bamjob, child of mapMerge
			Maq2BamJob maq2bamJob = new Maq2BamJob(mapMergeJob.getSingleOutputFile().getFilename(),referenceGenome.replace(".bfa", ".fa"));
			dax.addJob(maq2bamJob);
			dax.addChild(maq2bamJob.getID(), mapMergeJob.getID());
			

			if(dax.getChildCount() > 0)
			{
				dax.saveAsDot("simpleAlignment_dax.dot");
				dax.saveAsSimpleDot("simpleAlignment_dax_simple.dot");
				if(pbsMode)
				{
					par.getWorkFlowArgsMap().put("WorkflowName", label);
					dax.runWorkflow(dryrun);
				}
				dax.saveAsXML("simpleAlignment_dax.xml");
			}
			
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}	
}
