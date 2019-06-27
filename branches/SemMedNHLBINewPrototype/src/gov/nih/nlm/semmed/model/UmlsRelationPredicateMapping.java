package gov.nih.nlm.semmed.model;

public class UmlsRelationPredicateMapping {

	private String umlsRelation;
	private Predicate predicate;
	private boolean inverse;
	
	public UmlsRelationPredicateMapping() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * @param umlsRelation
	 * @param predicate
	 * @param inverse
	 */
	public UmlsRelationPredicateMapping(String umlsRelation, Predicate predicate, boolean inverse) {
		super();
		// TODO Auto-generated constructor stub
		this.umlsRelation = umlsRelation;
		this.predicate = predicate;
		this.inverse = inverse;
	}	

	/**
	 * @return Returns the inverse.
	 */
	public boolean isInverse() {
		return inverse;
	}

	/**
	 * @param inverse The inverse to set.
	 */
	public void setInverse(boolean inverse) {
		this.inverse = inverse;
	}

	/**
	 * @return Returns the predicate.
	 */
	public Predicate getPredicate() {
		return predicate;
	}

	/**
	 * @param predicate The predicate to set.
	 */
	public void setPredicate(Predicate predicate) {
		this.predicate = predicate;
	}

	/**
	 * @return Returns the umlsRelation.
	 */
	public String getUmlsRelation() {
		return umlsRelation;
	}

	/**
	 * @param umlsRelation The umlsRelation to set.
	 */
	public void setUmlsRelation(String umlsRelation) {
		this.umlsRelation = umlsRelation;
	}



}
