package gov.nih.nlm.semmed.util;

import gov.nih.nlm.semmed.model.PubmedArticle;

import java.util.ArrayList;
import java.util.List;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

/**
 * 
 * Sax parser for a list of articles of pubmed, as returned by the EFetch web service (in xml format).
 * 
 * 
 * @author rodriguezal
 *
 */
public class PubMedArticleParser extends DefaultHandler {
	
	private PubmedArticle currentArticle;
	private boolean inID = false;
	private boolean inTitle = false;
	private boolean inAbstract = false;
	private boolean inPubDate = false;
	private boolean inMonth = false;
	private boolean inYear = false;
	private boolean inDay = false;
	
	private boolean inAuthor = false;
	private boolean inFirstName = false;
	private boolean inLastName = false;
	
	private boolean firstAuthor;
	
	private StringBuffer sbAbstract;
	private StringBuffer sbTitle;
	private StringBuffer sbAuthor;
	private StringBuffer sbID;
	
	
	private String year;
	private String month;
	private String day;
	
	private List<PubmedArticle> articles  = new ArrayList<PubmedArticle>();
	
	public List<PubmedArticle> getArticles(){
		return articles;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException{
		String eName = localName; // element name
        if ("".equals(eName)) eName = qName; // namespaceAware = false
		
        if (eName.equalsIgnoreCase("pubmedarticle")){
        	currentArticle = new PubmedArticle();
        	sbID=null;
        }else if (eName.equalsIgnoreCase("pmid") && sbID==null){        	
        	inID = true;
        	sbID = new StringBuffer();
        }else if (eName.equalsIgnoreCase("articletitle")){
        	inTitle = true;
        	sbTitle = new StringBuffer();
        }else if (eName.equalsIgnoreCase("abstracttext")){
        	inAbstract = true;
        	sbAbstract = new StringBuffer();
        }else if (eName.equalsIgnoreCase("pubdate"))
        	inPubDate = true;
        else if (inPubDate && eName.equalsIgnoreCase("year"))
        	inYear = true;
        else if (inPubDate && eName.equalsIgnoreCase("month"))
        	inMonth = true;
        else if (inPubDate && eName.equalsIgnoreCase("day"))
        	inDay = true;
        else if (eName.equalsIgnoreCase("authorlist")){
        	sbAuthor = new StringBuffer();
        	firstAuthor = true;
        }else if (eName.equalsIgnoreCase("author"))
        	inAuthor = true;
        else if (inAuthor && eName.equalsIgnoreCase("lastname"))
        	inLastName = true;
        else if (inAuthor && (eName.equalsIgnoreCase("forename") || eName.equalsIgnoreCase("firstname")))
        	inFirstName = true;
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException{
		String eName = localName; // element name
        if ("".equals(eName)) eName = qName; // namespaceAware = false
		
        if (eName.equalsIgnoreCase("PubmedArticle"))
        	articles.add(currentArticle);
        else if (eName.equalsIgnoreCase("PMID")){
        	inID = false;
        	currentArticle.setId(sbID.toString().trim());
        }else if (eName.equalsIgnoreCase("ArticleTitle")){
        	inTitle = false;
        	currentArticle.setTitleText(sbTitle.toString().trim());
        }else if (eName.equalsIgnoreCase("AbstractText")){
        	inAbstract = false;
        	currentArticle.setAbstractText(sbAbstract.toString().trim());
        }else if (eName.equalsIgnoreCase("PubDate")){
        	inPubDate = inDay = inMonth = inYear = false;        	
        	String pubDate = "";
        	if (year!=null){
        		pubDate = year;
        		year = null;
        		if (month!=null){
        			pubDate += " "+month;
        			month = null;
        			if (day!=null){
        				pubDate += " "+day;
        				day = null;
        			}
        		}
        	}
        	currentArticle.setPubDate(pubDate);
        }
        else if (inPubDate && eName.equalsIgnoreCase("Year"))
        	inYear = false;
        else if (inPubDate && eName.equalsIgnoreCase("Month"))
        	inMonth = false;
        else if (inPubDate && eName.equalsIgnoreCase("Day"))
        	inDay = false;
        else if (eName.equalsIgnoreCase("authorlist")){
        	sbAuthor.append(".");
        	currentArticle.setAuthorList(sbAuthor.toString().trim());
        }else if (eName.equalsIgnoreCase("author"))
        	inAuthor = false;
        else if (inAuthor && eName.equalsIgnoreCase("lastname"))
        	inLastName = false;
        else if (inAuthor && (eName.equalsIgnoreCase("forename") || eName.equalsIgnoreCase("firstname")))
        	inFirstName = false;
        	
	}
	
	@Override
	public void characters(char[] ch,int start,int length) throws SAXException{
		if (inID)
			sbID.append(ch,start,length);
		else if (inTitle)
			sbTitle.append(ch,start,length);
		else if (inAbstract)
			sbAbstract.append(ch,start,length);
		else if (inYear)
			year = new String(ch,start,length).trim();
		else if (inMonth)
			month = new String(ch,start,length).trim();
		else if (inDay)
			day = new String(ch,start,length).trim();
		else if (inLastName){
			if (firstAuthor)
				firstAuthor = false;
			else
				sbAuthor.append(", ");
			sbAuthor.append(ch,start,length);
		}else if (inFirstName){
			sbAuthor.append(" ");
			sbAuthor.append(new String(ch,start,length>0?1:length).trim());
		}
	}
}
