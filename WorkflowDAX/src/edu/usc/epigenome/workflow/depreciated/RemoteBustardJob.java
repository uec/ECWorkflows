package edu.usc.epigenome.workflow.depreciated;

import org.griphyn.vdl.dax.PseudoText;

import edu.usc.epigenome.workflow.DAX.WorkflowConstants;
import edu.usc.epigenome.workflow.job.ECJob;

public class RemoteBustardJob extends ECJob
{
	public RemoteBustardJob(String flowcell, String[] genomes, String referenceLane, String copyBackTmpDir, String copyBackUsername, String copyToUsername, String webdir)
	{
		super(WorkflowConstants.NAMESPACE, "remotebustard", WorkflowConstants.VERSION, "remotebustard_" + flowcell);
		for(String s : genomes)
		{
			if(s != null)
				this.addArgument(new PseudoText(s + " "));
		}
		this.addArgument(new PseudoText("flowcell=" + flowcell + " "));
		this.addArgument(new PseudoText("remotetmpdir=" + copyBackTmpDir + " "));
		this.addArgument(new PseudoText("username=" + copyBackUsername + " "));
		this.addArgument(new PseudoText("remoteuser=" + copyToUsername + " "));
		this.addArgument(new PseudoText("Eland.webdir=" + webdir + " "));
		this.addArgument(new PseudoText("--GERALD=config.txt --with-prb --with-seq --control-lane="+ referenceLane + " --make ./Data/IPAR_1.3"));
	}
}
