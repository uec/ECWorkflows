package edu.usc.epigenome.workflow.job.ecjob;

import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class ApplicationStackJob extends ECJob
{

	public ApplicationStackJob(String outputFileName)
	{
		super(WorkflowConstants.NAMESPACE, "applicationstack", WorkflowConstants.VERSION, "applicationstack_" + outputFileName);
		Filename outputMetricFile = new Filename(outputFileName, LFN.OUTPUT);
		outputMetricFile.setRegister(false);
		this.addUses(outputMetricFile);
	}

}
