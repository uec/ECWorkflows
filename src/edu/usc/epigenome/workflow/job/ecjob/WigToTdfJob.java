package edu.usc.epigenome.workflow.job.ecjob;
import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class WigToTdfJob extends ECJob
{

	public WigToTdfJob(String inputFile, String genome)
	{
		super(WorkflowConstants.NAMESPACE, "wigtotdf", WorkflowConstants.VERSION, "wigtotdf_" + inputFile);
		// construct the input filename for job
		Filename input = new Filename(inputFile, LFN.INPUT);
		input.setRegister(false);
		this.addUses(input);

		// construct the output filenames for job
		String tdfOutputName = new String(inputFile);
		tdfOutputName = tdfOutputName.replaceAll("wig.*.*.*$", "tdf");
		Filename output = new Filename(tdfOutputName, LFN.OUTPUT);
		output.setRegister(false);

		this.addUses(output);

		// add the arguments to the job
		this.addArgument(input);
		this.addArgument(new PseudoText(" "));
		this.addArgument(output);
		this.addArgument(new PseudoText(" "));
		this.addArgument(new PseudoText(genome));
	}
}