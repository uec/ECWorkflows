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
import edu.usc.epigenome.workflow.job.PipelineSegment.ecPipelineSegments.CommonBamQC;
import edu.usc.epigenome.workflow.job.PipelineSegment.ecPipelineSegments.OrgContamCheckQC;
import edu.usc.epigenome.workflow.job.ecjob.CleanUpFilesJob;
import edu.usc.epigenome.workflow.job.ecjob.CountAdapterTrimJob;
import edu.usc.epigenome.workflow.job.ecjob.CountFastQJob;
import edu.usc.epigenome.workflow.job.ecjob.CountInvertedDupsJob;
import edu.usc.epigenome.workflow.job.ecjob.CountNmerJob;
import edu.usc.epigenome.workflow.job.ecjob.Cufflinks2Job;
import edu.usc.epigenome.workflow.job.ecjob.FastQConstantSplitJob;
import edu.usc.epigenome.workflow.job.ecjob.FilterContamsJob;
import edu.usc.epigenome.workflow.job.ecjob.MergeBamsJob;
import edu.usc.epigenome.workflow.job.ecjob.PicardJob;
import edu.usc.epigenome.workflow.job.ecjob.QCMetricsJob;
import edu.usc.epigenome.workflow.job.ecjob.TopHat2Job;


public class RNAseqV2Workflow
{
	/**
	 * Creates an AlignPileUp workflow from an empty dax object 
	 * @param dax The ECDAX to which processing jobs will be added
	 */
	public static String WorkflowName = "rnaseqv2";
	
	public static void createWorkFlow(String sample, GAParams par,EnumSet<RunOptions> runOptions)
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
			
			List<TopHat2Job> tophatJobs = new LinkedList<TopHat2Job>();
			List<String> filterTrimCountFiles = new LinkedList<String>();
			
			String laneInputFileNameR1 = runOptions.contains(RunOptions.PBSMODE) ? new File(fileInput.split(",")[0]).getAbsolutePath() : new File(fileInput.split(",")[0]).getName();
			String laneInputFileNameR2 = null;
			//split Fastq Job. handle paired end and non pbs
			int splitSize = 1; //tophat cant merge bams.
			FastQConstantSplitJob fastqSplitJob = null;
			if(isPE)
			{
				laneInputFileNameR2 = runOptions.contains(RunOptions.PBSMODE) ? new File(fileInput.split(",")[1]).getAbsolutePath() : new File(fileInput.split(",")[1]).getName();
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
					
					TopHat2Job tophat = new TopHat2Job(read1, read2,referenceGenome, 150);
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
					
					TopHat2Job tophat = new TopHat2Job(read1, referenceGenome);
					dax.addJob(tophat);
					dax.addChild(tophat.getID(),parentJobEnd1);
					tophatJobs.add(tophat);
				}
			}
			
			//no bam merging support in tophat for now
			TopHat2Job tophat = tophatJobs.get(0);
			
			//sort
			PicardJob picardSortJob = new PicardJob(tophat.getBamFile(), "SortSam", "SORT_ORDER=coordinate", tophat.getBamFile() + ".sorted.bam");
			dax.addJob(picardSortJob);
			dax.addChild(picardSortJob.getID(),  tophat.getID());
			
			//reorder contigs
			PicardJob picardReorderContigsJob = new PicardJob(picardSortJob.getSingleOutputFile().getFilename(), "ReorderSam", "REFERENCE=" + referenceGenome + ".fa", picardSortJob.getSingleOutputFile().getFilename() + ".reorder.bam");
			dax.addJob(picardReorderContigsJob);
			dax.addChild(picardReorderContigsJob.getID(), picardSortJob.getID());
						
			//single file merge for now, just for metrics
			ArrayList<String> bams = new ArrayList<String>();
			bams.add(picardReorderContigsJob.getSingleOutputFile().getFilename());
			MergeBamsJob mergebams = new MergeBamsJob(bams,"ResultCount_" + flowcellID + "_" + laneNumber + "_" + sampleName + "." + new File(referenceGenome).getName() + ".bam");
			dax.addJob(mergebams);
			dax.addChild(mergebams.getID(), picardReorderContigsJob.getID());
			
				
			//CollectAlignmentMetrics
			PicardJob collectAlignmentMetricsJob = new PicardJob(mergebams.getBam(), "CollectAlignmentSummaryMetrics", "IS_BISULFITE_SEQUENCED=false REFERENCE_SEQUENCE=" + referenceGenome + ".fa", mergebams.getBam() + ".CollectAlignmentSummaryMetrics.metric.txt");
			dax.addJob(collectAlignmentMetricsJob);
			dax.addChild(collectAlignmentMetricsJob.getID(),  mergebams.getID());
	
			//add the general QC job pipeline
			CommonBamQC bamQCPipeSegment = new CommonBamQC(dax);
			bamQCPipeSegment.addToDax(sample,mergebams);
			
			//add the organism contam check qc pipeline
			OrgContamCheckQC orgContamCheck = new OrgContamCheckQC(dax);
			orgContamCheck.addToDax(sample, fastqSplitJob);
			
						
			//run cufflinks
			Cufflinks2Job cufflinks = new Cufflinks2Job(mergebams.getBam(), referenceGenome + ".fa", workFlowParams.getSetting("refGene"));
			dax.addJob(cufflinks);
			dax.addChild(cufflinks.getID(),  mergebams.getID());
						
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

			
			//create nmercount for 3,5,10, child of mapmerge
			CountNmerJob count3mer = new CountNmerJob(splitFiles.toArray(new String[0]), flowcellID, Integer.parseInt(laneNumber));
			dax.addJob(count3mer);
			dax.addChild(count3mer.getID(),mergebams.getID());
			
			//cleanup garbage job
			CleanUpFilesJob cleanup = new CleanUpFilesJob(workFlowParams.getSetting("tmpDir") + "/" + flowcellID + "/" + label);
			dax.addJob(cleanup);
			dax.addChild(cleanup.getID(),count3mer.getID());
			dax.addChild(cleanup.getID(),qcjob.getID());

			//inverted dups count using yapins fastq analyzer
			CountInvertedDupsJob dupsjob = new CountInvertedDupsJob(laneInputFileNameR1,laneInputFileNameR2,mergebams.getBam() + ".InvertedReadPairDups.metric.txt");
			dax.addJob(dupsjob);
			dax.addChild(dupsjob.getID(),  fastqSplitJob.getID());
			
			if(dax.getChildCount() > 0)
			{
				dax.saveAsDot("rnaseqv2_dax_" + label + ".dot");
				dax.saveAsSimpleDot("rnaseqv2_dax_simple_" + label + ".dot");
				par.getWorkFlowArgsMap().put("WorkflowName", label);
				dax.runWorkflow(runOptions);
				dax.saveAsXML("rnaseqv2_dax_" +label+".xml");
			}
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}	
}