package gov.nih.nlm.semmed.summarization;

import gov.nih.nlm.semmed.util.Constants;

import java.util.List;

public class InteractionRowFilter implements SingleRowFilter {

	private static InteractionRowFilter myInstance = new InteractionRowFilter();
	
	private InteractionRowFilter(){}
	
	public static InteractionRowFilter getInstance(){
		return myInstance;
	}
	 
	public boolean filter(String predicate, List<String> subjSemtype,
			List<String> objSemtype) {
		
		if (predicate.endsWith(Constants.CAUSES) || predicate.endsWith(Constants.TREATS) || predicate.equals(Constants.PREVENTS)) 
			return 	Summarizer.intersects(Constants.DRUG_SEMGROUP,subjSemtype) &&
					Summarizer.intersects(Constants.DISORDER_SEMGROUP,objSemtype);			
		else if (predicate.endsWith(Constants.DISRUPTS)) 
			return Summarizer.intersects(Constants.DRUG_SEMGROUP,subjSemtype) &&
			       (Summarizer.intersects(Constants.DRUG_ANAT_SEMGROUP,objSemtype) || 
			    		   Summarizer.intersects(Constants.PHYSIOLOGY_SEMGROUP,objSemtype));
		else if (predicate.endsWith(Constants.AFFECTS) || predicate.endsWith(Constants.COMPLICATES))
			return Summarizer.intersects(Constants.DRUG_SEMGROUP,subjSemtype) &&
				   (Summarizer.intersects(Constants.DISORDER_SEMGROUP,objSemtype) ||
						   Summarizer.intersects(Constants.PHYSIOLOGY_SEMGROUP,objSemtype));			
		else if (predicate.endsWith(Constants.ISA) || predicate.endsWith(Constants.INTERACTS_WITH)) 
			return (Summarizer.intersects(Constants.DRUG_SEMGROUP,subjSemtype) && Summarizer.intersects(Constants.DRUG_CHEM_SEMGROUP,objSemtype)) ||
			(Summarizer.intersects(Constants.DRUG_SEMGROUP,objSemtype) && Summarizer.intersects(Constants.DRUG_CHEM_SEMGROUP,subjSemtype));
			
		return false;	
	}
}
