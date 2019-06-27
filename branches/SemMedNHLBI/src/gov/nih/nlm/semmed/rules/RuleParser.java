package gov.nih.nlm.semmed.rules;

import gov.nih.nlm.semmed.struts.action.QuestionAction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class RuleParser {

	private static Pattern setPattern = Pattern.compile("SET\\(\\s*([a-zA-Z0-9_\\.\\(\\)]+)\\s*\\):([^;]*);");
	private static Pattern predicatePattern = Pattern.compile("(?:DOESNOT)?EXISTS\\(\\s*([a-zA-Z0-9_\\.\\(\\)]+)\\s*\\):([^;]*);");
	private static Pattern rulePattern = Pattern.compile("RULE\\(\\s*([a-zA-Z0-9_\\.\\(\\)]+)\\s*\\):([^;]*);");

	private static Pattern subjectPattern = Pattern.compile("subject\\(\\s*([a-zA-Z0-9_\\.\\(\\)]+)\\s*\\)");
	private static Pattern objectPattern = Pattern.compile("object\\(\\s*([a-zA-Z0-9_\\.\\(\\)]+)\\s*\\)");
	private static Pattern relationPattern = Pattern.compile("predicate\\(\\s*([a-zA-Z0-9_\\.\\(\\)]+)\\s*\\)");

	private static Log log = LogFactory.getLog(RuleParser.class);

	public static String read(InputStream is) throws IOException{

		StringBuffer sb = new StringBuffer();

		BufferedReader bfs = new BufferedReader(new InputStreamReader(is));

		String s = null;
		while((s=bfs.readLine())!=null)
			sb.append(s);
		return sb.toString();
	}

	public static Map<String,Predicate> parse(String sb) throws IOException{

		Map<String,Set<String>> sets = new HashMap<String,Set<String>>();
		Map<String,Predicate> predicates = new HashMap<String,Predicate>();
		Map<String,Predicate> rules = new HashMap<String,Predicate>();

		Matcher m = setPattern.matcher(sb);

		while(m.find()){
			Set<String> newSet = new HashSet<String>();
			String name = m.group(1);
			// log.debug("group name in the set: " + name);
			String[] elements = m.group(2).split("\\|");
			for(String element:elements) //TODO[Alejandro] check for the MINUS thing
				if(element.startsWith("MINUS")) {
					String[] elementArray = element.split("[,()]");
					// String tempSet = new String("SET(" + elementArray[1] + ")");
					String wholeSetName = new String(elementArray[1]);
					String excludedSetName = new String(elementArray[2]);
					// log.debug("Newly composed Whole Set Name:" + wholeSetName);
					// log.debug("Newly composed Elcluded Set Name:" + excludedSetName);
					Set<String> WholeSet = sets.get(wholeSetName);
					// if(WholeSet != null)
					// 	log.debug("Whole set:" + WholeSet.toString());
					// else
					// 	log.debug("Whole set is null.");
					Set<String> ExcludedSet = sets.get(excludedSetName);
					Set<String> NewWholeSet = new HashSet(WholeSet);
					NewWholeSet.removeAll(ExcludedSet);
					newSet.addAll(NewWholeSet);
				}
				else if (sets.containsKey(element))
					newSet.addAll(sets.get(element));
				else
					newSet.add(element);
			sets.put(name,newSet);
		}

		m = predicatePattern.matcher(sb);



		while(m.find()){
			//Set<String> newSet = new HashSet<String>();
			String name = m.group(1);
			String[] elements = m.group(2).split(",");

			Set<String> subject = null;
			Set<String> object = null;
			Set<String> predicate = null;

			for(int i=0;i<elements.length;i++){
				Matcher m1 = subjectPattern.matcher(elements[i]);

				if (m1.matches()){
					subject = sets.get(m1.group(1));
					continue;
				}

				m1 = objectPattern.matcher(elements[i]);

				if (m1.matches()){
					object = sets.get(m1.group(1));
					continue;
				}

				m1 = relationPattern.matcher(elements[i]);

				if (m1.matches())
					predicate = sets.get(m1.group(1));
			}

			Predicate p = new Exists(subject==null?new HashSet<String>():subject,
					                 predicate==null?new HashSet<String>():predicate,
					                 object==null?new HashSet<String>():object);
			if (m.group(0).startsWith("DOESNOT")){
				List<Predicate> lp = new ArrayList<Predicate>(1);
				lp.add(p);
				p = new Not().eval(lp);
			}
			predicates.put(name,p);
		}



		m = rulePattern.matcher(sb);
		while(m.find()){
			String name = m.group(1);

			String[] dis = m.group(2).split("\\|");

			Predicate p = null;

			if (dis.length==0)
				throw new Error("Don't know how to handle rules with no antecentes");//p = predicates.get(dis[0]); //MAKES NO SENSE, but this rule has no antecedent anyway
			else{
				List<Predicate> orOperands = new ArrayList<Predicate>(dis.length);

				for(int i=0;i<dis.length;i++){
					String[] con = dis[i].split(",");

					if (con.length==1)
						if (predicates.get(con[0].trim())==null)
							throw new Error("Don't know predicate named '"+con[0]+"'");
						else
							orOperands.add(predicates.get(con[0].trim()));
					else{
						List<Predicate> andOperands = new ArrayList<Predicate>(con.length);
						for(int j=0;j<con.length;j++)
							if (predicates.get(con[j].trim())==null)
								throw new Error("Don't know predicate named '"+con[j]+"'");
							else
								andOperands.add(predicates.get(con[j].trim()));
						orOperands.add(new And().eval(andOperands));
					}
				}
				p = new Or().eval(orOperands);
			}
			rules.put(name,p);
		}

		return rules;
	}
}
