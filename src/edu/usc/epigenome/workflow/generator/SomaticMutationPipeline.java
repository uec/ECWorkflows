package edu.usc.epigenome.workflow.generator;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;

import edu.usc.epigenome.workflow.RunOptions;
import edu.usc.epigenome.workflow.DAX.ECDax;
import edu.usc.epigenome.workflow.ECWorkflowParams.specialized.GAParams;
import edu.usc.epigenome.workflow.job.ecjob.MutectJob;

public class SomaticMutationPipeline
{
	public static String WorkflowName = "somatic";
	
	public static void createWorkFlow(String sample, GAParams par, EnumSet<RunOptions> runOptions)	
	{
		try
		{
			// construct a dax object
			// For every requested lane in this flowcell..
			ECDax dax = new ECDax(par);
			
			//get the params so that we have the input parameters
			GAParams workFlowParams = (GAParams) dax.getWorkFlowParams();
			
			
			String label = workFlowParams.getSetting("FlowCellName") + "_somaticMutation"; 
			String genome = "/home/uec-00/shared/production/genomes/encode_hg19_mf/male.hg19.fa";
			
			ArrayList<String> normalBams = new ArrayList<String>();
			ArrayList<String> tumorBams = new ArrayList<String>();
			String ids = "Somatic";
			 
			for(String sampleEntryKey : par.getSamples().keySet() )
			{
				ids+=par.getSamples().get(sampleEntryKey).get("SampleID") + "_";
				
				genome = par.getSamples().get(sampleEntryKey).get("Reference");
				if(!genome.endsWith(".fa"))
					genome += ".fa";
				String workflow  = par.getSamples().get(sampleEntryKey).get("Workflow");
				String inputFile =  par.getSamples().get(sampleEntryKey).get("Input");
				if(workflow.toLowerCase().equals("somaticnormal"))
					normalBams.add( new File(inputFile).getAbsolutePath());
				if(workflow.toLowerCase().equals("somatictumor"))
					tumorBams.add( new File(inputFile).getAbsolutePath());
			}
			
			System.out.println("Creating Somatic Mutation calling pipeline for " + label);
			
			//create a cuffdiff job from cuffcompare output and sample bams
			MutectJob mutect = new MutectJob(normalBams, tumorBams, genome, ids);
			dax.addJob(mutect);
			
			
			
			
			
			
			if(dax.getChildCount() >= 0)
			{
				dax.saveAsDot("somatic_dax.dot");
				dax.saveAsSimpleDot("somatic_dax_simple.dot");
				par.getWorkFlowArgsMap().put("WorkflowName", label);
				dax.runWorkflow(runOptions);
				dax.saveAsXML("somatic_dax.xml");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}

