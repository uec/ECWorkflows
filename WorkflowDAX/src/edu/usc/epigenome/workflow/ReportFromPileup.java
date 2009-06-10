package edu.usc.epigenome.workflow;

import java.io.File;
import edu.usc.epigenome.workflow.DAX.ECDax;
import edu.usc.epigenome.workflow.ECWorkflowParams.ECWorkflowParams;
import edu.usc.epigenome.workflow.job.ecjob.AlignFeaturePileupJob;
import edu.usc.epigenome.workflow.job.ecjob.CountPileupJob;
import edu.usc.epigenome.workflow.job.ecjob.PileupToWigJob;
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
				
				if(!(laneInputFileName.contains("up.gz")))
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
				
				//create AlignFeaturejob, child of gzipped pileupJob
				AlignFeaturePileupJob alignpileup1 = new AlignFeaturePileupJob(laneInputFileName, workFlowParams.getSetting("FlowCellName"), i, "Ku2008-Ring1B", "/home/rcf-40/bberman/storage/genomic-data-misc/" + "PcG_sites/Ku2008/hg18.ES.Ring1B.HMM.startsEnds.gff", 1000, 1, 0, 0, 1995);
				dax.addJob(alignpileup1);
								
				//create AlignFeaturejob, child of gzipped pileupJob
				AlignFeaturePileupJob alignpileup2 = new AlignFeaturePileupJob(laneInputFileName, workFlowParams.getSetting("FlowCellName"), i, "Ku2008-H3K27", "/home/rcf-40/bberman/storage/genomic-data-misc/" + "PcG_sites/Ku2008/hg18.ES.H3K27me3.HMM.startsEnds.gff", 1000, 1, 0, 0, 1995);
				dax.addJob(alignpileup2);
				
				//create AlignFeaturejob, child of gzipped pileupJob
				AlignFeaturePileupJob alignpileup3 = new AlignFeaturePileupJob(laneInputFileName, workFlowParams.getSetting("FlowCellName"), i, "guelen2008-LADs", "/home/rcf-40/bberman/storage/genomic-data-misc/" + "guelen2008-laminB1Lads.startsEnds.gff", 1000, 1, 0, 0, 1995);
				dax.addJob(alignpileup3);
				
				//create AlignFeaturejob, child of gzipped pileupJob
				AlignFeaturePileupJob alignpileup4 = new AlignFeaturePileupJob(laneInputFileName, workFlowParams.getSetting("FlowCellName"), i, "kg-tssNoncgi", "/home/rcf-40/bberman/storage/genomic-data-misc/" + "knownGene-tss.NO_overlap_tj_or_gg_cpgi.hg18.gtf", 1000, 1, 0, 0, 1995);
				dax.addJob(alignpileup4);
				
				//create AlignFeaturejob, child of gzipped pileupJob
				AlignFeaturePileupJob alignpileup5 = new AlignFeaturePileupJob(laneInputFileName, workFlowParams.getSetting("FlowCellName"), i, "kg-tssCgi", "/home/rcf-40/bberman/storage/genomic-data-misc/" + "knownGene-tss.overlap_tj_or_gg_cpgi.hg18.gtf", 1000, 1, 0, 0, 1995);
				dax.addJob(alignpileup5);
				
				//create AlignFeaturejob, child of gzipped pileupJob
				AlignFeaturePileupJob alignpileup6 = new AlignFeaturePileupJob(laneInputFileName, workFlowParams.getSetting("FlowCellName"), i, "kg-exon", "/home/rcf-40/bberman/storage/genomic-data-misc/" + "knownGene-exon.hg18.gtf", 1000, 1, 0, 0, 3995);
				dax.addJob(alignpileup6);
				
				//create AlignFeaturejob, child of gzipped pileupJob
				AlignFeaturePileupJob alignpileup7 = new AlignFeaturePileupJob(laneInputFileName, workFlowParams.getSetting("FlowCellName"), i, "kim2007-ctcf", "/home/rcf-40/bberman/storage/genomic-data-misc/" + "CTCF/Kim2007/ctcf.imr90.hg18.startsEnds.gff", 1000, 1, 0, 0, 1995);
				dax.addJob(alignpileup7);
				
				//create AlignFeaturejob, child of gzipped pileupJob
				AlignFeaturePileupJob alignpileup8 = new AlignFeaturePileupJob(laneInputFileName, workFlowParams.getSetting("FlowCellName"), i, "RepeatMaskerLINE", "/home/rcf-40/bberman/storage/genomic-data-misc/" + "repeats/DbRepeatMaskerLINE.hg18.startsEnds.gff", 1000, 1, 0, 0, 3995);
				dax.addJob(alignpileup8);
				
				//create AlignFeaturejob, child of gzipped pileupJob
				AlignFeaturePileupJob alignpileup9 = new AlignFeaturePileupJob(laneInputFileName, workFlowParams.getSetting("FlowCellName"), i, "RepeatMaskerSINE", "/home/rcf-40/bberman/storage/genomic-data-misc/" + "repeats/DbRepeatMaskerSINE.hg18.startsEnds.gff", 1000, 1, 0, 0, 3995);
				dax.addJob(alignpileup9);
				
				//create AlignFeaturejob, child of gzipped pileupJob
				AlignFeaturePileupJob alignpileup10 = new AlignFeaturePileupJob(laneInputFileName, workFlowParams.getSetting("FlowCellName"), i, "TJGG-exonNoTss", "/home/rcf-40/bberman/storage/genomic-data-misc/" + "CpG_islands/Takai_Jones_plus_GG.merged.exonOverlapNoPromoters.hg18.gtf", 1000, 1, 0, 0, 1995);
				dax.addJob(alignpileup10);
				
				//pileup to wig job child of gzipped pileupjob
				PileupToWigJob pilewig = new PileupToWigJob(laneInputFileName, workFlowParams.getSetting("FlowCellName"), i, 600, 50, 1, 0, 2);
				dax.addJob(pilewig);
				
				
				
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}	
	
	public static void usage()
	{
		System.out.println("Error: parameter file does not exist");
		System.out.println("Usage: program [-dryrun] [-pbs] workflowParameterFile.txt");
		System.out.println("workflowParameterFile.txt: contains all parameters");
		System.out.println("-pbs: operate in pbs mode");
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
