package edu.usc.epigenome.workflow.job.ecjob;

import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class MethFindChip extends ECJob
{

	public MethFindChip(String barcode)
	{
		super(WorkflowConstants.NAMESPACE, "methfindchip", WorkflowConstants.VERSION, "methfindchip_" + barcode);
		String outputFileName = new String("barcode_" + barcode);
		Filename output = new Filename(outputFileName, LFN.OUTPUT);
		output.setRegister(true);
		this.addUses(output);
		this.addArgument(new PseudoText(barcode));		
	}
}