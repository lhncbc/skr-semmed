package gov.nih.nlm.semmed.zsummarization;

import gov.nih.nlm.semmed.model.APredication;

import java.util.List;

public interface Filter {

	public abstract List<APredication> filter(List<APredication> preliminaryList,String[] predicateList,
			                                List<APredication> listIn);

}
