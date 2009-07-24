package edu.usc.epigenome.workflow.job.ecjob;

import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class MapJob extends ECJob
{

	public MapJob(String inputFile, String referenceGenomeFile, int minMismatches, boolean isBisulfite, int trim1, int trim2)
	{
		super(WorkflowConstants.NAMESPACE, "map", WorkflowConstants.VERSION, "map_" + inputFile);
		Filename input = new Filename(inputFile, LFN.INPUT);
		input.setRegister(false);
		this.addUses(input);

		//Filename referenceGenome = new Filename(referenceGenomeFile, LFN.INPUT);
		//input.setRegister(false);
		//job.addUses(referenceGenome);

		// construct the output filenames for job
		String outputFile = new String(inputFile);
		outputFile = outputFile.replaceAll("^(.+?)(\\.\\w+)$", "$1\\." + "map");
		Filename output = new Filename(outputFile, LFN.OUTPUT);
		output.setRegister(false);
		// output.setType(LFN.OUTPUT);
		this.addUses(output);

		// add the arguments to the job
		// job.addArgument(new PseudoText("map "));
		if (isBisulfite)
		{
			this.addArgument(new PseudoText(" -M c"));
		}
		
		if(trim1 > 0)
		{
			this.addArgument(new PseudoText(" -1 " + trim1 + " "));
		}
		if(trim2 > 0)
		{
			this.addArgument(new PseudoText(" -2 " + trim2 + " "));
		}
		
		this.addArgument(new PseudoText(" -n " + minMismatches + " "));
		this.addArgument(output);
		this.addArgument(new PseudoText(" "));
		this.addArgument(new PseudoText(referenceGenomeFile));
		this.addArgument(new PseudoText(" "));
		this.addArgument(input);
	}

}
