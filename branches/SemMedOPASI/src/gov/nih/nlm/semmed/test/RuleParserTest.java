package gov.nih.nlm.semmed.test;

import gov.nih.nlm.semmed.rules.Predicate;
import gov.nih.nlm.semmed.rules.RuleParser;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import junit.framework.TestCase;

public class RuleParserTest extends TestCase {

	public void testParse() throws IOException{
		System.out.println(System.getProperty("user.dir"));
		Map<String,Predicate> map = RuleParser.parse(new FileInputStream("WebRoot/rsc/rules.txt"));
				
		for(Map.Entry<String,Predicate> e : map.entrySet()){
			System.out.println("Rules "+e.getKey());
			System.out.print("\t");
			System.out.println(e.getValue());
		}	
	}

}
