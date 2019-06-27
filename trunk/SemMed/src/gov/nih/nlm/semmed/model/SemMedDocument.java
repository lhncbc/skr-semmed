package gov.nih.nlm.semmed.model;

import java.io.Serializable;

import org.jdom.Element;


public abstract class SemMedDocument implements Serializable{

	protected String id;
	protected String titleText;
	protected String abstractText;
	protected boolean include;

	
	/**
	 * @return Returns the id.
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * @param id The id to set.
	 */
	public void setId(String id) {
		this.id = id;
	}

	public String getAbstractText() {
		return abstractText;
	}
	/**
	 * @param abstractText The abstractText to set.
	 */
	public void setAbstractText(String abstractText) {
		this.abstractText = abstractText;
	}

	/**
	 * @return Returns the include.
	 */
	public boolean getInclude() {
		return include;
	}
	/**
	 * @param include The include to set.
	 */
	public void setInclude(boolean include) {
		this.include = include;
	}
	/**
	 * @return Returns the titleText.
	 */
	public String getTitleText() {
		return titleText;
	}
	/**
	 * @param titleText The titleText to set.
	 */
	public void setTitleText(String titleText) {
		this.titleText = titleText;
	}
	
	public String toString() {
		return "ID- " + getId() + 
			"\nTI  - " + getTitleText();
	}
	
	public Element toXml() {
		Element semmedNode = new Element("SemMedDocument");		    				
		Element idNode = new Element("ID");
		idNode.setText(getId());
		Element titleNode = new Element("Title");
		titleNode.setText(getTitleText());
		semmedNode.addContent(idNode);
		semmedNode.addContent(titleNode);

		return semmedNode;
	}	

}
