package edu.usc.epigenome.workflow.ECWorkflowParams.specialized;

import java.io.File;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



import edu.usc.epigenome.workflow.ECWorkflowParams.ECParams;

public class GAParams extends ECParams
{
	protected HashMap<String,HashMap<String,String>> samples = new HashMap<String,HashMap<String,String>>();

	
	
	//get list of samples
	public HashMap<String, HashMap<String, String>> getSamples()
	{
		return samples;
	}

	// initialize from file only
	public GAParams(File file)
	{
		super(file);
		this.parseParams();
		setGADefaults();
		System.out.print(this.toString());
	}
	
	//initialize from nothing! use defaults
	
	

	protected void parseParams()
	{
		for (String key : this.workFlowArgsMap.keySet())
		{
			Pattern p = Pattern.compile("(Sample\\.\\d+)\\.(.+)");
			Matcher m = p.matcher(key);
			
			if (m.find())
			{
				String sampleEntry = m.group(1);
				String sampleKey = m.group(2);
				String sampleValue = this.workFlowArgsMap.get(key);
				
				if (!(samples.containsKey(sampleEntry)))
					samples.put(sampleEntry,new HashMap<String,String>());
				
				samples.get(sampleEntry).put(sampleKey, sampleValue);
			}	
			
		}
	}

	
	protected void setGADefaults()
	{
		setDefault("RegularSplitFactor", "500000");
		setDefault("BisulfiteSplitFactor", "500000");
		setDefault("MinMismatches", "2");
		setDefault("MaqPileupQ", "30");
		setDefault("randomSubset", "1000000");
		setDefault("ReadyToRun", "true");
		setDefault("ClusterSize", "256");
		setDefault("MaqTrimEnd1", "0");
		setDefault("MaqTrimEnd2", "0");
		setDefault("WigWindSize","300");
		setDefault("hgBFA", "/home/uec-00/shared/production/genomes/hg18_unmasked/hg18_unmasked.plusContam.bfa");
	}

	public static void main(String[] args)
	{
		@SuppressWarnings("unused")
		GAParams par = null;
		if(args.length == 1)
		{
			if(new File(args[0]).exists())
			{
				par = new GAParams(new File("workFlowParams.txt"));
			}
			else if(args[0].contains("http://"))
			{
				//par = new GAParams(args[1]);				
			}
			else
			{
				System.err.println("arguement is neither an existing file nor valid URI");
				System.exit(1);
			}
		}
		else if (args.length == 2)
		{
			//par = new GAParams(new File(args[0]), args[1]);
		}
		
		else 
		{
			System.err.println("usage:  ECWorkflowParams  [paramfile.txt] [http://processURL]");
			System.err.println("Either one or both of the arguements must be specified");
			System.exit(1);
		}	
	}
}
