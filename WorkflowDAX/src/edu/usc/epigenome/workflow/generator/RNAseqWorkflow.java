package edu.usc.epigenome.workflow.generator;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;

import org.griphyn.vdl.dax.Filename;

import edu.usc.epigenome.workflow.DAX.ECDax;
import edu.usc.epigenome.workflow.ECWorkflowParams.specialized.GAParams;
import edu.usc.epigenome.workflow.job.ecjob.CountAdapterTrimJob;
import edu.usc.epigenome.workflow.job.ecjob.CountFastQJob;
import edu.usc.epigenome.workflow.job.ecjob.CountNmerJob;
import edu.usc.epigenome.workflow.job.ecjob.CuffCompareJob;
import edu.usc.epigenome.workflow.job.ecjob.CuffDiffJob;
import edu.usc.epigenome.workflow.job.ecjob.CufflinksJob;
import edu.usc.epigenome.workflow.job.ecjob.FastQConstantSplitJob;
import edu.usc.epigenome.workflow.job.ecjob.FilterContamsJob;
import edu.usc.epigenome.workflow.job.ecjob.Sol2SangerJob;
import edu.usc.epigenome.workflow.job.ecjob.TopHatJob;

public class RNAseqWorkflow
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
			HashMap<String,String> sampleBams = new HashMap<String,String>();
			HashMap<String,String> sampleGTFs = new HashMap<String,String>();
			HashMap<String,String> lastJobId = new HashMap<String,String>(); 
			
			for (int i : workFlowParams.getAvailableLanes())
			{
				if(workFlowParams.getSetting("Lane." + i + ".AlignmentType").toLowerCase().equals("rnaseq"))
				{
					boolean isPE = workFlowParams.getLaneInput(i).contains(",");
				
					List<Sol2SangerJob> fastqJobs = new LinkedList<Sol2SangerJob>();
					
					List<TopHatJob> tophatJobs = new LinkedList<TopHatJob>();
					List<String> filterTrimCountFiles = new LinkedList<String>();
					
					String laneInputFileNameR1 = pbsMode ? new File(workFlowParams.getLaneInput(i).split(",")[0]).getAbsolutePath() : new File(workFlowParams.getLaneInput(i).split(",")[0]).getName();
					
					//split Fastq Job. handle paired end and non pbs
					int splitSize = 1;
					FastQConstantSplitJob fastqSplitJob = null;
					if(isPE)
					{
						String laneInputFileNameR2 = pbsMode ? new File(workFlowParams.getLaneInput(i).split(",")[1]).getAbsolutePath() : new File(workFlowParams.getLaneInput(i).split(",")[1]).getName();
						fastqSplitJob = new FastQConstantSplitJob(laneInputFileNameR1, laneInputFileNameR2, splitSize);
						System.out.println("Creating RNAseq PE Processing workflow for lane " + i + ": " + laneInputFileNameR1 + " " + laneInputFileNameR2 );
					}
					else
					{
						fastqSplitJob = new FastQConstantSplitJob(laneInputFileNameR1, splitSize);
						System.out.println("Creating RNAseq SR Processing workflow for lane " + i + ": " + laneInputFileNameR1);
					}
					dax.addJob(fastqSplitJob);
					
					
					
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
							filterTrimCountFiles.add(filterContamJob.getContamAdapterTrimCountsOutputFileName());
							splitFileName = filterContamJob.getNoContamOutputFileName();
						}
						
						// sol2sanger job
						Sol2SangerJob sol2sangerJob = new Sol2SangerJob(splitFileName);
						dax.addJob(sol2sangerJob);
						dax.addChild(sol2sangerJob.getID(), isPE ? fastqSplitJob.getID() : filterContamJob.getID());
						fastqJobs.add(sol2sangerJob);
						
						
					}
					
					// map job. needs genome. PE and SE are processed diff due to extra file and args
					if(isPE)
					{
						for(int h = 0; h < fastqJobs.size(); h+=2)
						{
							Sol2SangerJob sangerJobE1 = fastqJobs.get(h);
							Sol2SangerJob sangerJobE2 = fastqJobs.get(h+1);
							TopHatJob tophat = new TopHatJob(sangerJobE1.getSingleOutputFile().getFilename(), sangerJobE2.getSingleOutputFile().getFilename(), workFlowParams.getSetting("Lane." + i + ".ReferenceBFA"), 150);
							

							dax.addJob(tophat);
							dax.addChild(tophat.getID(), sangerJobE1.getID());
							dax.addChild(tophat.getID(), sangerJobE2.getID());
							tophatJobs.add(tophat);
						}						
					}
					else
					{
						for(Sol2SangerJob sangerJob : fastqJobs)
						{
							TopHatJob tophat = new TopHatJob(sangerJob.getSingleOutputFile().getFilename(), workFlowParams.getSetting("Lane." + i + ".ReferenceBFA"));
							dax.addJob(tophat);
							dax.addChild(tophat.getID(), sangerJob.getID());
							tophatJobs.add(tophat);
						}
					}
					
					//we dont support merging muliple tophat runs yet
					TopHatJob tophat = tophatJobs.get(0);
					
					//run cufflinks
					CufflinksJob cufflinks = new CufflinksJob(tophat.getBamFile());
					dax.addJob(cufflinks);
					dax.addChild(cufflinks.getID(), tophat.getID());
					
					lastJobId.put(String.valueOf(i), cufflinks.getID());
					sampleGTFs.put(String.valueOf(i), cufflinks.getGtfFile());
					sampleBams.put(String.valueOf(i), tophat.getBamFile());
					
					
					//for each lane create a countfastq job, child of mapmerge
					CountFastQJob countFastQJob = new CountFastQJob(fastqJobs, workFlowParams.getSetting("FlowCellName"), i, false);
					dax.addJob(countFastQJob);
					dax.addChild(countFastQJob.getID(), tophat.getID());
					
					
					
					
					//countAdapterTrimJob needs all the adapterCount filenames from FilterContamsJob, , child of mapmerge
					if(!isPE)
					{
						CountAdapterTrimJob countAdapterTrim = new CountAdapterTrimJob(filterTrimCountFiles,  workFlowParams.getSetting("FlowCellName"), i);
						dax.addJob(countAdapterTrim);
						dax.addChild(countAdapterTrim.getID(), tophat.getID());
					}
					
					//create nmercount for 3, child of mapmerge
					CountNmerJob count3mer = new CountNmerJob(fastqJobs, workFlowParams.getSetting("FlowCellName"), i, 3);
					dax.addJob(count3mer);
					dax.addChild(count3mer.getID(), tophat.getID());
					
					//create nmercount for 5, child of mapmerge
					CountNmerJob count5mer = new CountNmerJob(fastqJobs, workFlowParams.getSetting("FlowCellName"), i, 5);
					dax.addJob(count5mer);
					dax.addChild(count5mer.getID(), count3mer.getID());
					
					//create nmercount for 10, child of mapmerge
					CountNmerJob count10mer = new CountNmerJob(fastqJobs, workFlowParams.getSetting("FlowCellName"), i, 10);
					dax.addJob(count10mer);
					dax.addChild(count10mer.getID(), count3mer.getID());
										
				}//end if(rnaseq)
			}//end lane loop
			
			//generate diff exp rnaseq analysis
			for(String rnadiffkey : workFlowParams.getWorkFlowArgsMap().keySet())
			{
				if(rnadiffkey.startsWith("rnaseq.diff"))
				{
					//parse params and build the input list of prereq files
					ArrayList<ArrayList<String>> analysisBams = new ArrayList<ArrayList<String>>();
					ArrayList<String> analysisGTFs = new ArrayList<String>();
					
					String[] rnadiffparam = workFlowParams.getSetting(rnadiffkey).split(":");
					String prefix = workFlowParams.getSetting(rnadiffkey).replace(":", "_").replace(",", "-");
					
					for(int k = 0;k<rnadiffparam.length;k++)
					{
						ArrayList<String> sample = new ArrayList<String>();
						for(String m : rnadiffparam[k].split(","))
						{
							sample.add(sampleBams.get(m));
							analysisGTFs.add(sampleGTFs.get(m));
						}
						analysisBams.add(sample);
					}
					
					//create cuffcompare job from ref gene gtf and sample gtfs
					CuffCompareJob cuffcompare = new CuffCompareJob(workFlowParams.getSetting("refGene"),analysisGTFs, prefix + "_" + new File(workFlowParams.getSetting("refGene")).getName());
					dax.addJob(cuffcompare);
					for(String s : rnadiffparam)
					{
						if(s.contains(","))
							for(String m : s.split(","))
								dax.addChild(cuffcompare.getID(),lastJobId.get(m));
						else
							dax.addChild(cuffcompare.getID(),lastJobId.get(s));
					}
					
					
					//create a cuffdiff job from cuffcompare output and sample bams
					CuffDiffJob cuffdiff = new CuffDiffJob(cuffcompare.getOutputGTF(), analysisBams,prefix,rnadiffkey.toLowerCase().contains("time"));
					dax.addJob(cuffdiff);
					dax.addChild(cuffdiff.getID(),cuffcompare.getID());
				}
			}
			
			
			
			
			if(dax.getChildCount() > 0)
			{
				dax.saveAsDot("rnaseq_dax.dot");
				dax.saveAsSimpleDot("rnaseq_dax_simple.dot");
				if(pbsMode)
					dax.runWorkflow(dryrun);
				dax.saveAsXML("rnaseq_dax.xml");
			}
			
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}	
}