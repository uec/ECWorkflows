package edu.usc.epigenome.workflow.Jobs;

import java.util.Iterator;
import java.util.List;

import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.Job;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;

public class MapMergeJob extends Job
{

	public MapMergeJob(List<MapJob> mapJobs, String flowcellName, int laneNumber)
	{
		super(WorkflowConstants.NAMESPACE, "mapMerge", WorkflowConstants.VERSION, "mapMerge_" + flowcellName + laneNumber);
		// only one output file
		String outputFileNameMap = "ResultCount_" + flowcellName + "_s_" + laneNumber + ".map";
		Filename outputFileMap = new Filename(outputFileNameMap, LFN.OUTPUT);
		outputFileMap.setRegister(true);
		this.addUses(outputFileMap);

		// add the arguments to the job
		this.addArgument(outputFileMap);
		this.addArgument(new PseudoText(" "));
		// iterate through all the map jobs

		for (Job j : mapJobs)
		{
			for (Iterator it = j.listIterateUses(); it.hasNext();)
			{
				Filename f = (Filename) it.next();
				if (f.getLink() == LFN.OUTPUT)
				{
					Filename input = new Filename(f.getFilename(), LFN.INPUT);
					input.setRegister(false);
					this.addUses(input);
				}
			}
		}
	}	
}
