package edu.usc.epigenome.workflow.job.ecjob;
import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;


public class CoverageExtrapJob extends ECJob
{
	public CoverageExtrapJob(String inputBamName, String outputFileName)
	{
		super(WorkflowConstants.NAMESPACE, "lcextrap", WorkflowConstants.VERSION, "lcextrap" + inputBamName);
		
		Filename inputBam = new Filename(inputBamName, LFN.INPUT);
		inputBam.setRegister(false);
		this.addUses(inputBam);
			Filename output = new Filename(outputFileName, LFN.OUTPUT);
		output.setRegister(false);
		this.addUses(output);
		
		this.addArgument(new PseudoText(" "));
		this.addArgument(inputBam);
		this.addArgument(new PseudoText(" "));
		this.addArgument(output);
	}
}
