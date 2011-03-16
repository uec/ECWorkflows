package edu.usc.epigenome.workflow.job.ecjob;

import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class FastQ2BFQJob extends ECJob
{
	public FastQ2BFQJob(String inputFile)
	{
		super(WorkflowConstants.NAMESPACE, "fast2bfq", WorkflowConstants.VERSION, "fast2bfq_" + inputFile);
		// construct the input filename for job
		Filename input = new Filename(inputFile, LFN.INPUT);
		input.setRegister(false);
		this.addUses(input);

		// construct the output filenames for job
		String outputFile = new String(inputFile);
		outputFile = outputFile.replaceAll("^(.+?)(\\.\\w+)$", "$1\\." + "bfq");
		Filename output = new Filename(outputFile, LFN.OUTPUT);
		output.setRegister(false);
		this.addUses(output);

		// add the arguments to the job
		// job.addArgument(new PseudoText("fastq2bfq "));
		this.addArgument(input);
		this.addArgument(new PseudoText(" "));
		this.addArgument(output);
	}
}
