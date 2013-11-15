package edu.usc.epigenome.workflow.deprecated;
import java.io.File;
import edu.usc.epigenome.workflow.DAX.ECDax;
import edu.usc.epigenome.workflow.ECWorkflowParams.specialized.GAParams;

public class BasecallingWorkflow
{

	public static void createWorkFlow(ECDax dax, String paramFilePathName)	
	{
		try
		{
			//Construct a dax, starting from solexa output
			GAParams workFlowParams = (GAParams) dax.getWorkFlowParams();
			
			//run bustard
			//first get genomes for each lane
			String elandGenomes[] = new String[8];
			int genomeIndex = 0;
			for(String key : workFlowParams.getWorkFlowArgsMap().keySet())
			{
				if(key.matches("Lane\\.\\d+\\.Eland\\.Genome"))
					elandGenomes[genomeIndex++] = key + "=" + workFlowParams.getSetting(key);				
			}
			RemoteBustardJob bustard = new RemoteBustardJob(workFlowParams.getSetting("FlowCellName"), elandGenomes, workFlowParams.getSetting("referenceLane"), workFlowParams.getSetting("tmpDir"),System.getProperty("user.name"), workFlowParams.getSetting("Eland.username"), workFlowParams.getSetting("Eland.webdir"));			
			dax.addJob(bustard);
			String[] seqFileNames = new String[workFlowParams.getAvailableLanes().length];
			int seqFNIndex =0;
			for (int i : workFlowParams.getAvailableLanes())
			{
				seqFileNames[seqFNIndex++] = workFlowParams.getLaneInput(i);
			}
			GenECDaxJob genECDax = new GenECDaxJob(paramFilePathName, seqFileNames);
			dax.addJob(genECDax);
			dax.addChild(genECDax.getID(), bustard.getID());
			
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}	
	
	public static void usage()
	{
		System.out.println("Usage: program [-dryrun] workflowParameterFile.txt");
		System.out.println("workflowParameterFile.txt: contains all parameters");
		System.out.println("-dryrun: display pbs output, do not run");
		System.exit(0);
	}
	public static void main(String[] args)
	{
		String paramFile = "";
		Boolean dryrun = false;
		//create a dax from the passed in param obj
		if(args.length == 1)
		{
			if((new File(args[0])).exists())
				paramFile = args[0];
			else
				usage();
		}
		else if(args.length==2)
		{
			if(args[0].equals("-dryrun") && (new File(args[1])).exists())
			{
				paramFile = args[1];
				dryrun = true;
			}
			else
				usage();
		}
		else
			usage();
				
		ECDax dax = new ECDax(new GAParams(paramFile));
		createWorkFlow(dax, new File(paramFile).getAbsolutePath());
		dax.saveAsDot("bustard_dax.dot");
		dax.runWorkflow(dryrun);
		dax.saveAsXML("bustard_dax.xml");
	}

}
