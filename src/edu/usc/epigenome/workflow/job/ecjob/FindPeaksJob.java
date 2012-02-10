package edu.usc.epigenome.workflow.job.ecjob;

import java.io.File;

import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class FindPeaksJob extends ECJob
{
	private String wigFile;
	public FindPeaksJob(String inputFile, int fragSize)
	{
		super(WorkflowConstants.NAMESPACE, "findpeaks", WorkflowConstants.VERSION, "findpeaks_" + new File(inputFile).getName());
		Filename input = new Filename(inputFile, LFN.INPUT);
		input.setRegister(false);
		this.addUses(input);

		// construct the output wig and peaks for input bam
		String outputPeaksFile = new String(inputFile);
		outputPeaksFile = outputPeaksFile.replaceAll("^(.+?)(\\.\\w+)$", "$1" +  "_triangle_standard.peaks");
		Filename outputPeaks = new Filename(outputPeaksFile, LFN.OUTPUT);
		outputPeaks.setRegister(false);
		this.addUses(outputPeaks);
		
		String outputWigFile = new String(inputFile);
		outputWigFile = outputWigFile.replaceAll("^(.+?)(\\.\\w+)$", "$1" + "_triangle_standard.wig.gz");
		wigFile = outputWigFile;
		Filename outputWig = new Filename(outputWigFile, LFN.OUTPUT);
		outputWig.setRegister(false);
		this.addUses(outputWig);
		
		
		
		// construct the output RAW wig and peaks for input bam
		String outputPeaksFileRAW = new String(inputFile);
		outputPeaksFileRAW = outputPeaksFileRAW.replaceAll("^(.+?)(\\.\\w+)$", "$1" +  "_raw_triangle_standard.peaks");
		Filename outputPeaksRAW = new Filename(outputPeaksFileRAW, LFN.OUTPUT);
		outputPeaksRAW.setRegister(false);
		this.addUses(outputPeaksRAW);
		
		String outputWigFileRAW = new String(inputFile);
		outputWigFileRAW = outputWigFileRAW.replaceAll("^(.+?)(\\.\\w+)$", "$1" + "_raw_triangle_standard.wig.gz");
		Filename outputWigRAW = new Filename(outputWigFileRAW, LFN.OUTPUT);
		outputWigRAW.setRegister(false);
		this.addUses(outputWigRAW);
		
		this.addArgument(input);
		this.addArgument(new PseudoText(" " + fragSize));		
	}
	
	public String getWigFile()
	{
		return wigFile;
	}
}
