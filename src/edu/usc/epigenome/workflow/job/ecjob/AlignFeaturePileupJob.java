package edu.usc.epigenome.workflow.job.ecjob;

import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class AlignFeaturePileupJob extends ECJob
{
	public AlignFeaturePileupJob(String inputFileName, String flowcellName, int laneNumber, String featureName, String featureFileName, int windowSize, int maxIdent, int minDepth, int minQuality, int processMemory)
	{
		super(WorkflowConstants.NAMESPACE, "featurepileup", WorkflowConstants.VERSION, "featurepileup_" + flowcellName + laneNumber + featureName + maxIdent + windowSize);
		Filename input = new Filename(inputFileName, LFN.INPUT);
		input.setRegister(false);
		this.addUses(input);
		
		String outputFileName = flowcellName + "_s_" + laneNumber + ".coverage.m" + maxIdent + ".q" + minQuality + "." + featureName + ".aligned.csv";
		Filename outputFile = new Filename(outputFileName , LFN.OUTPUT);
		outputFile.setRegister(true);
		this.addUses(outputFile);
		
		this.addArgument(new PseudoText(outputFileName));
		this.addArgument(new PseudoText( " java -Xmx" + processMemory + "m edu.usc.epigenome.scripts.PileupToAlignedByFeats -featWindSize " + windowSize + " -maxIdentical " + maxIdent + " -minDepth " + minDepth + " -minDepthEachStrand -minQual " + minQuality + " -featGtf " + featureFileName + " "));
		this.addArgument(input);
	}
}
