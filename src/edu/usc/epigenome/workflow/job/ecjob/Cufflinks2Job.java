package edu.usc.epigenome.workflow.job.ecjob;

import java.io.File;

import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class Cufflinks2Job extends ECJob
{
	private String gtfFile;
	
	
	public String getGtfFile()
	{
		return gtfFile;
	}


	public Cufflinks2Job(String inputFile, String refFa, String refGTF) throws Exception
	{
		super(WorkflowConstants.NAMESPACE, "cufflinks2", WorkflowConstants.VERSION, "cufflinks2_" + new File(inputFile).getName());
		Filename input = new Filename(inputFile, LFN.INPUT);
		input.setRegister(false);
		this.addUses(input);

		String outgtfName = inputFile + ".cufflinks_transcripts.gtf";
		gtfFile = outgtfName;
		Filename outgtf = new Filename(outgtfName, LFN.OUTPUT);
		outgtf.setRegister(true);
		this.addUses(outgtf);
		
		String outTransName = inputFile + ".cufflinks_isoforms.expr";
		Filename outTrans = new Filename(outTransName, LFN.OUTPUT);
		outTrans.setRegister(true);
		this.addUses(outTrans);
		
		String outGenesName = inputFile + ".cufflinks_genes.expr";
		Filename outGenes = new Filename(outGenesName, LFN.OUTPUT);
		outGenes.setRegister(true);
		this.addUses(outGenes);
		
		
		
		// add the arguments to the job
		this.addArgument(new PseudoText(" -p 8 -N --no-faux-reads -u -b " + refFa + " "));
		if(refGTF != null)
			this.addArgument(new PseudoText(" -g " + refGTF + " "));
		this.addArgument(input);
	}
}
