package gov.nih.nlm.semmed.summarization;

import gov.nih.nlm.semmed.model.APredication;
import gov.nih.nlm.semmed.struts.action.VisualizeAction;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class NoveltyFilter implements Filter {

	private SingleRowFilter filter;
	private List<String> constantFilters;
	private static Log log = LogFactory.getLog(NoveltyFilter.class);

	public NoveltyFilter(SingleRowFilter filter, List<String> constantFilters){
		this.filter = filter;
		this.constantFilters = constantFilters;
	}

	public List<APredication> filter(List<APredication> preliminaryList,
			String[] predicateList, List<APredication> listIn, String seed) {

		List<APredication> outList = new ArrayList<APredication>();
		System.out.println("Real Novelty Filter Name: " + this.filter.getClass().getName());
		//TODO [Alejandro] Add the semgem check (if p.type==semgem discard)
		for(APredication p : listIn)
			if (isNovel(p) && !constantFilters.contains(p.subject) && !constantFilters.contains(p.object) &&
				filter.filter(p.predicate, p.subjectSemtype,p.objectSemtype))
					outList.add(p);
		return outList;

	}

	private boolean isNovel(APredication p){
		// TREATS Patients and ADMINISTERED Patients can be Novel in SemMedNHLBI, Requirements from Marcelo 10/27/2008
		if(p.novelSubject && (p.predicate.compareTo("TREATS") == 0 || p.predicate.compareTo("ADMINISTERED_TO") == 0) && p.objectCUI.contains("C0030705"))
				return true;
		return p.novelSubject && p.novelObject;
	}

}
