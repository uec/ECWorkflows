package edu.usc.epigenome.workflow.job.ecjob;

import java.util.Calendar;
import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class  MergeBamFastqJob extends ECJob
{
	public MergeBamFastqJob(String inputFastqE1, String inputFastqE2, String inputBam, String refFa, String sampleName, String libName, 
			String flowcell, String lane, String program, String programVer, String programCmd, Boolean isBisulfite, String outputBam)
	{
		super(WorkflowConstants.NAMESPACE, "mergebamfastq", WorkflowConstants.VERSION, "mergebamfastq_" + inputFastqE1);
		// construct the input filename for job
		Filename input = new Filename(inputFastqE1, LFN.INPUT);
		input.setRegister(false);
		this.addUses(input);
		
		Filename input2 = null;
		//handle paired end fastqs
		if(inputFastqE2 != null)
		{
			input2 = new Filename(inputFastqE2, LFN.INPUT);
			input2.setRegister(false);
			this.addUses(input2);
		}
		
		Filename inputBamFile = new Filename(inputBam, LFN.INPUT);
		inputBamFile.setRegister(false);
		this.addUses(inputBamFile);
		
		// construct the output filenames for job
		Filename outputBamFile = new Filename(outputBam, LFN.OUTPUT);
		outputBamFile.setRegister(false);
		this.addUses(outputBamFile);
				
		String outputBaiFile =  outputBam + ".bai";
		Filename outputBai = new Filename(outputBaiFile, LFN.OUTPUT);
		outputBai.setRegister(false);
		this.addUses(outputBai);

		//Build the arguement string
		//  ~/devel/preparebam/merge_bam_fastq.pl --fastq s_1_sequence.txt --bam ResultCount_815C5ABXX_s_1.bam --samplename 'Terry Kelly Illumina-adapter Library' 
		//  --libraryname 'Terry Kelly Illumina-adapter Library' --flowcell 815C5ABXX --lane 1 --rundate 2011-01-27 
		//  --refgenome '/home/uec-00/shared/production/genomes/hg18_unmasked/hg18_unmasked.plusContam.fa' --program maq --programversion '0.7.1' 
		//  --programcmd 'maq map -n 2 -M c -1 75' --ispaired false --isbisulfite true --output 'ResultCount_815C5ABXX_s_1_all.bam'
		String cmd = "--fastq ";
		cmd+= (inputFastqE2 == null ? inputFastqE1 : (inputFastqE1 + ","+ inputFastqE2)) + " ";
		cmd += "--bam " + inputBam + " ";
		cmd += "--samplename '" + sampleName + "' --libraryname '" + libName + "' ";
		cmd += "--flowcell '" + flowcell + "' --lane '" + lane + "' ";
		
		Calendar cal = Calendar.getInstance();
		cmd += "--rundate " + cal.get(Calendar.YEAR) + "-" + cal.get(Calendar.MONTH) + "-" + cal.get(Calendar.DAY_OF_MONTH) + " ";
		
		cmd += "--refgenome " + refFa + " ";
		cmd += "--program '" + program + "' --programversion '" + programVer + "' --programcmd '" + programCmd + "' ";
		
		String ispaired = inputFastqE2 == null ? "false" : "true";
		String isBS = isBisulfite ? "true" : "false";
		
		cmd += "--ispaired " + ispaired + " --isbisulfite" + isBS + " ";
		cmd += "--output '" + outputBam + "'";
		this.addArgument(new PseudoText(cmd));
	}
}