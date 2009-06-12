package edu.usc.epigenome.workflow.job.ecjob;

import java.util.List;

import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class CountNmerJob extends ECJob
{
	public CountNmerJob(List<Sol2SangerJob> fastQJobs, String flowcellName, int laneNumber, int nmerSize)
	{
		super(WorkflowConstants.NAMESPACE, "countnmer", WorkflowConstants.VERSION, "countnmer_" + flowcellName + laneNumber + nmerSize);
		// only one output file
		String outputFileNameCSV = "nmerCount_" + flowcellName + "_s_" + laneNumber + "_" + nmerSize + "mers.csv";
		Filename outputFileCSV = new Filename(outputFileNameCSV, LFN.OUTPUT);
		outputFileCSV.setRegister(true);
		this.addUses(outputFileCSV);

		// add the arguments to the job
		this.addArgument(new PseudoText(outputFileNameCSV));
		this.addArgument(new PseudoText(" cat "));
		
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
		this.addArgument(new PseudoText(" \\| fastqToFasta.pl \\| java -Xmx1995m edu.usc.epigenome.scripts.FastaToNmerCounts -nmer " + nmerSize + " \\| head -n 1000 "));
	}
}
