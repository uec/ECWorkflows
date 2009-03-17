package edu.usc.epigenome.workflow;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.Job;
import edu.usc.epigenome.workflow.DAX.ECDax;
import edu.usc.epigenome.workflow.DAX.WorkFlowArgs;
import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.Jobs.CountPileupJob;
import edu.usc.epigenome.workflow.Jobs.FastQ2BFQJob;
import edu.usc.epigenome.workflow.Jobs.FastQSplitJob;
import edu.usc.epigenome.workflow.Jobs.FilterContamsJob;
import edu.usc.epigenome.workflow.Jobs.MapJob;
import edu.usc.epigenome.workflow.Jobs.MapMergeJob;
import edu.usc.epigenome.workflow.Jobs.MapViewJob;
import edu.usc.epigenome.workflow.Jobs.PileupJob;
import edu.usc.epigenome.workflow.Jobs.Sol2SangerJob;

public class MainWorkflow
{
	public static void createWorkFlow(ECDax dax)	
	{
		try
		{
			// construct a dax object
			// For every requested lane in this flowcell..
			WorkFlowArgs workFlowParams = dax.getWorkFlowParams();
			
			List<Job> mapMergeJobs = new LinkedList<Job>();
			for (int i : workFlowParams.getAvailableLanes())
			{
				List<MapJob> mapJobs = new LinkedList<MapJob>();
				String laneInputFile = workFlowParams.getSetting(i);
				System.out.println("Creating processing pipeline for lane " + i + ": " + laneInputFile);

				// create a fastSplit job
				int splitSize = 0;
				if (workFlowParams.laneIsBisulfite(i))
					splitSize = Integer.parseInt(workFlowParams.getSetting("BisulfiteSplitFactor"));
				else
					splitSize = Integer.parseInt(workFlowParams.getSetting("RegularSplitFactor"));
				FastQSplitJob fastqSplitJob = new FastQSplitJob(laneInputFile, splitSize);
				dax.addJob(fastqSplitJob);

				// iterate through the output files of fastQsplit jobs to create pipeline
				for (Filename f : fastqSplitJob.getOutputFiles())
				{
					//filter contam job
					String splitFastqOutputFile = f.getFilename();
					FilterContamsJob filterContamJob = new FilterContamsJob(splitFastqOutputFile);
					dax.addJob(filterContamJob);
					dax.addChild(filterContamJob.getID(), fastqSplitJob.getID());

					// sol2sanger job
					Sol2SangerJob sol2sangerJob = new Sol2SangerJob(filterContamJob.getSingleOutputFile().getFilename());
					dax.addJob(sol2sangerJob);
					dax.addChild(sol2sangerJob.getID(), filterContamJob.getID());

					// fastq2bfq job
					FastQ2BFQJob fastq2bfqJob = new FastQ2BFQJob(sol2sangerJob.getSingleOutputFile().getFilename());
					dax.addJob(fastq2bfqJob);
					dax.addChild(fastq2bfqJob.getID(), sol2sangerJob.getID());

					// map job. additional input grabbed from hg18.BS.bfa
					MapJob mapJob = new MapJob(fastq2bfqJob.getSingleOutputFile().getFilename(), workFlowParams.getSetting("ReferenceBFA"), Integer
							.parseInt(workFlowParams.getSetting("MinMismatches")), Integer.parseInt(workFlowParams.getSetting("FirstReadLength")),
							workFlowParams.laneIsBisulfite(i));
					dax.addJob(mapJob);
					dax.addChild(mapJob.getID(), fastq2bfqJob.getID());
					mapJobs.add(mapJob);

				}
				
				// for each lane create a map merge job
				MapMergeJob mapMergeJob = new MapMergeJob(mapJobs, workFlowParams.getSetting("FlowCellName"), i);
				dax.addJob(mapMergeJob);
				// mapmerge is child to all the map jobs
				for (Job map : mapJobs)
				{
					dax.addChild(mapMergeJob.getID(), map.getID());
				}
				mapMergeJobs.add(mapMergeJob);

				// crate mapview job, child of mapmerge
				MapViewJob mapViewJob =  new MapViewJob(mapMergeJob.getSingleOutputFile().getFilename(), Integer.parseInt(workFlowParams.getSetting("MaqPileupQ")));;
				dax.addJob(mapViewJob);
				dax.addChild(mapViewJob.getID(), mapMergeJob.getID());

				//create pileup job, child of mapview
				PileupJob pileupJob = new PileupJob(mapViewJob.getSingleOutputFile().getFilename(), workFlowParams.getSetting("ReferenceBFA"), Integer.parseInt(workFlowParams
						.getSetting("MaqPileupQ")));;
				dax.addJob(pileupJob);
				dax.addChild(pileupJob.getID(), mapViewJob.getID());
				
				//create countPileupJob, child of pileupJob
				CountPileupJob countMonoPileupJob = new CountPileupJob(pileupJob.getSingleOutputFile().getFilename(),WorkflowConstants.Mononucleotide);
				dax.addJob(countMonoPileupJob);
				dax.addChild(countMonoPileupJob.getID(), pileupJob.getID());
				
				//create countPileupJob, child of pileupJob
				CountPileupJob countCGPileupJob = new CountPileupJob(pileupJob.getSingleOutputFile().getFilename(),WorkflowConstants.CGdinucleotide);
				dax.addJob(countCGPileupJob);
				dax.addChild(countCGPileupJob.getID(), pileupJob.getID());
				
				//create countPileupJob, child of pileupJob
				CountPileupJob countCHPileupJob = new CountPileupJob(pileupJob.getSingleOutputFile().getFilename(),WorkflowConstants.CHdinucleotide);
				dax.addJob(countCHPileupJob);
				dax.addChild(countCHPileupJob.getID(), pileupJob.getID());				
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
		//create a dax from the passed in param obj
		if(args.length == 1)
		{
			if((new File(args[0])).exists())
				paramFile = args[0];
			else
				usage();
		}
		else if(args.length==2)
		{
			if(args[0].equals("-dryrun") && (new File(args[1])).exists())
			{
				paramFile = args[1];
				dryrun = true;
			}
			else
				usage();
		}
		else
			usage();
				
		ECDax dax = new ECDax(new WorkFlowArgs(paramFile));
		createWorkFlow(dax);
		dax.saveAsDot("epi_dax.dot");
		dax.runWorkflow(dryrun);
		dax.saveAsXML("epi_dax.xml");
	}

}
