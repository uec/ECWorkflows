package edu.usc.epigenome.workflow.depreciated;

import java.util.ArrayList;

import edu.usc.epigenome.workflow.job.ECJob;

public class LinearJobPipeline
{
	ArrayList<ECJob> pipeline;
	ECJob getHead()
	{
		return pipeline.get(0);
	}
	
	ECJob getTail()
	{
		return pipeline.get(pipeline.size() - 1);
	}	
}
