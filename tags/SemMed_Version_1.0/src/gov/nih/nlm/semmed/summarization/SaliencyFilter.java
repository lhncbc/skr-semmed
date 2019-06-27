package gov.nih.nlm.semmed.summarization;

import gov.nih.nlm.semmed.model.APredication;
// import gov.nih.nlm.semmed.model.summarization.SaliencyUtil;
import gov.nih.nlm.semmed.util.Constants;
import gov.nih.nlm.semmed.util.GraphUtils;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author rodriguezal
 *
 */
public abstract class SaliencyFilter implements Filter {
	private static Log log = LogFactory.getLog(SaliencyFilter.class);
	protected abstract List<APredication> postProcessing(List<APredication> preds, List<APredication> relPreds, Map<String,Boolean> sc1Map);

	public List<APredication> filter(List<APredication> preliminaryList,
			String[] predicateList, List<APredication> listIn, String seed) {

		List<APredication> outList = new ArrayList<APredication>();
		Map<String,Integer> conceptCountMap = SaliencyUtil.computeConceptCountMap(listIn);
		double avgActivationWeight = SaliencyUtil.computeAvgActivationWeight(conceptCountMap);
		log.debug("avgActivationWeight for Concepts : " + avgActivationWeight);
		log.debug("Concept Map size : " + conceptCountMap.size());
		Map<String,Boolean> sc1Map = SaliencyUtil.computeSC1Map(conceptCountMap, avgActivationWeight);
		for(String s: sc1Map.keySet()){
			if (sc1Map.get(s)) {
				log.debug("SC1: TRUE concept: " + s);
			}
		}

		//TODO [Alejandro] do we really want an integer for the second argument?
		// Map<String,Double> avgSumOtherMap = SaliencyUtil.computeAvgSumOtherMap(conceptCountMap, (int)(avgActivationWeight * conceptCountMap.size()));
		// Map<String,Boolean> sc2Map = SaliencyUtil.computeSC2Map(conceptCountMap, avgSumOtherMap);

		Map<String,Boolean> sc3Map = SaliencyUtil.computeSC3MapA(listIn);
		for(String s: sc3Map.keySet()){
			if (sc3Map.get(s)) {
				log.debug("SC3: TRUE concept: " + s);
			}
		}

		//conceptSaliency !!
		List<APredication> salientConcPredications = SaliencyUtil.incorporateSCRulesA(listIn, sc1Map, sc3Map);

		return postProcessing(listIn, salientConcPredications,sc1Map);

	}


}
