package edu.usc.epigenome.workflow;

import java.io.File;
import java.util.ArrayList;

import edu.usc.epigenome.workflow.DAX.ECDax;
import edu.usc.epigenome.workflow.ECWorkflowParams.ECWorkflowParams;
import edu.usc.epigenome.workflow.job.ecjob.AlignFeaturePileupJob;
import edu.usc.epigenome.workflow.job.ecjob.CountPileupJob;
import edu.usc.epigenome.workflow.job.ecjob.MapMergeJob;
import edu.usc.epigenome.workflow.job.ecjob.MapViewJob;
import edu.usc.epigenome.workflow.job.ecjob.PileupJob;
import edu.usc.epigenome.workflow.job.ecjob.PileupToWigJob;
import edu.usc.epigenome.workflow.job.ecjob.ReadCountJob;
import edu.usc.epigenome.workflow.job.ecjob.ReadDepthJob;

public class ReportFromMaps
{
	public static void createWorkFlow(ECDax dax, Boolean pbsMode, ArrayList<File> mapFiles)	
	{
		try
		{
			// construct a dax object
			// For every requested lane in this flowcell..
			ECWorkflowParams workFlowParams = dax.getWorkFlowParams();
			if(!(workFlowParams.getWorkFlowArgsMap().containsKey("FlowCellName")))
				workFlowParams.getWorkFlowArgsMap().put("FlowCellName", new File(new File(".").getCanonicalPath()).getName());
			workFlowParams.saveAs("workflowParamsUsed.log.txt");	
			String genome = "hg18"; 
			
			ArrayList<String> fileNames = new ArrayList<String>();
			if(pbsMode = true)
			{
				for(File f : mapFiles)
					fileNames.add(f.getCanonicalPath());				
			}
			else
			{
				for(File f : mapFiles)
					fileNames.add(f.getName());
			}

			System.out.println("Creating merged reporting pipeline from " + fileNames.size() + " maps" );							
			
			//do map merge
			MapMergeJob mapmerge = new MapMergeJob(fileNames, workFlowParams.getSetting("FlowCellName"));
			dax.addJob(mapmerge);
			
			//do mapview
			MapViewJob mapViewJob =  new MapViewJob(mapmerge.getSingleOutputFile().getFilename(), Integer.parseInt(workFlowParams.getSetting("MaqPileupQ")));
			dax.addJob(mapViewJob);
			dax.addChild(mapViewJob.getID(), mapmerge.getID());
			
			//do pileup.gz			
			PileupJob pileup = new PileupJob(mapmerge.getSingleOutputFile().getFilename(), workFlowParams.getSetting("hgBFA"), Integer.parseInt(workFlowParams.getSetting("MaqPileupQ")));
			dax.addJob(pileup);
			dax.addChild(pileup.getID(), mapmerge.getID());
			String pilupFileName = pileup.getSingleOutputFile().getFilename();

			//create countPileupJob, 
			CountPileupJob countMonoPileupJob = new CountPileupJob(pilupFileName,CountPileupJob.Mononucleotide);
			dax.addJob(countMonoPileupJob);
			dax.addChild(countMonoPileupJob.getID(),pileup.getID());

			//create countPileupJob, 
			CountPileupJob countCGPileupJob = new CountPileupJob(pilupFileName,CountPileupJob.CGdinucleotide);
			dax.addJob(countCGPileupJob);
			dax.addChild(countCGPileupJob.getID(),pileup.getID());

			//create countPileupJob,
			CountPileupJob countCHPileupJob = new CountPileupJob(pilupFileName,CountPileupJob.CHdinucleotide);
			dax.addJob(countCHPileupJob);
			dax.addChild(countCHPileupJob.getID(),pileup.getID());

			//create countPileupJob, 
			CountPileupJob countGenomePileupJob = new CountPileupJob(pilupFileName,CountPileupJob.RefComposition);
			dax.addJob(countGenomePileupJob);
			dax.addChild(countGenomePileupJob.getID(),pileup.getID());

			//create readdepth,
			int i = 0;
			ReadDepthJob readdepthJob0 = new ReadDepthJob(pilupFileName, workFlowParams.getSetting("FlowCellName"), i,genome, 5000, 0);
			dax.addJob(readdepthJob0);
			dax.addChild(readdepthJob0.getID(),pileup.getID());

			ReadDepthJob readdepthJob1 = new ReadDepthJob(pilupFileName, workFlowParams.getSetting("FlowCellName"), i,genome, 5000, 1);
			dax.addJob(readdepthJob1);
			dax.addChild(readdepthJob1.getID(),pileup.getID());

			//create readcount,
			ReadCountJob readcountJob = new ReadCountJob(pilupFileName, workFlowParams.getSetting("FlowCellName"), i, Integer.parseInt(workFlowParams.getSetting("randomSubset")), 100);
			dax.addJob(readcountJob);
			dax.addChild(readcountJob.getID(),pileup.getID());

			//create AlignFeaturejob, child of gzipped pileupJob
			AlignFeaturePileupJob alignpileup1 = new AlignFeaturePileupJob(pilupFileName, workFlowParams.getSetting("FlowCellName"), i, "Ku2008-Ring1B", "/home/uec-00/shared/production/genomic-data-misc/" + "PcG_sites/Ku2008/hg18.ES.Ring1B.HMM.startsEnds.gff", 1000, 1, 0, 0, 1995);
			dax.addJob(alignpileup1);
			dax.addChild(alignpileup1.getID(),pileup.getID());

			//create AlignFeaturejob, child of gzipped pileupJob
			AlignFeaturePileupJob alignpileup2 = new AlignFeaturePileupJob(pilupFileName, workFlowParams.getSetting("FlowCellName"), i, "Ku2008-H3K27", "/home/uec-00/shared/production/genomic-data-misc/" + "PcG_sites/Ku2008/hg18.ES.H3K27me3.HMM.startsEnds.gff", 1000, 1, 0, 0, 1995);
			dax.addJob(alignpileup2);
			dax.addChild(alignpileup2.getID(),pileup.getID());

			//create AlignFeaturejob, child of gzipped pileupJob
			AlignFeaturePileupJob alignpileup3 = new AlignFeaturePileupJob(pilupFileName, workFlowParams.getSetting("FlowCellName"), i, "guelen2008-LADs", "/home/uec-00/shared/production/genomic-data-misc/" + "guelen2008-laminB1Lads.startsEnds.gff", 1000, 1, 0, 0, 1995);
			dax.addJob(alignpileup3);
			dax.addChild(alignpileup3.getID(),pileup.getID());

			//create AlignFeaturejob, child of gzipped pileupJob
			AlignFeaturePileupJob alignpileup4 = new AlignFeaturePileupJob(pilupFileName, workFlowParams.getSetting("FlowCellName"), i, "kg-tssNoncgi", "/home/uec-00/shared/production/genomic-data-misc/" + "knownGene-tss.NO_overlap_tj_or_gg_cpgi.hg18.gtf", 1000, 1, 0, 0, 1995);
			dax.addJob(alignpileup4);
			dax.addChild(alignpileup4.getID(),pileup.getID());

			//create AlignFeaturejob, child of gzipped pileupJob
			AlignFeaturePileupJob alignpileup5 = new AlignFeaturePileupJob(pilupFileName, workFlowParams.getSetting("FlowCellName"), i, "kg-tssCgi", "/home/uec-00/shared/production/genomic-data-misc/" + "knownGene-tss.overlap_tj_or_gg_cpgi.hg18.gtf", 1000, 1, 0, 0, 1995);
			dax.addJob(alignpileup5);
			dax.addChild(alignpileup5.getID(),pileup.getID());

			//create AlignFeaturejob, child of gzipped pileupJob
			AlignFeaturePileupJob alignpileup6 = new AlignFeaturePileupJob(pilupFileName, workFlowParams.getSetting("FlowCellName"), i, "kg-exon", "/home/uec-00/shared/production/genomic-data-misc/" + "knownGene-exon.hg18.gtf", 1000, 1, 0, 0, 3995);
			dax.addJob(alignpileup6);
			dax.addChild(alignpileup6.getID(),pileup.getID());

			//create AlignFeaturejob, child of gzipped pileupJob
			AlignFeaturePileupJob alignpileup7 = new AlignFeaturePileupJob(pilupFileName, workFlowParams.getSetting("FlowCellName"), i, "kim2007-ctcf", "/home/uec-00/shared/production/genomic-data-misc/" + "CTCF/Kim2007/ctcf.imr90.hg18.startsEnds.gff", 1000, 1, 0, 0, 1995);
			dax.addJob(alignpileup7);
			dax.addChild(alignpileup7.getID(),pileup.getID());

			//create AlignFeaturejob, child of gzipped pileupJob
			AlignFeaturePileupJob alignpileup8 = new AlignFeaturePileupJob(pilupFileName, workFlowParams.getSetting("FlowCellName"), i, "RepeatMaskerLINE", "/home/uec-00/shared/production/genomic-data-misc/" + "repeats/DbRepeatMaskerLINE.hg18.startsEnds.gff", 1000, 1, 0, 0, 3995);
			dax.addJob(alignpileup8);
			dax.addChild(alignpileup8.getID(),pileup.getID());

			//create AlignFeaturejob, child of gzipped pileupJob
			AlignFeaturePileupJob alignpileup9 = new AlignFeaturePileupJob(pilupFileName, workFlowParams.getSetting("FlowCellName"), i, "RepeatMaskerSINE", "/home/uec-00/shared/production/genomic-data-misc/" + "repeats/DbRepeatMaskerSINE.hg18.startsEnds.gff", 1000, 1, 0, 0, 3995);
			dax.addJob(alignpileup9);
			dax.addChild(alignpileup9.getID(),pileup.getID());

			//create AlignFeaturejob, child of gzipped pileupJob
			AlignFeaturePileupJob alignpileup10 = new AlignFeaturePileupJob(pilupFileName, workFlowParams.getSetting("FlowCellName"), i, "TJGG-exonNoTss", "/home/uec-00/shared/production/genomic-data-misc/" + "CpG_islands/Takai_Jones_plus_GG.merged.exonOverlapNoPromoters.hg18.gtf", 1000, 1, 0, 0, 1995);
			dax.addJob(alignpileup10);
			dax.addChild(alignpileup10.getID(),pileup.getID());

			//pileup to wig job child of gzipped pileupjob
			PileupToWigJob pilewig = new PileupToWigJob(pilupFileName, workFlowParams.getSetting("FlowCellName"), i, 600, 50, 1, 0, 2);
			dax.addJob(pilewig);
			dax.addChild(pilewig.getID(),pileup.getID());


		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}	
	
	public static void usage()
	{
		System.out.println("Error: parameter file does not exist and no valid URL given");
		System.out.println("Usage: program [-dryrun] [-pbs] [workflowParameterFile.txt] [http://processURL] file1.map file2.map...");
		System.out.println("workflowParameterFile.txt: contains all parameters");
		System.out.println("workflowURL.txt: contains all parameters");
		System.out.println("-pbs: operate in pbs mode");
		System.out.println("-dryrun: display pbs output, do not run");		
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
		ArrayList<File> fileList = new ArrayList<File>();
		
		for(String s : args)
		{
			if(s.equals("-dryrun")) 
				dryrun = true;
			else if(s.equals("-pbs")) 
				pbsMode = true;
			else if(s.contains(".txt") && new File(s).exists())
				paramFile = s;
			else if(s.contains(".map") && new File(s).exists())
				fileList.add(new File(s));
			else if(s.contains("http://"))
				processURL = s;
			else
			{
				System.err.println("Not Found: " + s);
				usage();
			}
		}
		
		ECWorkflowParams par = null;
		
		if(paramFile.length() > 0 && processURL.length() > 7)
			par = new ECWorkflowParams(new File(paramFile), processURL);
		
		else if(paramFile.length() > 0 && processURL.length() == 0)
			par = new ECWorkflowParams(new File(paramFile));
		
		else if(paramFile.length() == 0 && processURL.length() > 7)
			par = new ECWorkflowParams(processURL);
		
		else if(paramFile.length() == 0 && processURL.length() == 0)
			par = new ECWorkflowParams();
		
		else if(fileList.size() < 1)
		{
			System.err.println("Map files not specified");
			usage();
		}
		else
			usage();
		
		ECDax dax = new ECDax(par);
		
		createWorkFlow(dax, pbsMode, fileList);
		dax.saveAsDot("reportMap_dax.dot");
		dax.saveAsSimpleDot("reportMap_dax_simple.dot");
		if(pbsMode)
			dax.runWorkflow(dryrun);
		dax.saveAsXML("reportMap_dax.xml");		
	}

}
