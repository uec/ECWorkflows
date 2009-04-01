package edu.usc.epigenome.workflow.job.ecjob;

import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.ECJob;
import edu.usc.epigenome.workflow.DAX.WorkflowConstants;

public class CountPileupJob extends ECJob 
{

	public CountPileupJob(String inputFileName, String chartType) throws Exception
	{
		super(WorkflowConstants.NAMESPACE, "countpileup", WorkflowConstants.VERSION, "countpileup_" + inputFileName + chartType);
		// only one input file
		Filename input = new Filename(inputFileName, LFN.INPUT);
		input.setRegister(false);
		this.addUses(input);

		// only one output file
		// construct the output filenames for job
		String outputFileName;
		if(chartType == WorkflowConstants.Mononucleotide)
		{
			outputFileName = new String(inputFileName);
			outputFileName = outputFileName.replaceAll("^(.+?)(\\.\\w+)$", "$1\\_maq_mononucleotide" + ".csv");
			Filename outputmono = new Filename(outputFileName, LFN.OUTPUT);
			outputmono.setRegister(true);
			this.addUses(outputmono);
			
		}
		else if(chartType == WorkflowConstants.CGdinucleotide)
		{
			outputFileName = new String(inputFileName);
			outputFileName = outputFileName.replaceAll("^(.+?)(\\.\\w+)$", "$1\\_cg_dinucleotide" + ".csv");
			Filename outputcg = new Filename(outputFileName, LFN.OUTPUT);
			outputcg.setRegister(true);
			this.addUses(outputcg);
		}
		
		else if(chartType == WorkflowConstants.CHdinucleotide)
		{
			outputFileName = new String(inputFileName);
			outputFileName = outputFileName.replaceAll("^(.+?)(\\.\\w+)$", "$1\\_ch_dinucleotide" + ".csv");
			Filename outputch = new Filename(outputFileName, LFN.OUTPUT);
			outputch.setRegister(true);
			this.addUses(outputch);
		}
		else if(chartType == WorkflowConstants.RefComposition)
		{
			outputFileName = new String(inputFileName);
			outputFileName = outputFileName.replaceAll("^(.+?)(\\.\\w+)$", "$1\\_referenceGenome" + ".csv");
			Filename outputch = new Filename(outputFileName, LFN.OUTPUT);
			outputch.setRegister(true);
			this.addUses(outputch);
		}
		else
		{
			throw new Exception("unknown chart type requested");
		}
		//String prefix = new String(inputFileName);
		//prefix = prefix.replaceAll("^(.+?)(_s_\\d_\\.\\w+)$", "$1");

		// add the arguments to the job
		this.addArgument(new PseudoText( " edu.usc.epigenome.scripts.PileupToBaseComposition"));
		this.addArgument(new PseudoText(" "));
		this.addArgument(new PseudoText(chartType));
		this.addArgument(new PseudoText(" "));
		//this.addArgument(new PseudoText("-additionalDesc " + graphDesc));
		this.addArgument(new PseudoText(" -cycles -quals "));		
		this.addArgument(input);
		this.addArgument(new PseudoText(" >" + outputFileName));
	}
}
