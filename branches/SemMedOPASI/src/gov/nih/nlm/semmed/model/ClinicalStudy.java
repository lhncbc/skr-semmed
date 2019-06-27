/**
 * 
 */
package gov.nih.nlm.semmed.model;

import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
import org.jdom.Element;

/**
 * @author hkilicoglu
 *
 */
public class ClinicalStudy extends SemMedDocument {
//	private static Log log = LogFactory.getLog(ClinicalStudy.class);
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String status;
	
	private String condition;
	
	/**
	 * 
	 */
	public ClinicalStudy() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * 
	 */	
	public ClinicalStudy(String id, String titleText, String status, boolean include) {
		this();
		// TODO Auto-generated constructor stub
		this.id = id;
		this.titleText = titleText;
		this.status = status;
		this.include = include;
	}
	
	
	/**
	 * 
	 */	
	public ClinicalStudy(String id, String titleText, String abstractText, String status, boolean include) {
		this(id,titleText,status,include);
		this.abstractText = abstractText;
	}
	
	/**
	 * 
	 */	
	public ClinicalStudy(Element e) {
		this();
	    Element study_id = (Element)e.getChild("study_id");
	    String nct_id = study_id.getChild("nct_id").getText();
	    String title = ((Element)e.getChild("brief_title").getChild("textblock")).getTextNormalize();

	      Charset asciiCharset = Charset.forName("US-ASCII");
	      CharsetEncoder encoder =  asciiCharset.newEncoder();
	      CharsetDecoder decoder = asciiCharset.newDecoder();	      
	      encoder.onMalformedInput(CodingErrorAction.IGNORE);
	      encoder.onUnmappableCharacter(CodingErrorAction.IGNORE);
	      
	    if (!title.endsWith(".")) {
	    	title = title + '.';
	    }

	    StringBuffer abstractBuf = new StringBuffer();
	    Element brief_summary = e.getChild("brief_summary");
	    if (brief_summary != null) {
	    	Element brief_summary_textblock = brief_summary.getChild("textblock");
	    	if (brief_summary_textblock != null) {
	    		if (brief_summary_textblock.getChildren().size() == 0) {
	    			abstractBuf.append(brief_summary_textblock.getTextNormalize());
	    		}	
	    		else {
	    			List summaryParagraphList = brief_summary_textblock.getChildren("paragraph");
	                if (summaryParagraphList != null) {
	                	Iterator summaryParagraphIter = summaryParagraphList.iterator();
	                	while (summaryParagraphIter.hasNext()) {	
	                		String paragraphTxt = ((Element)summaryParagraphIter.next()).getTextNormalize();
	                		abstractBuf.append(paragraphTxt);
	                	}
	                }
	            }
	    	}
	    }

	    Element detailed_descr = e.getChild("detailed_descr");
	    if (detailed_descr != null) {
	    	Element detailed_descr_textblock = detailed_descr.getChild("textblock");
	    	if (detailed_descr_textblock != null) {
	    		if (detailed_descr_textblock.getChildren().size() == 0) {
	    			abstractBuf.append(detailed_descr_textblock.getTextNormalize());
	    		}
	    		else {
	    			List detailParagraphList = detailed_descr_textblock.getChildren("paragraph");
	    			if (detailParagraphList != null) {
	    				Iterator detailParagraphIter = detailParagraphList.iterator();
	    				while (detailParagraphIter.hasNext()) {
	    					String paragraphTxt = ((Element) detailParagraphIter.next()).getTextNormalize();
	    					abstractBuf.append(paragraphTxt);
	                    }
	                }
	    		}
	    	}    	
	    }
	    	    
	    String abstractStr = abstractBuf.toString();
	    String replacement = ". $1$2";
	    Pattern pattern = Pattern.compile("\\.([A-Z])([a-z]+)");
	    Matcher matcher = pattern.matcher(abstractStr);
	    abstractStr = matcher.replaceAll(replacement);
	    pattern = Pattern.compile("\\.([A-Z ]+):");
	    matcher = pattern.matcher(abstractStr);
	    replacement = ". $1:";
	    abstractStr = matcher.replaceAll(replacement); 
	    
	    String status = e.getChild("status_block").getChildText("overall_status");
	    // take care of the encoding issues by removing non-ascii characters. 
	    /* TODO find a better way of handling this. */	    
	    try {
	    	this.id = nct_id;
	    	this.titleText = decoder.decode(encoder.encode(CharBuffer.wrap(title))).toString();
	    	this.abstractText = decoder.decode(encoder.encode(CharBuffer.wrap(abstractStr))).toString();
	    	this.status = status;
	    	this.include=true;
	    } catch (CharacterCodingException cde) {
	    }
	    //log.debug("CLINICAL STUDY: " + this.toString());
	}	
	
	/**
	 * @return Returns the status.
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status The status to set.
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getCondition(){
		return condition;
	}
	
	public void setCondition(String condition){
		this.condition = condition;
	}
	
	public String toString() {
		return "NCT ID - " + getId() + 
			"\nTITLE  - " + getTitleText() + 
			"\nSUMMARY- " + getAbstractText() + 
			"\nSTATUS - " + getStatus() ;
	}
	
	public Element toXml() {
		
		//TODO [Alejandro] According to DTD in http://clinicaltrials.gov/ct/screen/ShowDTD, this is missing required header
		Element studyNode = new Element("clinical_study");	
		Element studyIdNode = new Element("study_id");
		Element idNode = new Element("nct_id");
		idNode.setText(getId());
		
		Element titleNode = new Element("brief_title");
		Element titleBlockNode = new Element("textblock");
		titleBlockNode.setText(getTitleText());
	
		Element summaryNode = new Element("brief_summary");
		Element summaryBlockNode = new Element("textblock");
		summaryBlockNode.setText(getAbstractText());
		
		Element statusBlockNode = new Element("status_block");
		Element statusNode = new Element("overall_status");
		statusNode.setText(getStatus());
		
		studyIdNode.addContent(idNode);
		studyNode.addContent(studyIdNode);
		
		titleNode.addContent(titleBlockNode);
		studyNode.addContent(titleNode);

		summaryNode.addContent(summaryBlockNode);
		studyNode.addContent(summaryNode);
		
		statusBlockNode.addContent(statusNode);
		studyNode.addContent(statusBlockNode);	

		return studyNode;
	}	
	


}
