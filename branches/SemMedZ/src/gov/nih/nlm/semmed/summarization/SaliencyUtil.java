/**
 *
 */
package gov.nih.nlm.semmed.summarization;

import gov.nih.nlm.semmed.model.APredication;
import gov.nih.nlm.semmed.model.Concept;
import gov.nih.nlm.semmed.model.ConceptSemtype;
import gov.nih.nlm.semmed.model.Predication;
import gov.nih.nlm.semmed.model.SentencePredication;
import gov.nih.nlm.semmed.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author hkilicoglu
 *
 */
public class SaliencyUtil {

	private static Log log = LogFactory.getLog(SaliencyUtil.class);

	public static void printSPList(List l){
		log.debug("SentencePredication list size: " + l.size());
		Iterator iter = l.iterator();
		while (iter.hasNext())
			log.debug(((SentencePredication)iter.next()).toString());
	}

	public static void printMap(Map map, String keyType, String valType) {
		log.debug("Map size: "  + map.size());
		Iterator iter = map.keySet().iterator();
		while (iter.hasNext()){
			Object o = iter.next();
//			Object val = map.get(o);
			if (valType.equals("Integer")){
				int count = ((Integer)map.get(o)).intValue();
				log.debug("Count map:" +  o.toString() + "|" + count);
			}
			else if (valType.equals("Boolean")) {
				boolean salient = ((Boolean)map.get(o)).booleanValue();
				log.debug("Count map:" +  o.toString() + "|" + salient);
			}
			else if (valType.equals("Double")) {
				double d = ((Double)map.get(o)).doubleValue();
				log.debug("Count map:" +  o.toString() + "|" + d);
			}
			else if (valType.equals("BooleanMap")) {
				Map m = (Map)map.get(o);
				Iterator iter1 = m.keySet().iterator();
				while (iter1.hasNext()) {
					String s = (String)iter1.next();
					boolean b = ((Boolean)m.get(s)).booleanValue();
					log.debug("Count map:" +  o.toString() + "|" + s + "|" + b);
				}
			}
			else if (valType.equals("DoubleMap")) {
				Map m = (Map)map.get(o);
				Iterator iter1 = m.keySet().iterator();
				while (iter1.hasNext()) {
					String s = (String)iter1.next();
					double d = ((Double)m.get(s)).doubleValue();
					log.debug("Count map:" +  o.toString() + "|" + s + "|" + d);
				}
			}
			else if (valType.equals("IntegerMap")) {
				Map m = (Map)map.get(o);
				Iterator iter1 = m.keySet().iterator();
				while (iter1.hasNext()) {
					String s = (String)iter1.next();
					int d = ((Integer)m.get(s)).intValue();
					log.debug("Count map:" +  o.toString() + "|" + s + "|" + d);
				}
			}

		}
	}


	//TODO [Alejandro] The key on concepts should be the CUI
	public static Map<String,Integer> computeConceptCountMap(List<APredication> sps) {
		Map<String,Integer> countMap = new HashMap<String,Integer>();
		for(APredication p : sps){
			// Set predicationArgs = p.getBestSubjectSemtypes();
			// predicationArgs.addAll(p.getBestObjectSemtypes());
			// Iterator paIter = predicationArgs.iterator();
			/* String subj = null;
			String obj = null;
			if(p.subject != null && p.subject.isEmpty() != true) {
				subj = p.subject.get(0);
				if (countMap.containsKey(subj)) {
					int count = ((Integer)countMap.get(subj)).intValue();
					count++;
					countMap.put(subj, new Integer(count));
				} else
					countMap.put(subj, new Integer(1));
			}
			if(p.object != null && p.object.isEmpty() != true) {
				obj = p.object.get(0);
				if (countMap.containsKey(obj)) {
					int count = ((Integer)countMap.get(obj)).intValue();
					count++;
					countMap.put(obj, new Integer(count));
				} else
					countMap.put(obj, new Integer(1));
			}
		}

		log.debug("-------------------- Concept Count Map Size : " + countMap.size());
		Set keyset = countMap.keySet();
		for(Object key : keyset) {
			Integer c = countMap.get(key);
			log.debug(key + ", " +c);

		}
			return countMap; */
			for(String s: p.subject)
				if (countMap.containsKey(s)) {
					int count = countMap.get(s)+1;
					countMap.put(s, count);
				} else
					countMap.put(s, 1);
			for(String s: p.object)
				if (countMap.containsKey(s)) {
					int count = countMap.get(s)+1;
					countMap.put(s, count);
				} else
					countMap.put(s, 1);
		}
		/* log.debug("-------------------- Concept Count Map Size : " + countMap.size());
		Set keyset = countMap.keySet();
		for(Object key : keyset) {
			Integer c = countMap.get(key);
			log.debug(key + ", " +c);

		}
		log.debug("-------------------- Concept Count Map Size : " + countMap.size()); */
		return countMap;
	}

	public static Map<APredication,Integer> computePredicationCountMap(List<APredication> sps) {
		Map<APredication,Integer> countMap = new HashMap<APredication,Integer>();
		for(APredication p:sps){
			if (countMap.containsKey(p)) {
				int count = countMap.get(p);
				countMap.put(p, count+1);
			} else
				countMap.put(p, 1);
		}
		return countMap;
	}

	public static double computeAvgActivationWeight(Map<? extends Object,Integer> countMap) {
		double avgWeight = 0.0;
		int count = 0;
		int distinct_count = 0;
		for(Map.Entry<? extends Object, Integer> e : countMap.entrySet()){
			distinct_count++;
			count += e.getValue();
		}
		avgWeight = (double)count/(double)distinct_count;
		// log.debug("computeAvgActivationWeight: count = " + count + ", distinct_count = " + distinct_count + ", avgWeight = " + avgWeight);
		return avgWeight;
	}

	public static <T> Map<T,Double> computeAvgSumOtherMap(Map<T,Integer> countMap, int totalCount) {
		Map<T,Double> avgSumOtherMap = new HashMap<T,Double>();
		for(T o :countMap.keySet()){
			double avgSumOther = 0.0;
			if (countMap.size() > 1)
				avgSumOther = (totalCount - (countMap.get(o))) / (double)(countMap.size() -1);
			else
				avgSumOther = totalCount - countMap.get(o);
			avgSumOtherMap.put(o, avgSumOther);
		}
		Set keyset = avgSumOtherMap.keySet();
		/* log.debug("-------------------- computeAvgSumOtherMap ----------------------");
		for(Object key : keyset) {
			Double c = avgSumOtherMap.get(key);
			// log.debug(key + ", " +c);

		}
		log.debug("-------------------- computeAvgSumOtherMap  Map Size : " + avgSumOtherMap.size()); */
		return avgSumOtherMap;
	}

	public static <T> Map<T,Boolean> computeSC1Map(Map<T,Integer> countMap, double weight) {
		Map<T,Boolean> sc1Map = new HashMap<T,Boolean>();
		for(T s: countMap.keySet()){
			int count = countMap.get(s);
			if (count >= weight) {
				// log.debug("computeSC1Map: TRUE concept: " + s);
				sc1Map.put(s, Boolean.TRUE);
			}
			else
				sc1Map.put(s, Boolean.FALSE);
		}
		// log.debug("size of computeSC1Map = " + sc1Map.size());
		return sc1Map;
	}

	public static <T> Map<T,Boolean> computeSC2Map(Map<T,Integer> countMap, Map<T,Double> avgOtherMap) {
		Map<T,Boolean> sc2Map = new HashMap<T,Boolean>();
		for(T key : countMap.keySet()){
			int count = countMap.get(key);
			double avgOther = avgOtherMap.get(key);
			if (count >= avgOther){
				sc2Map.put(key, Boolean.TRUE);
				// log.debug("computeSC2Map: TRUE concept: " + key);
			}
			else {
				sc2Map.put(key, Boolean.FALSE);
				// log.debug("computeSC2Map: FALSE concept: " + key);
			}
		}
		// log.debug("size of computeSC2Map = " + sc2Map.size());
		return sc2Map;
	}

	public static Map<String,Boolean> computeSC3MapA(List<APredication> sps) {
		List<APredication> uniquePredications = new ArrayList<APredication>();
		Set<APredication> tempSet  = new HashSet<APredication>(sps);
		uniquePredications.addAll(tempSet);

		Map<String,Integer> conceptUniqueMap = computeConceptCountMap(uniquePredications);
		Map<String,Double> avgSumOtherUniqueMap = computeAvgSumOtherMap(conceptUniqueMap, uniquePredications.size()*2);
		Map<String,Boolean> sc3Map = computeSC2Map(conceptUniqueMap,avgSumOtherUniqueMap);
		// log.debug("size of computeSC3Map = " + sc3Map.size());
		return sc3Map;
	}


	public static Map<String,Map<String,Boolean>> computeSR1MapA(List<APredication> sps, Map<String,Boolean> sc1){
		Map<String,Map<String,Boolean>> sr1Map = new HashMap<String,Map<String,Boolean>>();

		Map<String,Map<String,Integer>> concPredicateMap = new HashMap<String,Map<String,Integer>>();
		int totCnt = 0;
		for(APredication p : sps){
			for(String s : p.subject){
				if (sc1.get(s)) {
					Map<String,Integer> valueMap = null;
					if (concPredicateMap.containsKey(s)) {
						valueMap = concPredicateMap.get(s);
						if (valueMap.containsKey(p.predicate)) {
							int cnt = valueMap.get(p.predicate);
							valueMap.put(p.predicate,cnt+1);
						} else {
							valueMap.put(p.predicate, 1);
						}
					} else {
						valueMap = new HashMap<String,Integer>();
						valueMap.put(p.predicate, 1);
					}
					concPredicateMap.put(s,valueMap);
				}
			}
			for(String s : p.object){
				if (sc1.get(s)) {
					Map<String,Integer> valueMap = null;
					if (concPredicateMap.containsKey(s)) {
						valueMap = concPredicateMap.get(s);
						if (valueMap.containsKey(p.predicate)) {
							int cnt = valueMap.get(p.predicate);
							valueMap.put(p.predicate,cnt+1);
						} else {
							valueMap.put(p.predicate, 1);
						}
					} else {
						valueMap = new HashMap<String,Integer>();
						valueMap.put(p.predicate, 1);
					}
					concPredicateMap.put(s,valueMap);
				}
			}
		}

		int coeff = computeRelationBalanceCoefficient(concPredicateMap, Constants.DEMOTED_PREDICATE_LIST);
		Map balancedConcPredicateMap = balanceRelations(concPredicateMap, coeff, Constants.DEMOTED_PREDICATE_LIST);

		Map<String,Map<String,Double>> tempMap = new HashMap<String,Map<String,Double>>();
		// for(String c: concPredicateMap.keySet()){
		for(String c: (Set<String>) balancedConcPredicateMap.keySet()){
			Map<String,Integer> predMap = concPredicateMap.get(c);
			totCnt = 0;
			for(Integer i : predMap.values())
				totCnt += i;

			Map<String,Double> avgSumOtherMap = computeAvgSumOtherMap(predMap,totCnt);

			Map<String,Double> tempValueMap = new HashMap<String,Double>();
			for(Map.Entry<String, Double> e : avgSumOtherMap.entrySet()){
				tempValueMap.put(e.getKey(),e.getValue());
				tempMap.put(c,tempValueMap);
			}
		}


		// this is arbitrary. Can be changed.
		int relSalient = 1;
		for(Map.Entry<String, Map<String,Integer>> e:concPredicateMap.entrySet()){
			String c = e.getKey();
			Map<String,Integer> predMap = e.getValue();
			Map<String,Boolean> tempMap2 = new HashMap<String,Boolean>();
			for(Map.Entry<String,Integer> e2:predMap.entrySet()){
				String pr = e2.getKey();
				int cnt = e2.getValue();
				Map<String,Double> tempMap1 = tempMap.get(c);
				double avg = tempMap1.get(pr);
				if (cnt > relSalient && cnt > avg)
					tempMap2.put(pr, Boolean.TRUE);
				else
					tempMap2.put(pr,Boolean.FALSE);
				sr1Map.put(c, tempMap2);
			}
		}
		return sr1Map;
	}

	public static <T> Map<T,Boolean> computeSRC1Map(Map<T,Integer> predCountMap, double avg) {
		Map<T,Boolean> src1Map = new HashMap<T,Boolean>();
		for(Map.Entry<T, Integer> e:predCountMap.entrySet()){
			if (e.getValue() > avg) {
				src1Map.put(e.getKey(), Boolean.TRUE);
				// log.debug("SRC1: " + ((APredication) e.getKey()).subject + " | " + ((APredication) e.getKey()).predicate + " | "  + ((APredication) e.getKey()).object);
			}
			else
				src1Map.put(e.getKey(),Boolean.FALSE);
		}
		return src1Map;
	}

	public static List<APredication> incorporateSCRulesA(List<APredication> sps, Map<String,Boolean> sc1, Map<String,Boolean> sc3) {
		List<APredication> salientConcPredications = new ArrayList<APredication>();
		for(APredication p:sps){
			boolean includeSubject = false;
			boolean includeObject = false;
			List<String> subjects = p.subject;
			List<String> objects = p.object;
			if (sc1 == null)
				log.debug("sc1 is null");
			if (sc3 == null)
				log.debug("sc2 is null");
			for(String s:subjects)
				if	(sc1.get(s) || sc3.get(s))  {
					includeSubject = true;
					break;
				}
			if (includeSubject)
				for(String o:objects)
					if (o != null && o != "" && sc1.get(o) || sc3.get(o))  {
						includeObject = true;
						break;
					}
			if (includeSubject && includeObject)
				salientConcPredications.add(p);
		}
		return salientConcPredications;
	}

	public static List<APredication> incorporateSRRulesA(List<APredication> sps, Map<String,Map<String,Boolean>> sr1) {
		List<APredication> salientRelPredications = new ArrayList<APredication>();
		main:for(APredication p:sps){
			for(String s:p.subject){
				Map<String,Boolean> m = sr1.get(s);
				if (m!=null && m.get(p.predicate)) {
					salientRelPredications.add(p);
					continue main;
				}
			}
			for(String s:p.object){
				Map<String,Boolean> m = sr1.get(s);
				if (m!=null && m.get(p.predicate)) {
					salientRelPredications.add(p);
					continue main;
				}
			}
		}
		return salientRelPredications;
	}

	public static List<APredication> incorporateSRCRulesA(List<APredication> sps, Map<APredication,Boolean> src1) {
		List<APredication> salientPredications = new ArrayList<APredication>();
		for(APredication p:sps){
			if (src1.get(p))
				salientPredications.add(p);
		}
		return salientPredications;
	}

	public static int computeBalanceCoefficient(Map predCountMap, List predicates) {
		int coeff = 1;
		Iterator iter = predCountMap.keySet().iterator();
		Map predicateCountMap = new HashMap();
		while (iter.hasNext()) {
			APredication p = (APredication)iter.next();
			String predicate = p.predicate;
			int predicationCount = ((Integer)predCountMap.get(p)).intValue();
			if (predicateCountMap.containsKey(predicate)) {
				int count = ((Integer)predicateCountMap.get(predicate)).intValue();
				count += predicationCount;
				predicateCountMap.put(predicate, new Integer(count));
			} else
				predicateCountMap.put(predicate, new Integer(predicationCount));
		}

		int pred_to_demote_count = 0;
		int other_count = 0;
		int distinct_pred_to_demote_count = 0;
		int distinct_other_count = 0;
		iter = predicateCountMap.keySet().iterator();
		while (iter.hasNext()) {
			String predicate = (String)iter.next();
			// log.debug("Predicate: " + predicate);
			if (predicates.contains(predicate)) {
				pred_to_demote_count += ((Integer)predicateCountMap.get(predicate)).intValue();
				// treat TREATS and PREVENTS as the same.
				if (!(predicates.contains(Constants.TREATS)) ||
					(predicates.contains(Constants.TREATS) && !Constants.PREVENTS.equals(predicate)))
					distinct_pred_to_demote_count++;
			}
			else {
				other_count +=  ((Integer)predicateCountMap.get(predicate)).intValue();
				distinct_other_count++;
			}
			// log.debug("Counts: " + pred_to_demote_count + " " + distinct_pred_to_demote_count + " " + other_count + " " + distinct_other_count);
		}
		coeff = (int)(((double)pred_to_demote_count/(double)distinct_pred_to_demote_count)/((double)other_count/(double)distinct_other_count)) + 1;
		if (coeff < 1.0) coeff = 1;
		// log.debug("Balance coefficient:" + coeff);
		return coeff;
	}

	public static Map balancePredications(Map predCountMap, int coeff, List predicates) {
		Map balancedMap = new HashMap();
		Iterator iter = predCountMap.keySet().iterator();
		while (iter.hasNext()) {
			APredication p = (APredication)iter.next();
			String predicate = p.predicate;
			int count = ((Integer)predCountMap.get(p)).intValue();
			if (!predicates.contains(predicate)) {
				int newCnt = count * coeff;
				balancedMap.put(p, new Integer(newCnt));
			}
			else balancedMap.put(p,new Integer(count));
		}
		return balancedMap;
	}

	public static int computeRelationBalanceCoefficient(Map relCountMap, List predicates) {
		int coeff = 1;
		Iterator iter = relCountMap.keySet().iterator();
		Map relationCountMap = new HashMap();
		while (iter.hasNext()) {
			String c = (String)iter.next();
			Map relMap = (Map)relCountMap.get(c);
			Iterator relIter = relMap.keySet().iterator();
			while (relIter.hasNext()) {
				String predicate = (String)relIter.next();
				// log.debug("Predicate: " + predicate);
				int relCount = ((Integer)relMap.get(predicate)).intValue();
				if (relationCountMap.containsKey(predicate)) {
					int count = ((Integer)relationCountMap.get(predicate)).intValue();
					count += relCount;
					relationCountMap.put(predicate, new Integer(count));
				} else
					relationCountMap.put(predicate, new Integer(relCount));
			}
		}

		int pred_to_demote_count = 0;
		int other_count = 0;
		int distinct_pred_to_demote_count = 0;
		int distinct_other_count = 0;
		iter = relationCountMap.keySet().iterator();
		while (iter.hasNext()) {
			String predicate = (String)iter.next();
			if (predicates.contains(predicate)) {
				pred_to_demote_count += ((Integer)relationCountMap.get(predicate)).intValue();
//				 treat TREATS and PREVENTS as the same.
				if (!(predicates.contains(Constants.TREATS)) ||
						(predicates.contains(Constants.TREATS) && !Constants.PREVENTS.equals(predicate)))
					distinct_pred_to_demote_count++;
			}
			else {
				other_count +=  ((Integer)relationCountMap.get(predicate)).intValue();
				distinct_other_count++;
			}
			// log.debug("Counts: " + pred_to_demote_count + " " + distinct_pred_to_demote_count + " " + other_count + " " + distinct_other_count);
		}
		coeff = (int)(((double)pred_to_demote_count/(double)distinct_pred_to_demote_count)/((double)other_count/(double)distinct_other_count)) + 1;
		if (coeff < 1.0) coeff= 1;
		// log.debug("Relation balance coefficient:" + coeff);
		return coeff;
	}

	public static Map balanceRelations(Map relCountMap, int coeff, List predicates) {
		Map balancedMap = new HashMap();
		Iterator iter = relCountMap.keySet().iterator();
		while (iter.hasNext()) {
			String c = (String)iter.next();
			Map relMap = (Map)relCountMap.get(c);
			Map valueMap = new HashMap();
			Iterator relIter = relMap.keySet().iterator();
			while (relIter.hasNext()) {
				String predicate = (String)relIter.next();
				int count = ((Integer)relMap.get(predicate)).intValue();
				if (!predicates.contains(predicate)) {
					int newCnt = count * coeff;
					valueMap.put(predicate, new Integer(newCnt));
				}
				else valueMap.put(predicate, new Integer(count));
			}
			balancedMap.put(c,valueMap);
		}
		return balancedMap;
	}

//	private static Map balanceRelations(Map<Concept,Map<String,Integer>> relCountMap, int coeff) {
//		Map<Concept,Map<String,Integer>> balancedMap = new HashMap<Concept,Map<String,Integer>>();
//		for(Map.Entry<Concept, Map<String,Integer>> entry: relCountMap.entrySet()){
//			Concept c = entry.getKey();
//			Map<String,Integer> relMap = entry.getValue();
//			Map<String,Integer> valueMap = new HashMap<String,Integer>();
//			for(Map.Entry<String, Integer> subEntry : relMap.entrySet()){
//				String predicate = subEntry.getKey();
//				int count = subEntry.getValue();
//				if (!Constants.ISA.equals(predicate) &&
//					!Constants.TREATS.equals(predicate) &&
//					!Constants.PREVENTS.equals(predicate)) {
//					int newCnt = count * coeff;
//					valueMap.put(predicate, newCnt);
//				}
//				else valueMap.put(predicate, count);
//			}
//			balancedMap.put(c,valueMap);
//		}
//		return balancedMap;
//	}
}
