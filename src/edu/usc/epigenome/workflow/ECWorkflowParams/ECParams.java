package edu.usc.epigenome.workflow.ECWorkflowParams;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.HashMap;
import java.util.Set;


public class ECParams
{
	protected HashMap<String, String> workFlowArgsMap = new HashMap<String, String>();
	

	public HashMap<String, String> getWorkFlowArgsMap()
	{
		return workFlowArgsMap;
	}

	
	//initialize from nothing! use defaults
	public ECParams()
	{
		if(new File("/home/uec-00/shared/production/software/ECWorkflow/workFlowParamsGlobalDefaults.txt").exists())
		{
			processFileSettings("/home/uec-00/shared/production/software/ECWorkflow/workFlowParamsGlobalDefaults.txt");
		}
		setDefaults();
		processPegasusTC();
		processJobTemplate();
	}
	
	public ECParams(File file)
	{
		processFileSettings(file.getAbsolutePath());
		setDefaults();
		processPegasusTC();
		processJobTemplate();
	}
	
	protected void processFileSettings(String filename)
	{
		try
		{
			FileReader fileReader = new FileReader(filename);
			BufferedReader fileIn = new BufferedReader(fileReader);
			String s;
			while ((s = fileIn.readLine()) != null)
			{
				if (s.matches("\\S+\\s*=\\s*\\S+") && !s.matches("\\s*#.*"))
				{
					String cleanLine = s.replaceAll("\\s", "");
					String[] matchedLine = cleanLine.split("=");
					workFlowArgsMap.put(matchedLine[0], matchedLine[1]);					
				}
			}
			fileReader.close();
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void saveAs(String filename)
	{
		FileWriter log;
		try
		{
			log = new FileWriter(filename, false);
			log.write(this.toString());
			log.close();
		} 
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	protected void processPegasusTC()
	{
		try
		{
			FileInputStream fstream = new FileInputStream(this.workFlowArgsMap.get("PegasusTC"));
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			// Read File Line By Line
			while ((strLine = br.readLine()) != null)
			{
				if (strLine.matches("\\S+\\s+\\S+\\s+\\S+.*"))
				{
					String[] tcMatch = strLine.split("\\s+");
					workFlowArgsMap.put(tcMatch[1], tcMatch[2]);
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	protected void processJobTemplate()
	{
		try
		{
			StringBuffer fileData = new StringBuffer(4092);
			BufferedReader reader = new BufferedReader(new FileReader(this.workFlowArgsMap.get("JobTemplate")));
			char[] buf = new char[2048];
			int numRead = 0;
			while ((numRead = reader.read(buf)) != -1)
			{
				fileData.append(buf, 0, numRead);
			}
			reader.close();
			workFlowArgsMap.put("JobTemplate", fileData.toString());
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	


	protected void setDefault(String key, String value)
	{
		if (!(this.workFlowArgsMap.containsKey(key)))
		{
			System.err.println("Using default: " + key + " = " + value);
			this.workFlowArgsMap.put(key, value);
		}
	}

	public String toString()
	{
		String retString = new String();
		String[] keys = this.workFlowArgsMap.keySet().toArray(new String[this.workFlowArgsMap.keySet().size()]);
		java.util.Arrays.sort(keys);
		for (String key : keys)
		{
			retString += key + " = " + workFlowArgsMap.get(key) + "\n";
		}
		return retString;
	}

	/**
	 * check to make sure parameters have been entered for a ga workflow
	 * 
	 * @throws Exception
	 */
	public void validate(Set<String> requiredArgs) throws Exception
	{
		for (String s : requiredArgs)
		{
			if (workFlowArgsMap.containsKey(s) == false)
			{
				throw new Exception("missing required arguement: " + s);
			}
		}
	}

	public String getSetting(String key)
	{
		return workFlowArgsMap.get(key);
	}


	protected void setDefaults()
	{
		File tmpdir = new File(".");
		try
		{
			setDefault("tmpDir", tmpdir.getCanonicalPath().replace("/auto/", "/home/") + "/results");
		} catch (IOException e)
		{
			setDefault("tmpDir", tmpdir.getAbsolutePath() + "/results");
			e.printStackTrace();
		}
		
		setDefault("JobTemplate", "/home/uec-00/shared/production/software/ECWorkflow/pbsTemplate.sh");
		setDefault("PegasusTC", "/home/uec-00/shared/production/software/ECWorkflow/tc.data");
		setDefault("queue", "laird");
		setDefault("ClusterSize", "256");
	}

}
