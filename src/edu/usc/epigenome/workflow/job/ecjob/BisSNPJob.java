package edu.usc.epigenome.workflow.job.ecjob;

import java.io.File;

import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class BisSNPJob extends ECJob
{
	public BisSNPJob(String inputBam, String inputBai, String genome)
	{
		super(WorkflowConstants.NAMESPACE, "bissnp", WorkflowConstants.VERSION, "bissnp_" + new File(inputBam).getName());
		
		Filename input = new Filename(inputBam, LFN.INPUT);
		input.setRegister(false);
		this.addUses(input);
		
		Filename baiInput = new Filename(inputBai, LFN.INPUT);
		baiInput.setRegister(false);
		this.addUses(baiInput);
		
		//output bam
		String outputBamName = inputBam.replaceAll("^(.+?)(\\.\\w+)$", "$1\\." + "realign.mdups.recal.bam");
		Filename outputBam= new Filename(outputBamName, LFN.OUTPUT);
		outputBam.setRegister(false);
		this.addUses(outputBam);
		
		//output bai
		String outputBaiName = inputBam.replaceAll("^(.+?)(\\.\\w+)$", "$1\\." + "realign.mdups.recal.bai");
		Filename outputBai= new Filename(outputBaiName, LFN.OUTPUT);
		outputBai.setRegister(false);
		this.addUses(outputBai);
		
		
		//output cpg vcf
		String outputCPGName = outputBamName.replaceAll("^(.+?)(\\.\\w+)$", "$1\\." + "cpg.filtered.sort.vcf");
		Filename outputCPG= new Filename(outputCPGName, LFN.OUTPUT);
		outputCPG.setRegister(false);
		this.addUses(outputCPG);
		
		
		//output snp vcf
		String outputSNPName = outputBamName.replaceAll("^(.+?)(\\.\\w+)$", "$1\\." + "snp.filtered.sort.vcf");
		Filename outputSNP= new Filename(outputSNPName, LFN.OUTPUT);
		outputSNP.setRegister(false);
		this.addUses(outputSNP);
		
		//output bed 
		String outputBedName = outputBamName.replaceAll("^(.+?)(\\.\\w+)$", "$1\\." + "cpg.filtered.sort.6plus2.bed");
		Filename outputBed= new Filename(outputBedName, LFN.OUTPUT);
		outputBed.setRegister(false);
		this.addUses(outputBed);
		
		//output methylation tdf 
		String outputMethTDFName = outputBamName.replaceAll("^(.+?)(\\.\\w+)$", "$1\\." + "cpg.filtered.sort.CG.tdf");
		Filename outputMethTDF= new Filename(outputMethTDFName, LFN.OUTPUT);
		outputMethTDF.setRegister(false);
		this.addUses(outputMethTDF);
		
		//output coverage tdf 
		String outputCovTDFName = outputBamName.replaceAll("^(.+?)(\\.\\w+)$", "$1\\." + "cpg.filtered.sort.CG.ct_coverage.tdf");
		Filename outputCovTDF= new Filename(outputCovTDFName, LFN.OUTPUT);
		outputCovTDF.setRegister(false);
		this.addUses(outputCovTDF);

		//output methylation summary txt 
		String outputMethSummaryName = outputBamName.replaceAll("^(.+?)(\\.\\w+)$", "$1\\." + "cpg.raw.vcf.MethySummarizeList.txt");
		Filename outputMethSummary= new Filename(outputMethSummaryName, LFN.OUTPUT);
		outputMethSummary.setRegister(false);
		this.addUses(outputMethSummary);
				
		this.addArgument(input);
		this.addArgument(new PseudoText(" "));
		this.addArgument(new PseudoText(genome));
	}
}