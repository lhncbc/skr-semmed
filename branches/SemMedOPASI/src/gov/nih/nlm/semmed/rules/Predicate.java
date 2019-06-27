package gov.nih.nlm.semmed.rules;

import java.util.List;

import gov.nih.nlm.semmed.model.APredication;

public interface Predicate {

	public boolean eval(List<APredication> atoms);
	
}
