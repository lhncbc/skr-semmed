package gov.nih.nlm.semmed.summarization;

import gov.nih.nlm.semmed.model.APredication;
import gov.nih.nlm.semmed.util.Constants;

import java.util.List;
import java.util.Map;

/**
 *
 * @author rodriguezal
 *
 */
public class InterPharmaPredicationSaliencyFilter extends SaliencyFilter {

	private static InterPharmaPredicationSaliencyFilter myInstance = new InterPharmaPredicationSaliencyFilter();

	public static InterPharmaPredicationSaliencyFilter getInstance(){
		return myInstance;
	}

	private InterPharmaPredicationSaliencyFilter(){}

	protected List<APredication> postProcessing(List<APredication> listIn, List<APredication> salientConcPredications, Map<String,Boolean> sc1Map){

		Map<APredication,Integer> predicationCountMap = SaliencyUtil.computePredicationCountMap(listIn);
		int balance_coeff = SaliencyUtil.computeBalanceCoefficient(predicationCountMap, Constants.INTERPHARMA_DEMOTED_PREDICATE_LIST);
		Map balancePredicationCountMap = SaliencyUtil.balancePredications(predicationCountMap, balance_coeff, Constants.INTERPHARMA_DEMOTED_PREDICATE_LIST);
		double avgPredActivationWeight = SaliencyUtil.computeAvgActivationWeight(balancePredicationCountMap);
		Map<APredication,Boolean> src1Map = SaliencyUtil.computeSRC1Map(balancePredicationCountMap, avgPredActivationWeight);

		return SaliencyUtil.incorporateSRCRulesA(salientConcPredications,src1Map);
	}
}
