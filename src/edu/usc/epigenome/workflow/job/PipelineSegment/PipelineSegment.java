package edu.usc.epigenome.workflow.job.PipelineSegment;

import edu.usc.epigenome.workflow.DAX.ECDax;
import edu.usc.epigenome.workflow.job.ECJob;

public abstract class PipelineSegment
{
	 protected ECDax dax;
	 public PipelineSegment(ECDax thisDax)
	 {
		 dax = thisDax;
	 }
	 
	 public abstract void addToDax(String sample, ECJob startPoint);
	 public abstract ECJob getEndPoint();	
}
