package edu.usc.epigenome.workflow.job.ecjob;

import java.io.File;
import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class GATKGenotyperJob extends ECJob
{
	public GATKGenotyperJob(String inputBamName, String inputBaiName,String genome)
	{
		super(WorkflowConstants.NAMESPACE, "gatkgenotyper", WorkflowConstants.VERSION, "gatkgenotyper_" + new File(inputBamName).getName());
		
		Filename input = new Filename(inputBamName, LFN.INPUT);
		input.setRegister(false);
		this.addUses(input);
		
		Filename inputbai = new Filename(inputBaiName, LFN.INPUT);
		inputbai.setRegister(false);
		this.addUses(inputbai);
		
		
		String[] outputFiles = {".snps.raw.vcf",".snps.raw.vcf.varianteval.metric.txt"};
		for(String suffix :outputFiles )
		{
			// construct the vcf and metric.txt for input bam
			String output = new String(inputBamName + suffix);
			Filename outputPeaks = new Filename(output, LFN.OUTPUT);
			outputPeaks.setRegister(true);
			this.addUses(outputPeaks);
		}

		this.addArgument(new PseudoText(" " + inputBamName + " " + genome));		
	}
}