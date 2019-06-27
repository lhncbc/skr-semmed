package gov.nih.nlm.semmed.summarization;

import gov.nih.nlm.semmed.model.APredication;

import java.util.List;
import java.util.Map;

/**
 *
 * @author rodriguezal
 *
 */
public class RelationSaliencyFilter extends SaliencyFilter {

	private static RelationSaliencyFilter myInstance = new RelationSaliencyFilter();

	public static RelationSaliencyFilter getInstance(){
		return myInstance;
	}

	private RelationSaliencyFilter(){}

	@Override
	protected List<APredication> postProcessing(List<APredication> listIn,
			List<APredication> concPreds, Map<String, Boolean> sc1Map) {

		Map<String,Map<String,Boolean>> sr1Map = SaliencyUtil.computeSR1MapA(listIn, sc1Map);
		return SaliencyUtil.incorporateSRRulesA(concPreds,sr1Map);
	}

}
