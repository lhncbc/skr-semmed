package gov.nih.nlm.semmed.util;

import gov.nih.nlm.semmed.model.SemMedDocument;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.List;

import javax.servlet.http.HttpSession;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

/**
 * 
 * @author rodriguezal
 */
public abstract class ArticleDataSource {

	public enum SourceType {
		MEDLINE("P"), CLINICAL_TRIALS("C");
		
		private String type;
		
		private SourceType(String s){
			type = s;
		}
		
		public String toString(){
			return type;
		}
	};

	public static ArticleDataSource getInstance(SourceType type) {
		switch (type) {
		case MEDLINE:
			return MedlineSource.getInstance();
		case CLINICAL_TRIALS:
			return ClinicalStudySource.getInstance();
		default:
			throw new IllegalArgumentException();
		}
	}

	public abstract int[] search(HttpSession session,String term, String startDate,String endDate, int maxCount) throws MalformedURLException,
			UnsupportedEncodingException, ParserConfigurationException,SAXException, IOException;

	public abstract List<? extends SemMedDocument> fetch(HttpSession session, int page) throws SAXException, ParserConfigurationException, IOException;
	
	public abstract List<? extends SemMedDocument> fetch(HttpSession session,List<Integer> ids) throws SAXException, ParserConfigurationException, IOException;
}
