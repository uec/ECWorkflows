package edu.usc.epigenome.workflow.job.PipelineSegment.ecPipelineSegments;

import edu.usc.epigenome.workflow.DAX.ECDax;
import edu.usc.epigenome.workflow.ECWorkflowParams.specialized.GAParams;
import edu.usc.epigenome.workflow.job.ECJob;
import edu.usc.epigenome.workflow.job.PipelineSegment.PipelineSegment;
import edu.usc.epigenome.workflow.job.ecjob.ApplicationStackJob;
import edu.usc.epigenome.workflow.job.ecjob.BamCPGCoverageJob;
import edu.usc.epigenome.workflow.job.ecjob.CoverageExtrapJob;
import edu.usc.epigenome.workflow.job.ecjob.GATKMetricJob;
import edu.usc.epigenome.workflow.job.ecjob.MergeBamsJob;
import edu.usc.epigenome.workflow.job.ecjob.PicardJob;
import edu.usc.epigenome.workflow.job.ecjob.WigToBigWigJob;

public class CommonBamQC extends PipelineSegment
{
	ECJob endPoint;
	public CommonBamQC(ECDax dax)
	{
		super(dax);
		
	}

	@Override
	public ECJob getEndPoint()
	{
		return endPoint;
	}

	@Override
	public void addToDax( String sample, ECJob startPoint)
	{
		MergeBamsJob mergebams = (MergeBamsJob) startPoint;
		
		GAParams workFlowParams = (GAParams) dax.getWorkFlowParams();
		String referenceGenome =  workFlowParams.getSamples().get(sample).get("Reference");
		if(! referenceGenome.toLowerCase().endsWith("fa"))
			referenceGenome = referenceGenome + ".fa";
		
		
		
		//GATKMetricJob dupReadPairsMetricJob = new GATKMetricJob(mergebams.getBam(), mergebams.getBai(), referenceGenome, "InvertedReadPairDups", "");
		//dax.addJob(dupReadPairsMetricJob);
		//dax.addChild(dupReadPairsMetricJob.getID(),  mergebams.getID());
		
		//create MethLevelAverages gatk job
		GATKMetricJob methLevelAveragesMetricJob = new GATKMetricJob(mergebams.getBam(), mergebams.getBai(), referenceGenome, "MethLevelAverages", "-cph");
		dax.addJob(methLevelAveragesMetricJob);
		dax.addChild(methLevelAveragesMetricJob.getID(),  mergebams.getID());
		
//		//create  50k BinDepths gatk job
//		GATKMetricJob binDepthsMetricJob50k = new GATKMetricJob(mergebams.getBam(), mergebams.getBai(), referenceGenome, "BinDepths", "-winsize 50000 -dumpv");
//		dax.addJob(binDepthsMetricJob50k);
//		dax.addChild(binDepthsMetricJob50k.getID(),  mergebams.getID());
		
		//create  5k BinDepths gatk job
		GATKMetricJob binDepthsMetricJob5k = new GATKMetricJob(mergebams.getBam(), mergebams.getBai(), referenceGenome, "BinDepths", "-winsize 5000 -dumpv",mergebams.getBam() + ".winsize5000dumpv.BinDepths.metric.wig");
		dax.addJob(binDepthsMetricJob5k);
		dax.addChild(binDepthsMetricJob5k.getID(),  mergebams.getID());
		
		//convert to bigwig
		WigToBigWigJob bigwig5k = new WigToBigWigJob(mergebams.getBam() + ".winsize5000dumpv.BinDepths.metric.wig",mergebams.getBam() + ".winsize5000dumpv.BinDepths.metric.wig.bw");
		dax.addJob(bigwig5k);
		dax.addChild(bigwig5k.getID(),binDepthsMetricJob5k.getID());
		
		//create  30 BinDepths gatk job
		GATKMetricJob binDepthsMetricJob30 = new GATKMetricJob(mergebams.getBam(), mergebams.getBai(), referenceGenome, "BinDepths", "-winsize 30 -dumpv", mergebams.getBam() + ".winsize30dumpv.BinDepths.metric.wig");
		dax.addJob(binDepthsMetricJob30);
		dax.addChild(binDepthsMetricJob30.getID(),  mergebams.getID());
		
		//convert to bigwig
		WigToBigWigJob bigwig30 = new WigToBigWigJob(mergebams.getBam() + ".winsize30dumpv.BinDepths.metric.wig",mergebams.getBam() + ".winsize30dumpv.BinDepths.metric.wig.bw");
		dax.addJob(bigwig30);
		dax.addChild(bigwig30.getID(),binDepthsMetricJob30.getID());
		
//		//create  50k downsample 5m BinDepths gatk job
//		GATKMetricJob binDepthsMetricJob50kds5 = new GATKMetricJob(mergebams.getBam(), mergebams.getBai(), referenceGenome, "BinDepths", "-p 5000000 -winsize 50000 -dumpv");
//		dax.addJob(binDepthsMetricJob50kds5);
//		dax.addChild(binDepthsMetricJob50kds5.getID(),  mergebams.getID());
//		
		//create  5k downsample 5m BinDepths gatk job
		GATKMetricJob binDepthsMetricJob5kds5 = new GATKMetricJob(mergebams.getBam(), mergebams.getBai(), referenceGenome, "BinDepths", "-p 5000000 -winsize 5000 -dumpv",mergebams.getBam() +".p5000000winsize50000dumpv.BinDepths.metric.wig");
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
		//PicardJob estimateLibraryComplexity = new PicardJob(mergebams.getBam(), "EstimateLibraryComplexity", "", mergebams.getBam() + ".EstimateLibraryComplexity.metric.txt");
		//dax.addJob(estimateLibraryComplexity);
		//dax.addChild(estimateLibraryComplexity.getID(),  mergebams.getID());
					
		//Application Stack tracking job
		ApplicationStackJob appstack = new ApplicationStackJob(mergebams.getBam(), mergebams.getBam() + ".ApplicationStackMetrics.metric.txt");
		dax.addJob(appstack);
		dax.addChild(appstack.getID(), mergebams.getID());
		
		//CPG vs randam cov job
		BamCPGCoverageJob bamcov = new BamCPGCoverageJob(mergebams.getBam(), mergebams.getBai(), "/home/rcf-40/bberman/tumor/genomic-data-misc/CGIs/Takai_Jones_from_Fei_122007.fixed.PROMOTERONLY.oriented.hg19.bed", mergebams.getBam() + ".CPGvsRandomCov.metric.txt");
		dax.addJob(bamcov);
		dax.addChild(bamcov.getID(),  mergebams.getID());
		
		//lc_extrap vs randam cov job
		CoverageExtrapJob lcexrap = new CoverageExtrapJob(mergebams.getBam(),mergebams.getBam() + ".CoverageProjection.metric.txt");
		dax.addJob(lcexrap);
		dax.addChild(lcexrap.getID(),  mergebams.getID());
		
		//create  read length cigar parser gatk job
		GATKMetricJob readlen = new GATKMetricJob(mergebams.getBam(), mergebams.getBai(), referenceGenome, "ReadLength", "");
		dax.addJob(readlen);
		dax.addChild(readlen.getID(),  mergebams.getID());
		
		endPoint = appstack; 		
	}

}
