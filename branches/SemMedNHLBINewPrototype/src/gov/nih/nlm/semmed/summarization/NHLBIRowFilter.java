package gov.nih.nlm.semmed.summarization;

import gov.nih.nlm.semmed.util.Constants;

import java.util.List;

public class NHLBIRowFilter implements SingleRowFilter {
	private static NHLBIRowFilter myInstance = new NHLBIRowFilter();

	private NHLBIRowFilter(){}

	public static NHLBIRowFilter getInstance(){
		return myInstance;
	}

	public boolean filter(String predicate, List<String> subjSemtype,
			List<String> objSemtype) {

		if (predicate.endsWith(Constants.CAUSES))
			return Summarizer.intersects(Constants.ETIOLOGY_SEMGROUP,subjSemtype) &&
			Summarizer.intersects(Constants.NHLBI_DISORDER_SEMGROUP,objSemtype);
		// New NHLBI requirements from Marcelo, 10/27/2008
		if (predicate.endsWith(Constants.ADMINISTERED_TO))
			return Summarizer.intersects(Constants.NHLBI_TREAT_SEMGROUP,subjSemtype) &&
			Summarizer.intersects(Constants.HUMAN_SEMGROUP,objSemtype);
		if (predicate.endsWith(Constants.USES))
			return Summarizer.intersects(Constants.NHLBI_TREAT_SEMGROUP,subjSemtype) &&
			Summarizer.intersects(Constants.NHLBI_TREAT_SEMGROUP,objSemtype);
		// if (predicate.endsWith(Constants.TREATS) || predicate.endsWith(Constants.PREVENTS))
		//	return Summarizer.intersects(Constants.NHLBI_TREAT_SEMGROUP,subjSemtype) &&
		//	Summarizer.intersects(Constants.NHLBI_DISORDER_SEMGROUP,objSemtype);
		//  New NHLBI requirements from Marcelo, 10/27/2008
		if (predicate.endsWith(Constants.TREATS) || predicate.endsWith(Constants.PREVENTS))
			return (Summarizer.intersects(Constants.NHLBI_TREAT_SEMGROUP,subjSemtype) &&
				(Summarizer.intersects(Constants.NHLBI_DISORDER_SEMGROUP,objSemtype) ||
				Summarizer.intersects(Constants.HUMAN_SEMGROUP,objSemtype)));

		if (predicate.endsWith(Constants.PREDISPOSES))
			return Summarizer.intersects(Constants.NHLBI_CHEM_SEMGROUP,subjSemtype) &&
			Summarizer.intersects(Constants.NHLBI_DISORDER_SEMGROUP,objSemtype);

		if (predicate.endsWith(Constants.AFFECTS))
			return Summarizer.intersects(Constants.NHLBI_TREAT_SEMGROUP,subjSemtype) &&
			Summarizer.intersects(Constants.NHLBI_DISORDER_SEMGROUP,objSemtype);

		if (predicate.endsWith(Constants.LOCATION_OF))
			return Summarizer.intersects(Constants.TREAT_ANAT_SEMGROUP,subjSemtype) &&
			Summarizer.intersects(Constants.NHLBI_DISORDER_SEMGROUP,objSemtype);
		if (predicate.endsWith(Constants.COEXISTS_WITH) || predicate.endsWith(Constants.ISA))
			return Summarizer.intersects(Constants.NHLBI_DISORDER_SEMGROUP,subjSemtype) &&
				Summarizer.intersects(Constants.NHLBI_DISORDER_SEMGROUP,objSemtype);
		if (predicate.endsWith(Constants.PROCESS_OF))
			return Summarizer.intersects(Constants.NHLBI_DISORDER_SEMGROUP,subjSemtype) &&
			       (Summarizer.intersects(Constants.NHLBI_DISORDER_SEMGROUP,objSemtype) ||
			    		   Summarizer.intersects(Constants.HUMAN_SEMGROUP,objSemtype));
		if (predicate.endsWith(Constants.ISA))
			return  (Summarizer.intersects(Constants.NHLBI_DISORDER_SEMGROUP,subjSemtype) &&
					Summarizer.intersects(Constants.NHLBI_DISORDER_SEMGROUP,objSemtype)) ||
			(Summarizer.intersects(Constants.ORG_CAUSE_SEMGROUP,subjSemtype) &&
					Summarizer.intersects(Constants.ORG_CAUSE_SEMGROUP,objSemtype)) ||
			(Summarizer.intersects(Constants.NHLBI_CHEM_SEMGROUP,subjSemtype) &&
					Summarizer.intersects(Constants.NHLBI_CHEM_SEMGROUP,objSemtype));

		return false;
	}
}
