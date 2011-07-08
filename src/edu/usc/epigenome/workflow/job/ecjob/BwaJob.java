package edu.usc.epigenome.workflow.job.ecjob;

import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class BwaJob extends ECJob {
	/**
	 * The single end BWA job constructor
	 * @param inputFile
	 * @param referenceGenomeFile
	 */
	public BwaJob(String inputFile, String referenceGenomeFile)
	{
		super(WorkflowConstants.NAMESPACE, "bwa", WorkflowConstants.VERSION, "bwa_" + inputFile);
		Filename input = new Filename(inputFile, LFN.INPUT);
		input.setRegister(false);
		this.addUses(input);

		// construct the output filenames for job
		String outputFile = new String(inputFile);
		outputFile = outputFile.replaceAll("^(.+?)(\\.\\w+)$", "$1.sam");
		Filename output = new Filename(outputFile, LFN.OUTPUT);
		output.setRegister(false);
		// output.setType(LFN.OUTPUT);
		this.addUses(output);

		this.addArgument(new PseudoText(" "));
		this.addArgument(new PseudoText(referenceGenomeFile));
		this.addArgument(new PseudoText(" "));
		this.addArgument(input);
		this.addArgument(new PseudoText(" "));
		this.addArgument(output);
	}
	
	/**
	 * The paired end BWA job constructor.
	 * @param inputFileR1
	 * @param inputFileR2
	 * @param referenceGenomeFile
	 */
	public BwaJob(String inputFileR1, String inputFileR2, String referenceGenomeFile)
	{
		super(WorkflowConstants.NAMESPACE, "bwape", WorkflowConstants.VERSION, "bwape_" + inputFileR1);
		Filename inputR1 = new Filename(inputFileR1, LFN.INPUT);
		inputR1.setRegister(false);
		this.addUses(inputR1);
		
		Filename inputR2 = new Filename(inputFileR2, LFN.INPUT);
		inputR2.setRegister(false);
		this.addUses(inputR2);

		// construct the output filenames for job
		String outputFile = new String(inputFileR1);
		outputFile = outputFile.replaceAll("^(.+?)(\\.\\w+)$", "$1.sam");
		Filename output = new Filename(outputFile, LFN.OUTPUT);
		output.setRegister(false);
		// output.setType(LFN.OUTPUT);
		this.addUses(output);

		this.addArgument(new PseudoText(" "));
		this.addArgument(new PseudoText(referenceGenomeFile));
		this.addArgument(new PseudoText(" "));
		this.addArgument(inputR1);
		this.addArgument(new PseudoText(" "));
		this.addArgument(inputR2);
		this.addArgument(new PseudoText(" "));
		this.addArgument(output);
	}
}
