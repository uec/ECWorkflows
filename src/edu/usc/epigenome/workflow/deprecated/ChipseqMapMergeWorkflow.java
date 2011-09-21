package edu.usc.epigenome.workflow.deprecated;

import java.io.File;
import java.util.ArrayList;

import org.griphyn.vdl.dax.Job;

import edu.usc.epigenome.workflow.DAX.ECDax;
import edu.usc.epigenome.workflow.ECWorkflowParams.specialized.GAParams;
import edu.usc.epigenome.workflow.job.ecjob.CountAdapterTrimJob;
import edu.usc.epigenome.workflow.job.ecjob.CountFastQJob;
import edu.usc.epigenome.workflow.job.ecjob.CountNmerJob;
import edu.usc.epigenome.workflow.job.ecjob.CountPileupJob;
import edu.usc.epigenome.workflow.job.ecjob.FindPeaksJob;
import edu.usc.epigenome.workflow.job.ecjob.MapJob;
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
	public static String WorkflowName = "chipseqmerge";
	
	public static void createWorkFlow(String sample, GAParams par,Boolean pbsMode, Boolean dryrun)	
	{
		try
		{
			
			// construct a dax object
			// For every requested lane in this flowcell..
			ECDax dax = new ECDax(par);
			
			//get the params so that we have the input parameters
			GAParams workFlowParams = (GAParams) dax.getWorkFlowParams();
			
			String sampleName = par.getSamples().get(sample).get("SampleID");
			String laneNumber = par.getSamples().get(sample).get("Lane");
			String fileInput = par.getSamples().get(sample).get("Input");
			String referenceGenome = par.getSamples().get(sample).get("Reference");
			String sampleWorkflow = par.getSamples().get(sample).get("Workflow");
			String label = workFlowParams.getSetting("FlowCellName") + "_" + laneNumber + "_" + sampleName;
			String[] laneInputFileList = fileInput.split(",");
			if(pbsMode = true)
			{
					for(int j = 0; j < laneInputFileList.length; j++)
					{
						laneInputFileList[j] = new File(laneInputFileList[j]).getAbsolutePath();
					}
					System.out.println("Creating ChipSeq Map-Merge Processing workflow. Group " + label);
			}
			
			ArrayList<String> inputMaps = new ArrayList<String>();
			for(String laneInput : laneInputFileList)
				inputMaps.add(laneInput);
			
			// for each lane create a map merge job
			MapMergeJob mapMergeJob = new MapMergeJob(inputMaps, workFlowParams.getSetting("FlowCellName") + "_" + Integer.parseInt(laneNumber));
			dax.addJob(mapMergeJob);
			
			//create qcmetrics job 
			QCMetricsJob qcjob = new QCMetricsJob(workFlowParams.getSetting("tmpDir") + "/" + workFlowParams.getSetting("FlowCellName") + "/" + label, workFlowParams.getSetting("FlowCellName"));
			dax.addJob(qcjob);
			
			
			//create pileup.gz job, child of mapmerge
			PileupJob pileupJob = new PileupJob(mapMergeJob.getSingleOutputFile().getFilename(), referenceGenome, Integer.parseInt(workFlowParams
					.getSetting("MaqPileupQ")));;
			dax.addJob(pileupJob);
			dax.addChild(pileupJob.getID(), mapMergeJob.getID());				
		
			//create maq2bamjob, child of mapMerge
			Maq2BamJob maq2bamJob = new Maq2BamJob(mapMergeJob.getSingleOutputFile().getFilename(),referenceGenome.replace(".bfa", ".fa"));
			dax.addJob(maq2bamJob);
			dax.addChild(maq2bamJob.getID(), mapMergeJob.getID());
			
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
			if(referenceGenome.contains("phi")) { genome = "phiX";}
			else if(referenceGenome.contains("hg18")) { genome = "hg18";}
			else if(referenceGenome.contains("hg19")) { genome = "hg19";}
			else if(referenceGenome.contains("tair8")) { genome = "tair8";}
			else if(referenceGenome.contains("sacCer")) { genome = "sacCer1";}
			else if(referenceGenome.contains("mm")) { genome = "mm9";}
			else {genome = "hg18";}
			
			//FINDPEAKs job, child of maq2bam
			FindPeaksJob findpeaks = new FindPeaksJob(maq2bamJob.getNodupsOutput(),200);
			dax.addJob(findpeaks);
			dax.addChild(findpeaks.getID(), maq2bamJob.getID());
			
			//wig to tdf (IGVTOOLS )job child of pileup to wig
			WigToTdfJob fpwigtotdf = new WigToTdfJob(findpeaks.getWigFile(),genome);
			dax.addJob(fpwigtotdf);
			dax.addChild(fpwigtotdf.getID(),findpeaks.getID());
			
			ReadDepthJob readdepthJob0 = new ReadDepthJob(pileupJob.getSingleOutputFile().getFilename(), workFlowParams.getSetting("FlowCellName"), Integer.parseInt(laneNumber),genome, 5000, 0);
			dax.addJob(readdepthJob0);
			dax.addChild(readdepthJob0.getID(), pileupJob.getID());
			//dax.addChild(qcjob.getID(), readdepthJob0.getID());

			//create readdepth:1, child of gzipped pileupJob
			ReadDepthJob readdepthJob1 = new ReadDepthJob(pileupJob.getSingleOutputFile().getFilename(), workFlowParams.getSetting("FlowCellName"), Integer.parseInt(laneNumber),genome, 5000, 1);
			dax.addJob(readdepthJob1); 
			dax.addChild(readdepthJob1.getID(), pileupJob.getID());
			//dax.addChild(qcjob.getID(), readdepthJob1.getID());
			
			//create readcount, child of gzipped pileupJob
			ReadCountJob readcountJob = new ReadCountJob(pileupJob.getSingleOutputFile().getFilename(), workFlowParams.getSetting("FlowCellName"), Integer.parseInt(laneNumber), Integer.parseInt(workFlowParams.getSetting("randomSubset")), 20);
			dax.addJob(readcountJob);
			dax.addChild(readcountJob.getID(), readdepthJob1.getID());
			dax.addChild(readcountJob.getID(), readdepthJob0.getID());
			dax.addChild(qcjob.getID(), readcountJob.getID());
			
			
			//pileup to wig job child of gzipped pileupjob
			PileupToWigJob pilewig = new PileupToWigJob(pileupJob.getSingleOutputFile().getFilename(), workFlowParams.getSetting("FlowCellName"), Integer.parseInt(laneNumber), Integer.parseInt(workFlowParams.getSetting("WigWindSize")), 50, 1, 0, 2);
			dax.addJob(pilewig);
			dax.addChild(pilewig.getID(), pileupJob.getID());
			
			//wig to tdf (IGVTOOLS )job child of pileup to wig
			WigToTdfJob wigtotdf = new WigToTdfJob(pilewig.getSingleOutputFile().getFilename(),genome);
			dax.addJob(wigtotdf);
			dax.addChild(wigtotdf.getID(),pilewig.getID());

			if(dax.getChildCount() > 0)
			{
				dax.saveAsDot("chipSeqMerge_dax.dot");
				dax.saveAsSimpleDot("chipSeqMerge_dax_simple.dot");
				if(pbsMode)
				{
					par.getWorkFlowArgsMap().put("WorkflowName", WorkflowName);
					dax.runWorkflow(dryrun);
				}
				dax.saveAsXML("chipSeqMerge_dax.xml");
			}
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}