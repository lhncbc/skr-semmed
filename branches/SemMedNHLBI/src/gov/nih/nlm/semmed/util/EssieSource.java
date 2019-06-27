package gov.nih.nlm.semmed.util;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

import gov.nih.nlm.se3.client.RemoteAPI.*;
import gov.nih.nlm.se3.core.SearchDefs.Doc;
import gov.nih.nlm.se3.core.SearchDefs.ExactArea;
import gov.nih.nlm.se3.core.SearchDefs.FreeArea;
import gov.nih.nlm.se3.core.SearchDefs.SearchDefs;
import gov.nih.nlm.semmed.model.PubmedArticle;
import gov.nih.nlm.uncommon.rpc.log.EventLog;
import gov.nih.nlm.uncommon.util.ExceptionUtils;
import gov.nih.nlm.common.utils.*;

import gov.nih.nlm.semmed.model.EssieArticle;

public class EssieSource extends ArticleDataSource
{
	public static final String	kDefaultHost	= Preferences
														.getString(
																"se3.client.server_host",
																"essie.nlm.nih.gov");
	public static final int		kDefaultPort	= Preferences
														.getInt(
																"se3.client.server_port",
																451);

	// public static final String	kLogName		= "RemoteAPI.Tests";
	private static Log			log				= LogFactory
														.getLog(EssieSource.class);
	private static Search		gSearch			= null;

	// TODO HLJ maybe this should be put in a preference file
	private static final int	MaxNSearchDocs	= 67696;
	private static EssieSource	myInstance;

	// TODO HLJ need to find the best place to define the number of results displayed
	private static final int defaultNPerPage = 20;


	public static EssieSource getInstance()
	{
		if (myInstance == null)
		{
			synchronized (EssieSource.class)
			{
				if (myInstance == null) {
					myInstance = new EssieSource();
					log.debug("EssieSource singleton object instantiated!");
				}
			}
		}
		return myInstance;
	}

	static
	{
		try
		{
			gSearch = new Search(kDefaultHost, kDefaultPort);
			log.debug("Essie search is instantiated!");
		}
		catch (Exception e)
		{
			// EventLog.logException(kLogName, "Could not initialize", e);
			throw new RuntimeException("Could not initialize:  "
					+ ExceptionUtils.report(e));
		}
	}


	public int[] search(HttpSession session, String term, String year1,
			String constraints, int maxCount) throws MalformedURLException,
			UnsupportedEncodingException, ParserConfigurationException,
			SAXException, IOException
	{
		int[] resultIds = null;
		try
		{
			SearchDefs theSearchDefs = gSearch.getSearchDefs();

			Doc theStudyDoc = theSearchDefs.getDocForUniquePartialName("Citation");
			ExactArea EssieTitle = theSearchDefs
					.getExactAreaForUniquePartialName("ArticleTitle");
			ExactArea EssiePMID = theSearchDefs
			 .getExactAreaForUniquePartialName("PMID");
			ExactArea EssieAuthor = theSearchDefs
			 .getExactAreaForUniquePartialName("Author");
			ExactArea EssieYear = theSearchDefs
			 .getExactAreaForUniquePartialName("PubYear");

			log.debug("search term : " + term);
			log.debug("search year : " + year1);
			log.debug("constraints : " + constraints);
			FreeArea EssieAll = theSearchDefs
					.getFreeAreaForUniquePartialName("EverythingUseful");

			Query theQuery = new Query(theStudyDoc);

			ExpressionClause theClause1 = new ExpressionClause("Clause 1",
					EssieAll, false, Clause.kRelaxationExpansion, term);

			theQuery.addClause(theClause1);
			if(!year1.equals("")) {
				ExpressionClause theClause2 = new ExpressionClause("Clause 2",
						EssieYear, false, Clause.kRelaxationExpansion, year1);
				theQuery.addClause(theClause2);
			}

			ResultDocOptions theResultDocOptions = new ResultDocOptions(1, Math
					.min(MaxNSearchDocs, maxCount), new ExactArea[] {EssiePMID, EssieTitle, EssieAuthor});

			theQuery.addOptions(theResultDocOptions);
			log.debug("Calling Essie search engine");

			if(gSearch == null) {
				log.debug("gSearch is null and cannot do essie search.");
			} else {
				log.debug("gSearch is not null!");
				Result theResult = gSearch.search(theQuery);
				log.debug("Essie search engine returned results.");
				session.setAttribute("gSearch", gSearch);
				session.setAttribute("EssieSearchResult", theResult);
				log.debug("The number of search result = " + theResult.getNResultDocs());
				resultIds = new int[theResult.getNResultDocs()];
				for (int i = 0; i < theResult.getNResultDocs(); i++)
				{
					// log.debug(theResult.getResultDoc(i)
					//		.getInfoText(0));
					resultIds[i] = Integer.parseInt(theResult.getResultDoc(i)
						.getInfoText(0));
				}
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return resultIds;
	}

	public List<EssieArticle> fetch(HttpSession session, int page)
			throws SAXException, ParserConfigurationException, IOException
	{
		List<EssieArticle> EssieArticles = new ArrayList<EssieArticle>();
		Result theResult = (Result) session.getAttribute("EssieSearchResult");
		if (theResult != null)
		{
			int startDocNum = page * defaultNPerPage;
			for (int i = 0; startDocNum < theResult.getNResultDocs() && i < defaultNPerPage ; startDocNum++, i++)
			{
				String Id = theResult.getResultDoc(startDocNum).getInfoText(0);
				String Title = theResult.getResultDoc(startDocNum).getInfoText(1);
				String Author = theResult.getResultDoc(startDocNum).getInfoText(2);
				EssieArticle theEssieArticle = new EssieArticle(Id, Title,"", Author, true);
				EssieArticles.add(theEssieArticle);
			}
		}
		return EssieArticles;
	}

	// TODO HLJ not used. Copy from PubmedArticle. Should be cleaned later.
	public List<PubmedArticle> fetch(HttpSession session, List<Integer> ids)
			throws SAXException, ParserConfigurationException, IOException
	{
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();
		PubMedArticleParser handler = new PubMedArticleParser();

		StringBuffer sb = new StringBuffer();

		sb.append("id=" + ids.get(0));
		for (int i = 1; i < ids.size(); i++)
			sb.append("&id=" + ids.get(i));
		URL url = new URL(
				"http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi");
		URLConnection conn = url.openConnection();
		conn.setDoOutput(true);
		OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		wr
				.write("db=pubmed&tool=semmed&email=shindongwoo@mail.nih.gov&retmode=xml&retmax=20000&"
						+ sb.toString());
		wr.flush();

		saxParser.parse(conn.getInputStream(), handler);

		return handler.getArticles();
	}

	// HLJ get full text of a document
	private String getDocText(HttpServletRequest request, HttpServletResponse response, long pmid)
	{
		HttpSession session = request.getSession();
		Search savedSearch = (Search)session.getAttribute("gSearch");
		if(savedSearch != null) {
			gSearch = savedSearch;
			// log.debug("There is a saved search and restore it!");
		}
		try
		{
			Doc theStudyDoc = gSearch.getSearchDefs().getDocForUniquePartialName("Citation");
			ExactArea EssieID = gSearch.getSearchDefs()
				.getExactAreaForUniquePartialName("PMID");

			String theXML =gSearch.getXML(theStudyDoc,EssieID, Long.toString(pmid));
			// TODO he function should not be used if data originally is in good format
			// String tmpXML = postProcText(tmpXML);
			return theXML;
		}
		catch (Exception e)
		{
			log.debug("Failed to get the XML of the document.");
			return null;
		}
	}
	// HLJ post process the xml text retrieved by Essie. This function is data specific. Hopefully,
	// we will never use it.
	/*
	private String postProcText(String theText)
	{
		String tmpStr = theText.replaceFirst("\\)\\: ", " ");
		return tmpStr;
	}
	*/

	// HLJ TODO tmp solution for converting XML to HTML.
	private String xmlToHtml (String theXML)
	{
		String xhtmlHeader = "<!DOCTYPE html\nPUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"" +
		"\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n" +
		"<html xmlns=\"http://www.w3.org/1999/xhtml\" " +
		"xml:lang=\"en\" lang=\"en\">\n<HEAD>";

		String t1[] = {"<CITATION>", "</CITATION>", "<ID>", "</ID>","<TITLE>", "</TITLE>", "<ABSTRACT>", "</ABSTRACT>",
				"<PUBLICHEALTHRELEVANCE/>", "<SPECIFICAIMS>", "</SPECIFICAIMS>", "<SPECIFICAIMS/>"};

		StringBuffer tmpBuffer = new StringBuffer(theXML);
		int pos = tmpBuffer.indexOf(t1[0]);
		tmpBuffer.replace(pos, pos + t1[0].length(), "");
		pos = tmpBuffer.indexOf(t1[1]);
		tmpBuffer.replace(pos,pos + t1[1].length(),"</BODY>\n</HTML>");
		pos = tmpBuffer.indexOf(t1[2]);
		tmpBuffer.replace(pos,pos + t1[2].length(), "ID: ");
		pos = tmpBuffer.indexOf(t1[3]);
		tmpBuffer.replace(pos, pos + t1[3].length(),"<br>");
		pos = tmpBuffer.indexOf(t1[4]);
		tmpBuffer.replace(pos, pos + t1[4].length(), "<H1 text-align=center>");
		pos = tmpBuffer.indexOf(t1[5]);
		tmpBuffer.replace(pos, pos + t1[5].length(),"</H1>");
		pos = tmpBuffer.indexOf(t1[6]);
		tmpBuffer.replace(pos, pos + t1[6].length(), "</HEAD>\n<BODY><H2>Abstract: </H2>");
		pos = tmpBuffer.indexOf(t1[7]);
		tmpBuffer.replace(pos, pos + t1[7].length(), "<p>");
		pos = tmpBuffer.indexOf(t1[8]);
		tmpBuffer.replace(pos, pos + t1[8].length(), "<p>");
		pos = tmpBuffer.indexOf(t1[9]);
		if(pos >= 0) // "Specific Aims" field is not empty
		{
			tmpBuffer.replace(pos, pos + t1[9].length(),"<H2>Specific Aims:</H2>");
			pos = tmpBuffer.indexOf(t1[10]);
			tmpBuffer.replace(pos, pos + t1[10].length(), "");
		}
		else
		{
			pos = tmpBuffer.indexOf(t1[11]);
			tmpBuffer.replace(pos, pos + t1[11].length(), "");
		}
		tmpBuffer.insert(0,xhtmlHeader);
		return (tmpBuffer.toString());
	}
	// HLJ display a document's full text
	public void displayDocText(HttpServletRequest request, HttpServletResponse response, long pmid)
		throws IOException
	{
		String theDocText = getDocText(request, response, pmid);
		// String path = request.getPathInfo();
		// String tmpXMLHeader = "<?xml version=\"1.0\"?><?xml-stylesheet href=\"/css/EssieGrant.css\" type=\"text/css\"?>\n";
		//http://localhost:8080/SemMedEssie/css/semmed.txt
		/*
		String path = request.getContextPath();
		String tmpXMLHeader = "<?xml version=\"1.0\"?>\n<?xml-stylesheet href=\"/css/tt.css\" type=\"text/css\"?>\n";
		theDocText.insert(0, tmpXMLHeader);
		*/
		String finalText = xmlToHtml(theDocText);
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.print(finalText);
		out.close();
	}
	// HLJ generates a list of full text in XML format
	public void displayDocTextList(HttpServletRequest request, HttpServletResponse response, HashSet<Long> pmidSet)
	{
		HttpSession session = request.getSession();
		Search savedSearch = (Search)session.getAttribute("gSearch");
		if(savedSearch != null) {
			gSearch = savedSearch;
			// log.debug("There is a saved search and restore it!");
		}

		StringBuffer xmlOutput = new StringBuffer("<CITATIONLIST>");
		try
		{
			Iterator it = pmidSet.iterator();
			while(it.hasNext()) {
				Doc theStudyDoc = gSearch.getSearchDefs().getDocForUniquePartialName("Citation");
				ExactArea EssieID = gSearch.getSearchDefs()
					.getExactAreaForUniquePartialName("PMID");

				String theXML =gSearch.getXML(theStudyDoc,EssieID, Long.toString((Long)it.next()));
				xmlOutput.append(theXML);
			}
			xmlOutput.append("</CITATIONLIST>");
			response.setContentType("text/xml");
			PrintWriter out = response.getWriter();
			out.print(xmlOutput.toString());
			// log.debug("XML Output : " + xmlOutput.toString());
			out.close();
		}
		catch (Exception e)
		{
			log.debug("Essie search error. Possibly search object is missing.");
		}
	}
}
