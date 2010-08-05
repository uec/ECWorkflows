package edu.usc.epigenome.workflow.deprecated;

import java.io.File;
import edu.usc.epigenome.workflow.DAX.ECDax;
import edu.usc.epigenome.workflow.ECWorkflowParams.specialized.GAParams;
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
			GAParams workFlowParams = (GAParams) dax.getWorkFlowParams();
			workFlowParams.saveAs("workflowParamsUsed.log.txt");	
			for (int i : workFlowParams.getAvailableLanes())
			{				
				String laneInputFileName;
				if(pbsMode = true)
					laneInputFileName = new File(workFlowParams.getLaneInput(i)).getAbsolutePath();
				else
					laneInputFileName = new File(workFlowParams.getLaneInput(i)).getName();
				
				if(laneInputFileName.contains("up.gz"))
				{
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
					if(workFlowParams.getSetting("Lane." + i + ".ReferenceBFA").contains("phi")) { genome = "phiX";}
					else if(workFlowParams.getSetting("Lane." + i + ".ReferenceBFA").contains("hg18")) { genome = "hg18";}
					else if(workFlowParams.getSetting("Lane." + i + ".ReferenceBFA").contains("sacCer")) { genome = "sacCer1";}
					else if(workFlowParams.getSetting("Lane." + i + ".ReferenceBFA").contains("mm")) { genome = "mm9";}
					else {genome = "hg18";}
					
					
					ReadDepthJob readdepthJob0 = new ReadDepthJob(laneInputFileName, workFlowParams.getSetting("FlowCellName"), i,genome, 5000, 0);
					dax.addJob(readdepthJob0);
					
					ReadDepthJob readdepthJob1 = new ReadDepthJob(laneInputFileName, workFlowParams.getSetting("FlowCellName"), i,genome, 5000, 1);
					dax.addJob(readdepthJob1);
					
					//create readcount,
					ReadCountJob readcountJob = new ReadCountJob(laneInputFileName, workFlowParams.getSetting("FlowCellName"), i, Integer.parseInt(workFlowParams.getSetting("randomSubset")), 100);
					dax.addJob(readcountJob);
					
					//create AlignFeaturejob, child of gzipped pileupJob
					AlignFeaturePileupJob alignpileup1 = new AlignFeaturePileupJob(laneInputFileName, workFlowParams.getSetting("FlowCellName"), i, "Ku2008-Ring1B", "/home/uec-00/shared/production/genomic-data-misc/" + "PcG_sites/Ku2008/hg18.ES.Ring1B.HMM.startsEnds.gff", 1000, 1, 0, 0, 1995);
					dax.addJob(alignpileup1);
									
					//create AlignFeaturejob, child of gzipped pileupJob
					AlignFeaturePileupJob alignpileup2 = new AlignFeaturePileupJob(laneInputFileName, workFlowParams.getSetting("FlowCellName"), i, "Ku2008-H3K27", "/home/uec-00/shared/production/genomic-data-misc/" + "PcG_sites/Ku2008/hg18.ES.H3K27me3.HMM.startsEnds.gff", 1000, 1, 0, 0, 1995);
					dax.addJob(alignpileup2);
					
					//create AlignFeaturejob, child of gzipped pileupJob
					AlignFeaturePileupJob alignpileup3 = new AlignFeaturePileupJob(laneInputFileName, workFlowParams.getSetting("FlowCellName"), i, "guelen2008-LADs", "/home/uec-00/shared/production/genomic-data-misc/" + "guelen2008-laminB1Lads.startsEnds.gff", 1000, 1, 0, 0, 1995);
					dax.addJob(alignpileup3);
					
					//create AlignFeaturejob, child of gzipped pileupJob
					AlignFeaturePileupJob alignpileup4 = new AlignFeaturePileupJob(laneInputFileName, workFlowParams.getSetting("FlowCellName"), i, "kg-tssNoncgi", "/home/uec-00/shared/production/genomic-data-misc/" + "knownGene-tss.NO_overlap_tj_or_gg_cpgi.hg18.gtf", 1000, 1, 0, 0, 1995);
					dax.addJob(alignpileup4);
					
					//create AlignFeaturejob, child of gzipped pileupJob
					AlignFeaturePileupJob alignpileup5 = new AlignFeaturePileupJob(laneInputFileName, workFlowParams.getSetting("FlowCellName"), i, "kg-tssCgi", "/home/uec-00/shared/production/genomic-data-misc/" + "knownGene-tss.overlap_tj_or_gg_cpgi.hg18.gtf", 1000, 1, 0, 0, 1995);
					dax.addJob(alignpileup5);
					
					//create AlignFeaturejob, child of gzipped pileupJob
					AlignFeaturePileupJob alignpileup6 = new AlignFeaturePileupJob(laneInputFileName, workFlowParams.getSetting("FlowCellName"), i, "kg-exon", "/home/uec-00/shared/production/genomic-data-misc/" + "knownGene-exon.hg18.gtf", 1000, 1, 0, 0, 3995);
					dax.addJob(alignpileup6);
					
					//create AlignFeaturejob, child of gzipped pileupJob
					AlignFeaturePileupJob alignpileup7 = new AlignFeaturePileupJob(laneInputFileName, workFlowParams.getSetting("FlowCellName"), i, "kim2007-ctcf", "/home/uec-00/shared/production/genomic-data-misc/" + "CTCF/Kim2007/ctcf.imr90.hg18.startsEnds.gff", 1000, 1, 0, 0, 1995);
					dax.addJob(alignpileup7);
					
					//create AlignFeaturejob, child of gzipped pileupJob
					AlignFeaturePileupJob alignpileup8 = new AlignFeaturePileupJob(laneInputFileName, workFlowParams.getSetting("FlowCellName"), i, "RepeatMaskerLINE", "/home/uec-00/shared/production/genomic-data-misc/" + "repeats/DbRepeatMaskerLINE.hg18.startsEnds.gff", 1000, 1, 0, 0, 3995);
					dax.addJob(alignpileup8);
					
					//create AlignFeaturejob, child of gzipped pileupJob
					AlignFeaturePileupJob alignpileup9 = new AlignFeaturePileupJob(laneInputFileName, workFlowParams.getSetting("FlowCellName"), i, "RepeatMaskerSINE", "/home/uec-00/shared/production/genomic-data-misc/" + "repeats/DbRepeatMaskerSINE.hg18.startsEnds.gff", 1000, 1, 0, 0, 3995);
					dax.addJob(alignpileup9);
					
					//create AlignFeaturejob, child of gzipped pileupJob
					AlignFeaturePileupJob alignpileup10 = new AlignFeaturePileupJob(laneInputFileName, workFlowParams.getSetting("FlowCellName"), i, "TJGG-exonNoTss", "/home/uec-00/shared/production/genomic-data-misc/" + "CpG_islands/Takai_Jones_plus_GG.merged.exonOverlapNoPromoters.hg18.gtf", 1000, 1, 0, 0, 1995);
					dax.addJob(alignpileup10);
					
					//pileup to wig job child of gzipped pileupjob
					PileupToWigJob pilewig = new PileupToWigJob(laneInputFileName, workFlowParams.getSetting("FlowCellName"), i, 600, 50, 1, 0, 2);
					dax.addJob(pilewig);
				}
				else
				{
					System.err.println("expected pileup.gz file as input for lane " + i +", skipping, File=" + laneInputFileName);
				}
				
				
				
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}	
	
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
		
		GAParams par = null;
		if(paramFile.length() > 0 && processURL.length() > 7)
			par = new GAParams(new File(paramFile), processURL);
		else if(paramFile.length() > 0 && processURL.length() == 0)
			par = new GAParams(new File(paramFile));
		else if(paramFile.length() == 0 && processURL.length() > 7)
			par = new GAParams(processURL);
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
