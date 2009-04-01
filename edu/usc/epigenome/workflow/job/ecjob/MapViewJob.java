package edu.usc.epigenome.workflow.job.ecjob;

import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.ECJob;
import edu.usc.epigenome.workflow.DAX.WorkflowConstants;

public class MapViewJob extends ECJob
{

	public MapViewJob(String inputFileName, int minPileUpQ)
	{
		super(WorkflowConstants.NAMESPACE, "mapview", WorkflowConstants.VERSION, "mapview_" + inputFileName);
		// only one input file
		Filename input = new Filename(inputFileName, LFN.INPUT);
		input.setRegister(false);
		this.addUses(input);

		// construct the output filenames for job
		String outputFileName = inputFileName;
		outputFileName = outputFileName.replaceAll("^(.+?)(\\.\\w+)$", "$1\\." + "map.q" + minPileUpQ + ".txt");
		Filename outputFile = new Filename(outputFileName, LFN.OUTPUT);
		outputFile.setRegister(true);
		this.addUses(outputFile);

		// add the arguments to the job
		this.addArgument(outputFile);
		this.addArgument(new PseudoText(" "));
		this.addArgument(input);
		this.addArgument(new PseudoText(" " + minPileUpQ));
	}	
}
