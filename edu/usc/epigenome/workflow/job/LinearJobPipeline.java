package edu.usc.epigenome.workflow.job;

import java.util.ArrayList;

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
