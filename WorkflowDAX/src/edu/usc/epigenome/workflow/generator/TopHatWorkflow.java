package edu.usc.epigenome.workflow.generator;

import java.io.File;
import edu.usc.epigenome.workflow.DAX.ECDax;
import edu.usc.epigenome.workflow.ECWorkflowParams.specialized.GAParams;
import edu.usc.epigenome.workflow.job.ecjob.TopHatJob;

public class TopHatWorkflow
{
	/**
	 * Creates an AlignPileUp workflow from an empty dax object 
	 * @param dax The ECDAX to which processing jobs will be added
	 */	
	public static void createWorkFlow(GAParams par,Boolean pbsMode, Boolean dryrun)	
	{
		try
		{
			// construct a dax object
			// For every requested lane in this flowcell..
			ECDax dax = new ECDax(par);
			
			//get the params so that we have the input parameters
			GAParams workFlowParams = (GAParams) dax.getWorkFlowParams();
			
			for (int i : workFlowParams.getAvailableLanes())
			{
				if(workFlowParams.getSetting("Lane." + i + ".AlignmentType").toLowerCase().equals("tophat"))
				{
					boolean isPE = workFlowParams.getLaneInput(i).contains(",");
					
					String laneInputFileNameR1 = null;
					String laneInputFileNameR2 = null;
					
					//split Fastq Job. handle paired end and non pbs
					
					TopHatJob tophat = null;
					if(pbsMode = true)
					{
						if( isPE)
						{
							laneInputFileNameR1 = new File(workFlowParams.getLaneInput(i).split(",")[0]).getAbsolutePath();
							laneInputFileNameR2 = new File(workFlowParams.getLaneInput(i).split(",")[1]).getAbsolutePath();
							tophat = new TopHatJob(laneInputFileNameR1, laneInputFileNameR2, workFlowParams.getSetting("Lane." + i + ".ReferenceBFA"), 150);
							System.out.println("Creating TopHat PE Processing workflow for lane " + i + ": " + laneInputFileNameR1 + " " + laneInputFileNameR2 );
						}
						else
						{
							laneInputFileNameR1 = new File(workFlowParams.getLaneInput(i)).getAbsolutePath();
							tophat = new TopHatJob(laneInputFileNameR1, workFlowParams.getSetting("Lane." + i + ".ReferenceBFA"));
							System.out.println("Creating Tophat SR Processing workflow for lane " + i + ": " + laneInputFileNameR1);
						}
					}
					else					{
						if( isPE)
						{
							laneInputFileNameR1 = new File(workFlowParams.getLaneInput(i).split(",")[0]).getName();
							laneInputFileNameR2 = new File(workFlowParams.getLaneInput(i).split(",")[1]).getName();
							tophat = new TopHatJob(laneInputFileNameR1, laneInputFileNameR2, workFlowParams.getSetting("Lane." + i + ".ReferenceBFA"), 150);
						}
						else
						{
							laneInputFileNameR1 = new File(workFlowParams.getLaneInput(i)).getName();
							tophat = new TopHatJob(laneInputFileNameR1, workFlowParams.getSetting("Lane." + i + ".ReferenceBFA"));
						}
					}
						
					dax.addJob(tophat);
					dax.addChild(tophat.getID());
				}

			}
			if(dax.getChildCount() > 0)
			{
				dax.saveAsDot("tophat_dax.dot");
				dax.saveAsSimpleDot("tophat_dax_simple.dot");
				if(pbsMode)
					dax.runWorkflow(dryrun);
				dax.saveAsXML("tophat_dax.xml");
			}
			
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}	
}