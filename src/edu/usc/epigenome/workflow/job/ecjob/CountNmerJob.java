package edu.usc.epigenome.workflow.job.ecjob;

import java.util.List;

import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class CountNmerJob extends ECJob
{
	public CountNmerJob(List<Sol2SangerJob> fastQJobs, String flowcellName, int laneNumber)
	{
		super(WorkflowConstants.NAMESPACE, "countnmer", WorkflowConstants.VERSION, "countnmer_" + flowcellName + laneNumber);
		//  output 3,5,10 nmer files
		String outputFileNameCSV3 = "nmerCount_" + flowcellName + "_s_" + laneNumber + "_" + "3" + "mers.csv";
		Filename outputFileCSV3 = new Filename(outputFileNameCSV3, LFN.OUTPUT);
		outputFileCSV3.setRegister(true);
		this.addUses(outputFileCSV3);
		
		String outputFileNameCSV5 = "nmerCount_" + flowcellName + "_s_" + laneNumber + "_" + "5" + "mers.csv";
		Filename outputFileCSV5 = new Filename(outputFileNameCSV5, LFN.OUTPUT);
		outputFileCSV5.setRegister(true);
		this.addUses(outputFileCSV5);
		
		String outputFileNameCSV10 = "nmerCount_" + flowcellName + "_s_" + laneNumber + "_" + "10" + "mers.csv";
		Filename outputFileCSV10 = new Filename(outputFileNameCSV10, LFN.OUTPUT);
		outputFileCSV10.setRegister(true);
		this.addUses(outputFileCSV10);
		
		// add the arguments to the job
		this.addArgument(new PseudoText(outputFileNameCSV3));
		this.addArgument(new PseudoText(" "));
		
		// iterate through all the map jobs
		for (ECJob j : fastQJobs)
		{
			for (Filename f : j.getOutputFiles())
			{
					Filename input = new Filename(f.getFilename(), LFN.INPUT);
					input.setRegister(false);
					this.addUses(input);
					this.addArgument(input);
					this.addArgument(new PseudoText(" "));				
			}
		}
	}
	
	public CountNmerJob(Filename[] fastQJobs, String flowcellName, int laneNumber)
	{
		super(WorkflowConstants.NAMESPACE, "countnmer", WorkflowConstants.VERSION, "countnmer_" + flowcellName + laneNumber);
		// only one output file
		//  output 3,5,10 nmer files
		String outputFileNameCSV3 = "nmerCount_" + flowcellName + "_s_" + laneNumber + "_" + "3" + "mers.csv";
		Filename outputFileCSV3 = new Filename(outputFileNameCSV3, LFN.OUTPUT);
		outputFileCSV3.setRegister(true);
		this.addUses(outputFileCSV3);
		
		String outputFileNameCSV5 = "nmerCount_" + flowcellName + "_s_" + laneNumber + "_" + "5" + "mers.csv";
		Filename outputFileCSV5 = new Filename(outputFileNameCSV5, LFN.OUTPUT);
		outputFileCSV5.setRegister(true);
		this.addUses(outputFileCSV5);
		
		String outputFileNameCSV10 = "nmerCount_" + flowcellName + "_s_" + laneNumber + "_" + "10" + "mers.csv";
		Filename outputFileCSV10 = new Filename(outputFileNameCSV10, LFN.OUTPUT);
		outputFileCSV10.setRegister(true);
		this.addUses(outputFileCSV10);
		
		// add the arguments to the job
		this.addArgument(new PseudoText(outputFileNameCSV3));
		this.addArgument(new PseudoText(" "));
		
		// iterate through all the map jobs
		for (Filename f : fastQJobs)
		{
				Filename input = new Filename(f.getFilename(), LFN.INPUT);
				input.setRegister(false);
				this.addUses(input);
				this.addArgument(input);
				this.addArgument(new PseudoText(" "));				
		}
	}
	
	public CountNmerJob(String[] fastQJobs, String flowcellName, int laneNumber)
	{
		super(WorkflowConstants.NAMESPACE, "countnmer", WorkflowConstants.VERSION, "countnmer_" + flowcellName + laneNumber);
		//  output 3,5,10 nmer files
		String outputFileNameCSV3 = "nmerCount_" + flowcellName + "_s_" + laneNumber + "_" + "3" + "mers.csv";
		Filename outputFileCSV3 = new Filename(outputFileNameCSV3, LFN.OUTPUT);
		outputFileCSV3.setRegister(true);
		this.addUses(outputFileCSV3);
		
		String outputFileNameCSV5 = "nmerCount_" + flowcellName + "_s_" + laneNumber + "_" + "5" + "mers.csv";
		Filename outputFileCSV5 = new Filename(outputFileNameCSV5, LFN.OUTPUT);
		outputFileCSV5.setRegister(true);
		this.addUses(outputFileCSV5);
		
		String outputFileNameCSV10 = "nmerCount_" + flowcellName + "_s_" + laneNumber + "_" + "10" + "mers.csv";
		Filename outputFileCSV10 = new Filename(outputFileNameCSV10, LFN.OUTPUT);
		outputFileCSV10.setRegister(true);
		this.addUses(outputFileCSV10);
		
		// add the arguments to the job
		this.addArgument(new PseudoText(outputFileNameCSV3));
		this.addArgument(new PseudoText(" "));
		
		// iterate through all the map jobs
		for (String f : fastQJobs)
		{
				Filename input = new Filename(f, LFN.INPUT);
				input.setRegister(false);
				this.addUses(input);
				this.addArgument(input);
				this.addArgument(new PseudoText(" "));				
		}
	}
}
