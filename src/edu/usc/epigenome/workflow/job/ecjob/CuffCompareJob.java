package edu.usc.epigenome.workflow.job.ecjob;

import java.io.File;
import java.util.ArrayList;

import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class CuffCompareJob extends ECJob
{
	String outputGTF = "";
	public String getOutputGTF()
	{
		return outputGTF;
	}
	
	public CuffCompareJob(String refgtf, ArrayList<String> samplegtfs, String outputPrefix, String refFaContigsDir) throws Exception
	{
		super(WorkflowConstants.NAMESPACE, "cuffcompare", WorkflowConstants.VERSION, "cuffcompare_" + outputPrefix + new File(refgtf).getName());
		
		String[] outputFileNames = {"loci", "combined.gtf", "tracking"};
	
		for(String s : samplegtfs)
		{
			Filename input = new Filename(s, LFN.INPUT);
			input.setRegister(false);
			this.addUses(input);
		}
		outputGTF = outputPrefix.replaceAll("\\.gtf$", "") + ".combined.gtf";
		
		Filename refGTFfile = new Filename(refgtf, LFN.INPUT);
		refGTFfile.setRegister(false);
		this.addUses(refGTFfile);
		
		for(String outputFileName : outputFileNames)
		{
			Filename outFiles = new Filename(outputPrefix.replaceAll("\\.gtf$", "") + "." + outputFileName, LFN.OUTPUT);
			outFiles.setRegister(true);
			this.addUses(outFiles);
		}
		
		
		// add the arguments to the job
		this.addArgument(new PseudoText(" -o " + outputPrefix + " "));
		this.addArgument(new PseudoText(" -r "));
		this.addArgument(refGTFfile);
		this.addArgument(new PseudoText(" -s " + refFaContigsDir));
		this.addArgument(new PseudoText(" "));
		for(String s  : samplegtfs)
		{
			this.addArgument(new PseudoText(s));
			this.addArgument(new PseudoText(" "));
		}
	}
}
