package edu.usc.epigenome.workflow.job.ecjob;

import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class PileupJob extends ECJob
{

	/**
	 * creates a gz compressed pileup from map
	 * @param inputFileName map file
	 * @param referenceGenomeFileName ref genome to use
	 * @param minPileUpQ
	 */
	public PileupJob(String inputFileName, String referenceGenomeFileName, int minPileUpQ)
	{
		super(WorkflowConstants.NAMESPACE, "pileup", WorkflowConstants.VERSION, "pileup_" + inputFileName);
		// only one input file
		Filename input = new Filename(inputFileName, LFN.INPUT);
		input.setRegister(false);
		this.addUses(input);

		// only one output file
		// construct the output filenames for job
		String outputFileName = inputFileName;
		outputFileName = outputFileName.replaceAll("^(.+?)(\\.\\w+)$", "$1\\." + "pileup.gz");
		Filename outputFile = new Filename(outputFileName, LFN.OUTPUT);
		outputFile.setRegister(true);
		this.addUses(outputFile);

		// add the arguments to the job
		this.addArgument(new PseudoText(outputFileName.replace(".gz", "")));
		this.addArgument(new PseudoText(" -m 100"));
		this.addArgument(new PseudoText(" -q " + minPileUpQ + " -v"));
		this.addArgument(new PseudoText(" -P " + referenceGenomeFileName + " "));
		this.addArgument(input);
	}
}
