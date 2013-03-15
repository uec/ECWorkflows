 package edu.usc.epigenome.workflow.job.ecjob;

import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class MethLevelAveragesJob  extends ECJob
{
	public MethLevelAveragesJob(String inputFileName, String inputFileNameBai, String outputFileName, String genome, String interval)
	{
		super(WorkflowConstants.NAMESPACE, "methlevels", WorkflowConstants.VERSION, "methlevel_" + inputFileName + interval);
		if(interval == null)
			interval = "";
		
		Filename input = new Filename(inputFileName, LFN.INPUT);
		input.setRegister(false);
		this.addUses(input);
		
		Filename inputBai = new Filename(inputFileNameBai, LFN.INPUT);
		input.setRegister(false);
		this.addUses(inputBai);
		
		Filename outputFile = new Filename(outputFileName , LFN.OUTPUT);
		outputFile.setRegister(true);
		this.addUses(outputFile);
		
		this.addArgument(new PseudoText(outputFileName));
		this.addArgument(new PseudoText(" "));
		this.addArgument(new PseudoText(inputFileName));
		this.addArgument(new PseudoText(" "));
		this.addArgument(new PseudoText(genome));
		this.addArgument(new PseudoText(" "));
		this.addArgument(new PseudoText(interval));
	}	
}
