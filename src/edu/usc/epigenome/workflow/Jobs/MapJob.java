package edu.usc.epigenome.workflow.Jobs;

import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.Job;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;

public class MapJob extends Job
{

	public MapJob(String inputFile, String referenceGenomeFile, int minMismatches, int firstReadLength, boolean methylationMode)
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
		this.addArgument(new PseudoText(" -n " + minMismatches));
		if (methylationMode)
		{
			this.addArgument(new PseudoText(" -M c "));
		}
		this.addArgument(new PseudoText(" -1 " + firstReadLength + " "));
		this.addArgument(output);
		this.addArgument(new PseudoText(" "));
		this.addArgument(new PseudoText(referenceGenomeFile));
		this.addArgument(new PseudoText(" "));
		this.addArgument(input);
	}

}
