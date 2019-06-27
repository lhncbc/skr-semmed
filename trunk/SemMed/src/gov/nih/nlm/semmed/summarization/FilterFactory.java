package gov.nih.nlm.semmed.summarization;

import gov.nih.nlm.semmed.util.Constants;

public class FilterFactory {

	public enum BatchFilter {
		CONNECTIVITY, INTERACTION_CONNECTIVITY, PHARMACOGENOMICS_CONNECTIVITY, PRELIMINARY, NHLBI_RELEVANCE, NHLBI_NOVELTY,
		DIAGNOSIS_RELEVANCE, DIAGNOSIS_NOVELTY,
		INTERACTION_RELEVANCE, INTERACTION_NOVELTY,
		TREATMENT_RELEVANCE, TREATMENT_NOVELTY,
		PHARMACOGENOMICS_RELEVANCE, PHARMACOGENOMICS_NOVELTY,
		CONCEPT_SALIENCY, PREDICATION_SALIENCY, INTERPHARMA_PREDICATION_SALIENCY, RELATION_SALIENCY, PREDICATE_FILTER
	};

	private enum RelevanceFilters {
		NHLBI(new RelevanceFilter(NHLBIRowFilter.getInstance())),
		DIAGNOSIS(new RelevanceFilter(DiagnosisRowFilter.getInstance())),
		INTERACTION(new RelevanceFilter(InteractionRowFilter.getInstance())),
		TREATMENT(new RelevanceFilter(TreatmentRowFilter.getInstance())),
		PHARMACOGENOMICS(new RelevanceFilter(PharmacogenomicsRowFilter.getInstance()));

		private RelevanceFilter filter;

		private RelevanceFilters(RelevanceFilter filter){
			this.filter = filter;
		}
		public RelevanceFilter getFilter(){
			return filter;
		}
	}

	// private static final PredicateFilter pf = new PredicateFilter(PredicateSingleRowFilter.getInstance());

	private enum NoveltyFilters {
		NHLBI(new NoveltyFilter(NHLBIRowFilter.getInstance(),Constants.NOVELTY_FILTER_CONCEPT_LIST)),
		// DIAGNOSIS(new NoveltyFilter(DiagnosisRowFilter.getInstance(),Constants.NOVELTY_FILTER_CONCEPT_LIST)),
		// DIAGNOSIS Novelty should have different Row Filter than DiagnosisRowFilter
		DIAGNOSIS(new NoveltyFilter(DiagnosisNoveltyRowFilter.getInstance(),Constants.NOVELTY_FILTER_CONCEPT_LIST)),
		INTERACTION(new NoveltyFilter(InteractionRowFilter.getInstance(),Constants.NOVELTY_FILTER_CONCEPT_LIST)),
		// TREATMENT(new NoveltyFilter(TreatmentRowFilter.getInstance(),Constants.NOVELTY_FILTER_CONCEPT_LIST)),
		// Treatment Novelty should have different Row Filter than Relevance
		TREATMENT(new NoveltyFilter(TreatmentNoveltyRowFilter.getInstance(),Constants.NOVELTY_FILTER_CONCEPT_LIST)),
		// PHARMACOGENOMICS(new NoveltyFilter(PharmacogenomicsRowFilter.getInstance(),Constants.NOVELTY_FILTER_GENOMIC_CONCEPT_LIST));
		// Pharmacogenomics Novelty should have different Row Filter than Relevance
		PHARMACOGENOMICS(new NoveltyFilter(PharmacogenomicsNoveltyRowFilter.getInstance(),Constants.NOVELTY_FILTER_GENOMIC_CONCEPT_LIST));

		private NoveltyFilter filter;

		private NoveltyFilters(NoveltyFilter filter){
			this.filter = filter;
		}
		public NoveltyFilter getFilter(){
			return filter;
		}
	}

	public static Filter getFilter(BatchFilter type){
		switch(type){
		case PRELIMINARY:
			return PreliminaryFilter.getInstance();

		case CONNECTIVITY:
			return ConnectivityFilter.getInstance();

		// Has a separate connectivity rule in summarize_interaction.pl
		case INTERACTION_CONNECTIVITY:
			return InteractionConnectivityFilter.getInstance();

		// Has a separate connectivity rule in summarize_pharmgen.pl
		case PHARMACOGENOMICS_CONNECTIVITY:
			return PharmacogenomicsConnectivityFilter.getInstance();

		case DIAGNOSIS_RELEVANCE:
			return RelevanceFilters.DIAGNOSIS.getFilter();
		case DIAGNOSIS_NOVELTY:
			return NoveltyFilters.DIAGNOSIS.getFilter();

		case INTERACTION_RELEVANCE:
			return RelevanceFilters.INTERACTION.getFilter();
		case INTERACTION_NOVELTY:
			return NoveltyFilters.INTERACTION.getFilter();

		case TREATMENT_RELEVANCE:
			return RelevanceFilters.TREATMENT.getFilter();
		case TREATMENT_NOVELTY:
			return NoveltyFilters.TREATMENT.getFilter();

		case NHLBI_RELEVANCE:
			return RelevanceFilters.NHLBI.getFilter();
		case NHLBI_NOVELTY:
			return NoveltyFilters.NHLBI.getFilter();

		case PHARMACOGENOMICS_RELEVANCE:
			return RelevanceFilters.PHARMACOGENOMICS.getFilter();
		case PHARMACOGENOMICS_NOVELTY:
			return NoveltyFilters.PHARMACOGENOMICS.getFilter();

		case CONCEPT_SALIENCY:
			return ConceptSaliencyFilter.getInstance();
		case INTERPHARMA_PREDICATION_SALIENCY:
			return InterPharmaPredicationSaliencyFilter.getInstance();
		case PREDICATION_SALIENCY:
			return PredicationSaliencyFilter.getInstance();

		case RELATION_SALIENCY:
			return RelationSaliencyFilter.getInstance();

		/* case PREDICATE_FILTER:
			return pf; */

		default:
			throw new IllegalArgumentException();
		}
	}
}
