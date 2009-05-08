package edu.usc.epigenome.workflow.job.ecjob;

import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class ReadDepthJob extends ECJob
{
	public ReadDepthJob(String inputFileName, String flowcellName, int laneNumber, String genome, int windowSize, int maxIdent)
	{
		super(WorkflowConstants.NAMESPACE, "readdepth", WorkflowConstants.VERSION, "readdepth_" + flowcellName + laneNumber);
		Filename input = new Filename(inputFileName, LFN.INPUT);
		input.setRegister(false);
		this.addUses(input);
		
		String outputFileName="ReadDepths_maxIden" + maxIdent + "_" + flowcellName + "_" + laneNumber + "_maq_" + windowSize + ".csv";
		Filename outputFile = new Filename(outputFileName , LFN.OUTPUT);
		outputFile.setRegister(true);
		this.addUses(outputFile);
		
		this.addArgument(new PseudoText( " edu.usc.epigenome.scripts.PileupToReadDepthWindows -genomeVers "+ genome + " -strandSpecific -maxIdentical " + maxIdent + " -windSize " + windowSize + " "));
		this.addArgument(input);
		this.addArgument(new PseudoText(" >" + outputFileName));		
	}
}
