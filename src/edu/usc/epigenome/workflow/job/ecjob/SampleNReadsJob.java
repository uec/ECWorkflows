package edu.usc.epigenome.workflow.job.ecjob;

import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class SampleNReadsJob extends ECJob
{

	public SampleNReadsJob(String inputFile, long readstoSample)
	{
		super(WorkflowConstants.NAMESPACE, "samplenreads", WorkflowConstants.VERSION, "samplenreads_" + inputFile + readstoSample);
		// construct the input filename for job
		Filename input = new Filename(inputFile, LFN.INPUT);
		input.setRegister(false);
		this.addUses(input);

		// construct the output filenames for job
		String sangerOutputFileName = new String(inputFile + ".sampled." + readstoSample);
		Filename output = new Filename(sangerOutputFileName, LFN.OUTPUT);
		output.setRegister(false);

		this.addUses(output);

		// add the arguments to the job
		this.addArgument(input);
		this.addArgument(new PseudoText(" " + readstoSample + " "));
		this.addArgument(output);
	}
}
