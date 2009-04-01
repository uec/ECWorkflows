package edu.usc.epigenome.workflow.DAX;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
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

import edu.usc.epigenome.workflow.parameter.WorkFlowArgs;

public class ECDax extends ADAG
{

	// Globals
	HashMap<String, ArrayList<String>> hasChildren;
	HashMap<String, ArrayList<String>> hasParents;
	HashMap<String, ArrayList<String>> hasInputs;
	HashMap<String, ArrayList<String>> hasOutputs;
	HashMap<String, String> jobIDs;
	HashMap<String, String> hasCmdLine;
	HashMap<String, String> hasExecName;
	int tmpNum = 1;
	WorkFlowArgs workFlowParams;

	public WorkFlowArgs getWorkFlowParams()
	{
		return workFlowParams;
	}

	String pbsScriptTemplate = "#PBS -q DAXPBS_QUEUE \n" + "#PBS -S /bin/bash \n" + "#PBS -l walltime=48:00:00 \n" + "#DAXPBS_DEPS\n"
			+ "export RESULTS_DIR=DAXPBS_RESULTSDIR \n" + "export TMP=DAXPBS_TMPDIR \n" + "mkdir -p $RESULTS_DIR \n" + "mkdir $TMP/$PBS_JOBID \n"
			+ "cd $TMP/$PBS_JOBID \n" + "#DAXPBS_COPYIN \n" + "#DAXPBS_RUN\n" + "#DAXPBS_COPYOUT \n";

	public ECDax(WorkFlowArgs workFlowParamsIn)
	{
		super(1, 0, WorkflowConstants.NAMESPACE);
		this.workFlowParams = workFlowParamsIn;
	}

	public void parseDAX()
	{
		hasChildren = new HashMap<String, ArrayList<String>>();
		hasParents = new HashMap<String, ArrayList<String>>();
		hasInputs = new HashMap<String, ArrayList<String>>();
		hasOutputs = new HashMap<String, ArrayList<String>>();
		hasCmdLine = new HashMap<String, String>();
		hasExecName = new HashMap<String, String>();
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

				// input/output deps
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

	public void runWorkflow(Boolean isDryrun)
	{
		parseDAX();
		//copy over dependant starting files, stage in initial file deps
		for (String job : hasParents.keySet())
		{
			if (hasParents.get(job).size() == 0 && !isDryrun)
			{				
				for(String inputFile : hasInputs.get(job))
				{
					try
					{
						File fromFile = new File(inputFile);
						File outPath = new File(workFlowParams.getSetting("tmpDir") + "/" + workFlowParams.getSetting("FlowCellName"));
						outPath.mkdirs();
						File toFile = new File(workFlowParams.getSetting("tmpDir") + "/" + workFlowParams.getSetting("FlowCellName"), fromFile.getName());
						ECDax.copyFile(fromFile, toFile);
						System.out.println("Staging in initial input: " + fromFile.getName());
					} 
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}							
		}
		
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
	}

	private void runPBSJob(String job, Boolean isDryrun)
	{
		String jobScript;

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
		jobScript = jobScript.replace("DAXPBS_RESULTSDIR", workFlowParams.getSetting("tmpDir") + "/" + workFlowParams.getSetting("FlowCellName"));
		jobScript = jobScript.replace("DAXPBS_TMPDIR", workFlowParams.getSetting("tmpDir"));

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

		// COPYIN
		String copyin = new String();
		for (String s : hasInputs.get(job))
		{
			File f = new File(s);
			copyin += "ln -s " + workFlowParams.getSetting("tmpDir") + "/" + workFlowParams.getSetting("FlowCellName") + "/" + f.getName() + "\n";
		}
		jobScript = jobScript.replace("#DAXPBS_COPYIN", copyin);

		// RUN
		jobScript = jobScript.replace("#DAXPBS_RUN", hasCmdLine.get(job));

		// COPYOUT
		String copyout = new String();
		for (String s : hasOutputs.get(job))
		{
			File f = new File(s);
			copyout += "mv " + f.getName() + " " + workFlowParams.getSetting("tmpDir") + "/" + workFlowParams.getSetting("FlowCellName") + "\n";
		}
		jobScript = jobScript.replace("#DAXPBS_COPYOUT", copyout);

		// execute and get jobID

		// do not run, just display
		if (isDryrun)
		{
			jobIDs.put(job, String.valueOf(tmpNum++));
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
				tmpFile = File.createTempFile("ECjob_" + hasExecName.get(job).replace("::", "_"), ".sh");
				BufferedWriter out = new BufferedWriter(new FileWriter(tmpFile));
				out.write(jobScript);
				out.close();

				// exec
				Process p = thisApp.exec("qsub " + tmpFile.getAbsolutePath());

				// capture and parse output
				String processLine;
				BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
				while ((processLine = input.readLine()) != null)
				{
					if (processLine.matches("\\d+.+"))
					{
						jobIDs.put(job, processLine.trim());
					}
				}
				input.close();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		System.out.print("##############\n" + jobScript);
	}

	private Boolean checkJobDepsOK(String job)
	{
		for (String dep : hasParents.get(job))
		{
			if (jobIDs.get(dep).equals(WorkflowConstants.EMPTY))
				return false;
		}
		return true;
	}

	private Boolean checkAllProcessed()
	{
		for (String s : jobIDs.keySet())
		{
			if (jobIDs.get(s).equals(WorkflowConstants.EMPTY))
				return false;
		}
		return true;
	}

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

	public static void copyFile(File f1, File f2) throws IOException
	{
		try {
		 InputStream in = new FileInputStream(f1);
	      
	      OutputStream out = new FileOutputStream(f2);

	      byte[] buf = new byte[1024];
	      int len;
	      while ((len = in.read(buf)) > 0){
	        out.write(buf, 0, len);
	      }
	      in.close();
	      out.close();	      
	    }
	    catch(FileNotFoundException ex){
	      System.out.println(ex.getMessage() + " in the specified directory.");
	      System.exit(0);
	    }
	    catch(IOException e){
	      System.out.println(e.getMessage());      
	    }
	}

}
