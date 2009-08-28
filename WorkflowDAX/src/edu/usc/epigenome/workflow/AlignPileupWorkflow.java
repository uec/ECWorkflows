package edu.usc.epigenome.workflow;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.Job;
import edu.usc.epigenome.workflow.DAX.ECDax;
import edu.usc.epigenome.workflow.ECWorkflowParams.ECWorkflowParams;
import edu.usc.epigenome.workflow.job.ECJob;
import edu.usc.epigenome.workflow.job.ecjob.AlignFeaturePileupJob;
import edu.usc.epigenome.workflow.job.ecjob.CountFastQJob;
import edu.usc.epigenome.workflow.job.ecjob.CountNmerJob;
import edu.usc.epigenome.workflow.job.ecjob.CountPileupJob;
import edu.usc.epigenome.workflow.job.ecjob.FastQ2BFQJob;
import edu.usc.epigenome.workflow.job.ecjob.FastQConstantSplitJob;
//import edu.usc.epigenome.workflow.job.ecjob.FastQSplitJob;
import edu.usc.epigenome.workflow.job.ecjob.FilterContamsJob;
import edu.usc.epigenome.workflow.job.ecjob.MapJob;
import edu.usc.epigenome.workflow.job.ecjob.MapMergeJob;
import edu.usc.epigenome.workflow.job.ecjob.MapViewJob;
import edu.usc.epigenome.workflow.job.ecjob.PileupJob;
import edu.usc.epigenome.workflow.job.ecjob.PileupToWigJob;
import edu.usc.epigenome.workflow.job.ecjob.QCMetricsJob;
import edu.usc.epigenome.workflow.job.ecjob.ReadCountJob;
import edu.usc.epigenome.workflow.job.ecjob.ReadDepthJob;
import edu.usc.epigenome.workflow.job.ecjob.Sol2SangerJob;

public class AlignPileupWorkflow
{
	/**
	 * Creates an AlignPileUp workflow from an empty dax object 
	 * @param dax The ECDAX to which processing jobs will be added
	 */	
	public static void createWorkFlow(ECDax dax, Boolean pbsMode)	
	{
		try
		{
			// construct a dax object
			// For every requested lane in this flowcell..
			
			//get the params so that we have the input parameters
			ECWorkflowParams workFlowParams = dax.getWorkFlowParams();
			workFlowParams.saveAs("workflowParamsUsed.log.txt");
			//List<Job> mapMergeJobs = new LinkedList<Job>();
			for (int i : workFlowParams.getAvailableLanes())
			{
				List<ECJob> mapJobs = new LinkedList<ECJob>();
				List<Sol2SangerJob> fastqJobs = new LinkedList<Sol2SangerJob>();
				
				String laneInputFileName;
				if(pbsMode = true)
					laneInputFileName = new File(workFlowParams.getLaneInput(i)).getAbsolutePath();
				else
					laneInputFileName = new File(workFlowParams.getLaneInput(i)).getName();
				
					
				System.out.println("Creating processing pipeline for lane " + i + ": " + laneInputFileName);

				// create a fastSplit job
				/* 
				int splitSize = 0;
				if (workFlowParams.laneIsBisulfite(i))
					splitSize = Integer.parseInt(workFlowParams.getSetting("BisulfiteSplitFactor"));
				else
					splitSize = Integer.parseInt(workFlowParams.getSetting("RegularSplitFactor"));
				FastQSplitJob fastqSplitJob = new FastQSplitJob(laneInputFileName, splitSize);
				dax.addJob(fastqSplitJob);
				*/
				int splitSize = Integer.parseInt(workFlowParams.getSetting("ClusterSize")) / 7;
				FastQConstantSplitJob fastqSplitJob = new FastQConstantSplitJob(laneInputFileName, splitSize);
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
					
					//get the nocontam file
					String nocontamFile = "";
					for(Filename s : filterContamJob.getOutputFiles())
					{
						if(s.getFilename().contains(".nocontam"))
						{
							nocontamFile = s.getFilename();
						}
					}
					Sol2SangerJob sol2sangerJob = new Sol2SangerJob(nocontamFile);
					dax.addJob(sol2sangerJob);
					dax.addChild(sol2sangerJob.getID(), filterContamJob.getID());
					fastqJobs.add(sol2sangerJob);
					
					// fastq2bfq job
					FastQ2BFQJob fastq2bfqJob = new FastQ2BFQJob(sol2sangerJob.getSingleOutputFile().getFilename());
					dax.addJob(fastq2bfqJob);
					dax.addChild(fastq2bfqJob.getID(), sol2sangerJob.getID());

					// map job. additional input grabbed from hg18.BS.bfa
					MapJob mapJob = new MapJob(fastq2bfqJob.getSingleOutputFile().getFilename(), workFlowParams.getSetting("Lane." + i + ".ReferenceBFA"),  Integer.parseInt(workFlowParams.getSetting("MinMismatches")),
							workFlowParams.laneIsBisulfite(i), Integer.parseInt(workFlowParams.getSetting("MaqTrimEnd1")), Integer.parseInt(workFlowParams.getSetting("MaqTrimEnd2")));
					dax.addJob(mapJob);
					dax.addChild(mapJob.getID(), fastq2bfqJob.getID());
					mapJobs.add(mapJob);

				}
				
				
				//for each lane create a countfastq job
				CountFastQJob countFastQJob = new CountFastQJob(fastqJobs, workFlowParams.getSetting("FlowCellName"), i);
				dax.addJob(countFastQJob);
				// mapmerge is child to all the map jobs
				for (Job fastqjob : fastqJobs)
				{
					dax.addChild(countFastQJob.getID(), fastqjob.getID());
				}
				
				
				//create nmercount for 3
				CountNmerJob count3mer = new CountNmerJob(fastqJobs, workFlowParams.getSetting("FlowCellName"), i, 3);
				dax.addJob(count3mer);
				// mapmerge is child to all the map jobs
				for (Job fastqjob : fastqJobs)
				{
					dax.addChild(count3mer.getID(), fastqjob.getID());
				}
				
				//create nmercount for 5
				CountNmerJob count5mer = new CountNmerJob(fastqJobs, workFlowParams.getSetting("FlowCellName"), i, 5);
				dax.addJob(count5mer);
				// mapmerge is child to all the map jobs
				for (Job fastqjob : fastqJobs)
				{
					dax.addChild(count5mer.getID(), fastqjob.getID());
				}
				
				//create nmercount for 10
				CountNmerJob count10mer = new CountNmerJob(fastqJobs, workFlowParams.getSetting("FlowCellName"), i, 10);
				dax.addJob(count10mer);
				// mapmerge is child to all the map jobs
				for (Job fastqjob : fastqJobs)
				{
					dax.addChild(count10mer.getID(), fastqjob.getID());
				}
				
				// for each lane create a map merge job
				MapMergeJob mapMergeJob = new MapMergeJob(mapJobs, workFlowParams.getSetting("FlowCellName"), i);
				dax.addJob(mapMergeJob);
				// mapmerge is child to all the map jobs
				for (Job map : mapJobs)
				{
					dax.addChild(mapMergeJob.getID(), map.getID());
				}
				//mapMergeJobs.add(mapMergeJob);
				
				//create qcmetrics job 
				QCMetricsJob qcjob = new QCMetricsJob(workFlowParams.getSetting("tmpDir") + "/" + workFlowParams.getSetting("FlowCellName"), workFlowParams.getSetting("FlowCellName"));
				dax.addJob(qcjob);

				// crate mapview job, child of mapmerge
				MapViewJob mapViewJob =  new MapViewJob(mapMergeJob.getSingleOutputFile().getFilename(), Integer.parseInt(workFlowParams.getSetting("MaqPileupQ")));;
				dax.addJob(mapViewJob);
				dax.addChild(mapViewJob.getID(), mapMergeJob.getID());
				dax.addChild(qcjob.getID(), mapViewJob.getID());

				//create pileup.gz job, child of mapview
				PileupJob pileupJob = new PileupJob(mapMergeJob.getSingleOutputFile().getFilename(), workFlowParams.getSetting("Lane." + i +".ReferenceBFA"), Integer.parseInt(workFlowParams
						.getSetting("MaqPileupQ")));;
				dax.addJob(pileupJob);
				dax.addChild(pileupJob.getID(), mapMergeJob.getID());				
				
				
				
				//create countPileupJob, child of gziped pileupJob
				CountPileupJob countMonoPileupJob = new CountPileupJob(pileupJob.getSingleOutputFile().getFilename() ,CountPileupJob.Mononucleotide);
				dax.addJob(countMonoPileupJob);
				dax.addChild(countMonoPileupJob.getID(), pileupJob.getID());
				dax.addChild(qcjob.getID(), countMonoPileupJob.getID());
				
				//create countPileupJob, child of gzipped pileupJob
				CountPileupJob countCGPileupJob = new CountPileupJob(pileupJob.getSingleOutputFile().getFilename(),CountPileupJob.CGdinucleotide);
				dax.addJob(countCGPileupJob);
				dax.addChild(countCGPileupJob.getID(), pileupJob.getID());
				dax.addChild(qcjob.getID(), countCGPileupJob.getID());
				
				//create countPileupJob, child of gzipped pileupJob
				CountPileupJob countCHPileupJob = new CountPileupJob(pileupJob.getSingleOutputFile().getFilename(),CountPileupJob.CHdinucleotide);
				dax.addJob(countCHPileupJob);
				dax.addChild(countCHPileupJob.getID(), pileupJob.getID());
				dax.addChild(qcjob.getID(), countCHPileupJob.getID());
				
				//create countPileupJob, child of gzipped pileupJob
				CountPileupJob countGenomePileupJob = new CountPileupJob(pileupJob.getSingleOutputFile().getFilename(),CountPileupJob.RefComposition);
				dax.addJob(countGenomePileupJob);
				dax.addChild(countGenomePileupJob.getID(), pileupJob.getID());
				dax.addChild(qcjob.getID(), countGenomePileupJob.getID());
				
				//create readdepth:0, child of gzipped pileupJob
				String genome;
				if(workFlowParams.getSetting("Lane." + i + ".ReferenceBFA").contains("phi")) { genome = "phiX";}
				else if(workFlowParams.getSetting("Lane." + i + ".ReferenceBFA").contains("hg18")) { genome = "hg18";}
				else if(workFlowParams.getSetting("Lane." + i + ".ReferenceBFA").contains("sacCer")) { genome = "sacCer1";}
				else if(workFlowParams.getSetting("Lane." + i + ".ReferenceBFA").contains("mm")) { genome = "mm9";}
				else {genome = "hg18";}
				
				ReadDepthJob readdepthJob0 = new ReadDepthJob(pileupJob.getSingleOutputFile().getFilename(), workFlowParams.getSetting("FlowCellName"), i,genome, 5000, 0);
				dax.addJob(readdepthJob0);
				dax.addChild(readdepthJob0.getID(), pileupJob.getID());
				dax.addChild(qcjob.getID(), readdepthJob0.getID());

				//create readdepth:1, child of gzipped pileupJob
				ReadDepthJob readdepthJob1 = new ReadDepthJob(pileupJob.getSingleOutputFile().getFilename(), workFlowParams.getSetting("FlowCellName"), i,genome, 5000, 1);
				dax.addJob(readdepthJob1); 
				dax.addChild(readdepthJob1.getID(), pileupJob.getID());
				dax.addChild(qcjob.getID(), readdepthJob1.getID());
				
				//create readcount, child of gzipped pileupJob
				ReadCountJob readcountJob = new ReadCountJob(pileupJob.getSingleOutputFile().getFilename(), workFlowParams.getSetting("FlowCellName"), i, Integer.parseInt(workFlowParams.getSetting("randomSubset")), 100);
				dax.addJob(readcountJob);
				dax.addChild(readcountJob.getID(), pileupJob.getID());
				dax.addChild(qcjob.getID(), readcountJob.getID());
				
				//create AlignFeaturejob, child of gzipped pileupJob
				AlignFeaturePileupJob alignpileup1 = new AlignFeaturePileupJob(pileupJob.getSingleOutputFile().getFilename(), workFlowParams.getSetting("FlowCellName"), i, "Ku2008-Ring1B", "/home/uec-00/shared/production/genomic-data-misc/" + "PcG_sites/Ku2008/hg18.ES.Ring1B.HMM.startsEnds.gff", 1000, 1, 0, 0, 1995);
				dax.addJob(alignpileup1);
				dax.addChild(alignpileup1.getID(), pileupJob.getID());
				
				
				//create AlignFeaturejob, child of gzipped pileupJob
				AlignFeaturePileupJob alignpileup2 = new AlignFeaturePileupJob(pileupJob.getSingleOutputFile().getFilename(), workFlowParams.getSetting("FlowCellName"), i, "Ku2008-H3K27", "/home/uec-00/shared/production/genomic-data-misc/" + "PcG_sites/Ku2008/hg18.ES.H3K27me3.HMM.startsEnds.gff", 1000, 1, 0, 0, 1995);
				dax.addJob(alignpileup2);
				dax.addChild(alignpileup2.getID(), pileupJob.getID());
				
				
				//create AlignFeaturejob, child of gzipped pileupJob
				AlignFeaturePileupJob alignpileup3 = new AlignFeaturePileupJob(pileupJob.getSingleOutputFile().getFilename(), workFlowParams.getSetting("FlowCellName"), i, "guelen2008-LADs", "/home/uec-00/shared/production/genomic-data-misc/" + "guelen2008-laminB1Lads.startsEnds.gff", 1000, 1, 0, 0, 1995);
				dax.addJob(alignpileup3);
				dax.addChild(alignpileup3.getID(), pileupJob.getID());
				
				
				//create AlignFeaturejob, child of gzipped pileupJob
				AlignFeaturePileupJob alignpileup4 = new AlignFeaturePileupJob(pileupJob.getSingleOutputFile().getFilename(), workFlowParams.getSetting("FlowCellName"), i, "kg-tssNoncgi", "/home/uec-00/shared/production/genomic-data-misc/" + "knownGene-tss.NO_overlap_tj_or_gg_cpgi.hg18.gtf", 1000, 1, 0, 0, 1995);
				dax.addJob(alignpileup4);
				dax.addChild(alignpileup4.getID(), pileupJob.getID());
				

				//create AlignFeaturejob, child of gzipped pileupJob
				AlignFeaturePileupJob alignpileup5 = new AlignFeaturePileupJob(pileupJob.getSingleOutputFile().getFilename(), workFlowParams.getSetting("FlowCellName"), i, "kg-tssCgi", "/home/uec-00/shared/production/genomic-data-misc/" + "knownGene-tss.overlap_tj_or_gg_cpgi.hg18.gtf", 1000, 1, 0, 0, 1995);
				dax.addJob(alignpileup5);
				dax.addChild(alignpileup5.getID(), pileupJob.getID());
				

				//create AlignFeaturejob, child of gzipped pileupJob
				AlignFeaturePileupJob alignpileup6 = new AlignFeaturePileupJob(pileupJob.getSingleOutputFile().getFilename(), workFlowParams.getSetting("FlowCellName"), i, "kg-exon", "/home/uec-00/shared/production/genomic-data-misc/" + "knownGene-exon.hg18.gtf", 1000, 1, 0, 0, 3995);
				dax.addJob(alignpileup6);
				dax.addChild(alignpileup6.getID(), pileupJob.getID());
				
				
				//create AlignFeaturejob, child of gzipped pileupJob
				AlignFeaturePileupJob alignpileup7 = new AlignFeaturePileupJob(pileupJob.getSingleOutputFile().getFilename(), workFlowParams.getSetting("FlowCellName"), i, "kim2007-ctcf", "/home/uec-00/shared/production/genomic-data-misc/" + "CTCF/Kim2007/ctcf.imr90.hg18.startsEnds.gff", 1000, 1, 0, 0, 1995);
				dax.addJob(alignpileup7);
				dax.addChild(alignpileup7.getID(), pileupJob.getID());
				
				
				//create AlignFeaturejob, child of gzipped pileupJob
				AlignFeaturePileupJob alignpileup8 = new AlignFeaturePileupJob(pileupJob.getSingleOutputFile().getFilename(), workFlowParams.getSetting("FlowCellName"), i, "RepeatMaskerLINE", "/home/uec-00/shared/production/genomic-data-misc/" + "repeats/DbRepeatMaskerLINE.hg18.startsEnds.gff", 1000, 1, 0, 0, 3995);
				dax.addJob(alignpileup8);
				dax.addChild(alignpileup8.getID(), pileupJob.getID());
				
				
				//create AlignFeaturejob, child of gzipped pileupJob
				AlignFeaturePileupJob alignpileup9 = new AlignFeaturePileupJob(pileupJob.getSingleOutputFile().getFilename(), workFlowParams.getSetting("FlowCellName"), i, "RepeatMaskerSINE", "/home/uec-00/shared/production/genomic-data-misc/" + "repeats/DbRepeatMaskerSINE.hg18.startsEnds.gff", 1000, 1, 0, 0, 3995);
				dax.addJob(alignpileup9);
				dax.addChild(alignpileup9.getID(), pileupJob.getID());
				

				//create AlignFeaturejob, child of gzipped pileupJob
				AlignFeaturePileupJob alignpileup10 = new AlignFeaturePileupJob(pileupJob.getSingleOutputFile().getFilename(), workFlowParams.getSetting("FlowCellName"), i, "TJGG-exonNoTss", "/home/uec-00/shared/production/genomic-data-misc/" + "CpG_islands/Takai_Jones_plus_GG.merged.exonOverlapNoPromoters.hg18.gtf", 1000, 1, 0, 0, 1995);
				dax.addJob(alignpileup10);
				dax.addChild(alignpileup10.getID(), pileupJob.getID());
				
				
				//pileup to wig job child of gzipped pileupjob
				PileupToWigJob pilewig = new PileupToWigJob(pileupJob.getSingleOutputFile().getFilename(), workFlowParams.getSetting("FlowCellName"), i, 600, 50, 1, 0, 2);
				dax.addJob(pilewig);
				dax.addChild(pilewig.getID(), pileupJob.getID());
				
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}	
	
	/**
	 * print the usage
	 */
	public static void usage()
	{
		System.out.println("Error: parameter file does not exist and no valid URL given");
		System.out.println("Usage: program [-dryrun] [-pbs] [workflowParameterFile.txt] [http://processURL]");
		System.out.println("workflowParameterFile.txt: contains all parameters");
		System.out.println("workflowURL.txt: contains all parameters");
		System.out.println("-pbs: operate in pbs mode");
		System.out.println("-dryrun: display pbs output, do not run");
		System.err.println("Either one of the arguements or both must be specified");
		System.exit(0);
	}
	/**
	 * @param args input parameter filename to use
	 */
	public static void main(String[] args)
	{
		String paramFile = "";
		String processURL = "";
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
			else if(s.contains("http://"))
				processURL = s;
			else
				usage();
		}
		
		ECWorkflowParams par = null;
		if(paramFile.length() > 0 && processURL.length() > 7)
			par = new ECWorkflowParams(new File(paramFile), processURL);
		else if(paramFile.length() > 0 && processURL.length() == 0)
			par = new ECWorkflowParams(new File(paramFile));
		else if(paramFile.length() == 0 && processURL.length() > 7)
			par = new ECWorkflowParams(processURL);
		else
			usage();
		
		ECDax dax = new ECDax(par);
		createWorkFlow(dax, pbsMode);
		dax.saveAsDot("alignpileup_dax.dot");
		dax.saveAsSimpleDot("alignpileup_dax_simple.dot");
		if(pbsMode)
			dax.runWorkflow(dryrun);
		dax.saveAsXML("alignpileup_dax.xml");		
	}

}
