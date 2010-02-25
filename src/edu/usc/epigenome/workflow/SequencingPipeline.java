package edu.usc.epigenome.workflow;

import java.io.File;
import edu.usc.epigenome.workflow.ECWorkflowParams.specialized.GAParams;
import edu.usc.epigenome.workflow.generator.BasicAlignmentWorkflow;
import edu.usc.epigenome.workflow.generator.BisulfiteAlignmentWorkflow;
import edu.usc.epigenome.workflow.generator.ChipSeqWorkflow;

public class SequencingPipeline
{

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
		

		BasicAlignmentWorkflow.createWorkFlow(par, pbsMode, dryrun);
		BisulfiteAlignmentWorkflow.createWorkFlow(par, pbsMode, dryrun);
		ChipSeqWorkflow.createWorkFlow(par, pbsMode, dryrun);
	}

}
