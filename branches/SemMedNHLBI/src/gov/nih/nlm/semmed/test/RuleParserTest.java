package gov.nih.nlm.semmed.test;

import gov.nih.nlm.semmed.rules.Predicate;
import gov.nih.nlm.semmed.rules.RuleParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import junit.framework.TestCase;

public class RuleParserTest extends TestCase {
	String ruleString;
	public void testParse() throws IOException{
		System.out.println(System.getProperty("user.dir"));
		System.out.println(System.getProperty("user.dir"));
		ruleString = RuleParser.read(new FileInputStream(new File(System.getProperty("user.dir"),"webroot/rsc/rules.txt")));

		/* for(Map.Entry<String,Predicate> e : map.entrySet()){
			System.out.println("Rules "+e.getKey());
			System.out.print("\t");
			System.out.println(e.getValue());
		}	*/
	}

}
