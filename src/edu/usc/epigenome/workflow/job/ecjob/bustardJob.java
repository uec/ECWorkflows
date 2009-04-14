package edu.usc.epigenome.workflow.job.ecjob;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class bustardJob extends ECJob
{

	public bustardJob(String flowcellName)
	{
		super(WorkflowConstants.NAMESPACE, "bustard", WorkflowConstants.VERSION, "bustard _" + flowcellName);
		// TODO Auto-generated constructor stub
	}

}
