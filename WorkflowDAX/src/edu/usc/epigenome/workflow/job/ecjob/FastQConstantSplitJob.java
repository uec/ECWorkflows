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
}
