package edu.usc.epigenome.workflow.deprecated;

import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class StageJob extends ECJob
{
	public StageJob(String inputFile, String outputFile)
	{
		super(WorkflowConstants.NAMESPACE, "stage", WorkflowConstants.VERSION, "stage_" + inputFile + "_" + outputFile);
		// construct the input filename for job
		Filename input = new Filename(inputFile, LFN.INPUT);
		input.setRegister(false);
		this.addUses(input);

		// construct the input filename for job
		Filename output = new Filename(outputFile, LFN.OUTPUT);
		input.setRegister(false);
		this.addUses(output);

		// add the arguments to the job
		this.addArgument(input);
		this.addArgument(new PseudoText(" "));
		this.addArgument(output);
	}
}
