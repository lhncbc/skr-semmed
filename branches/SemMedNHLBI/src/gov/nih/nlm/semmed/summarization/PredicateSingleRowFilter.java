package gov.nih.nlm.semmed.summarization;

import java.util.List;
import java.util.Arrays;

public class PredicateSingleRowFilter implements SingleRowFilter {

	private static final String[] PREDICATES = { "BRANCH_OF","CARRIES_OUT","COMPLICATES","CONTAINS","DEGREE OF","DERIVATIVE_OF",
		"EXHIBITS","EVALUATION_OF","HAS_METHOD","INDICATES","INTERACTS_WITH","INTERCONNECTS","MANAGES","MANIFESTATION_OF","MEASURES",
		"METHOD_OF","PRECEDES","PART_OF","PRACTICES","PRODUCES","PROPERTY_OF","SURROUNDS","ADMINISTERED_TO","ASSOCIATED_WITH","INHIBITS",
		"STIMULATES","DISRUPTS","USES","AUGMENTS","CONVERTS_TO","compared_with","higher_than","lower_than","same_as"};
	//TODO [Alejandro] 
	//scale|<variable>
	
	static {
		Arrays.sort(PREDICATES);
	}
	
	private static PredicateSingleRowFilter myInstance = new PredicateSingleRowFilter();
	
	private PredicateSingleRowFilter(){}
	
	public static PredicateSingleRowFilter getInstance(){
		return myInstance;
	}
	
	
	public boolean filter(String predicate, List<String> subjSemtype,
			List<String> objSemtype) {
	
	    if (predicate.startsWith("NEG_"))
		predicate = predicate.substring(4);

	    if (Arrays.binarySearch(PREDICATES,predicate)>=0)
		return false;
	    else
		return true;
	}

}
