package edu.usc.epigenome.workflow.job.ecjob;

import java.io.File;

import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;
import java.util.ArrayList;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class CuffDiffJob extends ECJob
{
	public CuffDiffJob(String gtfFilename,ArrayList<ArrayList<String>> sampleAlns, String outputPrefix, Boolean isTimeSeries, String refFa) throws Exception
	{
		super(WorkflowConstants.NAMESPACE, "cuffdiff", WorkflowConstants.VERSION, "cuffdiff_" + new File(gtfFilename).getName());
		
		String[] outputFileNames = {"isoforms.fpkm_tracking", "genes.fpkm_tracking", "cds.fpkm_tracking","tss_groups.fpkm_tracking", "isoform_exp.diff","gene_exp.diff","tss_group_exp.diff","cds_exp.fpkm_tracking","splicing.diff","cds.diff","cds_exp.diff","promoters.diff"};
	
		for(ArrayList<String> i : sampleAlns)
			for(String j : i)
			{
				Filename input = new Filename(j, LFN.INPUT);
				input.setRegister(false);
				this.addUses(input);
			}
		
		Filename gtfFile = new Filename(gtfFilename, LFN.INPUT);
		gtfFile.setRegister(false);
		this.addUses(gtfFile);
		
		for(String outputFileName : outputFileNames)
		{
			Filename outFiles = new Filename(outputPrefix + ".cuffdiff_" + outputFileName, LFN.OUTPUT);
			outFiles.setRegister(true);
			this.addUses(outFiles);
		}
		
		
		// add the arguments to the job
		this.addArgument(new PseudoText(outputPrefix + " "));
		this.addArgument(new PseudoText("-p 8 -b " + refFa + " "));
		if(isTimeSeries)
			this.addArgument(new PseudoText(" -T "));
		this.addArgument(gtfFile);
		this.addArgument(new PseudoText(" "));
		for(ArrayList<String> i : sampleAlns)
		{
			for(String j : i)
			{
				this.addArgument(new PseudoText(j));
				if(i.size() > 1)
					this.addArgument(new PseudoText(","));
			}
			this.addArgument(new PseudoText(" "));
		}
	}
}
