package edu.usc.epigenome.workflow.ECWorkflowParams.specialized;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import edu.usc.epigenome.workflow.ECWorkflowParams.ECParams;

public class GAParams extends ECParams
{
	protected HashSet<Integer> lanesUsed = new HashSet<Integer>();

	
	// initialize from file then remote web, web trumps file
	public GAParams(File file, String processIDSite)
	{
		super(file);
		processRemoteSettings(processIDSite);
		setGADefaults();
		System.out.print(this.toString());		
	}

	// initialize from web only
	public GAParams(String processIDSite)
	{
		super();
		processRemoteSettings(processIDSite);
		setGADefaults();
		System.out.print(this.toString());
	}

	// initialize from file only
	public GAParams(File file)
	{
		super(file);
		this.findAvailableLanes();
		setGADefaults();
		System.out.print(this.toString());
	}
	
	//initialize from nothing! use defaults
	public GAParams()
	{
		super();
		this.findAvailableLanes();
		setGADefaults();	
		System.out.print(this.toString());
	}
	
	

	protected void findAvailableLanes()
	{
		for (String key : this.workFlowArgsMap.keySet())
		{
//			if (key.matches("Lane\\.(\\d+)\\.Input"))
//			{
//				if (!(this.lanesUsed.contains(Integer.parseInt(key.substring(5, 6)))))
//					lanesUsed.add(Integer.parseInt(key.substring(5, 6)));
//			}
			Pattern p = Pattern.compile("Lane\\.(\\d+)\\.Input");
			Matcher m = p.matcher(key);
			if (m.find())
			{
				int lane = Integer.parseInt(m.group(1));
				if (!(this.lanesUsed.contains(lane)))
					lanesUsed.add(lane);
			}
		}
	}

	private void processRemoteSettings(String processIDSite)
	{
		Document processDom = getDomFromAddress(processIDSite);
		//get lims id
		NodeList processRoot = processDom.getElementsByTagName("process");
		Element process = (Element) processRoot.item(0);
		this.workFlowArgsMap.put("LimsID", process.getAttribute("limsid"));
		
		// get process types (Bs-seq etc) and run settings set for each lane
		NodeList processFieldList = processDom.getElementsByTagName("ns2:field");
		for (int i = 0; i < processFieldList.getLength(); i++)
		{
			Element processField = (Element) processFieldList.item(i);
			// check processing type
			if (processField.getAttribute("name").contains(" Processing"))
			{
				String laneNumber = processField.getAttribute("name").substring(1, 2);
				String alignmentType = "regular";
				// regular?
				if (processField.getTextContent().contains("basic genomic DNA processing"))
					alignmentType = "regular";
				// bisulfite?
				else if (processField.getTextContent().contains("BS-seq processing (USC)"))
					alignmentType = "bisulfite";
				// other?
				// else if...

				this.workFlowArgsMap.put("Lane." + laneNumber + ".AlignmentType", alignmentType);
				if (!(this.lanesUsed.contains(Integer.parseInt(laneNumber))))
					this.lanesUsed.add(Integer.parseInt(laneNumber));
			}

			// check control lane
			if (processField.getAttribute("name").contains("Control lane"))
			{
				this.workFlowArgsMap.put("referenceLane", processField.getTextContent());
			}

			// check flowcell ID
			if (processField.getAttribute("name").contains("Flowcell S/N"))
			{
				this.workFlowArgsMap.put("FlowCellName", processField.getTextContent());
			}

			// check ready to run
			if (processField.getAttribute("name").contains("Ready to start analysis"))
			{
				if (processField.getTextContent().contains("rue"))
					this.workFlowArgsMap.put("ReadyToRun", "true");
				else
					this.workFlowArgsMap.put("ReadyToRun", "false");
			}
			
			//check end1 maq trim
			if (processField.getAttribute("name").contains("End1 Trim Read Length"))
			{
				this.workFlowArgsMap.put("MaqTrimEnd1", processField.getTextContent());
			}
			
			//check end2 maq trim
			if (processField.getAttribute("name").contains("End2 Trim Read Length"))
			{
				this.workFlowArgsMap.put("MaqTrimEnd2", processField.getTextContent());
			}
		}

		NodeList inputNodeList = processDom.getElementsByTagName("input");

		// foreach lane-artifact
		for (int i = 0; i < inputNodeList.getLength(); i++)
		{
			// get link to lane info
			Element artifactLink = (Element) inputNodeList.item(i);
			Document laneDom = getDomFromAddress(artifactLink.getAttribute("uri"));

			// determine lane-number
			Element laneValue = (Element) laneDom.getElementsByTagName("value").item(0);
			String laneNumber = laneValue.getTextContent().substring(2);

			// get sample url
			Element sampleLink = (Element) (laneDom.getElementsByTagName("sample").item(0));
			Document sampleDom = getDomFromAddress(sampleLink.getAttribute("uri"));

			// get organism
			NodeList sampleFieldList = sampleDom.getElementsByTagName("ns2:field");
			String genome = "/home/uec-00/shared/production/genomes/hg18_unmasked/hg18_unmasked.plusContam.bfa";
			for (int j = 0; j < sampleFieldList.getLength(); j++)
			{
				Element sampleField = (Element) sampleFieldList.item(j);
				if (sampleField.getAttribute("name").contains("Species"))
				{
					if (sampleField.getTextContent().contains("hi"))
						genome = "/home/uec-00/shared/production/genomes/phi-X174/phi_plus_SNPs.bfa";
					else if (sampleField.getTextContent().contains("sacCer"))
						genome = "/auto/uec-00/shared/production/genomes/sacCer1/sacCer1.bfa";
				}
			}
			this.workFlowArgsMap.put("Lane." + laneNumber + ".ReferenceBFA", genome);
		}
	}

	private String readURL(String url)
	{
		URL urlIn;
		String fetched = "";
		try
		{
			urlIn = new URL(url);
			URLConnection conn = urlIn.openConnection();
			String encoding = new sun.misc.BASE64Encoder().encode("zack:genzack".getBytes());
			conn.setRequestProperty("Authorization", "Basic " + encoding);
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = in.readLine()) != null)
			{
				fetched = fetched + line;
			}
			return fetched;
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private Document getDom(String xmlText)
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		StringReader reader = new StringReader(xmlText);
		InputSource inputSource = new InputSource(reader);
		DocumentBuilder db;
		try
		{
			db = dbf.newDocumentBuilder();
			Document dom = db.parse(inputSource);
			return dom;
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	private Document getDomFromAddress(String url)
	{
		String xml = readURL(url);
		Document dom = getDom(xml);
		return dom;
	}





	/**
	 * check to make sure parameters have been entered for a ga workflow
	 * 
	 * @throws Exception
	 */
	public void validate(Set<String> requiredArgs) throws Exception
	{
		// String[] requiredArgs = {
		// "RegularSplitFactor",
		// "PegasusTC",
		// "BisulfiteSplitFactor",
		// "MinMismatches",
		// "MaqPileupQ",
		// "FlowCellName"};
		for (String s : requiredArgs)
		{
			if (workFlowArgsMap.containsKey(s) == false)
			{
				throw new Exception("missing required arguement: " + s);
			}
		}
		//if (lanesUsed.isEmpty())
		//	throw new Exception("no lanes specified");
	}

	

	public String getLaneInput(int laneNumber)
	{
		return workFlowArgsMap.get("Lane." + laneNumber + ".Input");
	}

	public Integer[] getAvailableLanes()
	{
		return lanesUsed.toArray(new Integer[lanesUsed.size()]);
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

		String files[] = new File(".").list();
		for (int lane : getAvailableLanes())
		{
			setDefault("Lane." + lane + ".AlignmentType", "regular");
			setDefault("Lane." + lane + ".ReferenceBFA", "/home/uec-00/shared/production/genomes/hg18_unmasked/hg18_unmasked.plusContam.bfa");
			if (!(this.workFlowArgsMap.containsKey("Lane." + lane + ".Input")))
			{
				String laneInputFile = "s_" + lane + "_sequence.txt";
				for (String s : files)
				{
					if (s.matches(".*s_" + lane + "[_\\.].+?\\.(txt|gz)"))
					{
						laneInputFile = s;
						System.err.println("Guessing input file for lane " + lane + ": " + s + " (found in current directory)");
					}
				}
				setDefault("Lane." + lane + ".Input", laneInputFile);
			}
		}		
	}

	public Boolean laneIsBisulfite(int laneNumber)
	{
		if (workFlowArgsMap.containsKey("Lane." + laneNumber + ".AlignmentType"))
			if (workFlowArgsMap.get("Lane." + laneNumber + ".AlignmentType").contains("sulfite"))
				return true;
			else
				return false;
		else
			return false;
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
				par = new GAParams(args[1]);				
			}
			else
			{
				System.err.println("arguement is neither an existing file nor valid URI");
				System.exit(1);
			}
		}
		else if (args.length == 2)
		{
			par = new GAParams(new File(args[0]), args[1]);
		}
		
		else 
		{
			System.err.println("usage:  ECWorkflowParams  [paramfile.txt] [http://processURL]");
			System.err.println("Either one or both of the arguements must be specified");
			System.exit(1);
		}	
	}
}
