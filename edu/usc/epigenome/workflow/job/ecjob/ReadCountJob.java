package edu.usc.epigenome.workflow.job.ecjob;

import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class ReadCountJob extends ECJob
{
	public ReadCountJob(String inputFileName, String flowcellName, int laneNumber, int randomSubset, int numTrials)
	{
		super(WorkflowConstants.NAMESPACE, "readcount", WorkflowConstants.VERSION, "readcount_" + flowcellName + laneNumber);
		Filename input = new Filename(inputFileName, LFN.INPUT);
		input.setRegister(false);
		this.addUses(input);
		
		String outputFileName="ReadCounts_" + flowcellName + "_" + laneNumber + "_maq.csv";
		Filename outputFile = new Filename(outputFileName , LFN.OUTPUT);
		outputFile.setRegister(true);
		this.addUses(outputFile);
		
		this.addArgument(new PseudoText(outputFileName));
		this.addArgument(new PseudoText( " java edu.usc.epigenome.scripts.PileupToDepthReport -randomSubset " + randomSubset + " -randomSubsetNumTrials " + numTrials +" "));
		this.addArgument(input);
			
	}	
}
