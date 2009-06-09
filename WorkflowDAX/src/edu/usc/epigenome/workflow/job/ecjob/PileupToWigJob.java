package edu.usc.epigenome.workflow.job.ecjob;

import java.io.File;

import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class PileupToWigJob extends ECJob
{
	public PileupToWigJob(String inputFileName, String flowcellName, int laneNumber, int windowSize, int stepSize, int maxIdent, int minQuality, int type)
	{
		super(WorkflowConstants.NAMESPACE, "pileupwig", WorkflowConstants.VERSION, "pileupwig_" + flowcellName + laneNumber + maxIdent + windowSize);
		Filename input = new Filename(inputFileName, LFN.INPUT);
		input.setRegister(false);
		this.addUses(input);
		
		String outputFileName = flowcellName + "_s_" + laneNumber + ".coverage.m" + maxIdent + ".q" + minQuality + ".wig";
		Filename outputFile = new Filename(outputFileName , LFN.OUTPUT);
		outputFile.setRegister(true);
		this.addUses(outputFile);
		
		this.addArgument(new PseudoText( "pileuptowig.log java -Xmx1995m edu.usc.epigenome.scripts.PileupToWig -stepSize " + stepSize + " -windSize " + windowSize + " -name " + new File(inputFileName).getName().replace(".pileup", "").replace(".gz", "") + " -maxIdentical " + maxIdent + " -desc java_pileupwig_" + stepSize + "_" + windowSize + " -type " + type + " -output " + outputFileName + " "));
		this.addArgument(input);
	}
}