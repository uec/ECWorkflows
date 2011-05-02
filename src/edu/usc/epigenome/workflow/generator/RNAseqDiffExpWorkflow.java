package edu.usc.epigenome.workflow.generator;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import edu.usc.epigenome.workflow.DAX.ECDax;
import edu.usc.epigenome.workflow.ECWorkflowParams.specialized.GAParams;
import edu.usc.epigenome.workflow.job.ecjob.CuffCompareJob;
import edu.usc.epigenome.workflow.job.ecjob.CuffDiffJob;

public class RNAseqDiffExpWorkflow
{
	public static String WorkflowName = "rnaseqdiff";
	
	public static void createWorkFlow(String sample, GAParams par,Boolean pbsMode, Boolean dryrun)	
	{
		try
		{
			// construct a dax object
			// For every requested lane in this flowcell..
			ECDax dax = new ECDax(par);
			
			//get the params so that we have the input parameters
			GAParams workFlowParams = (GAParams) dax.getWorkFlowParams();
			
			
			String label = workFlowParams.getSetting("FlowCellName") + "_rnaseqdiff_"; 
			
			HashMap<String,String> sampleBams = new HashMap<String,String>();
			HashMap<String,String> sampleGTFs = new HashMap<String,String>();
			//HashMap<String,String> lastJobId = new HashMap<String,String>(); 
			
			for(String sampleEntryKey : par.getSamples().keySet() )
			{
				String workflow  = par.getSamples().get(sampleEntryKey).get("Workflow");
				if(workflow.toLowerCase().equals("rnaseqdiff"))
				{
					String sampleNum = sampleEntryKey.split("\\.")[1];
					label += sampleNum + "";
					for(String inputFile : par.getSamples().get(sampleEntryKey).get("Input").split(",")) 
					{
						if(inputFile.toLowerCase().endsWith("gtf"))
							sampleGTFs.put(sampleNum, pbsMode ? new File(inputFile).getAbsolutePath() : new File(inputFile).getName());
						if(inputFile.toLowerCase().endsWith("bam"))
							sampleBams.put(sampleNum, pbsMode ? new File(inputFile).getAbsolutePath() : new File(inputFile).getName());
					}
				}
			}
			
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
					String lastLane = "";
					
					for(int k = 0;k<rnadiffparam.length;k++)
					{
						ArrayList<String> diffSample = new ArrayList<String>();
						for(String m : rnadiffparam[k].split(","))
						{
							diffSample.add(sampleBams.get(m));
							analysisGTFs.add(sampleGTFs.get(m));
							lastLane = m;
						}
						analysisBams.add(diffSample);
					}
					
					//create cuffcompare job from ref gene gtf and sample gtfs
					CuffCompareJob cuffcompare = new CuffCompareJob(workFlowParams.getSetting("refGene"),analysisGTFs, prefix + "_" + new File(workFlowParams.getSetting("refGene")).getName(),workFlowParams.getSetting("Sample." + lastLane + ".Reference") + "split");
					dax.addJob(cuffcompare);
					
					
					//create a cuffdiff job from cuffcompare output and sample bams
					CuffDiffJob cuffdiff = new CuffDiffJob(cuffcompare.getOutputGTF(), analysisBams,prefix,rnadiffkey.toLowerCase().contains("time"),workFlowParams.getSetting("Sample." + lastLane + ".Reference") + ".fa" );
					dax.addJob(cuffdiff);
					dax.addChild(cuffdiff.getID(),cuffcompare.getID());
				}
			}
			if(dax.getChildCount() > 0)
			{
				dax.saveAsDot("rnaseqdiffexp_dax.dot");
				dax.saveAsSimpleDot("rnaseqdiffexp_dax_simple.dot");
				if(pbsMode)
				{
					par.getWorkFlowArgsMap().put("WorkflowName", label);
					dax.runWorkflow(dryrun);
				}
				dax.saveAsXML("rnaseqdiffexp_dax.xml");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}

