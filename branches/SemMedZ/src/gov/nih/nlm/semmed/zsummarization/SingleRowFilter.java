package gov.nih.nlm.semmed.zsummarization;

import java.util.List;

public interface SingleRowFilter {

	public boolean filter(String predicate, List<String> subjSemtype,List<String> objSemtype);

}
