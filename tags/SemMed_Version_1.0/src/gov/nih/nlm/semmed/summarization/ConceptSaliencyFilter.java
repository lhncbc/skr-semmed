package gov.nih.nlm.semmed.summarization;

import gov.nih.nlm.semmed.model.APredication;
import java.util.List;
import java.util.Map;

/**
 *
 * @author rodriguezal
 *
 */
public class ConceptSaliencyFilter extends SaliencyFilter {

	private static ConceptSaliencyFilter myInstance = new ConceptSaliencyFilter();

	public static ConceptSaliencyFilter getInstance(){
		return myInstance;
	}

	private ConceptSaliencyFilter(){}

	protected List<APredication> postProcessing(List<APredication> preds, List<APredication> relPreds, Map<String,Boolean> sc1Map){
		return relPreds;
	}


}
