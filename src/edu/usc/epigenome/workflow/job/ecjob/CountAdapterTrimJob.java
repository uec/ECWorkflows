package edu.usc.epigenome.workflow.job.ecjob;

import java.util.List;
import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class CountAdapterTrimJob extends ECJob
{
	public CountAdapterTrimJob(List<String> adapterTrimCountFileNames, String flowcellName, int laneNumber)
	{
		super(WorkflowConstants.NAMESPACE, "countadaptertrim", WorkflowConstants.VERSION, "countadaptertrim" + flowcellName + laneNumber);
		// only one output file
		String outputFileNameCSV = "ReadCounts_" + flowcellName + "_s_" + laneNumber + "_adapterTrim.csv";
		Filename outputFileCSV = new Filename(outputFileNameCSV, LFN.OUTPUT);
		outputFileCSV.setRegister(true);
		this.addUses(outputFileCSV);

		// add the arguments to the job
		this.addArgument(new PseudoText(outputFileNameCSV));
		this.addArgument(new PseudoText(" "));
		// iterate through all the map jobs
		for (String f : adapterTrimCountFileNames)
		{
				Filename input = new Filename(f, LFN.INPUT);
				input.setRegister(false);
				this.addUses(input);
				this.addArgument(input);
				this.addArgument(new PseudoText(" "));				
		}			
	}
}

