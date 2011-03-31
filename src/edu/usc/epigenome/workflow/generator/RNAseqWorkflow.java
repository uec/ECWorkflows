package edu.usc.epigenome.workflow.generator;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;

import org.griphyn.vdl.dax.Filename;

import edu.usc.epigenome.workflow.DAX.ECDax;
import edu.usc.epigenome.workflow.ECWorkflowParams.specialized.GAParams;
import edu.usc.epigenome.workflow.job.ecjob.CountAdapterTrimJob;
import edu.usc.epigenome.workflow.job.ecjob.CountFastQJob;
import edu.usc.epigenome.workflow.job.ecjob.CountNmerJob;
import edu.usc.epigenome.workflow.job.ecjob.CuffCompareJob;
import edu.usc.epigenome.workflow.job.ecjob.CuffDiffJob;
import edu.usc.epigenome.workflow.job.ecjob.CufflinksJob;
import edu.usc.epigenome.workflow.job.ecjob.FastQConstantSplitJob;
import edu.usc.epigenome.workflow.job.ecjob.FilterContamsJob;
import edu.usc.epigenome.workflow.job.ecjob.Sol2SangerJob;
import edu.usc.epigenome.workflow.job.ecjob.TopHatJob;

public class RNAseqWorkflow
{
	/**
	 * Creates an AlignPileUp workflow from an empty dax object 
	 * @param dax The ECDAX to which processing jobs will be added
	 */
	public static String WorkflowName = "rnaseq";
	
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
			//String sampleWorkflow = par.getSamples().get(sample).get("Workflow");
			String label = workFlowParams.getSetting("FlowCellName") + "_" + laneNumber + "_" + sampleName;
					
			boolean isPE = fileInput.contains(",");
		
			List<Sol2SangerJob> fastqJobs = new LinkedList<Sol2SangerJob>();
			
			List<TopHatJob> tophatJobs = new LinkedList<TopHatJob>();
			List<String> filterTrimCountFiles = new LinkedList<String>();
			
			String laneInputFileNameR1 = pbsMode ? new File(fileInput.split(",")[0]).getAbsolutePath() : new File(fileInput.split(",")[0]).getName();					
			//split Fastq Job. handle paired end and non pbs
			int splitSize = 1;
			FastQConstantSplitJob fastqSplitJob = null;
			if(isPE)
			{
				String laneInputFileNameR2 = pbsMode ? new File(fileInput.split(",")[1]).getAbsolutePath() : new File(fileInput.split(",")[1]).getName();
				fastqSplitJob = new FastQConstantSplitJob(laneInputFileNameR1, laneInputFileNameR2, splitSize);
				System.out.println("Creating RNAseq PE Processing workflow for lane " + label + ": " + laneInputFileNameR1 + " " + laneInputFileNameR2 );
			}
			else
			{
				fastqSplitJob = new FastQConstantSplitJob(laneInputFileNameR1, splitSize);
				System.out.println("Creating RNAseq SR Processing workflow for lane " + label + ": " + laneInputFileNameR1);
			}
			dax.addJob(fastqSplitJob);
			
			
			
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
				
				
			}
			
			// map job. needs genome. PE and SE are processed diff due to extra file and args
			if(isPE)
			{
				for(int h = 0; h < fastqJobs.size(); h+=2)
				{
					Sol2SangerJob sangerJobE1 = fastqJobs.get(h);
					Sol2SangerJob sangerJobE2 = fastqJobs.get(h+1);
					TopHatJob tophat = new TopHatJob(sangerJobE1.getSingleOutputFile().getFilename(), sangerJobE2.getSingleOutputFile().getFilename(), referenceGenome, 150);
					

					dax.addJob(tophat);
					dax.addChild(tophat.getID(), sangerJobE1.getID());
					dax.addChild(tophat.getID(), sangerJobE2.getID());
					tophatJobs.add(tophat);
				}						
			}
			else
			{
				for(Sol2SangerJob sangerJob : fastqJobs)
				{
					TopHatJob tophat = new TopHatJob(sangerJob.getSingleOutputFile().getFilename(), referenceGenome);
					dax.addJob(tophat);
					dax.addChild(tophat.getID(), sangerJob.getID());
					tophatJobs.add(tophat);
				}
			}
			
			//we dont support merging muliple tophat runs yet
			TopHatJob tophat = tophatJobs.get(0);
			
			//run cufflinks
			CufflinksJob cufflinks = new CufflinksJob(tophat.getBamFile(), referenceGenome + ".fa");
			dax.addJob(cufflinks);
			dax.addChild(cufflinks.getID(), tophat.getID());
			
			if(dax.getChildCount() > 0)
			{
				dax.saveAsDot("rnaseq_dax.dot");
				dax.saveAsSimpleDot("rnaseq_dax_simple.dot");
				if(pbsMode)
				{
					par.getWorkFlowArgsMap().put("WorkflowName", label);
					dax.runWorkflow(dryrun);
				}
				dax.saveAsXML("rnaseq_dax.xml");
			}
			
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}	
}