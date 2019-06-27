package gov.nih.nlm.semmed.util;

import gov.nih.nlm.semmed.model.PubmedArticle;

import java.util.ArrayList;
import java.util.HashSet;
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
public class PubMedArticleMetaParser extends DefaultHandler {

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
	private boolean inIssn = false;

	private boolean firstAuthor;

	private StringBuffer sbAbstract;
	private StringBuffer sbTitle;
	private StringBuffer sbAuthor;
	private StringBuffer sbID;
	private StringBuffer sbMetadata;
	private StringBuffer sbIssn;


	private String year;
	private String month;
	private String day;

	private List<PubmedArticle> articles  = new ArrayList<PubmedArticle>();
	private boolean inMetaData = false;
	private boolean inPubType = false;
	private boolean inMeshHeading = false;
	private HashSet<String> metadataList = null;

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
        	metadataList = new HashSet<String>();
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
        else if(eName.equalsIgnoreCase("publicationtype")) {
        	inPubType = true;
        	sbMetadata = new StringBuffer();
        }
        else if(eName.equalsIgnoreCase("meshheading")) {
        	inMetaData = true;
        	sbMetadata = new StringBuffer();
        } else if(inMetaData && (eName.equalsIgnoreCase("descriptorname") || eName.equalsIgnoreCase("qualifiername"))) {
        	inMeshHeading = true;
        	if(eName.equalsIgnoreCase("qualifiername"))
        		sbMetadata.append("_"); // Add a "_" in between description
        } else if(eName.equalsIgnoreCase("issn")) {
        	inIssn = true;
        	sbIssn = new StringBuffer();
        }
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException{
		String eName = localName; // element name
        if ("".equals(eName)) eName = qName; // namespaceAware = false

        if (eName.equalsIgnoreCase("PubmedArticle")) {
        	currentArticle.setMetadata(metadataList);
        	articles.add(currentArticle);
        	 metadataList = null;
        } else if (eName.equalsIgnoreCase("PMID")){
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
        else if (inPubType && eName.equalsIgnoreCase("publicationtype")) {
        	StringBuffer sfMetadata = new StringBuffer();
        	for(int i = 0; i < sbMetadata.length(); i++) {
        		if(sbMetadata.charAt(i) == ' ' ||
        		   sbMetadata.charAt(i) == ',' ||
        		   sbMetadata.charAt(i) == '.' ||
        		   sbMetadata.charAt(i) == '-' ||
        		   sbMetadata.charAt(i) == '(' ||
        		   sbMetadata.charAt(i) == ')' ||
        		   sbMetadata.charAt(i) == '\'')
        			sfMetadata.append("_");
        		else if(sbMetadata.charAt(i) == '*') // Ignore '*' in metadata
        			;
        		else
        			sfMetadata.append(sbMetadata.charAt(i));
        	}
        	inPubType = false;
        	if(sfMetadata.length() > 0) {
        		String standardForm = sfMetadata.toString();
        		metadataList.add(standardForm.replaceAll("&amp;", "_").replaceAll("&","_"));
        	}
        } else if (inMetaData && eName.equalsIgnoreCase("meshheading")) {
        	StringBuffer sfMetadata = new StringBuffer();
        	for(int i = 0; i < sbMetadata.length(); i++) {
        		if(sbMetadata.charAt(i) == ' ' ||
        		   sbMetadata.charAt(i) == ',' ||
          		   sbMetadata.charAt(i) == '.' ||
          		   sbMetadata.charAt(i) == '-' ||
          		   sbMetadata.charAt(i) == '(' ||
          		   sbMetadata.charAt(i) == ')' ||
          		   sbMetadata.charAt(i) == '\'')
        			sfMetadata.append("_");
        		else if(sbMetadata.charAt(i) == '*') // Ignore '*' in metadata
        			;
        		else
        			sfMetadata.append(sbMetadata.charAt(i));
        	}
        	inMetaData = false;
        	if(sfMetadata.length() > 0) {
        		String standardForm = sfMetadata.toString();
        		metadataList.add(standardForm.replaceAll("&amp;", "_").replaceAll("&","_"));
        	}
        }
        else if (inMetaData && (eName.equalsIgnoreCase("descriptorname") || eName.equalsIgnoreCase("qualifiername"))) {
        	inMeshHeading = false;
        }
        else if (inIssn && eName.equalsIgnoreCase("issn")) {
        	inIssn = false;
        	currentArticle.setIssn(sbIssn.toString().trim());
        }
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
		} else if (inPubType)
			sbMetadata.append(ch,start,length);
		else if (inMetaData && inMeshHeading)
				sbMetadata.append(ch,start,length);
		else if (inIssn)
			sbIssn.append(ch,start,length);
	}
}
