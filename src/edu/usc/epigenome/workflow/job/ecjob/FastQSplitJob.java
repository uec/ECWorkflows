package edu.usc.epigenome.workflow.job.ecjob;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class FastQSplitJob extends ECJob
{
	public FastQSplitJob(String inputFile, int binSize) throws Exception
	{
		super(WorkflowConstants.NAMESPACE, "fastqSplit", WorkflowConstants.VERSION, "fastqSpit_" + inputFile);
		Filename input = new Filename(inputFile, LFN.INPUT);
		input.setRegister(false);
		this.addUses(input);

		// construct the output filenames for job
		FileReader fr;
		long lineCount = 0;
		try
		{
			fr = new FileReader(inputFile);
			LineNumberReader ln = new LineNumberReader(fr);
			while (ln.readLine() != null)
			{
				lineCount++;
			}
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}

		// TODO rounding errors?
		long numOfOutputs;
		if(lineCount % 4 != 0)
			throw new Exception("ERROR! " + inputFile + "lines is not divisible by four!");
		if(((lineCount / 4) % binSize) == 0)
			numOfOutputs = ((lineCount / 4) / binSize);
		else
			numOfOutputs = ((lineCount / 4) / binSize) + 1;
		//System.out.println("Number of outputs for fastqsplit are " + numOfOutputs);
		for (int i = 1; i <= numOfOutputs; i++)
		{
			String outFile = new String(inputFile);
			outFile = outFile.replaceAll("^(.+?)(\\.\\w+)$", "$1\\." + i + "$2");
			Filename output = new Filename(outFile, LFN.OUTPUT);
			output.setRegister(false);
			this.addUses(output);
		}
		// add the arguments to the job
		this.addArgument(new PseudoText(binSize + " "));
		this.addArgument(input);		
	}
}
