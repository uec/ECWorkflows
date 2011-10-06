package edu.usc.epigenome.workflow.job.ecjob;

import java.io.File;

import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class OrgContamCheckJob extends ECJob
{

	public OrgContamCheckJob(String inputFile, long readstoSample, String[] genomes)
	{
		super(WorkflowConstants.NAMESPACE, "orgcontamcheck", WorkflowConstants.VERSION, "orgCheck_" + inputFile + readstoSample);
		// construct the input filename for job
		Filename input = new Filename(inputFile, LFN.INPUT);
		input.setRegister(false);
		this.addUses(input);

		// construct the output filenames for job
		for(String s : genomes)
		{
			String orgToCheckBaseName = new File(s).getName();
			String sangerOutputFileName = new String("ContamCheck." + orgToCheckBaseName + "." + readstoSample + "." + new File(inputFile).getName() +  ".flagstat.txt");
			Filename output = new Filename(sangerOutputFileName, LFN.OUTPUT);
			output.setRegister(false);
			this.addUses(output);
		}
		

		// add the arguments to the job
		this.addArgument(input);
		this.addArgument(new PseudoText(" " + readstoSample + " "));
		for(String s : genomes)
		{
			this.addArgument(new PseudoText(" " + s + " "));
		}
		
	}
}
