package edu.usc.epigenome.workflow.job.ecjob;

import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class WigToBigWigJob extends ECJob
{
	public WigToBigWigJob(String inputFileName, String outputFileName)
	{
		super(WorkflowConstants.NAMESPACE, "wigtobigwig", WorkflowConstants.VERSION, "wigtobigwig_" + inputFileName);
		Filename input = new Filename(inputFileName, LFN.INPUT);
		input.setRegister(false);
		this.addUses(input);
		
		Filename outputFile = new Filename(outputFileName , LFN.OUTPUT);
		outputFile.setRegister(true);
		this.addUses(outputFile);
		
		this.addArgument(input);
		this.addArgument(new PseudoText(" "));
		this.addArgument(outputFile);	
	}	
}