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
import edu.usc.epigenome.workflow.job.PipelineSegment.ecPipelineSegments.CommonBamQC;
import edu.usc.epigenome.workflow.job.PipelineSegment.ecPipelineSegments.LambdaPhageBisAlnQC;
import edu.usc.epigenome.workflow.job.PipelineSegment.ecPipelineSegments.OrgContamCheckQC;
import edu.usc.epigenome.workflow.job.ecjob.BSMapJob;
import edu.usc.epigenome.workflow.job.ecjob.BWAMethJob;
import edu.usc.epigenome.workflow.job.ecjob.BisSNPJob;
import edu.usc.epigenome.workflow.job.ecjob.CleanUpFilesJob;
import edu.usc.epigenome.workflow.job.ecjob.CountAdapterTrimJob;
import edu.usc.epigenome.workflow.job.ecjob.CountFastQJob;
import edu.usc.epigenome.workflow.job.ecjob.CountNmerJob;
import edu.usc.epigenome.workflow.job.ecjob.FastQConstantSplitJob;
import edu.usc.epigenome.workflow.job.ecjob.FilterContamsJob;
import edu.usc.epigenome.workflow.job.ecjob.MergeBamsJob;
import edu.usc.epigenome.workflow.job.ecjob.MethLevelAveragesJob;
import edu.usc.epigenome.workflow.job.ecjob.NovoAlignBisJob;
import edu.usc.epigenome.workflow.job.ecjob.PicardJob;
import edu.usc.epigenome.workflow.job.ecjob.QCMetricsJob;


public class BisulfiteAlignmentWorkflow
{
	/**
	 * Creates an AlignPileUp workflow from an empty dax object 
	 * @param dax The ECDAX to which processing jobs will be added
	 */	
	public static String WorkflowName = "bisulfite";
	
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
				System.out.println("Creating BiS PE Processing workflow for lane " + label + ": " + laneInputFileNameR1 + " " + laneInputFileNameR2 );
			}
			else
			{
				fastqSplitJob = new FastQConstantSplitJob(laneInputFileNameR1, splitSize);
				System.out.println("Creating BiS SR Processing workflow for lane " + label + ": " + laneInputFileNameR1);
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
					if(sampleWorkflow.contains("bsmap"))
					{
						BSMapJob bsmap = new BSMapJob(read1, read2,referenceGenome, read1 + ".bam");
						dax.addJob(bsmap);
						dax.addChild(bsmap.getID(),parentJobEnd1);
						dax.addChild(bsmap.getID(),parentJobEnd2);
						bsmapJobs.add(bsmap);
					}
					else
					{
						BWAMethJob bisAlignment = new BWAMethJob(read1,referenceGenome);
						dax.addJob(bisAlignment);
						dax.addChild(bisAlignment.getID(),parentJobEnd1);
						bsmapJobs.add(bisAlignment);
					}
				}
				
			}
			else
			{
				for(int h = 0; h < splitFiles.size(); h++)
				{
					String read1 = splitFiles.get(h);
					String parentJobEnd1 = splitIDs.get(h);
					if(sampleWorkflow.contains("bsmap"))
					{
						BSMapJob bsmap = new BSMapJob(read1, null,referenceGenome, read1 + ".bam");
						dax.addJob(bsmap);
						dax.addChild(bsmap.getID(),parentJobEnd1);
						bsmapJobs.add(bsmap);
					}
					else
					{
						BWAMethJob bisAlignment = new BWAMethJob(read1,referenceGenome);
						dax.addJob(bisAlignment);
						dax.addChild(bisAlignment.getID(),parentJobEnd1);
						bsmapJobs.add(bisAlignment);
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
			
			
			//countAdapterTrimJob needs all the adapterCount filenames from FilterContamsJob, , child of mapmerge
			CountAdapterTrimJob countAdapterTrim = new CountAdapterTrimJob(filterTrimCountFiles,  flowcellID, Integer.parseInt(laneNumber));
			dax.addJob(countAdapterTrim);
			dax.addChild(countAdapterTrim.getID(), mergebams.getID());
			
			
			//for each lane create a countfastq job, child of bammerge
			CountFastQJob countFastQJob = new CountFastQJob(splitFiles.toArray(new String[0]), flowcellID, Integer.parseInt(laneNumber), false);
			dax.addJob(countFastQJob);
			dax.addChild(countFastQJob.getID(), mergebams.getID());
			
			//create qcmetrics job child of bammerge
			QCMetricsJob qcjob = new QCMetricsJob(workFlowParams.getSetting("tmpDir") + "/" + flowcellID + "/" + label, flowcellID);
			dax.addJob(qcjob);
			dax.addChild(qcjob.getID(), mergebams.getID());

			
			//create nmercount for 3,5,10, child of mapmerge
			CountNmerJob count3mer = new CountNmerJob(splitFiles.toArray(new String[0]), flowcellID, Integer.parseInt(laneNumber));
			dax.addJob(count3mer);
			dax.addChild(count3mer.getID(),mergebams.getID());
			
			
			
			//cleanup garbage job
			CleanUpFilesJob cleanup = new CleanUpFilesJob(workFlowParams.getSetting("tmpDir") + "/" + flowcellID + "/" + label);
			dax.addJob(cleanup);
			dax.addChild(cleanup.getID(),count3mer.getID());
			dax.addChild(cleanup.getID(),qcjob.getID());
			dax.addChild(cleanup.getID(),countFastQJob.getID());
			dax.addChild(cleanup.getID(),countAdapterTrim.getID());
			
			//PICARD CollectAlignmentMetrics
			PicardJob collectAlignmentMetricsJob = new PicardJob(mergebams.getMdupsBam(), "CollectAlignmentSummaryMetrics", "IS_BISULFITE_SEQUENCED=true REFERENCE_SEQUENCE=" + referenceGenome, mergebams.getMdupsBam() + ".CollectAlignmentSummaryMetrics.metric.txt");
			dax.addJob(collectAlignmentMetricsJob);
			dax.addChild(collectAlignmentMetricsJob.getID(),  mergebams.getID());
			
			//create MethLevelAverages yaping job
			MethLevelAveragesJob methlevels = new MethLevelAveragesJob(mergebams.getBam(), mergebams.getBai(),  mergebams.getBam() + ".MethLevelAverages.metric.txt", referenceGenome, "");
			dax.addJob(methlevels);
			dax.addChild(methlevels.getID(),  mergebams.getID());
			
			//create MethLevelAverages CHROM M yaping job
			MethLevelAveragesJob methlevelsM = new MethLevelAveragesJob(mergebams.getBam(), mergebams.getBai(),  mergebams.getBam() + ".chrM.MethLevelAverages.metric.txt", referenceGenome, "chrM");
			dax.addJob(methlevelsM);
			dax.addChild(methlevelsM.getID(),  mergebams.getID());
			
			
			//create BISSNP JOB
			BisSNPJob bissnp = new BisSNPJob(mergebams.getBam(),mergebams.getBai(), referenceGenome, sampleWorkflow.contains("nomeseq"),sampleWorkflow.contains("rbs"));
			dax.addJob(bissnp);
			dax.addChild(bissnp.getID(),  mergebams.getID());
			
			//inverted dups count using yapins fastq analyzer (NOT WORKING FOR NOW)
			//CountInvertedDupsJob dupsjob = new CountInvertedDupsJob(laneInputFileNameR1,laneInputFileNameR2,mergebams.getBam() + ".InvertedReadPairDups.metric.txt");
			//dax.addJob(dupsjob);
			//dax.addChild(dupsjob.getID(),  fastqSplitJob.getID());
			
			
			
			//ADD THE COMMON SUB-PIPELINES
			
			//add the general QC job pipeline
			CommonBamQC bamQCPipeSegment = new CommonBamQC(dax);
			bamQCPipeSegment.addToDax(sample,mergebams);
			
			//add LambdaPhage aln and qc job pipeline
			LambdaPhageBisAlnQC lamdaAlnQC = new LambdaPhageBisAlnQC(dax);
			lamdaAlnQC.addToDax(sample, fastqSplitJob);
			
			//add the organism contam check qc pipeline
			OrgContamCheckQC orgContamCheck = new OrgContamCheckQC(dax);
			orgContamCheck.addToDax(sample, fastqSplitJob);
			
			
			if(dax.getChildCount() > 0)
			{
				dax.saveAsDot("bisulfite_dax_" + label + ".dot");
				dax.saveAsSimpleDot("bisulfite_dax_simple_" + label + ".dot");
				par.getWorkFlowArgsMap().put("WorkflowName", label);
				dax.runWorkflow(runOptions);
				dax.saveAsXML("bisulfite_dax_" + label + ".xml");
			}
			dax.release();
			
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
