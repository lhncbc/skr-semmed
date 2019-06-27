package gov.nih.nlm.semmed.summarization;

import gov.nih.nlm.semmed.model.APredication;
import gov.nih.nlm.semmed.util.Constants;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author rodriguezal
 *
 */
public class PredicationSaliencyFilter extends SaliencyFilter {
	private static Log log = LogFactory.getLog(PredicationSaliencyFilter.class);
	private static PredicationSaliencyFilter myInstance = new PredicationSaliencyFilter();

	public static PredicationSaliencyFilter getInstance(){
		return myInstance;
	}

	private PredicationSaliencyFilter(){}

	protected List<APredication> postProcessing(List<APredication> listIn, List<APredication> salientConcPredications, Map<String,Boolean> sc1Map){

		Map<APredication,Integer> predicationCountMap = SaliencyUtil.computePredicationCountMap(listIn);
		int balance_coeff = SaliencyUtil.computeBalanceCoefficient(predicationCountMap, Constants.DEMOTED_PREDICATE_LIST);
		Map balancePredicationCountMap = SaliencyUtil.balancePredications(predicationCountMap, balance_coeff, Constants.DEMOTED_PREDICATE_LIST);
		double avgPredActivationWeight = SaliencyUtil.computeAvgActivationWeight(balancePredicationCountMap);
		Map<APredication,Boolean> src1Map = SaliencyUtil.computeSRC1Map(balancePredicationCountMap, avgPredActivationWeight);

		return SaliencyUtil.incorporateSRCRulesA(salientConcPredications,src1Map);
	}
}
