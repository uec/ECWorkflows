package edu.usc.epigenome.workflow.job.ecjob;

import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class CountInvertedDupsJob extends ECJob
{
	public CountInvertedDupsJob(String inputFileR1, String inputFileR2, String outputFile)
	{
		super(WorkflowConstants.NAMESPACE, "invertdup", WorkflowConstants.VERSION, "invertdup" + inputFileR1);
		
		Filename inputR1 = new Filename(inputFileR1, LFN.INPUT);
		inputR1.setRegister(false);
		this.addUses(inputR1);
		
		Filename inputR2 = new Filename(inputFileR2, LFN.INPUT);
		inputR2.setRegister(false);
		this.addUses(inputR2);

		// construct the output filenames for job
		Filename output = new Filename(outputFile, LFN.OUTPUT);
		output.setRegister(false);
		this.addUses(output);

		this.addArgument(new PseudoText(" "));
		this.addArgument(output);
		this.addArgument(new PseudoText(" "));
		this.addArgument(inputR1);
		this.addArgument(new PseudoText(" "));
		this.addArgument(inputR2);
		
	}
}
