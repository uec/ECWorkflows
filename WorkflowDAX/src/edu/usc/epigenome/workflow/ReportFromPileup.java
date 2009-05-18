package edu.usc.epigenome.workflow;

import java.io.File;
import edu.usc.epigenome.workflow.DAX.ECDax;
import edu.usc.epigenome.workflow.ECWorkflowParams.ECWorkflowParams;
import edu.usc.epigenome.workflow.job.ecjob.CountPileupJob;
import edu.usc.epigenome.workflow.job.ecjob.ReadCountJob;
import edu.usc.epigenome.workflow.job.ecjob.ReadDepthJob;

public class ReportFromPileup
{
	public static void createWorkFlow(ECDax dax, Boolean pbsMode)	
	{
		try
		{
			// construct a dax object
			// For every requested lane in this flowcell..
			ECWorkflowParams workFlowParams = dax.getWorkFlowParams();
				
			for (int i : workFlowParams.getAvailableLanes())
			{				
				String laneInputFileName;
				if(pbsMode = true)
					laneInputFileName = new File(workFlowParams.getLaneInput(i)).getAbsolutePath();
				else
					laneInputFileName = new File(workFlowParams.getLaneInput(i)).getName();
				
				if(!(laneInputFileName.contentEquals("up.gz")))
				{
					System.err.println("expected pileup.gz file as input for lane " + i +", File=" + laneInputFileName);
					System.exit(1);
				}
				System.out.println("Creating report-only pipeline for lane " + i + ": " + laneInputFileName);

				//create countPileupJob, 
				CountPileupJob countMonoPileupJob = new CountPileupJob(laneInputFileName,CountPileupJob.Mononucleotide);
				dax.addJob(countMonoPileupJob);
				
				//create countPileupJob, 
				CountPileupJob countCGPileupJob = new CountPileupJob(laneInputFileName,CountPileupJob.CGdinucleotide);
				dax.addJob(countCGPileupJob);
				
				//create countPileupJob,
				CountPileupJob countCHPileupJob = new CountPileupJob(laneInputFileName,CountPileupJob.CHdinucleotide);
				dax.addJob(countCHPileupJob);
				
				//create countPileupJob, 
				CountPileupJob countGenomePileupJob = new CountPileupJob(laneInputFileName,CountPileupJob.RefComposition);
				dax.addJob(countGenomePileupJob);
				
				//create readdepth,
				String genome;
				if(workFlowParams.getSetting("Lane." + i + ".ReferenceBFA").contains("phi")) 
					genome = "phiX";
				else if(workFlowParams.getSetting("Lane." + i + ".ReferenceBFA").contains("hg18")) 
					genome = "hg18";
				else 
					genome = "hg18";
				
				
				ReadDepthJob readdepthJob0 = new ReadDepthJob(laneInputFileName, workFlowParams.getSetting("FlowCellName"), i,genome, 5000, 0);
				dax.addJob(readdepthJob0);
				
				ReadDepthJob readdepthJob1 = new ReadDepthJob(laneInputFileName, workFlowParams.getSetting("FlowCellName"), i,genome, 5000, 1);
				dax.addJob(readdepthJob1);
				
				//create readcount,
				ReadCountJob readcountJob = new ReadCountJob(laneInputFileName, workFlowParams.getSetting("FlowCellName"), i, 1000000, 100);
				dax.addJob(readcountJob);			
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}	
	
	public static void usage()
	{
		System.out.println("Usage: program [-dryrun] workflowParameterFile.txt");
		System.out.println("workflowParameterFile.txt: contains all parameters");
		System.out.println("-dryrun: display pbs output, do not run");
		System.exit(0);
	}
	public static void main(String[] args)
	{
		String paramFile = "";
		Boolean dryrun = false;
		Boolean pbsMode = false;
		
		for(String s : args)
		{
			if(s.equals("-dryrun")) 
				dryrun = true;
			else if(s.equals("-pbs")) 
				pbsMode = true;
			else if(new File(s).exists())
				paramFile = s;
			else
				usage();
		}		
				
		ECDax dax = new ECDax(new ECWorkflowParams(paramFile));
		createWorkFlow(dax, pbsMode);
		dax.saveAsDot("reportonly_dax.dot");
		dax.saveAsSimpleDot("reportonly_dax_simple.dot");
		if(pbsMode)
			dax.runWorkflow(dryrun);
		dax.saveAsXML("reportonly_dax.xml");
	}
}
