package edu.usc.epigenome.workflow.generator;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.griphyn.vdl.dax.Filename;

import edu.usc.epigenome.workflow.DAX.ECDax;
import edu.usc.epigenome.workflow.ECWorkflowParams.specialized.GAParams;
import edu.usc.epigenome.workflow.job.ecjob.CountAdapterTrimJob;
import edu.usc.epigenome.workflow.job.ecjob.CountFastQJob;
import edu.usc.epigenome.workflow.job.ecjob.CountNmerJob;
import edu.usc.epigenome.workflow.job.ecjob.CufflinksJob;
import edu.usc.epigenome.workflow.job.ecjob.FastQConstantSplitJob;
import edu.usc.epigenome.workflow.job.ecjob.FilterContamsJob;
import edu.usc.epigenome.workflow.job.ecjob.QCMetricsJob;

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
			
			List<TopHatJob> tophatJobs = new LinkedList<TopHatJob>();
			List<String> filterTrimCountFiles = new LinkedList<String>();
			
			String laneInputFileNameR1 = pbsMode ? new File(fileInput.split(",")[0]).getAbsolutePath() : new File(fileInput.split(",")[0]).getName();
			String laneInputFileNameR2 = null;
			//split Fastq Job. handle paired end and non pbs
			int splitSize = 1; //tophat cant merge bams.
			FastQConstantSplitJob fastqSplitJob = null;
			if(isPE)
			{
				laneInputFileNameR2 = pbsMode ? new File(fileInput.split(",")[1]).getAbsolutePath() : new File(fileInput.split(",")[1]).getName();
				fastqSplitJob = new FastQConstantSplitJob(laneInputFileNameR1, laneInputFileNameR2, splitSize);
				System.out.println("Creating rnaseq PE Processing workflow for lane " + label + ": " + laneInputFileNameR1 + " " + laneInputFileNameR2 );
			}
			else
			{
				fastqSplitJob = new FastQConstantSplitJob(laneInputFileNameR1, splitSize);
				System.out.println("Creating rnaseq SR Processing workflow for lane " + label + ": " + laneInputFileNameR1);
			}						
			dax.addJob(fastqSplitJob);


			// iterate through the output files of fastQsplit jobs to create pipeline
			for (Filename f : fastqSplitJob.getOutputFiles())
			{
				String splitFileName = f.getFilename();
				
				
				//filter contam job, cant do with PE since it messes up order
				String splitFastqOutputFile = f.getFilename();
				FilterContamsJob filterContamJob = new FilterContamsJob(splitFastqOutputFile);
				dax.addJob(filterContamJob);
				dax.addChild(filterContamJob.getID(), fastqSplitJob.getID());
				filterTrimCountFiles.add(filterContamJob.getContamAdapterTrimCountsOutputFileName());
				
				if(!isPE)
					splitFileName = filterContamJob.getNoContamOutputFileName();					
				
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
					
					TopHatJob tophat = new TopHatJob(read1, read2,referenceGenome, 150);
					dax.addJob(tophat);
					dax.addChild(tophat.getID(),parentJobEnd1);
					dax.addChild(tophat.getID(),parentJobEnd2);
					tophatJobs.add(tophat);
				}						
			}
			else
			{
				for(int h = 0; h < splitFiles.size(); h++)
				{
					String read1 = splitFiles.get(h);
					String parentJobEnd1 = splitIDs.get(h);
					
					TopHatJob tophat = new TopHatJob(read1, referenceGenome);
					dax.addJob(tophat);
					dax.addChild(tophat.getID(),parentJobEnd1);
					tophatJobs.add(tophat);
				}
			}
			
			//no bam merging support in tophat for now
			TopHatJob tophat = tophatJobs.get(0);
			
			//run cufflinks
			CufflinksJob cufflinks = new CufflinksJob(tophat.getBamFile(), referenceGenome + ".fa", workFlowParams.getSetting("refGene"));
			dax.addJob(cufflinks);
			dax.addChild(cufflinks.getID(), tophat.getID());
						
			//countAdapterTrimJob needs all the adapterCount filenames from FilterContamsJob, , child of mapmerge
			CountAdapterTrimJob countAdapterTrim = new CountAdapterTrimJob(filterTrimCountFiles,  flowcellID, Integer.parseInt(laneNumber));
			dax.addJob(countAdapterTrim);
			dax.addChild(countAdapterTrim.getID(), tophat.getID());
			
			//for each lane create a countfastq job, child of bammerge
			CountFastQJob countFastQJob = new CountFastQJob(splitFiles.toArray(new String[0]), flowcellID, Integer.parseInt(laneNumber), false);
			dax.addJob(countFastQJob);
			dax.addChild(countFastQJob.getID(), countAdapterTrim.getID());
			
			//create qcmetrics job child of bammerge
			QCMetricsJob qcjob = new QCMetricsJob(workFlowParams.getSetting("tmpDir") + "/" + flowcellID + "/" + label, flowcellID);
			dax.addJob(qcjob);
			dax.addChild(qcjob.getID(), countFastQJob.getID());

			
			//create nmercount for 3, child of mapmerge
			CountNmerJob count3mer = new CountNmerJob(splitFiles.toArray(new String[0]), flowcellID, Integer.parseInt(laneNumber), 3);
			dax.addJob(count3mer);
			dax.addChild(count3mer.getID(),tophat.getID());
			
			//create nmercount for 5, child of mapmerge
			CountNmerJob count5mer = new CountNmerJob(splitFiles.toArray(new String[0]), flowcellID, Integer.parseInt(laneNumber), 5);
			dax.addJob(count5mer);
			dax.addChild(count5mer.getID(), tophat.getID());
			
			//create nmercount for 10, child of mapmerge
			CountNmerJob count10mer = new CountNmerJob(splitFiles.toArray(new String[0]), flowcellID, Integer.parseInt(laneNumber), 10);
			dax.addJob(count10mer);
			dax.addChild(count10mer.getID(), tophat.getID());
			
			if(dax.getChildCount() > 0)
			{
				dax.saveAsDot("rnaseq_dax_" + label + ".dot");
				dax.saveAsSimpleDot("rnaseq_dax_simple_" + label + ".dot");
				if(pbsMode)
				{
					par.getWorkFlowArgsMap().put("WorkflowName", label);
					dax.runWorkflow(dryrun);
				}
				dax.saveAsXML("rnaseq_dax_" +label+".xml");
			}
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}	
}