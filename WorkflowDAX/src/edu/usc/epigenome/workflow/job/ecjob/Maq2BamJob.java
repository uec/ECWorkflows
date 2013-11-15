package edu.usc.epigenome.workflow.job.ecjob;


import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class Maq2BamJob extends ECJob
{
	private String nodupsBam = "";
	public Maq2BamJob(String inputFile, String genome)
	{
		super(WorkflowConstants.NAMESPACE, "maq2bam", WorkflowConstants.VERSION, "maq2bam_" + inputFile);
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
	
		
	public String getNodupsOutput()
	{
		return nodupsBam;
	}
}