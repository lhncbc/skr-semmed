package gov.nih.nlm.semmed.rules;

import java.util.List;

public interface Operator {

	public Predicate eval(List<Predicate> operands);
	
}
