package edu.usc.epigenome.workflow.job.ecjob;

import java.io.File;

import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class CountPileupJob extends ECJob 
{
	public static final String Mononucleotide = "";
	public static final String CGdinucleotide = "-cgonly";
	public static final String CHdinucleotide = "-chonly";
	public static final String RefComposition = "-refComposition";
	
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
		outputFileName = new String(new File(inputFileName).getName());
		if(chartType == CountPileupJob.Mononucleotide)
		{
			outputFileName = outputFileName.replaceAll("^(.+?)(\\.\\w+)$", "$1\\_maq_mononucleotide" + ".csv");
			Filename outputmono = new Filename(outputFileName, LFN.OUTPUT);
			outputmono.setRegister(true);
			this.addUses(outputmono);
			
		}
		else if(chartType == CountPileupJob.CGdinucleotide)
		{			
			outputFileName = outputFileName.replaceAll("^(.+?)(\\.\\w+)$", "$1\\_cg_dinucleotide" + ".csv");
			Filename outputcg = new Filename(outputFileName, LFN.OUTPUT);
			outputcg.setRegister(true);
			this.addUses(outputcg);
		}
		
		else if(chartType == CountPileupJob.CHdinucleotide)
		{			
			outputFileName = outputFileName.replaceAll("^(.+?)(\\.\\w+)$", "$1\\_ch_dinucleotide" + ".csv");
			Filename outputch = new Filename(outputFileName, LFN.OUTPUT);
			outputch.setRegister(true);
			this.addUses(outputch);
		}
		else if(chartType == CountPileupJob.RefComposition)
		{			
			outputFileName = outputFileName.replaceAll("^(.+?)(\\.\\w+)$", "$1\\_referenceGenome" + ".csv");
			Filename outputch = new Filename(outputFileName, LFN.OUTPUT);
			outputch.setRegister(true);
			this.addUses(outputch);
		}
		else
		{
			throw new Exception("unknown chart type requested");
		}
		
		// add the arguments to the job
		this.addArgument(new PseudoText(outputFileName));
		this.addArgument(new PseudoText( " java edu.usc.epigenome.scripts.PileupToBaseComposition"));
		this.addArgument(new PseudoText(" "));
		this.addArgument(new PseudoText(chartType));
		this.addArgument(new PseudoText(" "));
		//this.addArgument(new PseudoText("-additionalDesc " + graphDesc));
		this.addArgument(new PseudoText(" -cycles -quals "));		
		this.addArgument(input);
		
	}
}
