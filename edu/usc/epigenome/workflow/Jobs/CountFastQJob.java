package edu.usc.epigenome.workflow.Jobs;

import java.util.List;

import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.Job;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.ECJob;
import edu.usc.epigenome.workflow.DAX.WorkflowConstants;

public class CountFastQJob extends Job
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
		this.addArgument(new PseudoText(" edu.usc.epigenome.scripts.FastqToBaseComposition -solexa -cycles -quals "));
		
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
		this.addArgument(new PseudoText(">" + outputFileNameCSV));
	}
}
