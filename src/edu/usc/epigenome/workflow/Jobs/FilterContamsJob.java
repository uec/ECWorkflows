package edu.usc.epigenome.workflow.Jobs;

import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.Job;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;

public class FilterContamsJob extends Job
{
	public FilterContamsJob(String inputFile)
	{
		super(WorkflowConstants.NAMESPACE, "filterContams", WorkflowConstants.VERSION, "filterContams_" + inputFile);
		Filename input = new Filename(inputFile, LFN.INPUT);
		input.setRegister(false);
		this.addUses(input);

		// construct the output filenames for job
		String noContamOutputFileName = new String(inputFile);
		noContamOutputFileName = noContamOutputFileName.replaceAll("^(.+?)(\\.\\w+)$", "$1\\." + "nocontam" + "$2");
		Filename noContam = new Filename(noContamOutputFileName, LFN.OUTPUT);
		noContam.setRegister(false);
		this.addUses(noContam);

		String contamPolyaOutputFileName = new String(inputFile);
		contamPolyaOutputFileName = contamPolyaOutputFileName.replaceAll("^(.+?)(\\.\\w+)$", "$1\\." + "contam.polya" + "$2");
		//Filename contamPolya = new Filename(contamPolyaOutputFileName, LFN.OUTPUT);
		// contamPolya.setRegister(false);
		// output.setType(LFN.OUTPUT);
		// job.addUses(contamPolya);

		String contamAdapterOutputFileName = new String(inputFile);
		contamAdapterOutputFileName = contamAdapterOutputFileName.replaceAll("^(.+?)(\\.\\w+)$", "$1\\." + "contam.adapters" + "$2");
		//Filename contamAdapter = new Filename(contamAdapterOutputFileName, LFN.OUTPUT);
		// contamAdapter.setRegister(false);
		// output.setType(LFN.OUTPUT);
		// job.addUses(contamAdapter);

		// add the arguments to the job
		this.addArgument(input);
	}
}
