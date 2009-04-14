package edu.usc.epigenome.workflow.job.ecjob;

import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class RemoteExecJob extends ECJob
{
	public RemoteExecJob(String cmd)
	{
		super(WorkflowConstants.NAMESPACE, "stage", WorkflowConstants.VERSION, "rexec_" + cmd.replace(" ", "_"));
		// add the arguments to the job
		this.addArgument(new PseudoText(cmd));		
	}
}
