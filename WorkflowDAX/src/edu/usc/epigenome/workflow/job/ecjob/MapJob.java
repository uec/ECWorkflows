package edu.usc.epigenome.workflow.job.ecjob;

import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class MapJob extends ECJob
{
	//SR MAPPING (with and without specifying output file)
	public MapJob(String inputFile, String referenceGenomeFile, int minMismatches, String alignType, int trim1, int trim2)
	{
		super(WorkflowConstants.NAMESPACE, "map", WorkflowConstants.VERSION, "map_" + inputFile);
		buildMapJob(inputFile, referenceGenomeFile, minMismatches, alignType, trim1, trim2);
	}
	public MapJob(String inputFile, String referenceGenomeFile, int minMismatches, String alignType, int trim1, int trim2, String outputFile)
	{
		super(WorkflowConstants.NAMESPACE, "map", WorkflowConstants.VERSION, "map_" + inputFile + outputFile);
		buildMapJob(inputFile, outputFile, referenceGenomeFile, minMismatches, alignType, trim1, trim2);
	}

	//PAIRED END MAPPING (with and without specifying output file)
	public MapJob(String inputFileR1, String inputFileR2, String referenceGenomeFile, int minMismatches, String alignType, int trim1, int trim2)
	{
		super(WorkflowConstants.NAMESPACE, "map", WorkflowConstants.VERSION, "map_" + inputFileR1);
		buildMapJob(inputFileR1, referenceGenomeFile, minMismatches, alignType, trim1, trim2);		
		Filename inputR2 = new Filename(inputFileR2, LFN.INPUT);
		inputR2.setRegister(false);
		this.addUses(inputR2);
		
		this.addArgument(new PseudoText(" "));
		this.addArgument(inputR2);
	}
	
	public MapJob(String inputFileR1, String inputFileR2, String referenceGenomeFile, int minMismatches, String alignType, int trim1, int trim2, String outputFile)
	{
		super(WorkflowConstants.NAMESPACE, "map", WorkflowConstants.VERSION, "map_" + inputFileR1 + outputFile);
		buildMapJob(inputFileR1, outputFile, referenceGenomeFile, minMismatches, alignType, trim1, trim2);		
		Filename inputR2 = new Filename(inputFileR2, LFN.INPUT);
		inputR2.setRegister(false);
		this.addUses(inputR2);
		
		this.addArgument(new PseudoText(" "));
		this.addArgument(inputR2);
	}
	
	private void buildMapJob(String inputFile, String outputFile, String referenceGenomeFile, int minMismatches, String alignType, int trim1, int trim2)
	{
		Filename input = new Filename(inputFile, LFN.INPUT);
		input.setRegister(false);
		this.addUses(input);

		// construct the output filenames for job
		Filename output = new Filename(outputFile, LFN.OUTPUT);
		output.setRegister(false);
		this.addUses(output);

		// add the arguments to the job
		if (alignType.toLowerCase().contentEquals("bisulfite"))
			this.addArgument(new PseudoText(" -M c"));
		else if (alignType.toLowerCase().contentEquals("bisulfiterev"))
			this.addArgument(new PseudoText(" -M g"));
		
		if(trim1 > 0)
			this.addArgument(new PseudoText(" -1 " + trim1 + " "));
		if(trim2 > 0)
			this.addArgument(new PseudoText(" -2 " + trim2 + " "));
		
		this.addArgument(new PseudoText(" -n " + minMismatches + " "));
		this.addArgument(output);
		this.addArgument(new PseudoText(" "));
		this.addArgument(new PseudoText(referenceGenomeFile));
		this.addArgument(new PseudoText(" "));
		this.addArgument(input);
	}
	
	private void buildMapJob(String inputFile, String referenceGenomeFile, int minMismatches, String alignType, int trim1, int trim2)
	{
		String outputFile = new String(inputFile);
		outputFile = outputFile.replaceAll("^(.+?)(\\.\\w+)$", "$1\\." + "map");
		buildMapJob(inputFile, outputFile, referenceGenomeFile, minMismatches, alignType, trim1, trim2);
	}
}
