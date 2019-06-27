/*
 * Created Tue Feb 07 14:23:50 EST 2006 by MyEclipse Hibernate Tool.
 */ 
package gov.nih.nlm.semmed.model;

import java.io.Serializable;

import org.jdom.Element;

/**
 * A class that represents a row in the 'CONCEPT_SEMTYPE' table. 
 * This class may be customized as it is never re-generated 
 * after being created.
 */
public class ConceptSemtype
    extends AbstractConceptSemtype
    implements Serializable, Comparable
{
	
	//TODO should override getSemtype to get an enumerated type [Alejandro]
	
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** The cached hash code value for this instance.  Settting to 0 triggers re-calculation. */
    private int hashValue = 0;		
    
    /**
     * Simple constructor of ConceptSemtype instances.
     */
    public ConceptSemtype()
    {
    }
    
    /**
     * ConceptSemtype constructor from an XML element.
     * @param e an XML element containing data for ConceptSemtype instance.
     */    
    public ConceptSemtype(Element e) {
    	this.setConcept(new Concept(e.getChild("Concept")));
    	this.setSemtype(e.getChildTextTrim("Semtype")); 
    	this.setNovel(e.getChildTextTrim("Novel"));  

    }

    /**
     * Constructor of ConceptSemtype instances given a simple primary key.
     * @param conceptSemtypeId
     */
    public ConceptSemtype(java.lang.Long conceptSemtypeId)
    {
        super(conceptSemtypeId);
    }

    /* Add customized code below */
    /**
     * Returns String representation.
     * @return String a String containing instance data.
     */
    public String toString() {
    	return getConcept().toString() + " " + 
    			getSemtype() + " " + 
    				getNovel();
    }
    
    /**
     * Converts the instance to an XML element.
     * @param lang if the instance has translation, the language, otherwise null.
     * @return Element an XML element
     */
    public Element toXml(String lang) {
    	Element conceptSemtypeNode = new Element("ConceptSemtype");
    	Element concNode = getConcept().toXml("Concept",lang);    	
    	Element semNode = new Element("Semtype");
    	semNode.setText(getSemtype());
    	Element novelNode = new Element("Novel");
    	novelNode.setText(getNovel());
    	conceptSemtypeNode.addContent(concNode);
    	conceptSemtypeNode.addContent(semNode);
    	conceptSemtypeNode.addContent(novelNode);
    	return conceptSemtypeNode;
    }    
    
    /**
     * Implementation of the equals comparison. 
     * @param rhs
     * @return boolean
     */
    public boolean equals(Object rhs)
    {
        if (rhs == null)
            return false;
        if (! (rhs instanceof ConceptSemtype))
            return false;
        ConceptSemtype that = (ConceptSemtype) rhs;
        if (this.getConcept() == null || that.getConcept() == null)
        	return false;
        if (this.getSemtype() == null || that.getSemtype() == null)
        	return false;   
        if (this.getNovel() == null || that.getNovel() == null)
        	return false;           
        return (this.getConcept().equals(that.getConcept()) &&
        		this.getSemtype().equals(that.getSemtype()) &&
        		this.getNovel().equals(that.getNovel()));
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
            int conceptIdValue = this.getConceptSemtypeId() == null ? 0 : this.getConceptSemtypeId().hashCode();
            result = result * 37 + conceptIdValue;
            this.hashValue = result;
        }
        return this.hashValue;
    }  
    
    /**
     * Implementation of compareTo. As long as preferred names are the same, the concepts 
     * are considered equals. Purpose of this method is to enforce alphabetical order when multiple
     * concepts are displayed.
     * @param o
     * @return int
     */
    public int compareTo(Object o) {
    	ConceptSemtype cs = (ConceptSemtype)o;
    	return getConcept().compareTo(cs.getConcept());
    }    
    
}
