package edu.usc.epigenome.workflow.job.ecjob;

import java.io.File;
import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class MACSJob extends ECJob
{
	public MACSJob(String inputFile)
	{
		super(WorkflowConstants.NAMESPACE, "macs", WorkflowConstants.VERSION, "macs_" + new File(inputFile).getName());
		Filename input = new Filename(inputFile, LFN.INPUT);
		input.setRegister(false);
		this.addUses(input);
		String[] outputFiles = {"_model.r","_peaks.xls","_peaks.bed","_summits.bed"};
		for(String suffix :outputFiles )
		{
			// construct the output wig and peaks for input bam
			String output = new String(inputFile + suffix);
			Filename outputPeaks = new Filename(output, LFN.OUTPUT);
			outputPeaks.setRegister(true);
			this.addUses(outputPeaks);
		}
		//~/software/MACS/default/bin/wrap_macs.pl -n DK4.bam -f BAM -t DK4.bam
		this.addArgument(new PseudoText(" -f BAM -n " + inputFile + " -t " + inputFile));		
	}
}