package edu.usc.epigenome.workflow.job.ecjob;

import java.util.ArrayList;

import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class MergeBamsJob extends ECJob
{
	private String mdupsBam = "";
	private String mdupsBai = "";
	private String bam = "";
	private String bai = "";

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
		bam = outputFileName;
		outputBam.setRegister(false);
		this.addUses(outputBam);
		
		String outputBaiFile = outputFileName.replaceAll("^(.+?)(\\.\\w+)$", "$1\\." + "bam.bai");
		bai = outputBaiFile;
		Filename outputBai = new Filename(outputBaiFile, LFN.OUTPUT);
		outputBai.setRegister(false);
		this.addUses(outputBai);
		
		String outputBamStatsFile = outputFileName.replaceAll("^(.+?)(\\.\\w+)$", "$1\\." + "bam.flagstat.metric.txt");
		Filename outputBamStats = new Filename(outputBamStatsFile, LFN.OUTPUT);
		outputBamStats.setRegister(false);
		this.addUses(outputBamStats);
		
		
		String outputBamMdupsFile = outputFileName.replaceAll("^(.+?)(\\.\\w+)$", "$1\\." + "mdups.bam");
		mdupsBam = outputBamMdupsFile;
		Filename outputMdupBam = new Filename(outputBamMdupsFile, LFN.OUTPUT);
		outputMdupBam.setRegister(false);
		this.addUses(outputMdupBam);

		String outputBamBaiMdupsFile = outputFileName.replaceAll("^(.+?)(\\.\\w+)$", "$1\\." + "mdups.bam.bai");
		mdupsBai = outputBamBaiMdupsFile;
		Filename outputBaiMdupBam = new Filename(outputBamBaiMdupsFile, LFN.OUTPUT);
		outputBaiMdupBam.setRegister(false);
		this.addUses(outputBaiMdupBam);
		
		String outputDupBamStatsFile = outputFileName.replaceAll("^(.+?)(\\.\\w+)$", "$1\\." + "mdups.bam.flagstat.metric.txt");
		Filename outputDupBamStats = new Filename(outputDupBamStatsFile, LFN.OUTPUT);
		outputDupBamStats.setRegister(false);
		this.addUses(outputDupBamStats);
		

		
		// add the arguments to the job
		this.addArgument(outputBam);
		this.addArgument(new PseudoText(" "));
		for(Filename f : inputs)
		{
			this.addArgument(f);
			this.addArgument(new PseudoText(" "));
		}		
	}
	public String getMdupsBam()
	{
		return mdupsBam;
	}

	public String getMdupsBai()
	{
		return mdupsBai;
	}

	public String getBam()
	{
		return bam;
	}

	public String getBai()
	{
		return bai;
	}
}