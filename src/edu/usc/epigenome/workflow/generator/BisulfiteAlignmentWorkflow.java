package edu.usc.epigenome.workflow.generator;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.griphyn.vdl.dax.Filename;


import edu.usc.epigenome.workflow.DAX.ECDax;
import edu.usc.epigenome.workflow.ECWorkflowParams.specialized.GAParams;
import edu.usc.epigenome.workflow.job.ECJob;
import edu.usc.epigenome.workflow.job.ecjob.BSMapJob;
import edu.usc.epigenome.workflow.job.ecjob.CountAdapterTrimJob;
import edu.usc.epigenome.workflow.job.ecjob.CountFastQJob;
import edu.usc.epigenome.workflow.job.ecjob.CountNmerJob;
import edu.usc.epigenome.workflow.job.ecjob.FastQConstantSplitJob;
import edu.usc.epigenome.workflow.job.ecjob.FilterContamsJob;
import edu.usc.epigenome.workflow.job.ecjob.GATKMetricJob;
import edu.usc.epigenome.workflow.job.ecjob.MergeBamsJob;
import edu.usc.epigenome.workflow.job.ecjob.QCMetricsJob;


public class BisulfiteAlignmentWorkflow
{
	/**
	 * Creates an AlignPileUp workflow from an empty dax object 
	 * @param dax The ECDAX to which processing jobs will be added
	 */	
	public static String WorkflowName = "bisulfite";
	
	public static void createWorkFlow(String sample, GAParams par,Boolean pbsMode, Boolean dryrun)
	{
		try
		{
			
			// construct a dax object
			// For every requested lane in this flowcell..
			ECDax dax = new ECDax(par);
			
			//get the params so that we have the input parameters
			GAParams workFlowParams = (GAParams) dax.getWorkFlowParams();
			String flowcellID = workFlowParams.getSetting("FlowCellName");
			String sampleName = par.getSamples().get(sample).get("SampleID");
			String laneNumber = par.getSamples().get(sample).get("Lane");
			String fileInput = par.getSamples().get(sample).get("Input");
			String referenceGenome = par.getSamples().get(sample).get("Reference");
			String sampleWorkflow = par.getSamples().get(sample).get("Workflow");
			String label = flowcellID + "_" + laneNumber + "_" + sampleName;
			
			boolean isPE = fileInput.contains(",");
		
			List<String> splitIDs = new LinkedList<String>();
			List<String> splitFiles = new LinkedList<String>();
			
			List<ECJob> bsmapJobs = new LinkedList<ECJob>();
			List<String> filterTrimCountFiles = new LinkedList<String>();
			
			String laneInputFileNameR1 = pbsMode ? new File(fileInput.split(",")[0]).getAbsolutePath() : new File(fileInput.split(",")[0]).getName();
			String laneInputFileNameR2 = null;
			//split Fastq Job. handle paired end and non pbs
			int splitSize = Integer.parseInt(workFlowParams.getSetting("ClusterSize"));
			FastQConstantSplitJob fastqSplitJob = null;
			if(isPE)
			{
				laneInputFileNameR2 = pbsMode ? new File(fileInput.split(",")[1]).getAbsolutePath() : new File(fileInput.split(",")[1]).getName();
				fastqSplitJob = new FastQConstantSplitJob(laneInputFileNameR1, laneInputFileNameR2, splitSize);
				System.out.println("Creating BiS PE Processing workflow for lane " + label + ": " + laneInputFileNameR1 + " " + laneInputFileNameR2 );
			}
			else
			{
				fastqSplitJob = new FastQConstantSplitJob(laneInputFileNameR1, splitSize);
				System.out.println("Creating BiS SR Processing workflow for lane " + label + ": " + laneInputFileNameR1);
			}						
			dax.addJob(fastqSplitJob);


			// iterate through the output files of fastQsplit jobs to create pipeline
			for (Filename f : fastqSplitJob.getOutputFiles())
			{
				String splitFileName = f.getFilename();
		
				FilterContamsJob filterContamJob = new FilterContamsJob(splitFileName);
				dax.addJob(filterContamJob);
				dax.addChild(filterContamJob.getID(), fastqSplitJob.getID());
				filterTrimCountFiles.add(filterContamJob.getContamAdapterTrimCountsOutputFileName());
				
				//filter contam job, cant do with PE since it messes up order
				if(!isPE)
					splitFileName = filterContamJob.getNoContamOutputFileName();
				
				splitFiles.add(splitFileName);
				splitIDs.add(filterContamJob.getID());
				
			}
			
			// map job. needs genome. PE and SE are processed diff due to extra file and args
			if(isPE)
			{
				for(int h = 0; h < splitFiles.size(); h+=2)
				{
					String read1 = splitFiles.get(h);
					String read2 = splitFiles.get(h+1);
					String parentJobEnd1 = splitIDs.get(h);
					String parentJobEnd2 = splitIDs.get(h+1);
					
					BSMapJob bsmap = new BSMapJob(read1, read2,referenceGenome, read1 + ".bam");
					dax.addJob(bsmap);
					dax.addChild(bsmap.getID(),parentJobEnd1);
					dax.addChild(bsmap.getID(),parentJobEnd2);
					bsmapJobs.add(bsmap);
				}						
			}
			else
			{
				for(int h = 0; h < splitFiles.size(); h++)
				{
					String read1 = splitFiles.get(h);
					String parentJobEnd1 = splitIDs.get(h);
					
					BSMapJob bsmap = new BSMapJob(read1, null,referenceGenome, read1 + ".bam");
					dax.addJob(bsmap);
					dax.addChild(bsmap.getID(),parentJobEnd1);
					bsmapJobs.add(bsmap);
				}
			}
			
	
			
			ArrayList<String> splitBams = new ArrayList<String>();
			for(ECJob job : bsmapJobs)
				splitBams.add(job.getSingleOutputFile().getFilename());
			
			// for each lane create a map merge job
			MergeBamsJob mergebams = new MergeBamsJob(splitBams,"ResultCount_" + flowcellID + "_" + laneNumber + "_" + sampleName + ".bam");
			dax.addJob(mergebams);
			
			// mapmerge is child to all the map jobs
			for (ECJob job : bsmapJobs)
				dax.addChild(mergebams.getID(), job.getID());
			
			
			//countAdapterTrimJob needs all the adapterCount filenames from FilterContamsJob, , child of mapmerge
			CountAdapterTrimJob countAdapterTrim = new CountAdapterTrimJob(filterTrimCountFiles,  flowcellID, Integer.parseInt(laneNumber));
			dax.addJob(countAdapterTrim);
			dax.addChild(countAdapterTrim.getID(), mergebams.getID());
			
			
			//for each lane create a countfastq job, child of bammerge
			CountFastQJob countFastQJob = new CountFastQJob(splitFiles.toArray(new String[0]), flowcellID, Integer.parseInt(laneNumber), false);
			dax.addJob(countFastQJob);
			dax.addChild(countFastQJob.getID(), mergebams.getID());
			
			//create qcmetrics job child of bammerge
			QCMetricsJob qcjob = new QCMetricsJob(workFlowParams.getSetting("tmpDir") + "/" + flowcellID + "/" + label, flowcellID);
			dax.addJob(qcjob);
			dax.addChild(qcjob.getID(), mergebams.getID());

			
			//create nmercount for 3, child of mapmerge
			CountNmerJob count3mer = new CountNmerJob(splitFiles.toArray(new String[0]), flowcellID, Integer.parseInt(laneNumber), 3);
			dax.addJob(count3mer);
			dax.addChild(count3mer.getID(),mergebams.getID());
			
			//create nmercount for 5, child of nmer
			CountNmerJob count5mer = new CountNmerJob(splitFiles.toArray(new String[0]), flowcellID, Integer.parseInt(laneNumber), 5);
			dax.addJob(count5mer);
			dax.addChild(count5mer.getID(), count3mer.getID());
			
			//create nmercount for 10, child of nmer
			CountNmerJob count10mer = new CountNmerJob(splitFiles.toArray(new String[0]), flowcellID, Integer.parseInt(laneNumber), 10);
			dax.addJob(count10mer);
			dax.addChild(count10mer.getID(),  count3mer.getID());
			
			//create readpairdup gatk job
			GATKMetricJob dupReadPairsMetricJob = new GATKMetricJob(mergebams.getBam(), mergebams.getBai(), referenceGenome, "InvertedReadPairDups", "");
			dax.addJob(dupReadPairsMetricJob);
			dax.addChild(dupReadPairsMetricJob.getID(),  mergebams.getID());
			
			//create MethLevelAverages gatk job
			GATKMetricJob methLevelAveragesMetricJob = new GATKMetricJob(mergebams.getBam(), mergebams.getBai(), referenceGenome, "MethLevelAverages", "-cph");
			dax.addJob(methLevelAveragesMetricJob);
			dax.addChild(methLevelAveragesMetricJob.getID(),  mergebams.getID());
			
			//create MethLevelAverages gatk job
			//GATKMetricJob methLevelAveragesMetricJob = new GATKMetricJob(mergebams.getBam(), mergebams.getBai(), referenceGenome, "MethLevelAverages", "-cph");
			//dax.addJob(methLevelAveragesMetricJob);
			//dax.addChild(methLevelAveragesMetricJob.getID(),  mergebams.getID());
//			
//
//
//			//create pileup.gz job, child of mapmerge
//			PileupJob pileupJob = new PileupJob(mapMergeJob.getSingleOutputFile().getFilename(), referenceGenome, Integer.parseInt(workFlowParams
//					.getSetting("MaqPileupQ")));;
//			dax.addJob(pileupJob);
//			dax.addChild(pileupJob.getID(), mapMergeJob.getID());				
//		
//			//create maq2bamjob, child of mapMerge
//			Maq2BamJob maq2bamJob = new Maq2BamJob(mapMergeJob.getSingleOutputFile().getFilename(),referenceGenome.replace(".bfa", ".fa"));
//			dax.addJob(maq2bamJob);
//			dax.addChild(maq2bamJob.getID(), mapMergeJob.getID());
//			
//			//merge unaln and aln, mark dups
//			MergeBamFastqJob bamfastqJob = new MergeBamFastqJob(laneInputFileNameR1, laneInputFileNameR2, maq2bamJob.getBamOutput(), referenceGenome.replace(".bfa", ".fa"), sampleName, 
//					sampleName,	flowcellID, laneNumber, "maq", "0.7.1", "maq map -M c", false, "ResultCount_" + flowcellID  + "_s_" + laneNumber + "_all.bam");
//			dax.addJob(bamfastqJob);
//			dax.addChild(bamfastqJob.getID(), maq2bamJob.getID());
//			
//			
//			//create countPileupJob, child of gziped pileupJob
//			CountPileupJob countMonoPileupJob = new CountPileupJob(pileupJob.getSingleOutputFile().getFilename() ,CountPileupJob.Mononucleotide);
//			dax.addJob(countMonoPileupJob);
//			dax.addChild(countMonoPileupJob.getID(), pileupJob.getID());
//			dax.addChild(qcjob.getID(), countMonoPileupJob.getID());
//			
//			//create countPileupJob, child of gzipped pileupJob
//			CountPileupJob countCGPileupJob = new CountPileupJob(pileupJob.getSingleOutputFile().getFilename(),CountPileupJob.CGdinucleotide);
//			dax.addJob(countCGPileupJob);
//			dax.addChild(countCGPileupJob.getID(), pileupJob.getID());
//			dax.addChild(qcjob.getID(), countCGPileupJob.getID());
//			
//			//create countPileupJob, child of gzipped pileupJob
//			CountPileupJob countCHPileupJob = new CountPileupJob(pileupJob.getSingleOutputFile().getFilename(),CountPileupJob.CHdinucleotide);
//			dax.addJob(countCHPileupJob);
//			dax.addChild(countCHPileupJob.getID(), pileupJob.getID());
//			dax.addChild(qcjob.getID(), countCHPileupJob.getID());
//			
//			//create countPileupJob, child of gzipped pileupJob
//			CountPileupJob countGenomePileupJob = new CountPileupJob(pileupJob.getSingleOutputFile().getFilename(),CountPileupJob.RefComposition);
//			dax.addJob(countGenomePileupJob);
//			dax.addChild(countGenomePileupJob.getID(), pileupJob.getID());
//			dax.addChild(qcjob.getID(), countGenomePileupJob.getID());
//			
//			//create readdepth:0, child of gzipped pileupJob
//			String genome;
//			if(referenceGenome.contains("phi")) { genome = "phiX";}
//			else if(referenceGenome.contains("hg18")) { genome = "hg18";}
//			else if(referenceGenome.contains("hg19")) { genome = "hg19";}
//			else if(referenceGenome.contains("tair8")) { genome = "tair8";}
//			else if(referenceGenome.contains("sacCer")) { genome = "sacCer1";}
//			else if(referenceGenome.contains("mm")) { genome = "mm9";}
//			else {genome = "hg18";}
//			
//			ReadDepthJob readdepthJob0 = new ReadDepthJob(pileupJob.getSingleOutputFile().getFilename(), flowcellID, Integer.parseInt(laneNumber),genome, 5000, 0);
//			dax.addJob(readdepthJob0);
//			dax.addChild(readdepthJob0.getID(), pileupJob.getID());
//			//dax.addChild(qcjob.getID(), readdepthJob0.getID());
//
//			//create readdepth:1, child of gzipped pileupJob
//			ReadDepthJob readdepthJob1 = new ReadDepthJob(pileupJob.getSingleOutputFile().getFilename(), flowcellID, Integer.parseInt(laneNumber),genome, 5000, 1);
//			dax.addJob(readdepthJob1); 
//			dax.addChild(readdepthJob1.getID(), pileupJob.getID());
//			//dax.addChild(qcjob.getID(), readdepthJob1.getID());
//			
//			//create readcount, child of gzipped pileupJob
//			ReadCountJob readcountJob = new ReadCountJob(pileupJob.getSingleOutputFile().getFilename(), flowcellID, Integer.parseInt(laneNumber), Integer.parseInt(workFlowParams.getSetting("randomSubset")), 20);
//			dax.addJob(readcountJob);
//			dax.addChild(readcountJob.getID(), readdepthJob1.getID());
//			dax.addChild(readcountJob.getID(), readdepthJob0.getID());
//			dax.addChild(qcjob.getID(), readcountJob.getID());
//			
//			
//			//pileup to wig job child of gzipped pileupjob
//			PileupToWigJob pilewig = new PileupToWigJob(pileupJob.getSingleOutputFile().getFilename(), flowcellID, Integer.parseInt(laneNumber), Integer.parseInt(workFlowParams.getSetting("WigWindSize")), 50, 1, 0, 2);
//			dax.addJob(pilewig);
//			dax.addChild(pilewig.getID(), pileupJob.getID());
//			
//			//wig to tdf (IGVTOOLS )job child of pileup to wig
//			WigToTdfJob wigtotdf = new WigToTdfJob(pilewig.getSingleOutputFile().getFilename(),genome);
//			dax.addJob(wigtotdf);
//			dax.addChild(wigtotdf.getID(),pilewig.getID());
//				

			
			if(dax.getChildCount() > 0)
			{
				dax.saveAsDot("bisulfite_dax_" + label + ".dot");
				dax.saveAsSimpleDot("bisulfite_dax_simple_" + label + ".dot");
				if(pbsMode)
				{
					par.getWorkFlowArgsMap().put("WorkflowName", label);
					dax.runWorkflow(dryrun);
				}
				dax.saveAsXML("bisulfite_dax_" + label + ".xml");
			}
			dax.release();
			
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
