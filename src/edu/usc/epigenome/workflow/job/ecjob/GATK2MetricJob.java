package edu.usc.epigenome.workflow.job.ecjob;
import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class GATK2MetricJob extends ECJob
{
	public GATK2MetricJob(String inputBam, String inputBai, String genome, String walkerName, String args)
	{
		super(WorkflowConstants.NAMESPACE, "gatk2metrics", WorkflowConstants.VERSION, "gatk2metrics_" + args.replace(" ", "").replace("-","") + "_" + walkerName + "_" + inputBam );
		Filename input = new Filename(inputBam, LFN.INPUT);
		input.setRegister(false);
		this.addUses(input);
		
		Filename inputBaiFile = new Filename(inputBai, LFN.INPUT);
		inputBaiFile.setRegister(false);
		this.addUses(inputBaiFile);

		String argsPretty = args.replace(" ", "").replace("-","");
		
		// construct the output wig and peaks for input bam
		String outputMetricFileName = inputBam + "." + argsPretty + "." + walkerName + ".metric.txt";
		Filename outputMetricFile = new Filename(outputMetricFileName, LFN.OUTPUT);
		outputMetricFile.setRegister(false);
		this.addUses(outputMetricFile);
		
		String cmd = " -T " + walkerName ;//+ -cph -R ~/genomes/hg18_unmasked/hg18_unmasked.plusContam.fa -I ResultCount_B03BUABXX_6_KEL656A15.mdups.bam -o testout.txt
		cmd += " -R " + genome;
		cmd += " -I " + inputBam;
		cmd += " -o " + outputMetricFileName;
		cmd += " " + args;
		
		this.addArgument(new PseudoText(cmd));		
	}
	
	public GATK2MetricJob(String inputBam, String inputBai, String genome, String walkerName, String args, String outputMetricFileName)
	{
		super(WorkflowConstants.NAMESPACE, "gatk2metrics", WorkflowConstants.VERSION, "gatk2metrics_" + args.replace(" ", "").replace("-","") + "_" + walkerName + "_" + inputBam );
		Filename input = new Filename(inputBam, LFN.INPUT);
		input.setRegister(false);
		this.addUses(input);
		
		Filename inputBaiFile = new Filename(inputBai, LFN.INPUT);
		inputBaiFile.setRegister(false);
		this.addUses(inputBaiFile);

		// construct the output wig and peaks for input bam
		Filename outputMetricFile = new Filename(outputMetricFileName, LFN.OUTPUT);
		outputMetricFile.setRegister(false);
		this.addUses(outputMetricFile);
		
		String cmd = " -T " + walkerName ;//+ -cph -R ~/genomes/hg18_unmasked/hg18_unmasked.plusContam.fa -I ResultCount_B03BUABXX_6_KEL656A15.mdups.bam -o testout.txt
		cmd += " -R " + genome;
		cmd += " -I " + inputBam;
		cmd += " -o " + outputMetricFileName;
		cmd += " " + args;
		
		this.addArgument(new PseudoText(cmd));		
	}
}