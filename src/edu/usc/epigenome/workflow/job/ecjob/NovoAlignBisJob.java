package edu.usc.epigenome.workflow.job.ecjob;

import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class NovoAlignBisJob extends ECJob {
	/**
	 * The single end BWA job constructor
	 * @param inputFileNameR1
	 * @param referenceGenomeFile
	 */
	public NovoAlignBisJob(String inputFileNameR1, String inputFileNameR2, String referenceGenomeFile, String outputFile)
	{
		super(WorkflowConstants.NAMESPACE, "novobis", WorkflowConstants.VERSION, "novobis_" + inputFileNameR1);
		
		Filename inputFileR1 = new Filename(inputFileNameR1, LFN.INPUT);
		inputFileR1.setRegister(false);
		this.addUses(inputFileR1);
		
		//if PE
		Filename inputFileR2 = null;
		if(inputFileNameR2 != null)
		{
			inputFileR2 = new Filename(inputFileNameR2, LFN.INPUT);
			inputFileR2.setRegister(false);
			this.addUses(inputFileR2);

		}
		Filename output = new Filename(outputFile, LFN.OUTPUT);
		output.setRegister(false);
		// output.setType(LFN.OUTPUT);
		this.addUses(output);

		
		this.addArgument(new PseudoText(referenceGenomeFile));
		this.addArgument(new PseudoText(" "));
		
		this.addArgument(inputFileR1);
		this.addArgument(new PseudoText(" "));
		
		if(inputFileNameR2 != null)
			this.addArgument(inputFileR2);
		
		this.addArgument(output);
		this.addArgument(new PseudoText(" "));
	}
	
	
	
}