package edu.usc.epigenome.workflow.job.PipelineSegment.ecPipelineSegments;

import java.io.File;
import java.util.ArrayList;

import edu.usc.epigenome.workflow.DAX.ECDax;
import edu.usc.epigenome.workflow.ECWorkflowParams.specialized.GAParams;
import edu.usc.epigenome.workflow.job.ECJob;
import edu.usc.epigenome.workflow.job.PipelineSegment.PipelineSegment;
import edu.usc.epigenome.workflow.job.ecjob.BSMapJob;
import edu.usc.epigenome.workflow.job.ecjob.FastQConstantSplitJob;
import edu.usc.epigenome.workflow.job.ecjob.GATKMetricJob;
import edu.usc.epigenome.workflow.job.ecjob.MergeBamsJob;
import edu.usc.epigenome.workflow.job.ecjob.MethLevelAveragesJob;
import edu.usc.epigenome.workflow.job.ecjob.SamToolsJob;

public class LambdaPhageBisAlnQC extends PipelineSegment
{
	ECJob endPoint;
	public LambdaPhageBisAlnQC(ECDax thisDax)
	{
		super(thisDax);		
	}

	@Override
	public void addToDax(String sample, ECJob startPoint)
	{
		try
		{
			FastQConstantSplitJob fastqSplitJob = (FastQConstantSplitJob) startPoint;
			
			GAParams workFlowParams = (GAParams) dax.getWorkFlowParams();
			String flowcellID = workFlowParams.getSetting("FlowCellName");
			String sampleName = workFlowParams.getSamples().get(sample).get("SampleID");
			String laneNumber = workFlowParams.getSamples().get(sample).get("Lane");
			String fileInput = workFlowParams.getSamples().get(sample).get("Input");
			
			String laneInputFileNameR1 = new File(fileInput.split(",")[0]).getAbsolutePath();
			String laneInputFileNameR2 = fileInput.contains(",") ? new File(fileInput.split(",")[1]).getAbsolutePath() : null;
			
			//map to lambaphage
			BSMapJob lambdaphage = new BSMapJob(laneInputFileNameR1, laneInputFileNameR2,"/home/uec-00/shared/production/genomes/lambdaphage/NC_001416.fa", new File(laneInputFileNameR1).getName().replace(".txt", ".1.txt") + ".NC_001416.fa.bam");
			dax.addJob(lambdaphage);
			dax.addChild(lambdaphage.getID(), fastqSplitJob.getID());
			
			//samtools remove unaln
			ArrayList<String> splitLambdaBams = new ArrayList<String>();
			SamToolsJob removeUnAln = new SamToolsJob(lambdaphage.getSingleOutputFile().getFilename(), "view", " -h -F 4 -b -o " + lambdaphage.getSingleOutputFile().getFilename() + ".rmuln.bam " + lambdaphage.getSingleOutputFile().getFilename(), lambdaphage.getSingleOutputFile().getFilename() + ".rmuln.bam ");
			dax.addJob(removeUnAln);
			dax.addChild(removeUnAln.getID(), lambdaphage.getID());
			splitLambdaBams.add(removeUnAln.getSingleOutputFile().getFilename());
	
			//merge and create bai's for lambda aln
			MergeBamsJob mergelambdabams = new MergeBamsJob(splitLambdaBams,"ResultCount_" + flowcellID + "_" + laneNumber + "_" + sampleName + ".NC_001416.fa" + ".bam");
			dax.addJob(mergelambdabams);
			dax.addChild(mergelambdabams.getID(),removeUnAln.getID());
			
			//methlevelavgs for lambdaphage
			//create MethLevelAverages CHROM M gatk job
			MethLevelAveragesJob methlevels = new MethLevelAveragesJob(mergelambdabams.getBam(), mergelambdabams.getBai(),  mergelambdabams.getBam() + ".MethLevelAverages.metric.txt", "/home/uec-00/shared/production/genomes/lambdaphage/NC_001416.fa", "");
			dax.addJob(methlevels);
			dax.addChild(methlevels.getID(),  mergelambdabams.getID());
			
			
			endPoint =  methlevels;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	@Override
	public ECJob getEndPoint()
	{
		return endPoint;
	}

}
