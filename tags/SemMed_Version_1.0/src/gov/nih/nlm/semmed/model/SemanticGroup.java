/**
 * 
 */
package gov.nih.nlm.semmed.model;

import java.util.List;

/**
 * @author hkilicoglu
 *
 */
public class SemanticGroup {

	private List semanticTypes;
	private String colorCode;
	private String name;
	private String abbreviation;
	
	/**
	 * 
	 */
	public SemanticGroup() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * @param colorCode
	 * @param name
	 * @param abbreviation
	 */
	public SemanticGroup(String colorCode, String name, String abbreviation) {
		super();
		// TODO Auto-generated constructor stub
		this.colorCode = colorCode;
		this.name = name;
		this.abbreviation = abbreviation;
	}

	/**
	 * @return Returns the abbreviation.
	 */
	public String getAbbreviation() {
		return abbreviation;
	}

	/**
	 * @param abbreviation The abbreviation to set.
	 */
	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
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
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return Returns the semanticTypes.
	 */
	public List getSemanticTypes() {
		return semanticTypes;
	}

	/**
	 * @param semanticTypes The semanticTypes to set.
	 */
	public void setSemanticTypes(List semanticTypes) {
		this.semanticTypes = semanticTypes;
	}



}
