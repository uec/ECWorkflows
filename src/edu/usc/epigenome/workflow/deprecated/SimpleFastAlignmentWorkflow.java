package edu.usc.epigenome.workflow.deprecated;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import org.griphyn.vdl.dax.Filename;
import edu.usc.epigenome.workflow.DAX.ECDax;
import edu.usc.epigenome.workflow.ECWorkflowParams.specialized.GAParams;
import edu.usc.epigenome.workflow.job.ECJob;
import edu.usc.epigenome.workflow.job.ecjob.BwaJob;
import edu.usc.epigenome.workflow.job.ecjob.FastQConstantSplitJob;
import edu.usc.epigenome.workflow.job.ecjob.FilterContamsJob;
import edu.usc.epigenome.workflow.job.ecjob.Sam2BamJob;
import edu.usc.epigenome.workflow.job.ecjob.Sol2SangerJob;

public class SimpleFastAlignmentWorkflow
{
	/**
	 * Creates an AlignPileUp workflow from an empty dax object 
	 * @param dax The ECDAX to which processing jobs will be added
	 */
	public static String WorkflowName = "fast";
	
	public static void createWorkFlow(String sample, GAParams par,Boolean pbsMode, Boolean dryrun)
	{
		try
		{
			ECDax dax = new ECDax(par);
			//get the params so that we have the input parameters
			GAParams workFlowParams = (GAParams) dax.getWorkFlowParams();
			
			String sampleName = par.getSamples().get(sample).get("SampleID");
			String laneNumber = par.getSamples().get(sample).get("Lane");
			String fileInput = par.getSamples().get(sample).get("Input");
			String referenceGenome = par.getSamples().get(sample).get("Reference");
			String sampleWorkflow = par.getSamples().get(sample).get("Workflow");
			String label = workFlowParams.getSetting("FlowCellName") + "_" + laneNumber + "_" + sampleName;
			boolean isPE = fileInput.contains(",");
				
			List<Sol2SangerJob> fastqJobs = new LinkedList<Sol2SangerJob>();
			List<ECJob> bwaJobs = new LinkedList<ECJob>();

			
			//split Fastq Job. handle paired end and non pbs
			
			String laneInputFileNameR1 = pbsMode ? new File(fileInput.split(",")[0]).getAbsolutePath() : new File(fileInput.split(",")[0]).getName();
			
			//split Fastq Job. handle paired end and non pbs
			int splitSize = Integer.parseInt(workFlowParams.getSetting("ClusterSize"));
			FastQConstantSplitJob fastqSplitJob = null;
			if(isPE)
			{
				String laneInputFileNameR2 = pbsMode ? new File(fileInput.split(",")[1]).getAbsolutePath() : new File(fileInput.split(",")[1]).getName();
				fastqSplitJob = new FastQConstantSplitJob(laneInputFileNameR1, laneInputFileNameR2, splitSize);
				System.out.println("Creating BWA PE Processing workflow for lane " + label + ": " + laneInputFileNameR1 + " " + laneInputFileNameR2 );
			}
			else
			{
				fastqSplitJob = new FastQConstantSplitJob(laneInputFileNameR1, splitSize);
				System.out.println("Creating BWA SR Processing workflow for lane " + label + ": " + laneInputFileNameR1);
			}						
			dax.addJob(fastqSplitJob);

			// iterate through the output files of fastQsplit jobs to create pipeline
			for (Filename f : fastqSplitJob.getOutputFiles())
			{
				String splitFileName = f.getFilename();
				FilterContamsJob filterContamJob = null;
				if(!isPE)
				{
					//filter contam job, cant do with PE since it messes up order
					String splitFastqOutputFile = f.getFilename();
					filterContamJob = new FilterContamsJob(splitFastqOutputFile);
					dax.addJob(filterContamJob);
					dax.addChild(filterContamJob.getID(), fastqSplitJob.getID());
					//get the nocontam file							
					for(Filename s : filterContamJob.getOutputFiles())
					{
						if(s.getFilename().contains(".nocontam"))
						{
							splitFileName = s.getFilename();
						}
					}
				}
				
				// sol2sanger job
				Sol2SangerJob sol2sangerJob = new Sol2SangerJob(splitFileName);
				dax.addJob(sol2sangerJob);
				if(isPE)
					dax.addChild(sol2sangerJob.getID(), fastqSplitJob.getID());
				else
					dax.addChild(sol2sangerJob.getID(), filterContamJob.getID());
				fastqJobs.add(sol2sangerJob);
				
				
			}
			
			// map job. needs genome. PE and SE are processed diff due to extra file and args
			if(isPE)
			{
				for(int h = 0; h < fastqJobs.size(); h+=2)
				{
					ECJob fastqJobR1 = fastqJobs.get(h);
					ECJob fastqJobR2 = fastqJobs.get(h+1);
					BwaJob bwaJob = new BwaJob(fastqJobR1.getSingleOutputFile().getFilename(), fastqJobR2.getSingleOutputFile().getFilename(),referenceGenome);
					dax.addJob(bwaJob);
					dax.addChild(bwaJob.getID(), fastqJobR1.getID());
					dax.addChild(bwaJob.getID(), fastqJobR2.getID());
					bwaJobs.add(bwaJob);
				}						
			}
			else
			{
				for(Sol2SangerJob fqJob : fastqJobs)
				{
					BwaJob bwaJob = new BwaJob(fqJob.getSingleOutputFile().getFilename(), referenceGenome);
					dax.addJob(bwaJob);
					dax.addChild(bwaJob.getID(), fqJob.getID());
					bwaJobs.add(bwaJob);
				}
			}
			
			// build list of output SAM filenames for sam2bam job input 
			ArrayList<String> samFiles = new ArrayList<String>();
			for (ECJob bwa : bwaJobs)
			{
				for (Filename f : bwa.getOutputFiles())
				{
					samFiles.add(f.getFilename());
				}
			}
			
			/* BWA depends on reference .fa AND reference .fai file. */
			Sam2BamJob sam2BamJob = new Sam2BamJob(samFiles, referenceGenome);
			dax.addJob(sam2BamJob);
			
			for (ECJob bwa : bwaJobs)
			{
				dax.addChild(sam2BamJob.getID(),bwa.getID());
			}


			if(dax.getChildCount() > 0)
			{
				dax.saveAsDot("fastAlignment_dax.dot");
				dax.saveAsSimpleDot("fastAlignment_dax_simple.dot");
				if(pbsMode)
				{
					par.getWorkFlowArgsMap().put("WorkflowName", label);
					dax.runWorkflow(dryrun);
				}
				dax.saveAsXML("fastAlignment_dax.xml");
			}
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}	
}
