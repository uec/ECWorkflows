package edu.usc.epigenome.workflow.generator;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.Job;
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
				if(workFlowParams.getSetting("Lane." + i + ".AlignmentType").toLowerCase().equals("fast"))
				{
					boolean isPE = workFlowParams.getLaneInput(i).contains(",");
				
					List<Sol2SangerJob> fastqJobs = new LinkedList<Sol2SangerJob>();
					List<ECJob> bwaJobs = new LinkedList<ECJob>();
					
					String laneInputFileNameR1 = null;
					String laneInputFileNameR2 = null;
					
					//split Fastq Job. handle paired end and non pbs
					int splitSize = Integer.parseInt(workFlowParams.getSetting("ClusterSize")) / 8;
					FastQConstantSplitJob fastqSplitJob = null;
					if(pbsMode = true)
					{
						if( isPE)
						{
							laneInputFileNameR1 = new File(workFlowParams.getLaneInput(i).split(",")[0]).getAbsolutePath();
							laneInputFileNameR2 = new File(workFlowParams.getLaneInput(i).split(",")[1]).getAbsolutePath();
							fastqSplitJob = new FastQConstantSplitJob(laneInputFileNameR1, laneInputFileNameR2, splitSize);
							System.out.println("Creating Simple Fast PE Processing workflow for lane " + i + ": " + laneInputFileNameR1 + " " + laneInputFileNameR2 );
						}
						else
						{
							laneInputFileNameR1 = new File(workFlowParams.getLaneInput(i)).getAbsolutePath();
							fastqSplitJob = new FastQConstantSplitJob(laneInputFileNameR1, splitSize);
							System.out.println("Creating Simple Fast SR Processing workflow for lane " + i + ": " + laneInputFileNameR1);
						}
					}
					else
					{
						if( isPE)
						{
							laneInputFileNameR1 = new File(workFlowParams.getLaneInput(i).split(",")[0]).getName();
							laneInputFileNameR2 = new File(workFlowParams.getLaneInput(i).split(",")[1]).getName();
							fastqSplitJob = new FastQConstantSplitJob(laneInputFileNameR1, laneInputFileNameR2, splitSize);
						}
						else
						{
							laneInputFileNameR1 = new File(workFlowParams.getLaneInput(i)).getName();
							fastqSplitJob = new FastQConstantSplitJob(laneInputFileNameR1, splitSize);
						}
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
							BwaJob bwaJob = new BwaJob(fastqJobR1.getSingleOutputFile().getFilename(), fastqJobR2.getSingleOutputFile().getFilename(), workFlowParams.getSetting("Lane." + i + ".ReferenceFA"));
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
							BwaJob bwaJob = new BwaJob(fqJob.getSingleOutputFile().getFilename(), workFlowParams.getSetting("Lane." + i + ".Reference"));
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
					Sam2BamJob sam2BamJob = new Sam2BamJob(samFiles, workFlowParams.getSetting("Lane." + i + ".ReferenceFA"));
					dax.addJob(sam2BamJob);
					
					for (ECJob bwa : bwaJobs)
					{
						dax.addChild(sam2BamJob.getID(),bwa.getID());
					}

				}

			}
			if(dax.getChildCount() > 0)
			{
				dax.saveAsDot("fastAlignment_dax.dot");
				dax.saveAsSimpleDot("fastAlignment_dax_simple.dot");
				if(pbsMode)
					dax.runWorkflow(dryrun);
				dax.saveAsXML("fastAlignment_dax.xml");
			}
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}	
}
