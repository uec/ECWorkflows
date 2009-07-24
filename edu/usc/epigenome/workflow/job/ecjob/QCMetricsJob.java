package edu.usc.epigenome.workflow.job.ecjob;

import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class QCMetricsJob extends ECJob
{

	public QCMetricsJob(String resultsDir, String flowcell)
	{
		super(WorkflowConstants.NAMESPACE, "qcmetrics", WorkflowConstants.VERSION, "qcmetrics_" + flowcell);
		String outputFileName = new String(flowcell + "_qcmetrics.csv");
		Filename output = new Filename(outputFileName, LFN.OUTPUT);
		output.setRegister(false);
		this.addUses(output);
		this.addArgument(output);
		this.addArgument(new PseudoText(" /home/uec-00/shared/production/software/perl_utils_usc/bisulfiteQCMetrics.pl "));
		this.addArgument(new PseudoText(resultsDir));		
	}
}