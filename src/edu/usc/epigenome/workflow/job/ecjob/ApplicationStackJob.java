package edu.usc.epigenome.workflow.job.ecjob;

import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class ApplicationStackJob extends ECJob
{

	public ApplicationStackJob(String inputBam, String outputFile)
	{
		super(WorkflowConstants.NAMESPACE, "applicationstack", WorkflowConstants.VERSION, "applicationstack_" + outputFile);
		
		Filename input = new Filename(inputBam, LFN.INPUT);
		input.setRegister(false);
		this.addUses(input);
		// construct the output wig and peaks for input bam
		String outputMetricFileName = outputFile;
		Filename outputMetricFile = new Filename(outputMetricFileName, LFN.OUTPUT);
		outputMetricFile.setRegister(false);
		this.addUses(outputMetricFile);
		
		
		String cmd = outputFile;
		this.addArgument(new PseudoText(cmd));
	}

}
