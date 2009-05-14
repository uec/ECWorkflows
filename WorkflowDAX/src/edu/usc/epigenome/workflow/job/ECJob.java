package edu.usc.epigenome.workflow.job;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.griphyn.vdl.classes.LFN;
import org.griphyn.vdl.dax.Filename;
import org.griphyn.vdl.dax.Job;

public class ECJob extends Job
{

	/**
	 * @param namespace
	 * @param the app from tc.data this job does
	 * @param version 
	 * @param id a unique id
	 */
	public ECJob(String namespace, String name, String version, String id)
	{
		super(namespace, name, version, id);
		// TODO Auto-generated constructor stub
	}

	public ECJob(String namespace, String name, String version, String id, String dv_namespace, String dv_name, String dv_version)
	{
		super(namespace, name, version, id, dv_namespace, dv_name, dv_version);
		// TODO Auto-generated constructor stub
	}
	
	
	/**
	 * A convenience method to get the output files of a job.
	 */
	/**
	 * @param job the job you want to retrieve a list of output fileName for
	 * @return List<Filename> a list of all output filenames for the job
	 */
	public List<Filename> getOutputFiles()
	{
		List<Filename> o = new LinkedList<Filename>();

		for (Iterator it = this.listIterateUses(); it.hasNext();)
		{
			Filename f = (Filename) it.next();
			if (f.getLink() == LFN.OUTPUT)
				o.add(f);
		}
		return o;
	}
	
	/**
	 * A convenience method to get the output file of a job and 
	 * endsure that it is the one and only output
	 */
	/**
	 * @param job the job you want to retrieve output fileName for
	 * @return Filename	A single file that is the output of the job
	 * @throws Exception number of outputs for job is NOT EQUAL to 1
	 */
	public Filename getSingleOutputFile() throws Exception
	{
		List<Filename> o = new LinkedList<Filename>();

		for (Iterator it = this.listIterateUses(); it.hasNext();)
		{
			Filename f = (Filename) it.next();
			if (f.getLink() == LFN.OUTPUT)
				o.add(f);
		}

		if (o.size() != 1)
			throw new Exception("Single-file output check failed, There was not only 1 output!");
		return o.get(0);
	}
}
