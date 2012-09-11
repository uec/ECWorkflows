package edu.usc.epigenome.workflow.job.ecjob;

import java.io.File;

import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class BisSNPJob extends ECJob
{
	public BisSNPJob(String inputBam, String inputBai, String genome, Boolean isGnomeSeq)
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
		
		
		if(isGnomeSeq)
		{
			//output cpg vcf
			String outputCPGName = outputBamName.replaceAll("^(.+?)(\\.\\w+)$", "$1\\." + "cytosine.filtered.sort.vcf");
			Filename outputCPG= new Filename(outputCPGName, LFN.OUTPUT);
			outputCPG.setRegister(false);
			this.addUses(outputCPG);
			
			//output cpg raw vcf
			String outputCPGRawName = outputBamName.replaceAll("^(.+?)(\\.\\w+)$", "$1\\." + "cytosine.raw.sort.vcf");
			Filename outputCPGRaw= new Filename(outputCPGRawName, LFN.OUTPUT);
			outputCPGRaw.setRegister(false);
			this.addUses(outputCPGRaw);
			
			//output GCG bed 
			String outputBedGCHName = outputBamName.replaceAll("^(.+?)(\\.\\w+)$", "$1\\." + "cytosine.filtered.sort.GCH.6plus2.bed");
			Filename outputBedGCH= new Filename(outputBedGCHName, LFN.OUTPUT);
			outputBedGCH.setRegister(false);
			this.addUses(outputBedGCH);
			
			//output bed HCG 
			String outputBedHCGName = outputBamName.replaceAll("^(.+?)(\\.\\w+)$", "$1\\." + "cytosine.filtered.sort.HCG.6plus2.bed");
			Filename outputBedHCG= new Filename(outputBedHCGName, LFN.OUTPUT);
			outputBedHCG.setRegister(false);
			this.addUses(outputBedHCG);
			
			
			//output methylation tdf 
			String outputMethTDFName = outputBamName.replaceAll("^(.+?)(\\.\\w+)$", "$1\\." + "cytosine.filtered.sort.HCG.tdf");
			Filename outputMethTDF= new Filename(outputMethTDFName, LFN.OUTPUT);
			outputMethTDF.setRegister(false);
			this.addUses(outputMethTDF);
			
			//output coverage tdf 
			String outputCovTDFName = outputBamName.replaceAll("^(.+?)(\\.\\w+)$", "$1\\." + "cytosine.filtered.sort.HCG.ct_coverage.tdf");
			Filename outputCovTDF= new Filename(outputCovTDFName, LFN.OUTPUT);
			outputCovTDF.setRegister(false);
			this.addUses(outputCovTDF);

			//output methylation GCH tdf 
			String outputMethGCHTDFName = outputBamName.replaceAll("^(.+?)(\\.\\w+)$", "$1\\." + "cytosine.filtered.sort.GCH.tdf");
			Filename outputMethGCHTDF= new Filename(outputMethGCHTDFName, LFN.OUTPUT);
			outputMethGCHTDF.setRegister(false);
			this.addUses(outputMethGCHTDF);

			//output coverage tdf 
			String outputCovGCHTDFName = outputBamName.replaceAll("^(.+?)(\\.\\w+)$", "$1\\." + "cytosine.filtered.sort.GCH.ct_coverage.tdf");
			Filename outputCovGCHTDF= new Filename(outputCovGCHTDFName, LFN.OUTPUT);
			outputCovGCHTDF.setRegister(false);
			this.addUses(outputCovGCHTDF);
			
			
			//output methylation summary txt 
			String outputMethSummaryName = outputBamName.replaceAll("^(.+?)(\\.\\w+)$", "$1\\." + "cytosine.raw.vcf.MethySummarizeList.txt");
			Filename outputMethSummary= new Filename(outputMethSummaryName, LFN.OUTPUT);
			outputMethSummary.setRegister(false);
			this.addUses(outputMethSummary);		
			
			
		}
		else
		{
			//output cpg vcf
			String outputCPGName = outputBamName.replaceAll("^(.+?)(\\.\\w+)$", "$1\\." + "cpg.filtered.sort.vcf");
			Filename outputCPG= new Filename(outputCPGName, LFN.OUTPUT);
			outputCPG.setRegister(false);
			this.addUses(outputCPG);
			
			//output cpg raw vcf
			String outputCPGRawName = outputBamName.replaceAll("^(.+?)(\\.\\w+)$", "$1\\." + "cpg.raw.sort.vcf");
			Filename outputCPGRaw= new Filename(outputCPGRawName, LFN.OUTPUT);
			outputCPGRaw.setRegister(false);
			this.addUses(outputCPGRaw);
						
			//output bed 
			String outputBedName = outputBamName.replaceAll("^(.+?)(\\.\\w+)$", "$1\\." + "cpg.filtered.sort.CG.6plus2.bed");
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

		}
				
		
		//output snp vcf
		String outputSNPName = outputBamName.replaceAll("^(.+?)(\\.\\w+)$", "$1\\." + "snp.filtered.sort.vcf");
		Filename outputSNP= new Filename(outputSNPName, LFN.OUTPUT);
		outputSNP.setRegister(false);
		this.addUses(outputSNP);
		
		//output snp vcf
		String outputSNPRawName = outputBamName.replaceAll("^(.+?)(\\.\\w+)$", "$1\\." + "snp.raw.sort.vcf");
		Filename outputSNPRaw= new Filename(outputSNPRawName, LFN.OUTPUT);
		outputSNPRaw.setRegister(false);
		this.addUses(outputSNPRaw);
		
		
				
		this.addArgument(input);
		this.addArgument(new PseudoText(" "));
		this.addArgument(new PseudoText(genome));
		if(isGnomeSeq)
			this.addArgument(new PseudoText(" GNOME"));
	}
}