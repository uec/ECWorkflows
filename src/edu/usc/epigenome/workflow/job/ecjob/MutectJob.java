package edu.usc.epigenome.workflow.job.ecjob;

import java.io.File;
import java.util.List;

import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class MutectJob extends ECJob
{
	
	public MutectJob(List<String> normalBams,List<String> tumorBams, String genome, String outputPrefix) throws Exception
	{
		super(WorkflowConstants.NAMESPACE, "mutect", WorkflowConstants.VERSION, "mutect_" + new File(tumorBams.get(0)).getName());
	
		String files ="";
		
		for(String s : normalBams)
		{
			Filename input = new Filename(s, LFN.INPUT);
			input.setRegister(false);
			this.addUses(input);
			files += "-n " + s + " ";
		}
		
		for(String s : tumorBams)
		{
			Filename input = new Filename(s, LFN.INPUT);
			input.setRegister(false);
			this.addUses(input);
			files += "-t " + s + " ";
		}
		
		String[] outputFileNames = {".mutect_stats.txt", ".mutect_coverage.wig"};
		for(String outputFileName : outputFileNames)
		{
			Filename outFiles = new Filename(outputPrefix + outputFileName, LFN.OUTPUT);
			outFiles.setRegister(true);
			this.addUses(outFiles);
		}
		
		// add the arguments to the job
		this.addArgument(new PseudoText(" -r " + genome + " "));
		this.addArgument(new PseudoText(files));
		this.addArgument(new PseudoText(" -o " + outputPrefix));		
	}
}
