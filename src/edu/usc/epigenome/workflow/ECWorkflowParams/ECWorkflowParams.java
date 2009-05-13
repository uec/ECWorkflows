package edu.usc.epigenome.workflow.ECWorkflowParams;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ECWorkflowParams
{
    private HashMap<String,String> workFlowArgsMap = new HashMap<String,String>();
    public HashMap<String, String> getWorkFlowArgsMap()
	{
		return workFlowArgsMap;
	}
	private HashSet<Integer> lanesUsed = new HashSet<Integer>();
    public  ECWorkflowParams(String fileName) 
    {
    	try
		{
			FileReader fileReader = new FileReader(fileName);
			BufferedReader fileIn = new BufferedReader(fileReader);
			String s;
			while((s = fileIn.readLine()) != null) 
			{
				if(s.matches("\\S+\\s*=\\s*\\S+") && !s.matches("\\s*#.*"))
				{
					String cleanLine = s.replaceAll("\\s", "");
					String[] matchedLine = cleanLine.split("=");
					workFlowArgsMap.put(matchedLine[0], matchedLine[1]);
					if(matchedLine[0].matches("Lane\\.\\d\\.Input"))
					{
						lanesUsed.add(Integer.parseInt(matchedLine[0].substring(5, 6)));
					}
					if(matchedLine[0].matches("JobTemplate"))
					{
						StringBuffer fileData = new StringBuffer(1000);
				        BufferedReader reader = new BufferedReader(
				                new FileReader(matchedLine[1]));
				        char[] buf = new char[1024];
				        int numRead=0;
				        while((numRead=reader.read(buf)) != -1){
				            fileData.append(buf, 0, numRead);
				        }
				        reader.close();
				        workFlowArgsMap.put(matchedLine[0], fileData.toString());
					}
					if(matchedLine[0].matches("PegasusTC"))
					{
						FileInputStream fstream = new FileInputStream(matchedLine[1]);
						DataInputStream in = new DataInputStream(fstream);
				        BufferedReader br = new BufferedReader(new InputStreamReader(in));
				        String strLine;
					    //Read File Line By Line
					    while ((strLine = br.readLine()) != null)   
					    {
						    if(strLine.matches("\\S+\\s+\\S+\\s+\\S+.*"))
						    {
						    	String[] tcMatch = strLine.split("\\s+");
						    	workFlowArgsMap.put(tcMatch[1],tcMatch[2]);
						    }			      
					    }
					}
				}
			}
			fileReader.close(); 
//			try
//			{
//				this.Validate();
//			} 
//			catch (Exception e)
//			{
//				e.printStackTrace();
//			}
		} 
    	
    	catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
    	catch (IOException e)
		{
			e.printStackTrace();
		}
    }
    public String toString()
    {
    	String retString = new String();
    	for(String key : workFlowArgsMap.keySet())
    	{
    		retString += key + ":=" + workFlowArgsMap.get(key) + " ";
    	}
    	return retString;
    }
    
    /**
     * check to make sure parameters have been entered for a ga workflow
     * @throws Exception
     */
    public void validate(Set<String> requiredArgs) throws Exception
    {
//    	String[] requiredArgs = {
//    			"RegularSplitFactor",
//    			"PegasusTC",
//    			"BisulfiteSplitFactor",
//    			"MinMismatches",
//    			"MaqPileupQ",
//    			"FlowCellName"};
    	for(String s : requiredArgs)
    	{
    		if(workFlowArgsMap.containsKey(s) == false)
    		{
    			throw new Exception("missing required arguement: " + s);
    		}
    	}
    	if(lanesUsed.isEmpty())
    		throw new Exception("no lanes specified");
    }
    
    public String getSetting(String key)
    {
    	return workFlowArgsMap.get(key);
    }
    public String getLaneInput(int laneNumber)
    {
    	return workFlowArgsMap.get("Lane." + laneNumber + ".Input");
    }
    
    public Integer[] getAvailableLanes()
    {
    	return lanesUsed.toArray(new Integer[lanesUsed.size()]);
    }
    public Boolean laneIsBisulfite(int laneNumber)
    {
    	if(workFlowArgsMap.containsKey("Lane." + laneNumber + ".AlignmentType"))
    		if(workFlowArgsMap.get("Lane." + laneNumber + ".AlignmentType").contains("sulfite"))
    			return true;
    		else
    			return false;
    	else 
    		return false;
    }
}
