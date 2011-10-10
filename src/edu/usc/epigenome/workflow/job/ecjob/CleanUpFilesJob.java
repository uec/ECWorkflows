package edu.usc.epigenome.workflow.job.ecjob;

import java.io.File;

import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class CleanUpFilesJob extends ECJob
{

	public CleanUpFilesJob(String resultsDir)
	{
		super(WorkflowConstants.NAMESPACE, "cleanup", WorkflowConstants.VERSION, "cleanup_" + new File(resultsDir).getName());
		String outputFileName = new String("none");
		Filename output = new Filename(outputFileName, LFN.OUTPUT);
		output.setRegister(true);
		this.addUses(output);
		this.addArgument(new PseudoText(resultsDir));		
	}
}