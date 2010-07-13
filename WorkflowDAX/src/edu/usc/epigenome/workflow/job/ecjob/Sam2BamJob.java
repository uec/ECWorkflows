package edu.usc.epigenome.workflow.job.ecjob;

import java.util.ArrayList;

import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class Sam2BamJob extends ECJob
{
	private String nodupsBam = "";
	public Sam2BamJob(String inputFile, String genome)
	{
		super(WorkflowConstants.NAMESPACE, "sam2bam", WorkflowConstants.VERSION, "sam2bam_" + inputFile);
		// construct the input filename for job
		Filename input = new Filename(inputFile, LFN.INPUT);
		input.setRegister(false);
		this.addUses(input);

		// construct the output filenames for job
		String outputBamFile = new String(inputFile);
		outputBamFile = outputBamFile.replaceAll("^(.+?)(\\.\\w+)$", "$1\\." + "bam");
		Filename outputBam = new Filename(outputBamFile, LFN.OUTPUT);
		outputBam.setRegister(false);
		this.addUses(outputBam);
		
		String outputNodupsBamFile = new String(inputFile);
		outputNodupsBamFile = outputNodupsBamFile.replaceAll("^(.+?)(\\.\\w+)$", "$1\\." + "NODUPS.sorted.calmd.bam");
		Filename outputNodupsBam = new Filename(outputNodupsBamFile, LFN.OUTPUT);
		nodupsBam = outputNodupsBamFile;
		outputNodupsBam.setRegister(false);
		this.addUses(outputNodupsBam);
		
		String outputBaiFile = new String(inputFile);
		outputBaiFile = outputBaiFile.replaceAll("^(.+?)(\\.\\w+)$", "$1\\." + "NODUPS.sorted.calmd.bam.bai");
		Filename outputBai = new Filename(outputBaiFile, LFN.OUTPUT);
		outputBai.setRegister(false);
		this.addUses(outputBai);

		// add the arguments to the job
		// job.addArgument(new PseudoText("fastq2bfq "));
		this.addArgument(input);
		this.addArgument(new PseudoText(" "));
		this.addArgument(new PseudoText(genome));
	}
	public Sam2BamJob(ArrayList<String> inputFile, String genome)
	{
		//TODO handle checking for array of size 1
		
		super(WorkflowConstants.NAMESPACE, "sam2bam", WorkflowConstants.VERSION, "sam2bam_" + inputFile);
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
		String outputBamFile = new String(inputFile.get(0));
		outputBamFile = outputBamFile.replaceAll("^(.+?)(\\.\\w+)$", "$1\\." + "merged.bam");
		Filename outputBam = new Filename(outputBamFile, LFN.OUTPUT);
		outputBam.setRegister(false);
		this.addUses(outputBam);
		
		String outputNodupsBamFile = new String(inputFile.get(0));
		outputNodupsBamFile = outputNodupsBamFile.replaceAll("^(.+?)(\\.\\w+)$", "$1\\." + "merged.NODUPS.sorted.calmd.bam");
		Filename outputNodupsBam = new Filename(outputNodupsBamFile, LFN.OUTPUT);
		nodupsBam = outputNodupsBamFile;
		outputNodupsBam.setRegister(false);
		this.addUses(outputNodupsBam);
		
		String outputBaiFile = new String(inputFile.get(0));
		outputBaiFile = outputBaiFile.replaceAll("^(.+?)(\\.\\w+)$", "$1\\." + "merged.NODUPS.sorted.calmd.bam.bai");
		Filename outputBai = new Filename(outputBaiFile, LFN.OUTPUT);
		outputBai.setRegister(false);
		this.addUses(outputBai);

		// add the arguments to the job
		// job.addArgument(new PseudoText("fastq2bfq "));
		for(Filename f : inputs)
		{
			this.addArgument(f);
			this.addArgument(new PseudoText(" "));
		}
		this.addArgument(new PseudoText(genome));
	}
	
	public String getNodupsOutput()
	{
		return nodupsBam;
	}
}