/**
 *
 */
package gov.nih.nlm.semmed.util;

import gov.nih.nlm.semmed.model.ClinicalStudy;
import gov.nih.nlm.semmed.model.SemMedDocument;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jdom.Element;
import org.xml.sax.SAXException;

/**
 * @author halil
 *
 */
public class ClinicalStudySource extends ArticleDataSource{

	// private static Log log = LogFactory.getLog(ClinicalStudyUtils.class);

	private static final int MAX_RETURN = 1000000;

	private static ClinicalStudySource myInstance;

	private ClinicalStudySource() {}

	public static ClinicalStudySource getInstance(){
		if (myInstance==null)
			synchronized(ClinicalStudySource.class){
				if(myInstance==null)
					myInstance = new ClinicalStudySource();
			}
		return myInstance;
	}


	public int[] search(HttpSession session, String term, String startDate,String endDate, int maxCount) throws MalformedURLException,
			UnsupportedEncodingException, ParserConfigurationException, SAXException, IOException {

		URL url = new URL("http://clinicaltrials.gov/search/term="
				+ URLEncoder.encode(term, "UTF-8")
				+ "?displayxml=true");

		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();
		ClinicalStudyParser handler = new ClinicalStudyParser(maxCount>0?maxCount:MAX_RETURN);

		saxParser.parse(url.toString(), handler);
		session.setAttribute("clinicalStudies",handler.getStudies());

		return handler.getIds();
	}

	public List<ClinicalStudy> fetch(HttpSession session, int page)
			throws SAXException, ParserConfigurationException, IOException {

		ArrayList<ClinicalStudy> list = new ArrayList<ClinicalStudy>(20);
		ClinicalStudy[] studiesLocal = (ClinicalStudy[])session.getAttribute("clinicalStudies");

		for(int i=page*20;i<(page+1)*20 && i < studiesLocal.length;i++)
			list.add(studiesLocal[i]);

		return list;
	}

	@SuppressWarnings("unchecked")
	public List<ClinicalStudy> fetch(HttpSession session,List<Integer> ids) throws SAXException, ParserConfigurationException, IOException{
		ArrayList<ClinicalStudy> result = new ArrayList<ClinicalStudy>(ids.size());

		List<ClinicalStudy> all = (List<ClinicalStudy>)session.getAttribute("clinicalStudies");

		for(ClinicalStudy cs: all)
			for(int i:ids)
				if (Integer.parseInt(cs.getId().substring(4))==i)
					result.add(cs);

		return result;
	}

	public static Element buildStudyXml(List cits) {
		Element root = null;
		if (cits.size() > 0) {
			root = new Element("ClinicalStudySet");
			ListIterator studyIter = cits.listIterator();
			while (studyIter.hasNext()) {
				ClinicalStudy study = (ClinicalStudy) studyIter.next();
				if (study.getInclude()) {
					Element studyNode = study.toXml();
					root.addContent(studyNode);
				}
			}
		}
		return root;
	}

	public static List<SemMedDocument> parseStudyXml(Element e) {
		List<SemMedDocument> parsedStudies = new ArrayList<SemMedDocument>();
		if (e != null) {
			List studyList = e.getChildren("clinical_study");
			Iterator studyIter = studyList.iterator();
			while (studyIter.hasNext()) {
				Element s = (Element) studyIter.next();
				ClinicalStudy study = new ClinicalStudy(s);
				parsedStudies.add(study);
			}
		}
		return parsedStudies;
	}

	public void displayDocText(HttpServletRequest request, HttpServletResponse response, long pmid)
	throws IOException
	{
		; // do nothing
	}
	public void displayDocTextList(HttpServletRequest request, HttpServletResponse response, HashSet<Long> pmidSet)
	{
		;
	}

}
