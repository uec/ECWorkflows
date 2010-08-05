package edu.usc.epigenome.workflow.generator;

import java.io.File;
import java.util.ArrayList;
import edu.usc.epigenome.workflow.DAX.ECDax;
import edu.usc.epigenome.workflow.ECWorkflowParams.specialized.GAParams;
import edu.usc.epigenome.workflow.job.ecjob.CountPileupJob;
import edu.usc.epigenome.workflow.job.ecjob.FindPeaksJob;
import edu.usc.epigenome.workflow.job.ecjob.MapMergeJob;
import edu.usc.epigenome.workflow.job.ecjob.Maq2BamJob;
import edu.usc.epigenome.workflow.job.ecjob.PileupJob;
import edu.usc.epigenome.workflow.job.ecjob.PileupToWigJob;
import edu.usc.epigenome.workflow.job.ecjob.QCMetricsJob;
import edu.usc.epigenome.workflow.job.ecjob.ReadCountJob;
import edu.usc.epigenome.workflow.job.ecjob.ReadDepthJob;
import edu.usc.epigenome.workflow.job.ecjob.WigToTdfJob;


public class ChipseqMapMergeWorkflow
{
	/**
	 * Creates an AlignPileUp workflow from an empty dax object 
	 * @param dax The ECDAX to which processing jobs will be added
	 */	
	public static void createWorkFlow(GAParams par, Boolean pbsMode, Boolean dryrun)	
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
				if(workFlowParams.getSetting("Lane." + i + ".AlignmentType").toLowerCase().equals("chipseqmerge"))
				{
				
					String[] laneInputFileList = workFlowParams.getLaneInput(i).split(",");
					if(pbsMode = true)
					{
							for(int j = 0; j < laneInputFileList.length; j++)
							{
								laneInputFileList[j] = new File(laneInputFileList[j]).getAbsolutePath();
							}
							System.out.println("Creating ChipSeq Map-Merge Processing workflow. Group " + i + ": " + workFlowParams.getLaneInput(i));
					}
					
					ArrayList<String> inputMaps = new ArrayList<String>();
					for(String laneInput : laneInputFileList)
						inputMaps.add(laneInput);
					
					// for each lane create a map merge job
					MapMergeJob mapMergeJob = new MapMergeJob(inputMaps, workFlowParams.getSetting("FlowCellName") + "_" + i);
					dax.addJob(mapMergeJob);
					
					
					//create qcmetrics job 
					QCMetricsJob qcjob = new QCMetricsJob(workFlowParams.getSetting("tmpDir") + "/" + workFlowParams.getSetting("FlowCellName"), workFlowParams.getSetting("FlowCellName"));
					dax.addJob(qcjob);
	
	
					//create pileup.gz job, child of mapmerge
					PileupJob pileupJob = new PileupJob(mapMergeJob.getSingleOutputFile().getFilename(), workFlowParams.getSetting("Lane." + i +".ReferenceBFA"), Integer.parseInt(workFlowParams
							.getSetting("MaqPileupQ")));;
					dax.addJob(pileupJob);
					dax.addChild(pileupJob.getID(), mapMergeJob.getID());				
					
					//create maq2bamjob, child of mapMerge
					Maq2BamJob maq2bamJob = new Maq2BamJob(mapMergeJob.getSingleOutputFile().getFilename(),workFlowParams.getSetting("Lane." + i +".ReferenceBFA").replace(".bfa", ".fa"));
					dax.addJob(maq2bamJob);
					dax.addChild(maq2bamJob.getID(), mapMergeJob.getID());
					
					//FINDPEAKs job, child of maq2bam
					FindPeaksJob findpeaks = new FindPeaksJob(maq2bamJob.getNodupsOutput(),200);
					dax.addJob(findpeaks);
					dax.addChild(findpeaks.getID(), maq2bamJob.getID());
					
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
					else if(workFlowParams.getSetting("Lane." + i + ".ReferenceBFA").contains("hg19")) { genome = "hg19";}
					else if(workFlowParams.getSetting("Lane." + i + ".ReferenceBFA").contains("tair8")) { genome = "tair8";}
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
					ReadCountJob readcountJob = new ReadCountJob(pileupJob.getSingleOutputFile().getFilename(), workFlowParams.getSetting("FlowCellName"), i, Integer.parseInt(workFlowParams.getSetting("randomSubset")), 20);
					dax.addJob(readcountJob);
					dax.addChild(readcountJob.getID(), pileupJob.getID());
					dax.addChild(qcjob.getID(), readcountJob.getID());
					
//					//create AlignFeaturejob, child of gzipped pileupJob
//					AlignFeaturePileupJob alignpileup1 = new AlignFeaturePileupJob(pileupJob.getSingleOutputFile().getFilename(), workFlowParams.getSetting("FlowCellName"), i, "Ku2008-Ring1B", "/home/uec-00/shared/production/genomic-data-misc/" + "PcG_sites/Ku2008/hg18.ES.Ring1B.HMM.startsEnds.gff", 1000, 1, 0, 0, 1995);
//					dax.addJob(alignpileup1);
//					dax.addChild(alignpileup1.getID(), pileupJob.getID());
//					
//					
//					//create AlignFeaturejob, child of gzipped pileupJob
//					AlignFeaturePileupJob alignpileup2 = new AlignFeaturePileupJob(pileupJob.getSingleOutputFile().getFilename(), workFlowParams.getSetting("FlowCellName"), i, "Ku2008-H3K27", "/home/uec-00/shared/production/genomic-data-misc/" + "PcG_sites/Ku2008/hg18.ES.H3K27me3.HMM.startsEnds.gff", 1000, 1, 0, 0, 1995);
//					dax.addJob(alignpileup2);
//					dax.addChild(alignpileup2.getID(), pileupJob.getID());
//					
//					
//					//create AlignFeaturejob, child of gzipped pileupJob
//					AlignFeaturePileupJob alignpileup3 = new AlignFeaturePileupJob(pileupJob.getSingleOutputFile().getFilename(), workFlowParams.getSetting("FlowCellName"), i, "guelen2008-LADs", "/home/uec-00/shared/production/genomic-data-misc/" + "guelen2008-laminB1Lads.startsEnds.gff", 1000, 1, 0, 0, 1995);
//					dax.addJob(alignpileup3);
//					dax.addChild(alignpileup3.getID(), pileupJob.getID());
//					
//					
//					//create AlignFeaturejob, child of gzipped pileupJob
//					AlignFeaturePileupJob alignpileup4 = new AlignFeaturePileupJob(pileupJob.getSingleOutputFile().getFilename(), workFlowParams.getSetting("FlowCellName"), i, "kg-tssNoncgi", "/home/uec-00/shared/production/genomic-data-misc/" + "knownGene-tss.NO_overlap_tj_or_gg_cpgi.hg18.gtf", 1000, 1, 0, 0, 1995);
//					dax.addJob(alignpileup4);
//					dax.addChild(alignpileup4.getID(), pileupJob.getID());
//					
//	
//					//create AlignFeaturejob, child of gzipped pileupJob
//					AlignFeaturePileupJob alignpileup5 = new AlignFeaturePileupJob(pileupJob.getSingleOutputFile().getFilename(), workFlowParams.getSetting("FlowCellName"), i, "kg-tssCgi", "/home/uec-00/shared/production/genomic-data-misc/" + "knownGene-tss.overlap_tj_or_gg_cpgi.hg18.gtf", 1000, 1, 0, 0, 1995);
//					dax.addJob(alignpileup5);
//					dax.addChild(alignpileup5.getID(), pileupJob.getID());
//					
//	
//					//create AlignFeaturejob, child of gzipped pileupJob
//					AlignFeaturePileupJob alignpileup6 = new AlignFeaturePileupJob(pileupJob.getSingleOutputFile().getFilename(), workFlowParams.getSetting("FlowCellName"), i, "kg-exon", "/home/uec-00/shared/production/genomic-data-misc/" + "knownGene-exon.hg18.gtf", 1000, 1, 0, 0, 3995);
//					dax.addJob(alignpileup6);
//					dax.addChild(alignpileup6.getID(), pileupJob.getID());
//					
//					
//					//create AlignFeaturejob, child of gzipped pileupJob
//					AlignFeaturePileupJob alignpileup7 = new AlignFeaturePileupJob(pileupJob.getSingleOutputFile().getFilename(), workFlowParams.getSetting("FlowCellName"), i, "kim2007-ctcf", "/home/uec-00/shared/production/genomic-data-misc/" + "CTCF/Kim2007/ctcf.imr90.hg18.startsEnds.gff", 1000, 1, 0, 0, 1995);
//					dax.addJob(alignpileup7);
//					dax.addChild(alignpileup7.getID(), pileupJob.getID());
//					
//					
//					//create AlignFeaturejob, child of gzipped pileupJob
//					AlignFeaturePileupJob alignpileup8 = new AlignFeaturePileupJob(pileupJob.getSingleOutputFile().getFilename(), workFlowParams.getSetting("FlowCellName"), i, "RepeatMaskerLINE", "/home/uec-00/shared/production/genomic-data-misc/" + "repeats/DbRepeatMaskerLINE.hg18.startsEnds.gff", 1000, 1, 0, 0, 3995);
//					dax.addJob(alignpileup8);
//					dax.addChild(alignpileup8.getID(), pileupJob.getID());
//					
//					
//					//create AlignFeaturejob, child of gzipped pileupJob
//					AlignFeaturePileupJob alignpileup9 = new AlignFeaturePileupJob(pileupJob.getSingleOutputFile().getFilename(), workFlowParams.getSetting("FlowCellName"), i, "RepeatMaskerSINE", "/home/uec-00/shared/production/genomic-data-misc/" + "repeats/DbRepeatMaskerSINE.hg18.startsEnds.gff", 1000, 1, 0, 0, 3995);
//					dax.addJob(alignpileup9);
//					dax.addChild(alignpileup9.getID(), pileupJob.getID());
//					
//	
//					//create AlignFeaturejob, child of gzipped pileupJob
//					AlignFeaturePileupJob alignpileup10 = new AlignFeaturePileupJob(pileupJob.getSingleOutputFile().getFilename(), workFlowParams.getSetting("FlowCellName"), i, "TJGG-exonNoTss", "/home/uec-00/shared/production/genomic-data-misc/" + "CpG_islands/Takai_Jones_plus_GG.merged.exonOverlapNoPromoters.hg18.gtf", 1000, 1, 0, 0, 1995);
//					dax.addJob(alignpileup10);
//					dax.addChild(alignpileup10.getID(), pileupJob.getID());
					
					
					//pileup to wig job child of gzipped pileupjob
					PileupToWigJob pilewig = new PileupToWigJob(pileupJob.getSingleOutputFile().getFilename(), workFlowParams.getSetting("FlowCellName"), i, Integer.parseInt(workFlowParams.getSetting("WigWindSize")), 50, 1, 0, 2);
					dax.addJob(pilewig);
					dax.addChild(pilewig.getID(), pileupJob.getID());
					
					//wig to tdf (IGVTOOLS )job child of pileup to wig
					WigToTdfJob wigtotdf = new WigToTdfJob(pilewig.getSingleOutputFile().getFilename(),genome);
					dax.addJob(wigtotdf);
					dax.addChild(wigtotdf.getID(),pilewig.getID());
					
				}
			}
			if(dax.getChildCount() > 0)
			{
				dax.saveAsDot("chipSeqMerge_dax.dot");
				dax.saveAsSimpleDot("chipSeqMerge_dax_simple.dot");
				if(pbsMode)
					dax.runWorkflow(dryrun);
				dax.saveAsXML("chipSeqMerge_dax.xml");
			}
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}