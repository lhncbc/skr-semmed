package gov.nih.nlm.semmed.summarization;

import java.util.List;

public interface SingleRowFilter {

	public boolean filter(String predicate, List<String> subjSemtype,List<String> objSemtype);
	
}
