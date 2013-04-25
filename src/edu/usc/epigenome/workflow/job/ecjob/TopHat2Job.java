package edu.usc.epigenome.workflow.job.ecjob;

import java.io.File;

import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class TopHat2Job extends ECJob
{
	public String bamFile = "";
	public String getBamFile()
	{
		return bamFile;
	}
	public TopHat2Job(String inputFile, String referenceGenomeFile) throws Exception
	{
		super(WorkflowConstants.NAMESPACE, "tophat2", WorkflowConstants.VERSION, "tophat2_" + new File(inputFile).getName());
		Filename input = new Filename(inputFile, LFN.INPUT);
		input.setRegister(false);
		this.addUses(input);

		String outFileSam = inputFile + ".tophat_hits.bam";
		bamFile = outFileSam;
		Filename outputSam = new Filename(outFileSam, LFN.OUTPUT);
		outputSam.setRegister(true);
		this.addUses(outputSam);
		
		String outFileBai = inputFile + ".tophat_hits.bam.bai";
		Filename outputBai = new Filename(outFileBai, LFN.OUTPUT);
		outputBai.setRegister(true);
		this.addUses(outputBai);
		
		
		String outFileBed = inputFile + ".tophat_junctions.bed";
		Filename outputBed = new Filename(outFileBed, LFN.OUTPUT);
		outputBed.setRegister(true);
		this.addUses(outputBed);
				
		String outFileInsBed = inputFile + ".tophat_insertions.bed";
		Filename outputInsBed = new Filename(outFileInsBed, LFN.OUTPUT);
		outputInsBed.setRegister(true);
		this.addUses(outputInsBed);
		
		String outFileDelBed = inputFile + ".tophat_deletions.bed";
		Filename outputDelBed = new Filename(outFileDelBed, LFN.OUTPUT);
		outputDelBed.setRegister(true);
		this.addUses(outputDelBed);
		
		String outFileUnaln = inputFile + ".unmapped.bam";
		Filename unaln = new Filename(outFileUnaln, LFN.OUTPUT);
		unaln.setRegister(true);
		this.addUses(unaln);
		
		// add the arguments to the job
		this.addArgument(new PseudoText("-p 11 " + referenceGenomeFile + " "));
		this.addArgument(input);
	}
	public TopHat2Job(String inputFileR1, String inputFileR2, String referenceGenomeFile, int mate_inner_dist) throws Exception
	{
		super(WorkflowConstants.NAMESPACE, "tophat2", WorkflowConstants.VERSION, "tophat2_" + new File(inputFileR1).getName());
		Filename inputR1 = new Filename(inputFileR1, LFN.INPUT);
		inputR1.setRegister(false);
		this.addUses(inputR1);
		
		Filename inputR2 = new Filename(inputFileR2, LFN.INPUT);
		inputR1.setRegister(false);
		this.addUses(inputR2);

		String outFileSam = inputFileR1 + ".tophat_hits.bam";
		bamFile = outFileSam;
		Filename outputSam = new Filename(outFileSam, LFN.OUTPUT);
		outputSam.setRegister(true);
		this.addUses(outputSam);
		
		String outFileBai = inputFileR1 + ".tophat_hits.bam.bai";
		Filename outputBai = new Filename(outFileBai, LFN.OUTPUT);
		outputBai.setRegister(true);
		this.addUses(outputBai);
		
		String outFileBed = inputFileR1 + ".tophat_junctions.bed";
		Filename outputBed = new Filename(outFileBed, LFN.OUTPUT);
		outputBed.setRegister(true);
		this.addUses(outputBed);
		
		String outFileInsBed = inputFileR1 + ".tophat_insertions.bed";
		Filename outputInsBed = new Filename(outFileInsBed, LFN.OUTPUT);
		outputInsBed.setRegister(true);
		this.addUses(outputInsBed);
		
		String outFileDelBed = inputFileR1 + ".tophat_deletions.bed";
		Filename outputDelBed = new Filename(outFileDelBed, LFN.OUTPUT);
		outputDelBed.setRegister(true);
		this.addUses(outputDelBed);
		
		// add the arguments to the job
		this.addArgument(new PseudoText("-p 11 "));
		this.addArgument(new PseudoText("-r " + mate_inner_dist + " "));
		this.addArgument(new PseudoText(referenceGenomeFile + " "));
		this.addArgument(inputR1);
		this.addArgument(new PseudoText(" "));
		this.addArgument(inputR2);
	}
}