package edu.usc.epigenome.workflow.generator;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.griphyn.vdl.dax.Filename;


import edu.usc.epigenome.workflow.DAX.ECDax;
import edu.usc.epigenome.workflow.ECWorkflowParams.specialized.GAParams;
import edu.usc.epigenome.workflow.job.ECJob;
import edu.usc.epigenome.workflow.job.ecjob.ApplicationStackJob;
import edu.usc.epigenome.workflow.job.ecjob.BSMapJob;
import edu.usc.epigenome.workflow.job.ecjob.CountAdapterTrimJob;
import edu.usc.epigenome.workflow.job.ecjob.CountFastQJob;
import edu.usc.epigenome.workflow.job.ecjob.CountNmerJob;
import edu.usc.epigenome.workflow.job.ecjob.FastQConstantSplitJob;
import edu.usc.epigenome.workflow.job.ecjob.FilterContamsJob;
import edu.usc.epigenome.workflow.job.ecjob.GATKMetricJob;
import edu.usc.epigenome.workflow.job.ecjob.MergeBamsJob;
import edu.usc.epigenome.workflow.job.ecjob.PicardJob;
import edu.usc.epigenome.workflow.job.ecjob.QCMetricsJob;


public class BisulfiteAlignmentWorkflow
{
	/**
	 * Creates an AlignPileUp workflow from an empty dax object 
	 * @param dax The ECDAX to which processing jobs will be added
	 */	
	public static String WorkflowName = "bisulfite";
	
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
			
			List<ECJob> bsmapJobs = new LinkedList<ECJob>();
			List<String> filterTrimCountFiles = new LinkedList<String>();
			
			String laneInputFileNameR1 = pbsMode ? new File(fileInput.split(",")[0]).getAbsolutePath() : new File(fileInput.split(",")[0]).getName();
			String laneInputFileNameR2 = null;
			//split Fastq Job. handle paired end and non pbs
			int splitSize = Integer.parseInt(workFlowParams.getSetting("ClusterSize"));
			FastQConstantSplitJob fastqSplitJob = null;
			if(isPE)
			{
				laneInputFileNameR2 = pbsMode ? new File(fileInput.split(",")[1]).getAbsolutePath() : new File(fileInput.split(",")[1]).getName();
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
					
					BSMapJob bsmap = new BSMapJob(read1, read2,referenceGenome, read1 + ".bam");
					dax.addJob(bsmap);
					dax.addChild(bsmap.getID(),parentJobEnd1);
					dax.addChild(bsmap.getID(),parentJobEnd2);
					bsmapJobs.add(bsmap);
				}						
			}
			else
			{
				for(int h = 0; h < splitFiles.size(); h++)
				{
					String read1 = splitFiles.get(h);
					String parentJobEnd1 = splitIDs.get(h);
					
					BSMapJob bsmap = new BSMapJob(read1, null,referenceGenome, read1 + ".bam");
					dax.addJob(bsmap);
					dax.addChild(bsmap.getID(),parentJobEnd1);
					bsmapJobs.add(bsmap);
				}
			}
			
	
			
			ArrayList<String> splitBams = new ArrayList<String>();
			for(ECJob job : bsmapJobs)
				splitBams.add(job.getSingleOutputFile().getFilename());
			
			// for each lane create a map merge job
			MergeBamsJob mergebams = new MergeBamsJob(splitBams,"ResultCount_" + flowcellID + "_" + laneNumber + "_" + sampleName + ".bam");
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

			
			//create nmercount for 3, child of mapmerge
			CountNmerJob count3mer = new CountNmerJob(splitFiles.toArray(new String[0]), flowcellID, Integer.parseInt(laneNumber), 3);
			dax.addJob(count3mer);
			dax.addChild(count3mer.getID(),mergebams.getID());
			
			//create nmercount for 5, child of nmer
			CountNmerJob count5mer = new CountNmerJob(splitFiles.toArray(new String[0]), flowcellID, Integer.parseInt(laneNumber), 5);
			dax.addJob(count5mer);
			dax.addChild(count5mer.getID(), count3mer.getID());
			
			//create nmercount for 10, child of nmer
			CountNmerJob count10mer = new CountNmerJob(splitFiles.toArray(new String[0]), flowcellID, Integer.parseInt(laneNumber), 10);
			dax.addJob(count10mer);
			dax.addChild(count10mer.getID(),  count3mer.getID());
			
			//create readpairdup gatk job
			GATKMetricJob dupReadPairsMetricJob = new GATKMetricJob(mergebams.getBam(), mergebams.getBai(), referenceGenome, "InvertedReadPairDups", "");
			dax.addJob(dupReadPairsMetricJob);
			dax.addChild(dupReadPairsMetricJob.getID(),  mergebams.getID());
			
			//create MethLevelAverages gatk job
			GATKMetricJob methLevelAveragesMetricJob = new GATKMetricJob(mergebams.getBam(), mergebams.getBai(), referenceGenome, "MethLevelAverages", "-cph");
			dax.addJob(methLevelAveragesMetricJob);
			dax.addChild(methLevelAveragesMetricJob.getID(),  mergebams.getID());
			
			//create MethLevelAverages CHROM M gatk job
			GATKMetricJob methLevelAveragesChrmMetricJob = new GATKMetricJob(mergebams.getBam(), mergebams.getBai(), referenceGenome, "MethLevelAverages", "-L chrM -cph");
			dax.addJob(methLevelAveragesChrmMetricJob);
			dax.addChild(methLevelAveragesChrmMetricJob.getID(),  mergebams.getID());
			
			//create  50k BinDepths gatk job
			GATKMetricJob binDepthsMetricJob50k = new GATKMetricJob(mergebams.getBam(), mergebams.getBai(), referenceGenome, "BinDepths", "-winsize 50000 -dumpv");
			dax.addJob(binDepthsMetricJob50k);
			dax.addChild(binDepthsMetricJob50k.getID(),  mergebams.getID());
			
			//create  5k BinDepths gatk job
			GATKMetricJob binDepthsMetricJob5k = new GATKMetricJob(mergebams.getBam(), mergebams.getBai(), referenceGenome, "BinDepths", "-winsize 5000 -dumpv");
			dax.addJob(binDepthsMetricJob5k);
			dax.addChild(binDepthsMetricJob5k.getID(),  mergebams.getID());
			
			//create  50k downsample 5m BinDepths gatk job
			GATKMetricJob binDepthsMetricJob50kds5 = new GATKMetricJob(mergebams.getBam(), mergebams.getBai(), referenceGenome, "BinDepths", "-p 5000000 -winsize 50000 -dumpv");
			dax.addJob(binDepthsMetricJob50kds5);
			dax.addChild(binDepthsMetricJob50kds5.getID(),  mergebams.getID());
			
			//create  5k downsample 5m BinDepths gatk job
			GATKMetricJob binDepthsMetricJob5kds5 = new GATKMetricJob(mergebams.getBam(), mergebams.getBai(), referenceGenome, "BinDepths", "-p 5000000 -winsize 5000 -dumpv");
			dax.addJob(binDepthsMetricJob5kds5);
			dax.addChild(binDepthsMetricJob5kds5.getID(),  mergebams.getID());
			
			//create  5m Downsample dups gatk job
			GATKMetricJob dsdups = new GATKMetricJob(mergebams.getBam(), mergebams.getBai(), referenceGenome, "DownsampleDups", "-p 5000000 -trials 100 -nt 8");
			dax.addJob(dsdups);
			dax.addChild(dsdups.getID(),  mergebams.getID());
			
			//map to lambaphage
			ArrayList<String> splitLambdaBams = new ArrayList<String>();
			BSMapJob lambdaphage = new BSMapJob(laneInputFileNameR1, laneInputFileNameR2,referenceGenome, laneInputFileNameR1 + ".LambdaPhage.bam");
			splitLambdaBams.add(lambdaphage.getSingleOutputFile().getFilename());
			dax.addJob(lambdaphage);
			dax.addChild(lambdaphage.getID(), fastqSplitJob.getID());
			
			//merge and create bais for lambda aln
			MergeBamsJob mergelambdabams = new MergeBamsJob(splitBams,"ResultCount_" + flowcellID + "_" + laneNumber + "_" + sampleName + "_LambdaPhage" + ".bam");
			dax.addJob(mergelambdabams);
			dax.addChild(mergelambdabams.getID(),lambdaphage.getID());
			
			//methlevelavgs for lambdaphage
			GATKMetricJob methLevelLambdaAveragesMetricJob = new GATKMetricJob(mergelambdabams.getBam(), mergelambdabams.getBai(), "/home/uec-00/shared/production/genomes/lambdaphage/NC_001416.fa", "MethLevelAverages", "-cph");
			dax.addJob(methLevelLambdaAveragesMetricJob);
			dax.addChild(methLevelLambdaAveragesMetricJob.getID(),  mergelambdabams.getID());
			
			//PICARD insertsize metrics
			PicardJob insertSizeJob = new PicardJob(mergebams.getBam(), "CollectInsertSizeMetrics", "HISTOGRAM_FILE=chart", mergebams.getBam() + ".CollectInsertSizeMetrics.metric.txt");
			dax.addJob(insertSizeJob);
			dax.addChild(insertSizeJob.getID(),  mergebams.getID());
			
			//PICARD mean qual metrics
			PicardJob meanQualJob = new PicardJob(mergebams.getBam(), "MeanQualityByCycle", "CHART_OUTPUT=chart", mergebams.getBam() + ".MeanQualityByCycle.metric.txt");
			dax.addJob(meanQualJob);
			dax.addChild(meanQualJob.getID(),  mergebams.getID());
			
			//PICARD qual dist metrics
			PicardJob qualDistJob = new PicardJob(mergebams.getBam(), "QualityScoreDistribution", "CHART_OUTPUT=chart", mergebams.getBam() + ".QualityScoreDistribution.metric.txt");
			dax.addJob(qualDistJob);
			dax.addChild(qualDistJob.getID(),  mergebams.getID());
			
			//PICARD CollectGcBiasMetrics
			PicardJob gcBiasJob = new PicardJob(mergebams.getBam(), "CollectGcBiasMetrics", "CHART_OUTPUT=chart REFERENCE_SEQUENCE=" + referenceGenome, mergebams.getBam() + ".CollectGcBiasMetrics.metric.txt");
			dax.addJob(gcBiasJob);
			dax.addChild(gcBiasJob.getID(),  mergebams.getID());
			
			//PICARD CollectAlignmentMetrics
			PicardJob collectAlignmentMetricsJob = new PicardJob(mergebams.getBam(), "CollectAlignmentSummaryMetrics", "IS_BISULFITE_SEQUENCED=true REFERENCE_SEQUENCE=" + referenceGenome, mergebams.getBam() + ".CollectAlignmentSummaryMetrics.metric.txt");
			dax.addJob(collectAlignmentMetricsJob);
			dax.addChild(collectAlignmentMetricsJob.getID(),  mergebams.getID());
			
			//PICARD EstimateLibraryComplexity
			PicardJob estimateLibraryComplexity = new PicardJob(mergebams.getBam(), "EstimateLibraryComplexity", "", mergebams.getBam() + ".EstimateLibraryComplexity.metric.txt");
			dax.addJob(estimateLibraryComplexity);
			dax.addChild(estimateLibraryComplexity.getID(),  mergebams.getID());
			
			//Application Stack tracking job
			ApplicationStackJob appstack = new ApplicationStackJob(mergebams.getBam(), mergebams.getBam() + ".ApplicationStackMetrics.metric.txt");
			dax.addJob(appstack);
			dax.addChild(appstack.getID(),collectAlignmentMetricsJob.getID());
			
			
			if(dax.getChildCount() > 0)
			{
				dax.saveAsDot("bisulfite_dax_" + label + ".dot");
				dax.saveAsSimpleDot("bisulfite_dax_simple_" + label + ".dot");
				if(pbsMode)
				{
					par.getWorkFlowArgsMap().put("WorkflowName", label);
					dax.runWorkflow(dryrun);
				}
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
