package edu.usc.epigenome.workflow.depreciated;

import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class GzipJob extends ECJob
{

	public GzipJob(String inputFile, Boolean mergeInPlace)
	{
		super(WorkflowConstants.NAMESPACE, "gzip", WorkflowConstants.VERSION, "gzip_" + inputFile);
		if(mergeInPlace)
			this.addArgument(new PseudoText("$RESULTS_DIR/" + inputFile));
		else
		{
			Filename input = new Filename(inputFile, LFN.INPUT);
			input.setRegister(false);
			this.addUses(input);
			String outputFileName = new String(inputFile);
			outputFileName = inputFile + ".gz";
			Filename output = new Filename(outputFileName, LFN.OUTPUT);
			output.setRegister(false);
			this.addUses(output);
			this.addArgument(input);
		}
	}
}
