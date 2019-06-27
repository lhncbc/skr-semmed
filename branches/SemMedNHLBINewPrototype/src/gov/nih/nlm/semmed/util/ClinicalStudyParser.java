package gov.nih.nlm.semmed.util;

import gov.nih.nlm.semmed.model.ClinicalStudy;


import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

//TODO [Alejandro] Parse abstract { in brief_summary (textblock DATA) }
/**
 * @author rodriguezal
 */
public class ClinicalStudyParser extends DefaultHandler {

	private boolean inTitle = false;
	private boolean inCondition = false;
	private boolean inID = false;
		
	private StringBuffer sbTitle;
	private StringBuffer sbCondition;
	private StringBuffer sbID;
	private int[] ids;
	private int count = -1;
	private int current = 0;
	private int maxReturn = Integer.MAX_VALUE;

	private ClinicalStudy[] studies;
	
	private ClinicalStudy currentStudy; 

	public ClinicalStudyParser(int max){
		if(max>0)
			maxReturn=max;
	}
	
	public int[] getIds(){
		return ids;
	}
	
	public ClinicalStudy[] getStudies(){
		return studies;
	}
	
	public String getWebEnv(){
		return null;
	}

	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException{
		
		if (ids!=null && current>=ids.length)
			return;
		
		String eName = localName; // element name
        if ("".equals(eName)) eName = qName; // namespaceAware = false
        
        if (eName.equalsIgnoreCase("clinical_study")){
        	currentStudy = new ClinicalStudy();
        }else if (eName.equalsIgnoreCase("nct_id")){
        	inID = true;
        	sbID = new StringBuffer();
        }else if (eName.equalsIgnoreCase("title")){
        	inTitle = true;
        	sbTitle = new StringBuffer();
        }else if (eName.equalsIgnoreCase("condition_summary")){
        	inCondition = true;
        	sbCondition = new StringBuffer();
        }else if (eName.equalsIgnoreCase("search_results")){
        	count = Integer.parseInt(attributes.getValue("count"));
        	ids = new int[Math.min(count,maxReturn)];
        	studies = new ClinicalStudy[ids.length];
        }
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException{
		if (ids!=null && current>=ids.length)
			return;
		
		String eName = localName; // element name
        if ("".equals(eName)) eName = qName; // namespaceAware = false
		
        if (eName.equalsIgnoreCase("clinical_study")){
        	studies[current++] = currentStudy;
        }else if (eName.equalsIgnoreCase("nct_id")){
        	inID = false;
			currentStudy.setId(sbID.toString().trim());
			ids[current] = Integer.parseInt(currentStudy.getId().substring(3));
        }else if (eName.equalsIgnoreCase("title")){        
        	inTitle = false;
        	currentStudy.setTitleText(sbTitle.toString());
        }else if (eName.equalsIgnoreCase("condition_summary")){
        	inCondition = true;
        	currentStudy.setCondition(sbCondition.toString());
        }
	}
	
	@Override
	public void characters(char[] ch,int start,int length) throws SAXException{
		
		if (ids!=null && current>=ids.length)
			return;
		
		if (inID)
			sbID.append(ch,start,length);
		else if (inTitle)
        	sbTitle.append(ch, start, length);
        else if (inCondition)
        	sbCondition.append(ch,start,length);        
	}
}
