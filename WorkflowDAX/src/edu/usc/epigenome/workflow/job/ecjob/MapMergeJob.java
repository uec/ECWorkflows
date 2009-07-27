package edu.usc.epigenome.workflow.job.ecjob;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class MapMergeJob extends ECJob
{

	public MapMergeJob(List<ECJob> mapJobs, String flowcellName, int laneNumber)
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

		for (ECJob j : mapJobs)
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
	public MapMergeJob(ArrayList<String> mapJobs, String mergedName)
	{
		super(WorkflowConstants.NAMESPACE, "mapMerge", WorkflowConstants.VERSION, "mapMerge_" + mergedName);
		// only one output file
		String outputFileNameMap = mergedName + "_merged.map";
		Filename outputFileMap = new Filename(outputFileNameMap, LFN.OUTPUT);
		outputFileMap.setRegister(true);
		this.addUses(outputFileMap);

		// add the arguments to the job
		this.addArgument(outputFileMap);
		this.addArgument(new PseudoText(" "));
		// iterate through all the map jobs

		for (String f : mapJobs)
		{
				Filename input = new Filename(f, LFN.INPUT);
				input.setRegister(false);
				this.addUses(input);
				this.addArgument(input);
				this.addArgument(new PseudoText(" "));				
		}		
	}
}
