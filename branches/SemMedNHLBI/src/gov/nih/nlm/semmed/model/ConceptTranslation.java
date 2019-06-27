/*
 * Created Tue May 23 16:43:32 EDT 2006 by MyEclipse Hibernate Tool.
 */ 
package gov.nih.nlm.semmed.model;

import java.io.Serializable;

import org.jdom.Element;

/**
 * A class that represents a row in the 'CONCEPT_TRANSLATION' table. 
 * This class may be customized as it is never re-generated 
 * after being created.
 */
public class ConceptTranslation
    extends AbstractConceptTranslation
    implements Serializable
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** The cached hash code value for this instance.  Settting to 0 triggers re-calculation. */
    private int hashValue = 0;	
    
    /**
     * Simple constructor of ConceptTranslation instances.
     */
    public ConceptTranslation()
    {
    }

    /**
     * Constructor of ConceptTranslation instances given a simple primary key.
     * @param conceptTranslationId
     */
    public ConceptTranslation(java.lang.Long conceptTranslationId)
    {
        super(conceptTranslationId);
    }

    /* Add customized code below */
    public String toString() {
    	return getLanguage() + " " + getTranslation();
    }
    
    public ConceptTranslation(Element e){
    	this.setLanguage(e.getChildTextTrim("Language")); 
    	this.setTranslation(e.getChildTextTrim("Translation")); 
    }
    
    public Element toXml(String nodeName) {
    	Element ctNode = new Element(nodeName);
    	Element langNode = new Element("Language");
    	langNode.setText(getLanguage());
    	Element trNode = new Element("Translation"); 
    	trNode.setText(getTranslation());
    	    	
    	ctNode.addContent(langNode);
    	ctNode.addContent(trNode); 
    	
    	return ctNode;
    }  
    
    /**
     * Implementation of the equals comparison on the basis of equality of the primary key values.
     * @param rhs
     * @return boolean
     */
    public boolean equals(Object rhs)
    {
        if (rhs == null)
            return false;
        if (! (rhs instanceof ConceptTranslation))
            return false;
        ConceptTranslation that = (ConceptTranslation) rhs;
        //if (this.getConceptSemtypeId() == null || that.getConceptSemtypeId() == null)
        //    return false;
        if (this.getConcept() == null || that.getConcept() == null)
        	return false;
        if (this.getLanguage() == null || that.getLanguage() == null)
        	return false;   
         
        return (this.getConcept().equals(that.getConcept()) &&
        		this.getLanguage().equals(that.getLanguage()));
    }

    /**
     * Implementation of the hashCode method conforming to the Bloch pattern with
     * the exception of array properties (these are very unlikely primary key types).
     * @return int
     */
    public int hashCode()
    {
        if (this.hashValue == 0)
        {
            int result = 17;
            int conceptIdValue = this.getConceptTranslationId() == null ? 0 : this.getConceptTranslationId().hashCode();
            result = result * 37 + conceptIdValue;
            this.hashValue = result;
        }
        return this.hashValue;
    }     
}
