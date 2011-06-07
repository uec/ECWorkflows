package edu.usc.epigenome.workflow.job.ecjob;
import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class PicardJob extends ECJob
{
	public PicardJob(String inputBam,  String tool,String args)
	{
		super(WorkflowConstants.NAMESPACE, "picardtool", WorkflowConstants.VERSION, "picard_" + tool + "_" + inputBam );
		Filename input = new Filename(inputBam, LFN.INPUT);
		input.setRegister(false);
		this.addUses(input);
		
		

		// construct the output wig and peaks for input bam
		String outputMetricFileName = inputBam + "." + tool + ".metric.txt";
		Filename outputMetricFile = new Filename(outputMetricFileName, LFN.OUTPUT);
		outputMetricFile.setRegister(false);
		this.addUses(outputMetricFile);
		
		String cmd = tool + ".jar INPUT=" + inputBam + " OUTPUT=" + outputMetricFileName + " " + args;
		this.addArgument(new PseudoText(cmd));		
	}
}