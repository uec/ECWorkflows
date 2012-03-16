package edu.usc.epigenome.workflow.job.ecjob;
import java.io.File;

import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;


public class BamCPGCoverageJob extends ECJob
{
	public BamCPGCoverageJob(String inputBamName, String inputBaiName, String Bed, String outputFileName)
	{
		super(WorkflowConstants.NAMESPACE, "cpgcov", WorkflowConstants.VERSION, "cpgcov" + inputBamName);
		
		Filename inputBam = new Filename(inputBamName, LFN.INPUT);
		inputBam.setRegister(false);
		this.addUses(inputBam);
		
		Filename inputBai = new Filename(inputBaiName, LFN.INPUT);
		inputBai.setRegister(false);
		this.addUses(inputBai);
				
		Filename output = new Filename(outputFileName, LFN.OUTPUT);
		output.setRegister(false);
		this.addUses(output);
		
		this.addArgument(new PseudoText(" "));
		this.addArgument(inputBam);
		this.addArgument(new PseudoText(" "));
		this.addArgument(new PseudoText(Bed));
		this.addArgument(new PseudoText(" "));
		this.addArgument(output);
	}
}
