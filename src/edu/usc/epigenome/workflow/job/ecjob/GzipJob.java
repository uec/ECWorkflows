package edu.usc.epigenome.workflow.job.ecjob;

import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class GzipJob extends ECJob
{

	public GzipJob(String inputFile)
	{
		super(WorkflowConstants.NAMESPACE, "gzip", WorkflowConstants.VERSION, "gzip_" + inputFile);
		this.addArgument(new PseudoText("$RESULTS_DIR/" + inputFile));
	}
}
