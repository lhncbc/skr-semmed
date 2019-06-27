package gov.nih.nlm.semmed.model;

import java.util.Map;

public class Predicate {

	private String predicateText;
	private Map translationMap;
	private String colorCode;
	private boolean inverse;
	
	public Predicate() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * @param predicate
	 */
	public Predicate(String predicate) {
		super();
		// TODO Auto-generated constructor stub
		this.predicateText = predicate;
	}
	
	/**
	 * @param predicate
	 * @param colorCode
	 */
	public Predicate(String predicate, String colorCode) {
		super();
		// TODO Auto-generated constructor stub
		this.predicateText = predicate;
		this.colorCode = colorCode;
	}


	/**
	 * @param predicate
	 * @param translationMap
	 * @param colorCode
	 */
	public Predicate(String predicate, Map translationMap, String colorCode) {
		super();
		// TODO Auto-generated constructor stub
		this.predicateText = predicate;
		this.translationMap = translationMap;
		this.colorCode = colorCode;
	}	
	
	/**
	 * @return Returns the colorCode.
	 */
	public String getColorCode() {
		return colorCode;
	}

	/**
	 * @param colorCode The colorCode to set.
	 */
	public void setColorCode(String colorCode) {
		this.colorCode = colorCode;
	}

	/**
	 * @return Returns the predicateText.
	 */
	public String getPredicateText() {
		return predicateText;
	}

	/**
	 * @param predicate The predicateText to set.
	 */
	public void setPredicateText(String predicate) {
		this.predicateText = predicate;
	}

	/**
	 * @return Returns the translationMap.
	 */
	public Map getTranslationMap() {
		return translationMap;
	}

	/**
	 * @param translationMap The translationMap to set.
	 */
	public void setTranslationMap(Map translationMap) {
		this.translationMap = translationMap;
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
	
	public String toString() {
		return this.getPredicateText() + " " + this.getColorCode() + " " + this.isInverse();
	}
}
