/**
 *
 */
package gov.nih.nlm.semmed.util;

import gov.nih.nlm.semmed.model.APredicationList;
import gov.nih.nlm.semmed.model.TestPredicationList;
import gov.nih.nlm.semmed.model.PubmedArticle;
import gov.nih.nlm.semmed.model.SemMedDocument;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;

import org.xml.sax.*;

import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

/**
 * @author rodriguezal
 *
 */
public class MedlineSource extends ArticleDataSource{


	private static Log log = LogFactory.getLog(MedlineSource.class);

	private static final int MAX_RETURN = 1000000;

	private static MedlineSource myInstance;

	private MedlineSource(){}

	public static MedlineSource getInstance(){
		if (myInstance==null)
		synchronized(MedlineSource.class){
			if(myInstance==null)
				myInstance = new MedlineSource();
		}
		return myInstance;
	}

	public int[] search(HttpSession session, String term,String startDate,String endDate,int maxCount) throws
			MalformedURLException, UnsupportedEncodingException, ParserConfigurationException, SAXException, IOException{
		String medlineStartDate = startDate.substring(startDate.lastIndexOf("/")+1) + "/" + startDate.substring(0,startDate.lastIndexOf("/"));
		String medlineEndDate = endDate.substring(endDate.lastIndexOf("/")+1) + "/" + endDate.substring(0,endDate.lastIndexOf("/"));
		// String termWithAbstract = term + Constants.QUERY_WITH_ABSTRACT_ENGLISH;
		URL url = new URL("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pubmed&usehistory=y&tool=semmed&"+
				// "email=rodriguezal@mail.nih.gov&term="+URLEncoder.encode(term,"UTF-8")+"&mindate="+URLEncoder.encode(medlineStartDate,"UTF-8")+
				// Search with Abstract
				"email=shindongwoo@mail.nlm.nih.gov&term="+URLEncoder.encode(term,"UTF-8")+"&mindate="+URLEncoder.encode(medlineStartDate,"UTF-8")+
				"&maxdate="+URLEncoder.encode(medlineEndDate,"UTF-8")+"&retmode=xml&retstart=0"+
				((maxCount>0)?("&retmax="+maxCount):"&retmax="+MedlineSource.MAX_RETURN)+"&sort=pub+date&datetype=pdat");


		/* String request = Constants.EUTILS;
		request += "/esearch.fcgi?db=pubmed&sort=pub+date&usehistory=y&email=shindongwoo@mail.nlm.nih.gov&term=";
		request += term + Constants.QUERY_DEFAULT_LIMITS;
		String medlineStartDate = startDate.substring(startDate.lastIndexOf("/")+1) + "/" + startDate.substring(0,startDate.lastIndexOf("/"));
		String medlineEndDate = endDate.substring(endDate.lastIndexOf("/")+1) + "/" + endDate.substring(0,endDate.lastIndexOf("/"));
		//log.debug("Start Date: " + medlineStartDate + "|" + medlineEndDate);
		request += "(" + medlineStartDate + "[PDAT]:" + medlineEndDate + "[PDAT])";
		request += "&retmode=xml&retstart=0"+
		((maxCount>0)?("&retmax="+maxCount):"&retmax="+MedlineSource.MAX_RETURN)+"&sort=pub+date&datetype=pdat";
		request = request.replaceAll(" ", "%20");
		log.debug("Search query:" + request);
		// URL url = new URL(request); */


		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();
		PubMedIDParser handler = new PubMedIDParser(maxCount);

		saxParser.parse( url.toString(), handler );
		// saxParser.parse( request.toString(), handler );
		session.setAttribute("webenv", handler.getWebEnv());
		return handler.getIds();
	}

	public List<PubmedArticle> fetch(HttpSession session,int page)
			throws SAXException, ParserConfigurationException, IOException{

		String webEnv = (String)session.getAttribute("webenv");
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();
		PubMedArticleParser handler = new PubMedArticleParser();

		String url = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&tool=semmed&"+
				"email=shindongwoo@mail.nlm.nih.gov&retmode=xml&retmax=20&"+
				"retstart="+(page*20)+"&"+webEnv;
		saxParser.parse(url, handler );

		return handler.getArticles();
	}


	public List<PubmedArticle> fetchTitle(List<Integer> ids) throws Exception{
		DataSource ds = APredicationList.getDataSource();

		String connector = " or PMID in ";
		StringBuffer q = new StringBuffer("SELECT PMID,TITLE from TITLES where PMID in ");
		// StringBuffer q = new StringBuffer("SELECT PMID,TITLE from SENTENCE where TYPE = \"ti\" and PMID in ");

		int added=0;
		for(int i=0;i<ids.size();){
			if (added>0)
				q.append(connector);
			q.append("(");
			q.append(ids.get(i++));
			added++;
			while (added%1500!=0 && i<ids.size()){
				q.append(",");
				q.append(ids.get(i++));
				added++;
			}
			q.append(")");
		}

		Connection conn = ds.getConnection();

		Statement s = conn.createStatement();

		ResultSet rs = s.executeQuery(q.toString());

		List<PubmedArticle> articles = new ArrayList<PubmedArticle>();

		while(rs.next()){
			PubmedArticle article = new PubmedArticle();
			article.setId(""+rs.getInt(1));
			article.setTitleText(rs.getString(2));
			articles.add(article);
		}
		conn.close();
		return articles;
	}

	public List<PubmedArticle> fetchTitleFromTest(List<Integer> ids) throws Exception{
		DataSource ds = TestPredicationList.getDataSource();

		String connector = " or PMID in ";
		StringBuffer q = new StringBuffer("SELECT PMID,TITLE from TITLES where PMID in ");

		int added=0;
		for(int i=0;i<ids.size();){
			if (added>0)
				q.append(connector);
			q.append("(");
			q.append(ids.get(i++));
			added++;
			while (added%1500!=0 && i<ids.size()){
				q.append(",");
				q.append(ids.get(i++));
				added++;
			}
			q.append(")");
		}

		Connection conn = ds.getConnection();

		Statement s = conn.createStatement();

		ResultSet rs = s.executeQuery(q.toString());

		List<PubmedArticle> articles = new ArrayList<PubmedArticle>();

		while(rs.next()){
			PubmedArticle article = new PubmedArticle();
			article.setId(""+rs.getInt(1));
			article.setTitleText(rs.getString(2));
			articles.add(article);
		}
		conn.close();
		return articles;
	}

	public List<PubmedArticle> fetch(HttpSession session,List<Integer> ids) throws SAXException, ParserConfigurationException, IOException{
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();
		PubMedArticleParser handler = new PubMedArticleParser();

		StringBuffer sb = new StringBuffer();

		sb.append("id="+ids.get(0));
		for(int i=1;i<ids.size();i++)
			sb.append("&id="+ids.get(i));
		URL url = new URL("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi");
        URLConnection conn = url.openConnection();
        conn.setDoOutput(true);
        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
        wr.write("db=pubmed&tool=semmed&email=shindongwoo@mail.nlm.nih.gov&retmode=xml&retmax=20000&"+sb.toString());
        wr.flush();


		saxParser.parse(conn.getInputStream(), handler );


		return handler.getArticles();
	}

	public static Element buildMedlineXml(List cits)  {
		Element root = null;
	   	if (cits.size() > 0) {
    		root = new Element("PubmedArticleSet");
    		ListIterator articleIter = cits.listIterator();
    		while (articleIter.hasNext()) {
    			PubmedArticle article = (PubmedArticle)articleIter.next();
    			if (article.getInclude()) {
    				Element pubmedArticleNode = article.toXml();
		    		root.addContent(pubmedArticleNode);
        		}
    		}
	   	}
		return root;
	}

	/*
	 *
	 * Create a list of PubmedArticle by extracting them from an XML document
	 */
	public static List<SemMedDocument> parseMedlineXml(Element e)  {
		List<SemMedDocument> parsedCits = new Vector<SemMedDocument>();
		if (e != null) {
			List citationList = e.getChildren("PubmedArticle");
			Iterator citIter = citationList.iterator();
			while (citIter.hasNext()) {
				Element cit = (Element) citIter.next();
				PubmedArticle citation = new PubmedArticle(cit);
				log.debug("PMID: " + citation.getId());
				parsedCits.add(citation);
			}
		}
		return parsedCits;
	}

}
