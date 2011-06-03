package edu.usc.epigenome.workflow.job.ecjob;
import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class GATKMetricJob extends ECJob
{
	public GATKMetricJob(String inputBam, String inputBai, String genome, String walkerName, String args)
	{
		super(WorkflowConstants.NAMESPACE, "gatkmetrics", WorkflowConstants.VERSION, "gatkmetrics_" + walkerName + "_" + inputBam );
		Filename input = new Filename(inputBam, LFN.INPUT);
		input.setRegister(false);
		this.addUses(input);
		
		Filename inputBaiFile = new Filename(inputBai, LFN.INPUT);
		inputBaiFile.setRegister(false);
		this.addUses(inputBaiFile);

		// construct the output wig and peaks for input bam
		String outputMetricFileName = inputBam + "." + walkerName + ".metric.txt";
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