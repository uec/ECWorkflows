package edu.usc.epigenome.workflow.job.ecjob;

import java.util.ArrayList;

import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class MergeBamsJob extends ECJob
{
	private String nodupsBam = "";

	public MergeBamsJob(ArrayList<String> inputFile, String outputFileName)
	{
		super(WorkflowConstants.NAMESPACE, "mergebams", WorkflowConstants.VERSION, "mergebams_" + outputFileName);
		// construct the input filename for job
		ArrayList<Filename> inputs = new ArrayList<Filename>();
		for(String s : inputFile)
		{
			Filename input = new Filename(s, LFN.INPUT);
			inputs.add(input);
			input.setRegister(false);
			this.addUses(input);
		}
		
		// construct the output filenames for job
		Filename outputBam = new Filename(outputFileName, LFN.OUTPUT);
		outputBam.setRegister(false);
		this.addUses(outputBam);
		
		String outputBaiFile = outputFileName.replaceAll("^(.+?)(\\.\\w+)$", "$1\\." + "bai");
		Filename outputBai = new Filename(outputBaiFile, LFN.OUTPUT);
		outputBai.setRegister(false);
		this.addUses(outputBai);

		// add the arguments to the job
		// job.addArgument(new PseudoText("fastq2bfq "));
		this.addArgument(outputBam);
		this.addArgument(new PseudoText(" "));
		for(Filename f : inputs)
		{
			this.addArgument(f);
			this.addArgument(new PseudoText(" "));
		}
		
	}
}