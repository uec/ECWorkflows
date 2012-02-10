package edu.usc.epigenome.workflow.job.ecjob;

import java.io.File;

import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class BisSNPJob extends ECJob
{
	public BisSNPJob(String inputBam, String inputBai)
	{
		super(WorkflowConstants.NAMESPACE, "bissnp", WorkflowConstants.VERSION, "bissnp_" + new File(inputBam).getName());
		
		Filename input = new Filename(inputBam, LFN.INPUT);
		input.setRegister(false);
		this.addUses(input);
		
		Filename baiInput = new Filename(inputBai, LFN.INPUT);
		baiInput.setRegister(false);
		this.addUses(baiInput);

		// construct the output wig and peaks for input bam
		String cpgFile = inputBam + ".cpg.raw.vcf";
		Filename outputCPG = new Filename(cpgFile, LFN.OUTPUT);
		outputCPG.setRegister(false);
		this.addUses(outputCPG);
		
		String snpFile = inputBam + ".snp.raw.vcf";
		Filename outputSNP = new Filename(snpFile, LFN.OUTPUT);
		outputSNP.setRegister(false);
		this.addUses(outputSNP);
		
		
		this.addArgument(input);				
	}
}