package gov.nih.nlm.semmed.summarization;

import gov.nih.nlm.semmed.util.Constants;

import java.util.List;

public class TreatmentNoveltyRowFilter implements SingleRowFilter {

	private static TreatmentNoveltyRowFilter myInstance = new TreatmentNoveltyRowFilter();

	private TreatmentNoveltyRowFilter(){}

	public static TreatmentNoveltyRowFilter getInstance(){
		return myInstance;
	}

	public boolean filter(String predicate, List<String> subjSemtype,
			List<String> objSemtype) {

		if (predicate.endsWith(Constants.CAUSES))
			return Summarizer.intersects(Constants.ETIOLOGY_SEMGROUP,subjSemtype) &&
			Summarizer.intersects(Constants.DISORDER_SEMGROUP,objSemtype);
		if (predicate.endsWith(Constants.TREATS) || predicate.endsWith(Constants.PREVENTS))
			return Summarizer.intersects(Constants.TREATMENT_SEMGROUP,subjSemtype) &&
			Summarizer.intersects(Constants.DISORDER_SEMGROUP,objSemtype);
		if (predicate.endsWith(Constants.LOCATION_OF))
			return Summarizer.intersects(Constants.TREAT_ANAT_SEMGROUP,subjSemtype) &&
			Summarizer.intersects(Constants.DISORDER_SEMGROUP,objSemtype);
		// if (predicate.endsWith(Constants.COEXISTS_WITH) || predicate.endsWith(Constants.ISA)) // Error for Novelty
		if (predicate.endsWith(Constants.COEXISTS_WITH))
			return Summarizer.intersects(Constants.DISORDER_SEMGROUP,subjSemtype) &&
				Summarizer.intersects(Constants.DISORDER_SEMGROUP,objSemtype);
		if (predicate.endsWith(Constants.PROCESS_OF))
			return Summarizer.intersects(Constants.DISORDER_SEMGROUP,subjSemtype) &&
			       (Summarizer.intersects(Constants.DISORDER_SEMGROUP,objSemtype) ||
			    		   Summarizer.intersects(Constants.HUMAN_SEMGROUP,objSemtype));
		if (predicate.endsWith(Constants.ISA))
			return  (Summarizer.intersects(Constants.DISORDER_SEMGROUP,subjSemtype) &&
					Summarizer.intersects(Constants.DISORDER_SEMGROUP,objSemtype)) ||
			(Summarizer.intersects(Constants.ORG_CAUSE_SEMGROUP,subjSemtype) &&
					Summarizer.intersects(Constants.ORG_CAUSE_SEMGROUP,objSemtype)) ||
			(Summarizer.intersects(Constants.CHEM_SEMGROUP,subjSemtype) &&
					Summarizer.intersects(Constants.CHEM_SEMGROUP,objSemtype));

		return false;
	}

}
