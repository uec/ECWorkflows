package edu.usc.epigenome.workflow.job.ecjob;

import java.io.File;

import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class TopHatJob extends ECJob
{
	public TopHatJob(String inputFile, String referenceGenomeFile) throws Exception
	{
		super(WorkflowConstants.NAMESPACE, "tophat", WorkflowConstants.VERSION, "tophat_" + new File(inputFile).getName());
		Filename input = new Filename(inputFile, LFN.INPUT);
		input.setRegister(false);
		this.addUses(input);

		String outFileWig = inputFile + ".tophat_coverage.wig";
		Filename outputWig = new Filename(outFileWig, LFN.OUTPUT);
		outputWig.setRegister(true);
		this.addUses(outputWig);
	
		String outFileSam = inputFile + "tophat_hits.sam";
		Filename outputSam = new Filename(outFileSam, LFN.OUTPUT);
		outputSam.setRegister(true);
		this.addUses(outputSam);
		
		String outFileBed = inputFile + ".tophat_junctions.bed";
		Filename outputBed = new Filename(outFileBed, LFN.OUTPUT);
		outputBed.setRegister(true);
		this.addUses(outputBed);
		
		// add the arguments to the job
		this.addArgument(new PseudoText(referenceGenomeFile + " "));
		this.addArgument(new PseudoText("--solexa1.3-quals "));
		this.addArgument(input);
	}
	public TopHatJob(String inputFileR1, String inputFileR2, String referenceGenomeFile, int mate_inner_dist) throws Exception
	{
		super(WorkflowConstants.NAMESPACE, "tophat", WorkflowConstants.VERSION, "tophat_" + new File(inputFileR1).getName());
		Filename inputR1 = new Filename(inputFileR1, LFN.INPUT);
		inputR1.setRegister(false);
		this.addUses(inputR1);
		
		Filename inputR2 = new Filename(inputFileR2, LFN.INPUT);
		inputR1.setRegister(false);
		this.addUses(inputR2);

		String outFileWig = inputFileR1 + ".tophat_coverage.wig";
		Filename outputWig = new Filename(outFileWig, LFN.OUTPUT);
		outputWig.setRegister(true);
		this.addUses(outputWig);
	
		String outFileSam = inputFileR1 + ".tophat_hits.sam";
		Filename outputSam = new Filename(outFileSam, LFN.OUTPUT);
		outputSam.setRegister(true);
		this.addUses(outputSam);
		
		String outFileBed = inputFileR1 + ".tophat_junctions.bed";
		Filename outputBed = new Filename(outFileBed, LFN.OUTPUT);
		outputBed.setRegister(true);
		this.addUses(outputBed);
		
		// add the arguments to the job
		this.addArgument(new PseudoText("-r " + mate_inner_dist + " "));
		this.addArgument(new PseudoText("--solexa1.3-quals "));
		this.addArgument(new PseudoText(referenceGenomeFile + " "));
		this.addArgument(inputR1);
		this.addArgument(new PseudoText(" "));
		this.addArgument(inputR2);
	}
}