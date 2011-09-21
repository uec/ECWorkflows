package edu.usc.epigenome.workflow.deprecated;

import java.util.ArrayList;
import edu.usc.epigenome.workflow.DAX.ECDax;
import edu.usc.epigenome.workflow.ECWorkflowParams.specialized.MethParams;

public class MethWorkflow
{
	public static String WorkflowName = "methylation";
	
	public static void createWorkFlow(ECDax dax, Boolean pbsMode, ArrayList<String> barCodeList)	
	{
		try
		{
			// construct a dax object
			//MethParams workFlowParams = (MethParams) dax.getWorkFlowParams();
			System.out.println("Creating merged reporting pipeline from " + barCodeList.size() + " chips" );
			for(String s : barCodeList)
			{
				System.out.println(s);
			}
			
	

		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}	
	
	public static void usage()
	{
		System.out.println("Usage: program [-dryrun] [-pbs] barcode1 barcode2 barcodeN");
		System.out.println("-pbs: operate in pbs mode");
		System.out.println("-dryrun: display pbs output, do not run");		
		System.exit(0);
	}
	/**
	 * @param args input parameter filename to use
	 */
	public static void main(String[] args)
	{
		Boolean dryrun = false;
		Boolean pbsMode = false;
		ArrayList<String> barCodeList = new ArrayList<String>();
		
		for(String s : args)
		{
			if(s.equals("-dryrun")) 
				dryrun = true;
			else if(s.equals("-pbs")) 
				pbsMode = true;
			else
			{
				barCodeList.add(s);
			}			
		}
		
		MethParams par = new MethParams();
		
		if(barCodeList.size() < 1)
		{
			System.err.println("No barcode numbers specified");
			usage();
		}
		else
			usage();
		
		ECDax dax = new ECDax(par);
		
		createWorkFlow(dax, pbsMode, barCodeList);
		dax.saveAsDot("report_meth_dax.dot");
		dax.saveAsSimpleDot("report_meth_dax_simple.dot");
		if(pbsMode)
		{
			par.getWorkFlowArgsMap().put("WorkflowName", WorkflowName);
			dax.runWorkflow(dryrun);
		}
		dax.saveAsXML("report_meth_dax.xml");		
	}

}
