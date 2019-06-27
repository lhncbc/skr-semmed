package gov.nih.nlm.semmed.summarization;

import gov.nih.nlm.semmed.util.Constants;

import java.util.List;

/**
 *
 * @author rodriguezal
 *
 */
public class PharmacogenomicsNoveltyRowFilter implements SingleRowFilter {

	private static PharmacogenomicsNoveltyRowFilter myInstance = new PharmacogenomicsNoveltyRowFilter();

	private PharmacogenomicsNoveltyRowFilter() {
	}

	public static PharmacogenomicsNoveltyRowFilter getInstance() {
		return myInstance;
	}

	public boolean filter(String predicate, List<String> subjSemtype,
			List<String> objSemtype) {
		if (predicate.endsWith(Constants.CAUSES)
				|| predicate.endsWith(Constants.ASSOCIATED_WITH)
				|| predicate.endsWith(Constants.PREDISPOSES))
			return Summarizer.intersects(Constants.SUBSTANCE_SEMGROUP,
					subjSemtype)
					&& Summarizer.intersects(Constants.DISORDER_SEMGROUP,
							objSemtype);
		if (predicate.endsWith(Constants.INTERACTS_WITH)
				|| predicate.endsWith(Constants.INHIBITS)
				|| predicate.endsWith(Constants.STIMULATES)
				|| predicate.endsWith(Constants.CONVERTS_TO))
			return Summarizer.intersects(Constants.SUBSTANCE_SEMGROUP,
					subjSemtype)
					&& Summarizer.intersects(Constants.SUBSTANCE_SEMGROUP, objSemtype);
		if (predicate.endsWith(Constants.TREATS)
				|| predicate.endsWith(Constants.ADMINISTERED_TO))
			return Summarizer.intersects(Constants.DRUG_SEMGROUP, subjSemtype)
					&& Summarizer.intersects(Constants.DISORDER_SEMGROUP,
							objSemtype);
		if (predicate.endsWith(Constants.PART_OF))
			return Summarizer.intersects(Constants.SUBSTANCE_SEMGROUP,subjSemtype)
					&& Summarizer.intersects(Constants.ORG_SEMGROUP, objSemtype);
		if (predicate.endsWith(Constants.PROCESS_OF))
			return Summarizer.intersects(Constants.DISORDER_SEMGROUP,subjSemtype)
					&& Summarizer.intersects(Constants.ORG_SEMGROUP,objSemtype);
		if (predicate.endsWith(Constants.AFFECTS)
				|| predicate.endsWith(Constants.AUGMENTS)
				|| predicate.endsWith(Constants.DISRUPTS))
			return Summarizer.intersects(Constants.SUBSTANCE_SEMGROUP,subjSemtype)
					&& (Summarizer.intersects(Constants.GENE_ANAT_SEMGROUP,objSemtype) ||
							Summarizer.intersects(Constants.PROCESS_SEMGROUP,objSemtype));
		if (predicate.endsWith(Constants.MANIFESTATION_OF))
			return Summarizer.intersects(Constants.DISORDER_SEMGROUP,subjSemtype)
					&& Summarizer.intersects(Constants.DISORDER_SEMGROUP,objSemtype);
		if (predicate.endsWith(Constants.LOCATION_OF))
			return Summarizer.intersects(Constants.GENE_ANAT_SEMGROUP,subjSemtype)
					&& Summarizer.intersects(Constants.SUBSTANCE_SEMGROUP,objSemtype);
		if (predicate.endsWith(Constants.COEXISTS_WITH))
			return (Summarizer.intersects(Constants.DISORDER_SEMGROUP,subjSemtype) &&
					Summarizer.intersects(Constants.DISORDER_SEMGROUP,objSemtype))
					|| (Summarizer.intersects(Constants.SUBSTANCE_SEMGROUP,subjSemtype) &&
							Summarizer.intersects(Constants.SUBSTANCE_SEMGROUP,objSemtype));
		if (predicate.endsWith(Constants.ISA)) {
			return Summarizer.intersects(Constants.DRUG_CHEM_SEMGROUP,subjSemtype)
					&& Summarizer.intersects(Constants.DRUG_CHEM_SEMGROUP,objSemtype)
					|| Summarizer.intersects(Constants.SUBSTANCE_SEMGROUP,subjSemtype)
					&& Summarizer.intersects(Constants.SUBSTANCE_SEMGROUP,objSemtype);
		}
		return false;
	}
}
