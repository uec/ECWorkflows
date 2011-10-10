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
import edu.usc.epigenome.workflow.job.ecjob.BwaJob;
import edu.usc.epigenome.workflow.job.ecjob.CleanUpFilesJob;
import edu.usc.epigenome.workflow.job.ecjob.CountAdapterTrimJob;
import edu.usc.epigenome.workflow.job.ecjob.CountFastQJob;
import edu.usc.epigenome.workflow.job.ecjob.CountNmerJob;
import edu.usc.epigenome.workflow.job.ecjob.FastQConstantSplitJob;
import edu.usc.epigenome.workflow.job.ecjob.FilterContamsJob;
import edu.usc.epigenome.workflow.job.ecjob.FindPeaksJob;
import edu.usc.epigenome.workflow.job.ecjob.GATKMetricJob;
import edu.usc.epigenome.workflow.job.ecjob.MergeBamsJob;
import edu.usc.epigenome.workflow.job.ecjob.OrgContamCheckJob;
import edu.usc.epigenome.workflow.job.ecjob.PicardJob;
import edu.usc.epigenome.workflow.job.ecjob.QCMetricsJob;
import edu.usc.epigenome.workflow.job.ecjob.SampleNReadsJob;
import edu.usc.epigenome.workflow.job.ecjob.WigToTdfJob;


public class ChipSeqBWAWorkflow
{
	/**
	 * Creates an AlignPileUp workflow from an empty dax object 
	 * @param dax The ECDAX to which processing jobs will be added
	 */	
	public static String WorkflowName = "chipseq";
	
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
			
			List<ECJob> alignmentJobs = new LinkedList<ECJob>();
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
				System.out.println("Creating chipseq (BWA) PE Processing workflow for lane " + label + ": " + laneInputFileNameR1 + " " + laneInputFileNameR2 );
			}
			else
			{
				fastqSplitJob = new FastQConstantSplitJob(laneInputFileNameR1, splitSize);
				System.out.println("Creating chipseq (BWA) SR Processing workflow for lane " + label + ": " + laneInputFileNameR1);
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
					
					//align
					BwaJob bwajob = new BwaJob(read1,read2,referenceGenome);
					dax.addJob(bwajob);
					dax.addChild(bwajob.getID(),parentJobEnd1);
					dax.addChild(bwajob.getID(),parentJobEnd2);
					
					//sort
					PicardJob picardSortJob = new PicardJob(bwajob.getSingleOutputFile().getFilename(), "SortSam", "VALIDATION_STRINGENCY=SILENT SORT_ORDER=coordinate", bwajob.getSingleOutputFile().getFilename() + ".sorted.bam");
					dax.addJob(picardSortJob);
					dax.addChild(picardSortJob.getID(),  bwajob.getID());
					
					//reorder contigs
					PicardJob picardReorderContigsJob = new PicardJob(picardSortJob.getSingleOutputFile().getFilename(), "ReorderSam", "VALIDATION_STRINGENCY=SILENT  REFERENCE=" + referenceGenome, picardSortJob.getSingleOutputFile().getFilename() + ".reorder.bam");
					dax.addJob(picardReorderContigsJob);
					dax.addChild(picardReorderContigsJob.getID(), picardSortJob.getID());
					
					alignmentJobs.add(picardReorderContigsJob);
				}						
			}
			else
			{
				for(int h = 0; h < splitFiles.size(); h++)
				{
					String read1 = splitFiles.get(h);
					String parentJobEnd1 = splitIDs.get(h);
					
					//align
					BwaJob bwajob = new BwaJob(read1,referenceGenome);
					dax.addJob(bwajob);
					dax.addChild(bwajob.getID(),parentJobEnd1);
					
					//sort
					PicardJob picardSortJob = new PicardJob(bwajob.getSingleOutputFile().getFilename(), "SortSam", "VALIDATION_STRINGENCY=SILENT SORT_ORDER=coordinate", bwajob.getSingleOutputFile().getFilename() + ".sorted.bam");
					dax.addJob(picardSortJob);
					dax.addChild(picardSortJob.getID(),  bwajob.getID());
					
					//reorder contigs
					PicardJob picardReorderContigsJob = new PicardJob(picardSortJob.getSingleOutputFile().getFilename(), "ReorderSam", "VALIDATION_STRINGENCY=SILENT REFERENCE=" + referenceGenome, picardSortJob.getSingleOutputFile().getFilename() + ".reorder.bam");
					dax.addJob(picardReorderContigsJob);
					dax.addChild(picardReorderContigsJob.getID(), picardSortJob.getID());
					
					alignmentJobs.add(picardReorderContigsJob);
				}
			}
			
	
			
			ArrayList<String> splitBams = new ArrayList<String>();
			for(ECJob job : alignmentJobs)
				splitBams.add(job.getSingleOutputFile().getFilename());
			
			// for each lane create a map merge job
			MergeBamsJob mergebams = new MergeBamsJob(splitBams,"ResultCount_" + flowcellID + "_" + laneNumber + "_" + sampleName + "." + new File(referenceGenome).getName() + ".bam");
			dax.addJob(mergebams);
			
			// mapmerge is child to all the map jobs
			for (ECJob job : alignmentJobs)
				dax.addChild(mergebams.getID(), job.getID());
		
			//FINDPEAKs job, child of mergebams
			FindPeaksJob findpeaks = new FindPeaksJob(mergebams.getBam(),200);
			dax.addJob(findpeaks);
			dax.addChild(findpeaks.getID(), mergebams.getID());
			
			//wig to tdf (IGVTOOLS )job child of pileup to wig
			WigToTdfJob fpwigtotdf = new WigToTdfJob(findpeaks.getWigFile(),referenceGenome);
			dax.addJob(fpwigtotdf);
			dax.addChild(fpwigtotdf.getID(),findpeaks.getID());
			
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
			
			//cleanup garbage job
			CleanUpFilesJob cleanup = new CleanUpFilesJob(workFlowParams.getSetting("tmpDir") + "/" + flowcellID + "/" + label);
			dax.addJob(cleanup);
			dax.addChild(cleanup.getID(),count10mer.getID());
			dax.addChild(cleanup.getID(),qcjob.getID());
			dax.addChild(cleanup.getID(),countFastQJob.getID());
			dax.addChild(cleanup.getID(),countAdapterTrim.getID());
			
			//create readpairdup gatk job
			GATKMetricJob dupReadPairsMetricJob = new GATKMetricJob(mergebams.getBam(), mergebams.getBai(), referenceGenome, "InvertedReadPairDups", "");
			dax.addJob(dupReadPairsMetricJob);
			dax.addChild(dupReadPairsMetricJob.getID(),  mergebams.getID());
			
			//create MethLevelAverages gatk job
			GATKMetricJob methLevelAveragesMetricJob = new GATKMetricJob(mergebams.getBam(), mergebams.getBai(), referenceGenome, "MethLevelAverages", "-cph");
			dax.addJob(methLevelAveragesMetricJob);
			dax.addChild(methLevelAveragesMetricJob.getID(),  mergebams.getID());
			
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
			
			//insertsize metrics
			PicardJob insertSizeJob = new PicardJob(mergebams.getBam(), "CollectInsertSizeMetrics", "VALIDATION_STRINGENCY=SILENT HISTOGRAM_FILE=chart", mergebams.getBam() + ".CollectInsertSizeMetrics.metric.txt");
			dax.addJob(insertSizeJob);
			dax.addChild(insertSizeJob.getID(),  mergebams.getID());
			
			//mean qual metrics
			PicardJob meanQualJob = new PicardJob(mergebams.getBam(), "MeanQualityByCycle", "VALIDATION_STRINGENCY=SILENT CHART_OUTPUT=chart", mergebams.getBam() + ".MeanQualityByCycle.metric.txt");
			dax.addJob(meanQualJob);
			dax.addChild(meanQualJob.getID(),  mergebams.getID());
			
			//qual dist metrics
			PicardJob qualDistJob = new PicardJob(mergebams.getBam(), "QualityScoreDistribution", "VALIDATION_STRINGENCY=SILENT CHART_OUTPUT=chart", mergebams.getBam() + ".QualityScoreDistribution.metric.txt");
			dax.addJob(qualDistJob);
			dax.addChild(qualDistJob.getID(),  mergebams.getID());
			
			//CollectGcBiasMetrics
			PicardJob gcBiasJob = new PicardJob(mergebams.getBam(), "CollectGcBiasMetrics", "VALIDATION_STRINGENCY=SILENT CHART_OUTPUT=chart REFERENCE_SEQUENCE=" + referenceGenome, mergebams.getBam() + ".CollectGcBiasMetrics.metric.txt");
			dax.addJob(gcBiasJob);
			dax.addChild(gcBiasJob.getID(),  mergebams.getID());
			
			//PICARD EstimateLibraryComplexity
			PicardJob estimateLibraryComplexity = new PicardJob(mergebams.getBam(), "EstimateLibraryComplexity", "", mergebams.getBam() + ".EstimateLibraryComplexity.metric.txt");
			dax.addJob(estimateLibraryComplexity);
			dax.addChild(estimateLibraryComplexity.getID(),  mergebams.getID());
			
			//CollectAlignmentMetrics
			PicardJob collectAlignmentMetricsJob = new PicardJob(mergebams.getBam(), "CollectAlignmentSummaryMetrics", "VALIDATION_STRINGENCY=SILENT IS_BISULFITE_SEQUENCED=false REFERENCE_SEQUENCE=" + referenceGenome, mergebams.getBam() + ".CollectAlignmentSummaryMetrics.metric.txt");
			dax.addJob(collectAlignmentMetricsJob);
			dax.addChild(collectAlignmentMetricsJob.getID(),  mergebams.getID());
			
			//Application Stack tracking job
			ApplicationStackJob appstack = new ApplicationStackJob(mergebams.getBam(), mergebams.getBam() + ".ApplicationStackMetrics.metric.txt");
			dax.addJob(appstack);
			dax.addChild(appstack.getID(),collectAlignmentMetricsJob.getID());
			
			//Contam tests
			String[] organisms = {"/home/uec-00/shared/production/genomes/encode_hg19_mf/female.hg19.fa", 
					  "/home/uec-00/shared/production/genomes/sacCer1/sacCer1.fa",
					  "/home/uec-00/shared/production/genomes/phi-X174/phi_plus_SNPs.fa",
					  "/home/uec-00/shared/production/genomes/arabidopsis/tair8.pluscontam.fa",
					  "/home/uec-00/shared/production/genomes/mm9_unmasked/mm9_unmasked.fa",
					  "/home/uec-00/shared/production/genomes/Ecoli/EcoliIHE3034.fa",
					  "/home/uec-00/shared/production/genomes/rn4_unmasked/rn4.fa",
					  "/home/uec-00/shared/production/genomes/lambdaphage/NC_001416.fa"};
			
			OrgContamCheckJob bwaTestContam = new OrgContamCheckJob(laneInputFileNameR1,5000000,organisms);
			dax.addJob(bwaTestContam);
			dax.addChild(bwaTestContam.getID(),fastqSplitJob.getID());

			if(dax.getChildCount() > 0)
			{
				dax.saveAsDot("chipseq_dax_" + label + ".dot");
				dax.saveAsSimpleDot("chipseq_dax_simple_" + label + ".dot");
				if(pbsMode)
				{
					par.getWorkFlowArgsMap().put("WorkflowName", label);
					dax.runWorkflow(dryrun);
				}
				dax.saveAsXML("chipseq_dax_" + label + ".xml");
			}
			dax.release();
			
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
