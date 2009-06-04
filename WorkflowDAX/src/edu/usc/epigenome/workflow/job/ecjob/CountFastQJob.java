package edu.usc.epigenome.workflow.job.ecjob;

import java.util.List;

import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class CountFastQJob extends ECJob
{
	public CountFastQJob(List<Sol2SangerJob> fastQJobs, String flowcellName, int laneNumber)
	{
		super(WorkflowConstants.NAMESPACE, "countfastq", WorkflowConstants.VERSION, "countfastq_" + flowcellName + laneNumber);
		// only one output file
		String outputFileNameCSV = "ResultCount_" + flowcellName + "_s_" + laneNumber + "_Gerald_mononucleotide.csv";
		Filename outputFileCSV = new Filename(outputFileNameCSV, LFN.OUTPUT);
		outputFileCSV.setRegister(true);
		this.addUses(outputFileCSV);

		// add the arguments to the job
		this.addArgument(new PseudoText(outputFileNameCSV));
		this.addArgument(new PseudoText(" java edu.usc.epigenome.scripts.FastqToBaseComposition -solexa -cycles -quals "));
		
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
}
