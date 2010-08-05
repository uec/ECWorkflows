package edu.usc.epigenome.workflow.deprecated;

import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class GenECDaxJob extends ECJob
{
	public GenECDaxJob(String paramFile, String[] seqInputFileNames)
	{
		super(WorkflowConstants.NAMESPACE, "genecdax", WorkflowConstants.VERSION, "genecdax_" + paramFile);
		// construct the input filename for job
		Filename input = new Filename(paramFile, LFN.INPUT);
		input.setRegister(false);
		this.addUses(input);
		
		for (String s: seqInputFileNames)
		{
			this.addUses(new Filename(s, LFN.INPUT));
		}

		// add the arguments to the job
		this.addArgument(new PseudoText(" -cp /auto/uec-00/ramjan/software/ECWorkflow/ECWorkFlow.jar:/auto/uec-00/ramjan/software/ECWorkflow/pegasus.jar edu.usc.epigenome.workflow.AlignPileupWorkflow -dryrun "));
		this.addArgument(input);
	}

}
