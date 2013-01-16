package edu.usc.epigenome.workflow.generator;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

import org.griphyn.vdl.dax.Filename;


import edu.usc.epigenome.workflow.RunOptions;
import edu.usc.epigenome.workflow.DAX.ECDax;
import edu.usc.epigenome.workflow.ECWorkflowParams.specialized.GAParams;
import edu.usc.epigenome.workflow.job.ECJob;

import edu.usc.epigenome.workflow.job.ecjob.BSMapJob;
import edu.usc.epigenome.workflow.job.ecjob.CleanUpFilesJob;
import edu.usc.epigenome.workflow.job.ecjob.FastQConstantSplitJob;
import edu.usc.epigenome.workflow.job.ecjob.FilterContamsJob;
import edu.usc.epigenome.workflow.job.ecjob.MergeBamsJob;
import edu.usc.epigenome.workflow.job.ecjob.NovoAlignBisJob;

public class BisQuickAlignmentWorkflow
{
	/**
	 * Creates an AlignPileUp workflow from an empty dax object 
	 * @param dax The ECDAX to which processing jobs will be added
	 */	
	public static String WorkflowName = "bisquick";
	
	public static void createWorkFlow(String sample, GAParams par, EnumSet<RunOptions> runOptions)
	{
		try
		{
			
			// construct a dax object
			// For every requested lane in this flowcell..
			ECDax dax = new ECDax(par);
			
			//get the params so that we have the input parameters
			GAParams workFlowParams = (GAParams) dax.getWorkFlowParams();
			String flowcellID = workFlowParams.getSetting("FlowCellName");
			String sampleName = par.getSamples().get(sample).get("SampleID");
			String laneNumber = par.getSamples().get(sample).get("Lane");
			String fileInput = par.getSamples().get(sample).get("Input");
			String referenceGenome = par.getSamples().get(sample).get("Reference");
			String sampleWorkflow = par.getSamples().get(sample).get("Workflow");
			String label = flowcellID + "_" + laneNumber + "_" + sampleName;
			
			boolean isPE = fileInput.contains(",");
		
			List<String> splitIDs = new LinkedList<String>();
			List<String> splitFiles = new LinkedList<String>();
			
			List<ECJob> bsmapJobs = new LinkedList<ECJob>();
			List<String> filterTrimCountFiles = new LinkedList<String>();
			
			String laneInputFileNameR1 = runOptions.contains(RunOptions.PBSMODE) ? new File(fileInput.split(",")[0]).getAbsolutePath() : new File(fileInput.split(",")[0]).getName();
			String laneInputFileNameR2 = null;
			//split Fastq Job. handle paired end and non pbs
			int splitSize = Integer.parseInt(workFlowParams.getSetting("ClusterSize"));
			FastQConstantSplitJob fastqSplitJob = null;
			if(isPE)
			{
				laneInputFileNameR2 = runOptions.contains(RunOptions.PBSMODE) ? new File(fileInput.split(",")[1]).getAbsolutePath() : new File(fileInput.split(",")[1]).getName();
				fastqSplitJob = new FastQConstantSplitJob(laneInputFileNameR1, laneInputFileNameR2, splitSize);
				System.out.println("Creating BiSQUICK PE Processing workflow for lane " + label + ": " + laneInputFileNameR1 + " " + laneInputFileNameR2 );
			}
			else
			{
				fastqSplitJob = new FastQConstantSplitJob(laneInputFileNameR1, splitSize);
				System.out.println("Creating BiSQUICK SR Processing workflow for lane " + label + ": " + laneInputFileNameR1);
			}						
			dax.addJob(fastqSplitJob);


			// iterate through the output files of fastQsplit jobs to create pipeline
			for (Filename f : fastqSplitJob.getOutputFiles())
			{
				String splitFileName = f.getFilename();
		
				FilterContamsJob filterContamJob = new FilterContamsJob(splitFileName);
				dax.addJob(filterContamJob);
				dax.addChild(filterContamJob.getID(), fastqSplitJob.getID());
				filterTrimCountFiles.add(filterContamJob.getContamAdapterTrimCountsOutputFileName());
				
				//filter contam job, cant do with PE since it messes up order
				//if(!isPE)
					//splitFileName = filterContamJob.getNoContamOutputFileName();
				
				splitFiles.add(splitFileName);
				splitIDs.add(filterContamJob.getID());
				
			}
			
			// map job. needs genome. PE and SE are processed diff due to extra file and args
			if(isPE)
			{
				for(int h = 0; h < splitFiles.size(); h+=2)
				{
					String read1 = splitFiles.get(h);
					String read2 = splitFiles.get(h+1);
					String parentJobEnd1 = splitIDs.get(h);
					String parentJobEnd2 = splitIDs.get(h+1);
					if(sampleWorkflow.contains("novo"))
					{
						NovoAlignBisJob novobis = new NovoAlignBisJob(read1, read2,referenceGenome, read1 + ".bam");
						dax.addJob(novobis);
						dax.addChild(novobis.getID(),parentJobEnd1);
						dax.addChild(novobis.getID(),parentJobEnd2);
						bsmapJobs.add(novobis);
					}
					else
					{
						BSMapJob bsmap = new BSMapJob(read1, read2,referenceGenome, read1 + ".bam");
						dax.addJob(bsmap);
						dax.addChild(bsmap.getID(),parentJobEnd1);
						dax.addChild(bsmap.getID(),parentJobEnd2);
						bsmapJobs.add(bsmap);
					}
				}						
			}
			else
			{
				for(int h = 0; h < splitFiles.size(); h++)
				{
					String read1 = splitFiles.get(h);
					String parentJobEnd1 = splitIDs.get(h);
					if(sampleWorkflow.contains("novo"))
					{
						NovoAlignBisJob novobis = new NovoAlignBisJob(read1, null,referenceGenome, read1 + ".bam");
						dax.addJob(novobis);
						dax.addChild(novobis.getID(),parentJobEnd1);
						bsmapJobs.add(novobis);
					}
					else
					{
						BSMapJob bsmap = new BSMapJob(read1, null,referenceGenome, read1 + ".bam");
						dax.addJob(bsmap);
						dax.addChild(bsmap.getID(),parentJobEnd1);
						bsmapJobs.add(bsmap);
					}
				}
			}
			
	
			
			ArrayList<String> splitBams = new ArrayList<String>();
			for(ECJob job : bsmapJobs)
				splitBams.add(job.getSingleOutputFile().getFilename());
			
			// for each lane create a map merge job
			MergeBamsJob mergebams = new MergeBamsJob(splitBams,"ResultCount_" + flowcellID + "_" + laneNumber + "_" + sampleName + "." + new File(referenceGenome).getName() + ".bam");
			dax.addJob(mergebams);
			
			// mapmerge is child to all the map jobs
			for (ECJob job : bsmapJobs)
				dax.addChild(mergebams.getID(), job.getID());
			
			//cleanup garbage job
			CleanUpFilesJob cleanup = new CleanUpFilesJob(workFlowParams.getSetting("tmpDir") + "/" + flowcellID + "/" + label);
			dax.addJob(cleanup);
			dax.addChild(cleanup.getID(),mergebams.getID());
		
			
			
			if(dax.getChildCount() > 0)
			{
				dax.saveAsDot("bisulfitequick_dax_" + label + ".dot");
				dax.saveAsSimpleDot("bisulfitequick_dax_simple_" + label + ".dot");
				par.getWorkFlowArgsMap().put("WorkflowName", label);
				dax.runWorkflow(runOptions);
				dax.saveAsXML("bisulfitequick_dax_" + label + ".xml");
			}
			dax.release();
			
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
