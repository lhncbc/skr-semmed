package gov.nih.nlm.semmed.summarization;

import gov.nih.nlm.semmed.model.APredication;

import java.util.ArrayList;
import java.util.List;

public class PredicateFilter implements Filter {

	private SingleRowFilter filter;
	
	public PredicateFilter(SingleRowFilter filter){
		this.filter = filter;
	}
	
	public List<APredication> filter(List<APredication> preliminaryList, String[] predicateList, List<APredication> listIn, String seed) {
		List<APredication> outList = new ArrayList<APredication>();
		
		for(APredication p : listIn)
			if (!Summarizer.intersects(p.subject,p.object) &&
					filter.filter(p.predicate, p.subjectSemtype,p.objectSemtype))
				outList.add(p);		
		return outList;
	}

}
