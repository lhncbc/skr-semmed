/**
 * 
 */
package gov.nih.nlm.semmed.model;

import gov.nih.nlm.semmed.util.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.util.LabelValueBean;
import org.jdom.Element;

/**
 * @author hkilicoglu
 *
 */
public class SemrepResultSet {

	private static Log log = LogFactory.getLog(SemrepResultSet.class);		
	private List<SentencePredication> sentencePredications;
	
	
	/**
	 * @param sentencePredications
	 */
	public SemrepResultSet(List<SentencePredication> sentencePredications) {
		this.sentencePredications = sentencePredications;
	}

	/**
	 * 
	 */
	public SemrepResultSet() {
	}
	
	public SemrepResultSet(Element e){
		
	}
	
	/**
	 * @return Returns the sentencePredications.
	 */
	public List<SentencePredication> getSentencePredications() {
		return sentencePredications;
	}

	/**
	 * @param sentencePredications The sentencePredications to set.
	 */
	public void setSentencePredications(List<SentencePredication> sentencePredications) {
		this.sentencePredications = sentencePredications;
	}
	
	public String translate(String name, String lang) {
		log.debug("Name: " + name);
		log.debug("Language: " + lang);
		Iterator predIter = sentencePredications.iterator();
		while (predIter.hasNext()) {
			SentencePredication sp = (SentencePredication)predIter.next();
			Predication pred = sp.getPredication();
			Iterator argIter = pred.getPredicationArgumentSet().iterator();
			while (argIter.hasNext()) {
				Concept c = ((PredicationArgument)argIter.next()).getConceptSemtype().getConcept();
				if (name.equals(c.getPreferredName())) {
					Iterator trIter = c.getConceptTranslationSet().iterator();
					while (trIter.hasNext()) {
						ConceptTranslation ct = (ConceptTranslation)trIter.next();
						if (lang.equals(ct.getLanguage())) 
							return ct.getTranslation();
					}
				}
			}
		}
		return name;
	}
	
	
	/**
	 * 
	 * 
	 * Returns a list of the relevant concepts present in this SemrepResultSet, sorted by frequency (desc) and name (asc)
	 * 
	 * 
	 * 
	 * @param type determines the relevant semantic types. "genetic" or "treatment" : DISORDER
	 *                                                     "interaction" : DRUG_CHEM
	 *                                                     "pharmacogenomics" : SUBSTANCE and DISORDER and HUMAN
	 *                                                     "diagnosis" : DISORDER and DIAGNOSIS
	 * @return
	 */
	public List getRelevantConcepts(String type){ //TODO make type an enumeration [Alejandro]
        ArrayList<LabelValueBean> relevantConcepts = null;
        log.debug("Seed Type: " + type);
		if (sentencePredications.size() > 0) {	
			Map<String,Integer> map = new HashMap<String,Integer>(); //TODO Don't create a new instance until you know you will need it [Alejandro]
			Iterator predIter = sentencePredications.iterator();
			while (predIter.hasNext()) {
				SentencePredication sp = (SentencePredication)predIter.next();
				Predication pred = sp.getPredication();
				Set subjects = pred.getBestSubjectSemtypes();
				Set objects = pred.getBestObjectSemtypes();	
				Map<String,Integer> concMap = new HashMap<String,Integer>(); //TODO Don't create this until you know you will need it [Alejandro]
				if ("genetic".equals(type) || "treatment".equals(type)) 
					concMap = countArgs(map, subjects, objects, Constants.DISORDER_SEMGROUP);						
				else if ("interaction".equals(type)) 
					concMap = countArgs(map, subjects, objects, Constants.DRUG_CHEM_SEMGROUP);				
                else if ("pharmacogenomics".equals(type)) {
					List<String> relevantSemTypes = new ArrayList<String>(); //TODO Couldn't we have this arraylist as a class constant? [Alejandro]
					relevantSemTypes.addAll(Constants.SUBSTANCE_SEMGROUP);
					relevantSemTypes.addAll(Constants.DISORDER_SEMGROUP);
					relevantSemTypes.addAll(Constants.HUMAN_SEMGROUP);
					concMap = countArgs(map, subjects, objects, relevantSemTypes);							
				} 
                else if ("diagnosis".equals(type)) {
					List<String> relevantSemTypes = new ArrayList<String>(); //TODO Couldn't we have this arraylist as a class constant? [Alejandro]
					relevantSemTypes.addAll(Constants.DISORDER_SEMGROUP);
					relevantSemTypes.addAll(Constants.DIAGNOSIS_SEMGROUP);
					concMap = countArgs(map, subjects, objects, relevantSemTypes);						
                }
				map = concMap; //TODO this is not necessary as map gets modified to be concMap in one of the four (exhaustive cases above) [Alejandro] 
			}
			log.debug("Map size:" + map.size());
						
			//TODO Would be better to have the map sorted from the begining? [Alejandro]
			ArrayList<Map.Entry<String, Integer>> tempConcs = new ArrayList<Map.Entry<String, Integer>>( map.entrySet() );
	         Collections.sort( tempConcs , new Comparator<Map.Entry<String,Integer>>() { //TODO This comparator can be a singleton [Alejandro]
	             public int compare( Map.Entry<String,Integer> e1 , Map.Entry<String,Integer> e2 )
	             {
	                 Integer first = e1.getValue();
	                 Integer second = e2.getValue();
	                 if (!(first.equals(second)))
	                	 return second.compareTo( first );
	                 return e1.toString().compareToIgnoreCase(e2.toString());
	             }
	         });
	         
	         Iterator concIter = tempConcs.iterator();
	         relevantConcepts = new ArrayList<LabelValueBean>();
			 while (concIter.hasNext()) {
				Map.Entry entry = (Map.Entry)concIter.next();
				String concept = (String)entry.getKey();	
				String value =  concept + "(" + (Integer)entry.getValue() + ")";
				relevantConcepts.add(new LabelValueBean(concept, value));	         
			}
		}
		return relevantConcepts; 
	}	 
	
	
	/**
	 * 
	 * Adds to cntMap the number of occurrences of concepts in subSet and objSet 
	 * 
	 *  Side effects on cntMap, a return not really necessary [Alejandro]
	 * 
	 * @param cntMap
	 * @param subjSet a set of ConceptSemtype
	 * @param objSet a set if ConcetSemtype
	 * @param semgroup
	 * @return
	 */
	private Map<String,Integer> countArgs(Map<String,Integer> cntMap, Set subjSet, Set objSet, List semgroup) {	
		Iterator subjIter = subjSet.iterator();
		Map<String,Integer> map = cntMap;
		Integer count = null;
		while (subjIter.hasNext()) {
			ConceptSemtype csSubj = (ConceptSemtype)subjIter.next();
			if ("N".equals(csSubj.getNovel())) 
				continue;
			String subj = csSubj.getConcept().getPreferredName(); //TODO This should go inside the 'if' below [Alejandro]
			String s_semtype = csSubj.getSemtype();
			if (semgroup.contains(s_semtype)) {
				if (map.containsKey(subj)) {
					count = (Integer)map.get(subj);								
					map.put(subj, new Integer(count.intValue() + 1));						
				} else {
					map.put(subj, new Integer(1));
				}
			}
		}
		Iterator objIter = objSet.iterator();
		while (objIter.hasNext()) {
			ConceptSemtype csObj = (ConceptSemtype)objIter.next();
			if ("N".equals(csObj.getNovel())) 
				continue;
			String obj = csObj.getConcept().getPreferredName(); //TODO This should go inside the 'if' below [Alejandro]
			String o_semtype = csObj.getSemtype();
			if (semgroup.contains(o_semtype)) {
				if (!subjSet.contains(csObj)) {
					if (map.containsKey(obj)) {
						count = (Integer)map.get(obj);							
						map.put(obj, new Integer(count.intValue() + 1));										
					} else {
						map.put(obj, new Integer(1));
					}
				}
			}
		}

		return map;
	}

}
