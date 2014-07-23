package edu.usc.epigenome.workflow.job.PipelineSegment.ecPipelineSegments;

import java.io.File;

import edu.usc.epigenome.workflow.DAX.ECDax;
import edu.usc.epigenome.workflow.ECWorkflowParams.specialized.GAParams;
import edu.usc.epigenome.workflow.job.ECJob;
import edu.usc.epigenome.workflow.job.PipelineSegment.PipelineSegment;
import edu.usc.epigenome.workflow.job.ecjob.FastQConstantSplitJob;
import edu.usc.epigenome.workflow.job.ecjob.OrgContamCheckJob;

public class OrgContamCheckQC extends PipelineSegment
{
	ECJob endPoint;
	public OrgContamCheckQC(ECDax thisDax)
	{
		super(thisDax);		
	}

	@Override
	public void addToDax(String sample, ECJob startPoint)
	{
		FastQConstantSplitJob fastqSplitJob = (FastQConstantSplitJob) startPoint;
		
		GAParams workFlowParams = (GAParams) dax.getWorkFlowParams();
		String fileInput = workFlowParams.getSamples().get(sample).get("Input");
		String laneInputFileNameR1 = new File(fileInput.split(",")[0]).getAbsolutePath();
		
		//Contam tests
		String[] organisms = {"/home/uec-00/shared/production/genomes/encode_hg19_mf/female.hg19.fa", 
				  "/home/uec-00/shared/production/genomes/sacCer1/sacCer1.fa",
				  "/home/uec-00/shared/production/genomes/phi-X174/phi_plus_SNPs.fa",
				  "/home/uec-00/shared/production/genomes/arabidopsis/tair8.pluscontam.fa",
				  "/home/uec-00/shared/production/genomes/mm9_unmasked/mm9_unmasked.fa",
				  "/home/uec-00/shared/production/genomes/Ecoli/EcoliIHE3034.fa",
				  "/home/uec-00/shared/production/genomes/rn4_unmasked/rn4.fa",
				  "/home/uec-00/shared/production/genomes/salmon/salmosalar.fa",
				  "/home/uec-00/shared/production/genomes/rRNA/rRNA.fa",
				  "/home/uec-00/shared/production/genomes/chinese_hamster/criGri1.fa",
				  "/home/uec-00/shared/production/genomes/lambdaphage/NC_001416.fa"};
		
		OrgContamCheckJob bwaTestContam = new OrgContamCheckJob(laneInputFileNameR1,5000000,organisms);
		dax.addJob(bwaTestContam);
		dax.addChild(bwaTestContam.getID(),fastqSplitJob.getID());
		
		endPoint = bwaTestContam;

	}

	@Override
	public ECJob getEndPoint()
	{
		return endPoint;
	}

}
