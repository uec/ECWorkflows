package edu.usc.epigenome.workflow.Jobs;

import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.Job;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;

public class PileupJob extends Job
{

	public PileupJob(String inputFileName, String referenceGenomeFileName, int minPileUpQ)
	{
		super(WorkflowConstants.NAMESPACE, "pileup", WorkflowConstants.VERSION, "pileup_" + inputFileName);
		// only one input file
		Filename input = new Filename(inputFileName, LFN.INPUT);
		input.setRegister(false);
		this.addUses(input);

		//Filename referenceGenomeInput = new Filename(referenceGenomeFileName, LFN.INPUT);
		//referenceGenomeInput.setRegister(false);
		//job.addUses(referenceGenomeInput);

		// only one output file
		// construct the output filenames for job
		String outputFileName = inputFileName;
		outputFileName = outputFileName.replaceAll("^(.+?)(\\.\\w+)$", "$1\\." + "pileup.gz");
		Filename outputFile = new Filename(outputFileName, LFN.OUTPUT);
		// output.setType(LFN.OUTPUT);
		outputFile.setRegister(true);
		this.addUses(outputFile);

		// add the arguments to the job
		this.addArgument(outputFile);
		this.addArgument(new PseudoText(" -m 100"));
		this.addArgument(new PseudoText(" -q " + minPileUpQ + " -v"));
		this.addArgument(new PseudoText(" -p " + referenceGenomeFileName + " "));
		this.addArgument(input);
	}
}
