package edu.usc.epigenome.workflow.job.ecjob;

import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.ECJob;
import edu.usc.epigenome.workflow.DAX.WorkflowConstants;

public class Sol2SangerJob extends ECJob
{

	public Sol2SangerJob(String inputFile)
	{
		super(WorkflowConstants.NAMESPACE, "sol2sanger", WorkflowConstants.VERSION, "sol2sanger_" + inputFile);
		// construct the input filename for job
		Filename input = new Filename(inputFile, LFN.INPUT);
		input.setRegister(false);
		this.addUses(input);

		// construct the output filenames for job
		String sangerOutputFileName = new String(inputFile);
		sangerOutputFileName = sangerOutputFileName.replaceAll("^(.+?)(\\.\\w+)$", "$1\\." + "fastq");
		Filename output = new Filename(sangerOutputFileName, LFN.OUTPUT);
		output.setRegister(false);

		this.addUses(output);

		// add the arguments to the job
		this.addArgument(input);
		this.addArgument(new PseudoText(" "));
		this.addArgument(output);
	}
}
