package edu.usc.epigenome.workflow;

import java.io.File;
import java.util.EnumSet;

import edu.usc.epigenome.workflow.ECWorkflowParams.specialized.GAParams;
import edu.usc.epigenome.workflow.generator.BisQuickAlignmentWorkflow;
//import edu.usc.epigenome.workflow.deprecated.BasicAlignmentWorkflow;
//import edu.usc.epigenome.workflow.deprecated.ChipSeqWorkflow;
//import edu.usc.epigenome.workflow.deprecated.ChipseqMapMergeWorkflow;
//import edu.usc.epigenome.workflow.deprecated.MultiFileBSWorkflow;
//import edu.usc.epigenome.workflow.deprecated.SimpleBasicAlignmentWorkflow;
//import edu.usc.epigenome.workflow.deprecated.SimpleFastAlignmentWorkflow;
import edu.usc.epigenome.workflow.generator.MinimalBWAAligmentWorkflow;
import edu.usc.epigenome.workflow.generator.RNAseqV2Workflow;
import edu.usc.epigenome.workflow.generator.RegularBWAAlignmentWorkflow;
import edu.usc.epigenome.workflow.generator.BisulfiteAlignmentWorkflow;
import edu.usc.epigenome.workflow.generator.BisulfiteMergeWorkflow;
import edu.usc.epigenome.workflow.generator.ChipSeqBWAWorkflow;
import edu.usc.epigenome.workflow.generator.RNAseqDiffExpWorkflow;
import edu.usc.epigenome.workflow.generator.RNAseqWorkflow;
import edu.usc.epigenome.workflow.generator.UnalignedWorkflow;

public class SequencingPipeline
{

	public static void usage()
	{
		System.out.println("Error: parameter file does not exist");
		System.out.println("Usage: program [-dryrun] [-pbs] [workflowParameterFile.txt]");
		System.out.println("workflowParameterFile.txt: contains all parameters");		
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
		//Boolean dryrun = false;
		//Boolean pbsMode = false;
		final EnumSet<RunOptions> runOptions = EnumSet.noneOf(RunOptions.class);
		
		
		for(String s : args)
		{
			if(s.equals("-dryrun")) 
				runOptions.add(RunOptions.DRYRUN);
			else if(s.equals("-pbs")) 
				runOptions.add(RunOptions.PBSMODE);
			else if(s.equals("-linear")) 
				runOptions.add(RunOptions.SINGLEMACHINE);
			else if(new File(s).exists())
				paramFile = s;			
			else
			{
				usage();
				System.exit(0);
			}
		}
		
		GAParams par = null;
		par = new GAParams(new File(paramFile));
		
		for(String sampleEntryKey : par.getSamples().keySet() )
		{
			String workflow  = par.getSamples().get(sampleEntryKey).get("Workflow");
			
			if(workflow.toLowerCase().equals("regular")) 		RegularBWAAlignmentWorkflow.createWorkFlow(sampleEntryKey, par, runOptions);
			if(workflow.toLowerCase().equals("minimal")) 		MinimalBWAAligmentWorkflow.createWorkFlow(sampleEntryKey, par, runOptions);
			if(workflow.toLowerCase().equals("bisquick")) 		BisQuickAlignmentWorkflow.createWorkFlow(sampleEntryKey, par, runOptions);
			if(workflow.toLowerCase().contains("bisulfite"))	BisulfiteAlignmentWorkflow.createWorkFlow(sampleEntryKey, par, runOptions);
			if(workflow.toLowerCase().equals("chipseq")) 		ChipSeqBWAWorkflow.createWorkFlow(sampleEntryKey, par, runOptions);
			if(workflow.toLowerCase().equals("rnaseq")) 		RNAseqWorkflow.createWorkFlow(sampleEntryKey, par, runOptions);
			if(workflow.toLowerCase().equals("rnaseqv2")) 		RNAseqV2Workflow.createWorkFlow(sampleEntryKey, par, runOptions);
			if(workflow.toLowerCase().equals("unaligned")) 		UnalignedWorkflow.createWorkFlow(sampleEntryKey, par, runOptions);
			if(workflow.toLowerCase().contains("bismerge")) 		BisulfiteMergeWorkflow.createWorkFlow(sampleEntryKey, par, runOptions);
			//if(workflow.toLowerCase().equals("maqchipseq")) 	ChipSeqWorkflow.createWorkFlow(sampleEntryKey, par, pbsMode, dryrun);
			//if(workflow.toLowerCase().equals("chipseqmerge"))	ChipseqMapMergeWorkflow.createWorkFlow(sampleEntryKey, par, pbsMode, dryrun);
			//if(workflow.toLowerCase().equals("maqregular")) 		BasicAlignmentWorkflow.createWorkFlow(sampleEntryKey, par, pbsMode, dryrun);
			//if(workflow.toLowerCase().equals("fastbs")) 		MultiFileBSWorkflow.createWorkFlow(sampleEntryKey, par, pbsMode, dryrun);
			//if(workflow.toLowerCase().equals("simple")) 		SimpleBasicAlignmentWorkflow.createWorkFlow(sampleEntryKey, par, pbsMode, dryrun);
			//if(workflow.toLowerCase().equals("fast")) 			SimpleFastAlignmentWorkflow.createWorkFlow(sampleEntryKey, par, pbsMode, dryrun);
			
		}
		
		//one shot analysis ie do not do for each sample, do for all samples one time.
		for(String sampleEntryKey : par.getSamples().keySet() )
		{
			String workflow  = par.getSamples().get(sampleEntryKey).get("Workflow");
			if(workflow.toLowerCase().equals("rnaseqdiff"))
			{
				RNAseqDiffExpWorkflow.createWorkFlow(sampleEntryKey, par, runOptions);
				break;
			}
		}
	}

}
