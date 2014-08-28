package edu.usc.epigenome.workflow.generator;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import edu.usc.epigenome.workflow.RunOptions;
import edu.usc.epigenome.workflow.DAX.ECDax;
import edu.usc.epigenome.workflow.ECWorkflowParams.specialized.GAParams;
import edu.usc.epigenome.workflow.job.PipelineSegment.ecPipelineSegments.CommonBamQC;
import edu.usc.epigenome.workflow.job.ecjob.BisSNPJob;
import edu.usc.epigenome.workflow.job.ecjob.CleanUpFilesJob;
import edu.usc.epigenome.workflow.job.ecjob.MergeBamsJob;
import edu.usc.epigenome.workflow.job.ecjob.MethLevelAveragesJob;
import edu.usc.epigenome.workflow.job.ecjob.PicardJob;
import edu.usc.epigenome.workflow.job.ecjob.QCMetricsJob;



public class BisulfiteMergeWorkflow
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
						
			
			ArrayList<String> splitBams = new ArrayList<String>();
			for(String bam : fileInput.split(","))
				splitBams.add(bam);
			
			// for each lane create a map merge job
			MergeBamsJob mergebams = new MergeBamsJob(splitBams,"ResultCount_" + flowcellID + "_" + laneNumber + "_" + sampleName + "." + new File(referenceGenome).getName() + ".bam");
			dax.addJob(mergebams);
			
			//create qcmetrics job child of bammerge
			QCMetricsJob qcjob = new QCMetricsJob(workFlowParams.getSetting("tmpDir") + "/" + flowcellID + "/" + label, flowcellID);
			dax.addJob(qcjob);
			dax.addChild(qcjob.getID(), mergebams.getID());

			
			//cleanup garbage job
			CleanUpFilesJob cleanup = new CleanUpFilesJob(workFlowParams.getSetting("tmpDir") + "/" + flowcellID + "/" + label);
			dax.addJob(cleanup);
			dax.addChild(cleanup.getID(),qcjob.getID());
			
			//PICARD CollectAlignmentMetrics
			PicardJob collectAlignmentMetricsJob = new PicardJob(mergebams.getMdupsBam(), "CollectAlignmentSummaryMetrics", "IS_BISULFITE_SEQUENCED=true REFERENCE_SEQUENCE=" + referenceGenome,mergebams.getMdupsBam() + ".CollectAlignmentSummaryMetrics.metric.txt");
			dax.addJob(collectAlignmentMetricsJob);
			dax.addChild(collectAlignmentMetricsJob.getID(),  mergebams.getID());
			
			//create MethLevelAverages (yaping) job
			MethLevelAveragesJob methlevels = new MethLevelAveragesJob(mergebams.getBam(), mergebams.getBai(),  mergebams.getBam() + ".MethLevelAverages.metric.txt", referenceGenome, "");
			dax.addJob(methlevels);
			dax.addChild(methlevels.getID(),  mergebams.getID());
			
			//create MethLevelAverages CHROM M  (yaping) job
			MethLevelAveragesJob methlevelsM = new MethLevelAveragesJob(mergebams.getBam(), mergebams.getBai(),  mergebams.getBam() + ".chrM.MethLevelAverages.metric.txt", referenceGenome, "chrM");
			dax.addJob(methlevelsM);
			dax.addChild(methlevelsM.getID(),  mergebams.getID());
			
			//create BISSNP JOB
			BisSNPJob bissnp = new BisSNPJob(mergebams.getBam(),mergebams.getBai(), referenceGenome, sampleWorkflow.contains("nomeseq"),sampleWorkflow.contains("rrbs"));
			dax.addJob(bissnp);
			dax.addChild(bissnp.getID(),  mergebams.getID());
			
			
			//ADD THE COMMON SUB-PIPELINES
			
			//add the general QC job pipeline
			CommonBamQC bamQCPipeSegment = new CommonBamQC(dax);
			bamQCPipeSegment.addToDax(sample,mergebams);
			
			
			if(dax.getChildCount() > 0)
			{
				dax.saveAsDot("bisulfite_merge_dax_" + label + ".dot");
				dax.saveAsSimpleDot("bisulfite_merge_dax_simple_" + label + ".dot");
				par.getWorkFlowArgsMap().put("WorkflowName", label);
				dax.runWorkflow(runOptions);
				dax.saveAsXML("bisulfite_merge_dax_" + label + ".xml");
			}
			dax.release();
			
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
