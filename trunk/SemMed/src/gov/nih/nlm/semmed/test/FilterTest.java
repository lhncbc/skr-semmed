package gov.nih.nlm.semmed.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import gov.nih.nlm.semmed.model.APredication;
import gov.nih.nlm.semmed.model.APredicationList;
import gov.nih.nlm.semmed.rules.Filter;
import gov.nih.nlm.semmed.rules.Predicate;
import gov.nih.nlm.semmed.rules.RuleParser;
import gov.nih.nlm.semmed.util.ArticleDataSource;
import junit.framework.TestCase;

public class FilterTest extends TestCase {

	private Map<String,Predicate> rules;
	
	public void setUp() throws FileNotFoundException, IOException{
		System.out.println(System.getProperty("user.dir"));
		rules = RuleParser.parse(new FileInputStream(new File(System.getProperty("user.dir"),"webroot/rsc/rules.txt")));	
	}
	
	
	public void testFilterNonRelevants() throws SQLException {
		APredicationList predications = new APredicationList(new int[]{16359896},ArticleDataSource.SourceType.MEDLINE);
		
		List<APredication> relevantPredications = new ArrayList<APredication>();
		List<Integer> relevantCitations = new ArrayList<Integer>();
		Filter.filterNonRelevants(predications,rules.get("0"), relevantPredications, relevantCitations,new ArrayList<APredication>(),new ArrayList<Integer>());
		
		assertEquals(1,relevantCitations.size());
		assertEquals(16359896,relevantCitations.get(0).intValue());
	}

}
