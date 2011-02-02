package edu.usc.epigenome.workflow.DAX;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.griphyn.vdl.dax.ADAG;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import edu.usc.epigenome.workflow.ECWorkflowParams.ECParams;


/**
 * @author zack
 * extends ADAG to add dot, pbs
 */
public class ECDax extends ADAG
{

	// Globals
	//job -> dependents
	HashMap<String, ArrayList<String>> hasChildren;
	
	//job -> depends on
	HashMap<String, ArrayList<String>> hasParents;
	
	//job -> needs input files
	HashMap<String, ArrayList<String>> hasInputs;
	
	//job -> produces output files
	HashMap<String, ArrayList<String>> hasOutputs;
	
	//all job id's (unique)
	HashMap<String, String> jobIDs;
	
	//top level job id's (unique)
	HashMap<String, String> heldJobIDs;
	
	//a job complete cmd line
	HashMap<String, String> hasCmdLine;
	
	//the tc.data app that  job does
	HashMap<String, String> hasExecName;
	
	//the tc.data app mem reqs for pbs reservation
	HashMap<String, String> hasExecMemReqs;
	
	//used in dryrun as a substitute for pbs job ID
	int tmpNum = 1;
	
	public  Set<String> requiredParams()
	{
		return new HashSet<String>();
	}
	
	//the jobs param objects
	ECParams workFlowParams;

	/**
	 * get params
	 * @return the param object used by this dax
	 */
	public ECParams getWorkFlowParams()
	{
		return workFlowParams;
	}

	String pbsScriptTemplate = 	"#PBS -q DAXPBS_QUEUE \n" + 
							 	"#PBS -S /bin/bash \n" + 
							 	"#PBS -l walltime=48:00:00 \n" + 
							 	"#DAXPBS_DEPS\n"+ 
							 	"export RESULTS_DIR=DAXPBS_RESULTSDIR \n" + 
							 	"export TMP=DAXPBS_TMPDIR \n" + 
							 	"mkdir -p $RESULTS_DIR \n" + 
							 	"mkdir $TMP/$PBS_JOBID \n" + 
							 	"cd $TMP/$PBS_JOBID \n" + 
							 	"#DAXPBS_COPYIN \n" + 
							 	"#DAXPBS_RUN\n" + "#DAXPBS_COPYOUT \n";

	/**
	 * create an instance of ECDAX
	 * @param workFlowParamsIn params to use for this dax
	 */
	public ECDax(ECParams workFlowParamsIn)
	{
		super(1, 0, WorkflowConstants.NAMESPACE);
		this.workFlowParams = workFlowParamsIn;
		
		try
		{
			this.workFlowParams.validate(this.requiredParams());
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
	}
	

	/**
	 * parse the xml of this dax and populate internal objects so we can run/plot this job 
	 */
	public void parseDAX()
	{
		hasChildren = new HashMap<String, ArrayList<String>>();
		hasParents = new HashMap<String, ArrayList<String>>();
		hasInputs = new HashMap<String, ArrayList<String>>();
		hasOutputs = new HashMap<String, ArrayList<String>>();
		hasCmdLine = new HashMap<String, String>();
		hasExecName = new HashMap<String, String>();
		hasExecMemReqs = new HashMap<String, String>();
		heldJobIDs = new HashMap<String, String>();
		jobIDs = new HashMap<String, String>();

		String xmlString = this.toXML("", "");
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		StringReader reader = new StringReader(xmlString);
		InputSource inputSource = new InputSource(reader);

		try
		{
			// Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document dom = db.parse(inputSource);

			// parse all jobs
			NodeList jobNodeList = dom.getElementsByTagName("job");
			for (int i = 0; i < jobNodeList.getLength(); i++)
			{
				Element e = (Element) jobNodeList.item(i);
				String jobName = e.getAttribute("id");
				hasChildren.put(jobName, new ArrayList<String>());
				hasParents.put(jobName, new ArrayList<String>());
				jobIDs.put(jobName, WorkflowConstants.EMPTY);

				// input/output file deps
				hasInputs.put(jobName, new ArrayList<String>());
				hasOutputs.put(jobName, new ArrayList<String>());
				NodeList files = e.getElementsByTagName("uses");
				for (int j = 0; j < files.getLength(); j++)
				{
					Element p = (Element) files.item(j);
					String fileName = p.getAttribute("file");
					String fileDirection = p.getAttribute("link");
					if (fileDirection.contains("input"))
					{
						hasInputs.get(jobName).add(fileName);
					} else if (fileDirection.contains("output"))
					{
						hasOutputs.get(jobName).add(fileName);
					}
				}

				// command line (exec + args)
				String jobCmdLine = workFlowParams.getSetting(e.getAttribute("namespace") + "::" + e.getAttribute("name") + ":" + e.getAttribute("version"))
						+ " ";
				hasExecName.put(jobName, e.getAttribute("namespace") + "::" + e.getAttribute("name"));
				hasExecMemReqs.put(jobName, workFlowParams.getSetting(e.getAttribute("namespace") + "::" + e.getAttribute("name") + ":" + e.getAttribute("version") + "_MAXMEM"));
				
				NodeList argsNodeList = e.getElementsByTagName("argument");
				for (int j = 0; j < argsNodeList.getLength(); j++)
				{
					Element p = (Element) argsNodeList.item(j);
					NodeList argFrags = p.getChildNodes();

					for (int k = 0; k < argFrags.getLength(); k++)
					{
						Node argFrag = argFrags.item(k);
						if (argFrag.getNodeName().equals("filename"))
						{
							NamedNodeMap attr = argFrag.getAttributes();
							jobCmdLine += attr.getNamedItem("file").getNodeValue();
						} else
							jobCmdLine += argFrag.getNodeValue();
					}
					hasCmdLine.put(jobName, jobCmdLine);
				}
			}

			// get parent/child relations
			NodeList relationNodeList = dom.getElementsByTagName("child");
			for (int i = 0; i < relationNodeList.getLength(); i++)
			{
				Element e = (Element) relationNodeList.item(i);
				String child = e.getAttribute("ref");
				NodeList parents = e.getElementsByTagName("parent");
				for (int j = 0; j < parents.getLength(); j++)
				{
					Element p = (Element) parents.item(j);
					String parent = p.getAttribute("ref");
					hasChildren.get(parent).add(child);
					hasParents.get(child).add(parent);
				}
			}
		} catch (ParserConfigurationException pce)
		{
			pce.printStackTrace();
		} catch (SAXException se)
		{
			se.printStackTrace();
		} catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
		reader.close();
	}

	
	/**
	 * create a verbose dot graph with inputs ouputs and params
	 * @param dotFileName file to save as
	 */
	public void saveAsDot(String dotFileName)
	{
		parseDAX();
		String dotGraph = "digraph g {\n";

		// show starting parameters
		dotGraph += "\"Parameters\" [\nshape = \"Mrecord\"\n label = \"{ Parameters ";
		for (String key : workFlowParams.getWorkFlowArgsMap().keySet())
		{
			if (!workFlowParams.getSetting(key).contains("\n"))
				dotGraph += " | " + key + "=" + workFlowParams.getSetting(key);
		}
		dotGraph += "}\" ];\n";

		// define node contents
		for (String parent : hasChildren.keySet())
		{
			dotGraph += "\"" + parent + "\" [\nshape = \"Mrecord\"\n"
					+ "label =<<table border=\"0\" cellborder=\"0\" cellspacing=\"0\" cellpadding=\"4\"><tr><td bgcolor=\"navy\"><font color=\"white\">"
					+ hasExecName.get(parent) + "</font></td></tr>";
			for (String input : hasInputs.get(parent))
			{
				dotGraph += "<tr><td align=\"left\"><font color=\"brown4\"> input: " + input + "</font></td></tr>";
			}
			for (String output : hasOutputs.get(parent))
			{
				dotGraph += "<tr><td align=\"left\"><font color=\"darkgreen\"> output: " + output + "</font></td></tr>";
			}
			dotGraph += "</table>> ];\n";
		}

		// add relations
		for (String parent : hasChildren.keySet())
		{
			for (String child : hasChildren.get(parent))
			{
				dotGraph += "\"" + parent + "\" -> \"" + child + "\"\n";
			}
		}

		// add start node
		for (String parent : hasParents.keySet())
		{
			if (hasParents.get(parent).size() == 0)
				dotGraph += "Start -> \"" + parent + "\"\n";
		}
		dotGraph += "Parameters -> Start\n";
		dotGraph += "}\n";
		File dotFile = new File(dotFileName);
		BufferedWriter out;
		try
		{
			out = new BufferedWriter(new FileWriter(dotFile));
			out.write(dotGraph);
			out.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * create a simple(er) graph, save as dot file (use graphviz to view)
	 * @param dotFileName file to save as
	 */
	public void saveAsSimpleDot(String dotFileName)
	{
		parseDAX();
		//String colors[] = {"orange", "blue", "brown", "blueviolet", "plum4", "cadetblue", "chartreuse", "cornflowerblue", "crimson", "cyan", "darkgreen", "darkkhaki","deeppink","gray", "darkslateblue", "dodgerblue", "gold", "green", "darkgoldenrod"};
		String colors[] = {"red", "green", "blue", "orange", "yellow", "violet", "gray", "brown", "red4", "green4", "blue4", "orangered", "yellow3", "darkviolet", "gray19", "lightpink", "palegreen", "lightblue", "salmon", 
						   "red", "green", "blue", "orange", "yellow", "violet", "gray", "brown", "red4", "green4", "blue4", "orangered", "yellow3", "darkviolet", "gray19", "lightpink", "palegreen", "lightblue", "salmon"};
		int nextColor = 0;
		HashMap<String,Integer> nameToColor = new HashMap<String,Integer>(); 
		String dotGraph = "digraph g {\n";
		
		for (String parent : hasChildren.keySet())
		{
			
			if(!(nameToColor.containsKey(hasExecName.get(parent))))
			{
				nameToColor.put(hasExecName.get(parent), nextColor++);
			}
			dotGraph += "\"" + parent + "\" [shape = \"circle\" style=\"filled\" colorscheme=\"X11\" color="+ colors[nameToColor.get(hasExecName.get(parent))]+ " label = \"" +/* hasExecName.get(parent) +*/ "\"];\n";			
		}
		
		
		//add legend
		dotGraph += "\"Legend\" [\nshape = \"Mrecord\" colorscheme=\"X11\""
			+ "label =<<table border=\"0\" cellborder=\"0\" cellspacing=\"0\" cellpadding=\"4\"><tr><td bgcolor=\"white\"><font color=\"black\">Legend</font></td></tr><tr>";
		for (String jobtype : nameToColor.keySet())
		{
				dotGraph += "<td align=\"left\" bgcolor=\""+ colors[nameToColor.get(jobtype)] +"\" border=\"2\" color=\""+ colors[nameToColor.get(jobtype)] +"\">      </td><td align=\"left\" border=\"2\" color=\""+ colors[nameToColor.get(jobtype)] +"\"><font>" + jobtype + "</font></td><td> </td>";				
		}
		dotGraph += "</tr></table>> ];\n";
		
		// add relations
		for (String parent : hasChildren.keySet())
		{
			for (String child : hasChildren.get(parent))
			{
				dotGraph += "\"" + parent + "\" -> \"" + child + "\"\n";
			}
		}
		
		
		

		// add start node
		for (String parent : hasParents.keySet())
		{
			if (hasParents.get(parent).size() == 0)
				dotGraph += "Start -> \"" + parent + "\"\n";			
		}
		dotGraph += "{ edge [color=\"#ffffff\"]\n Legend -> Start\n}\n";
		
		dotGraph += "}\n";
		File dotFile = new File(dotFileName);
		BufferedWriter out;
		try
		{
			out = new BufferedWriter(new FileWriter(dotFile));
			out.write(dotGraph);
			out.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}


	/**
	 * run the workflow on pbs (porting to sge would be easy)
	 * @param isDryrun if true, a test run, if false really run on pbs
	 */
	public void runWorkflow(Boolean isDryrun)
	{
		parseDAX();

		//check job deps and run ready jobs until nothing left to run
		while (!checkAllProcessed())
		{
			for (String job : hasParents.keySet())
			{
				if (checkJobDepsOK(job))
				{					
					runPBSJob(job, isDryrun);
					hasParents.remove(job);
					break;
				}
			}
		}
		//release holds
		if(!isDryrun)
		{
			for(String job : heldJobIDs.keySet())
			{
				
				try
				{
					java.lang.Runtime.getRuntime().exec("qrls " + heldJobIDs.get(job));
					System.err.println("releasing hold on " + job + " (" + heldJobIDs.get(job) + ")");
				} 
				catch (IOException e)
				{
					e.printStackTrace();
				}
				
			}
		}
	}

	/**
	 * run a given job on pbs. capture the job id for dependency resolution
	 * @param job the job_id to run
	 * @param isDryrun true if testing, false if really running
	 */
	private void runPBSJob(String job, Boolean isDryrun)
	{
		String jobScript;
		Boolean hasNoDeps = false;

		// See if we have a template file
		try
		{
			jobScript = new String(workFlowParams.getSetting("JobTemplate"));
		}
		// otherwise use hardcoded internal value
		catch (Exception e)
		{
			jobScript = new String(pbsScriptTemplate);
		}
		jobScript = jobScript.replace("DAXPBS_QUEUE", workFlowParams.getSetting("queue"));
		jobScript = jobScript.replace("DAXPBS_RESULTSDIR", workFlowParams.getSetting("tmpDir") + "/" + workFlowParams.getSetting("FlowCellName") + "/" + workFlowParams.getSetting("WorkflowName"));
		jobScript = jobScript.replace("DAXPBS_TMPDIR", workFlowParams.getSetting("tmpDir"));
		if(hasExecMemReqs.containsKey(job))
			jobScript = jobScript.replace("DAXPBS_MEM", "PBS -l mem=" + hasExecMemReqs.get(job));

		// PREPARE DEPS
		String deps = new String("#PBS -W depend=afterany");
		for (String s : hasParents.get(job))
		{
			deps += ":" + jobIDs.get(s);
		}
		if (hasParents.get(job).size() > 0)
		{
			jobScript = jobScript.replace("#DAXPBS_DEPS", deps);
		}
		else
		{
			hasNoDeps = true;
		}
			
		// COPYIN
		String copyin = new String();
		for (String s : hasInputs.get(job))
		{
			File f = new File(s);
			if(f.isAbsolute())
			{
				copyin += "ln -s " + s + "\n";
			}
			else
				copyin += "ln -s " + workFlowParams.getSetting("tmpDir") + "/" + workFlowParams.getSetting("FlowCellName") + "/" + workFlowParams.getSetting("WorkflowName") + "/" + f.getName() + "\n";
		}
		jobScript = jobScript.replace("#DAXPBS_COPYIN", copyin);

		// RUN
		jobScript = jobScript.replace("#DAXPBS_RUN", hasCmdLine.get(job));

		// COPYOUT
		String copyout = new String();
		for (String s : hasOutputs.get(job))
		{
			File f = new File(s);
			copyout += "mv " + f.getName() + " " + workFlowParams.getSetting("tmpDir") + "/" + workFlowParams.getSetting("FlowCellName") + "/" + workFlowParams.getSetting("WorkflowName") + "\n";
		}
		jobScript = jobScript.replace("#DAXPBS_COPYOUT", copyout);

		// execute and get jobID

		// do not run, just display
		if (isDryrun)
		{
			jobIDs.put(job, String.valueOf(tmpNum++));
			try
			{
				FileWriter log = new FileWriter("dryrun.log.txt", true);
				log.write("##############\n" + jobScript);
				log.close();
			} 
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}

		// else qsub it
		else
		{
			Runtime thisApp = java.lang.Runtime.getRuntime();
			// copy to tmp
			File tmpFile;
			try
			{
				//tmpFile = File.createTempFile(job, ".sh", new File(WorkflowConstants.systemTmp));
				String jobName = workFlowParams.getWorkFlowArgsMap().containsKey("FlowCellName") ? workFlowParams.getSetting("FlowCellName") + "_" + workFlowParams.getSetting("WorkflowName") : "job";
				tmpFile = File.createTempFile("uec_" + jobName + "_" + hasExecName.get(job).replace("::", "_"), ".sh");
				tmpFile.deleteOnExit();
				
				System.out.print("############\n# " + tmpFile.getName() + ":\n");
				
				FileWriter log = new FileWriter("pbsrun.log.txt", true);
				log.write("\n############\n# " + tmpFile.getName() + ":\n");
				log.write(jobScript);
				log.close();
				
				BufferedWriter out = new BufferedWriter(new FileWriter(tmpFile));
				out.write(jobScript);
				out.close();

				// exec
				String execCmd = "qsub " + tmpFile.getAbsolutePath();
				if(hasNoDeps)
				{
					 execCmd = "qsub -h " + tmpFile.getAbsolutePath();
					 
				}
				System.out.print("\n##attempting to qsub############\n" + jobScript);
				Process p = thisApp.exec(execCmd);
				
				
				// capture and parse output
				String processLine;
				BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
				System.out.print("\n##submitted job############\n");
				while ((processLine = input.readLine()) != null)
				{
					System.out.println("##qsub response: " + processLine);
					if (processLine.matches("\\d+.+"))
					{
						jobIDs.put(job, processLine.trim());
						if(hasNoDeps)
						{
							heldJobIDs.put(job, processLine.trim());
						}
					}
					else
					{
						System.out.println("##error in processLine: " + processLine);
					}
				}
				input.close();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
	}

	/**
	 * see if job is ready to queue
	 * @param job job_id to run
	 * @return true of ok, false if not ready
	 */
	private Boolean checkJobDepsOK(String job)
	{
		for (String dep : hasParents.get(job))
		{
			if (jobIDs.get(dep).equals(WorkflowConstants.EMPTY))
				return false;
		}
		return true;
	}

	/**
	 * see if all jobs have been queued and dependencies reduced
	 * @return true if all done, false if still jobs to be queued
	 */
	private Boolean checkAllProcessed()
	{
		for (String s : jobIDs.keySet())
		{
			if (jobIDs.get(s).equals(WorkflowConstants.EMPTY))
				return false;
		}
		return true;
	}

	/**
	 * save dax as xml
	 * @param fileName file to save as
	 */
	public void saveAsXML(String fileName)
	{
		FileWriter daxFw;
		try
		{
			daxFw = new FileWriter(fileName);
			this.toXML(daxFw, "", null);
			daxFw.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
