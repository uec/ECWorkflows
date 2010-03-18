package edu.usc.epigenome.workflow.job.ecjob;

import java.io.File;

import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class FastQConstantSplitJob extends ECJob
{
	public FastQConstantSplitJob(String inputFile, int numberOfPieces) throws Exception
	{
		super(WorkflowConstants.NAMESPACE, "fastqConstantSplit", WorkflowConstants.VERSION, "fastqConstantSpit_" + new File(inputFile).getName());
		Filename input = new Filename(inputFile, LFN.INPUT);
		input.setRegister(false);
		this.addUses(input);

		// construct the output filenames for job
		for (int i = 1; i <= numberOfPieces; i++)
		{
			File inFile = new File(inputFile);
			String outFile = new String(inFile.getName());
			outFile = outFile.replaceAll("^(.+?)(\\.\\w+)$", "$1\\." + i + "$2");
			Filename output = new Filename(outFile, LFN.OUTPUT);
			output.setRegister(false);
			this.addUses(output);
		}
		// add the arguments to the job
		this.addArgument(new PseudoText(numberOfPieces + " "));
		this.addArgument(input);
	}
	public FastQConstantSplitJob(String inputFileR1, String inputFileR2, int numberOfPieces) throws Exception
	{
		super(WorkflowConstants.NAMESPACE, "fastqConstantSplit", WorkflowConstants.VERSION, "fastqConstantSpit_" + new File(inputFileR1).getName());
		Filename inputR1 = new Filename(inputFileR1, LFN.INPUT);
		inputR1.setRegister(false);
		this.addUses(inputR1);
		
		Filename inputR2 = new Filename(inputFileR2, LFN.INPUT);
		inputR2.setRegister(false);
		this.addUses(inputR2);

		// construct the output filenames for job
		for (int i = 1; i <= numberOfPieces; i++)
		{
			File inFileR1 = new File(inputFileR1);
			File inFileR2 = new File(inputFileR2);
			
			String outFileR1 = new String(inFileR1.getName());
			outFileR1 = outFileR1.replaceAll("^(.+?)(\\.\\w+)$", "$1\\." + i + "$2");
			Filename outputR1 = new Filename(outFileR1, LFN.OUTPUT);
			outputR1.setRegister(false);
			this.addUses(outputR1);
			
			String outFileR2 = new String(inFileR2.getName());
			outFileR2 = outFileR2.replaceAll("^(.+?)(\\.\\w+)$", "$1\\." + i + "$2");
			Filename outputR2 = new Filename(outFileR2, LFN.OUTPUT);
			outputR2.setRegister(false);
			this.addUses(outputR2);
		}
		// add the arguments to the job
		this.addArgument(new PseudoText(numberOfPieces + " "));
		this.addArgument(inputR1);
		this.addArgument(new PseudoText(" "));
		this.addArgument(inputR2);
	}
}
