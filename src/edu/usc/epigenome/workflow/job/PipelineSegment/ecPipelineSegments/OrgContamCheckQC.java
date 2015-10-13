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
		String[] organisms = {"/primary/vari/genomicdata/genomes/encode_hg19_mf/female.hg19.fa", 
				  "/primary/vari/genomicdata/genomes/sacCer1/sacCer1.fa",
				  "/primary/vari/genomicdata/genomes/phi-X174/phi_plus_SNPs.fa",
				  "/primary/vari/genomicdata/genomes/arabidopsis/tair8.pluscontam.fa",
				  "/primary/vari/genomicdata/genomes/mm9_unmasked/mm9_unmasked.fa",
				  "/primary/vari/genomicdata/genomes/Ecoli/EcoliIHE3034.fa",
				  "/primary/vari/genomicdata/genomes/rn4_unmasked/rn4.fa",
				  "/primary/vari/genomicdata/genomes/salmon/salmosalar.fa",
				  "/primary/vari/genomicdata/genomes/rRNA/rRNA.fa",
				  "/primary/vari/genomicdata/genomes/chinese_hamster/criGri1.fa",
				  "/primary/vari/genomicdata/genomes/lambdaphage/NC_001416.fa"};
		
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
