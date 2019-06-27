/*
 * Created Thu Mar 23 16:12:49 EST 2006 by MyEclipse Hibernate Tool.
 */ 
package gov.nih.nlm.semmed.model;

import java.io.Serializable;

import org.jdom.Element;

/**
 * A class that represents a row in the 'PREDICATION_ARGUMENT' table. 
 * This class may be customized as it is never re-generated 
 * after being created.
 */
public class PredicationArgument
    extends AbstractPredicationArgument
    implements Serializable
{
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int hashValue = 0;	
    /**
     * Simple constructor of PredicationArgument instances.
     */
    public PredicationArgument()
    {
    }

    /**
     * Constructor of PredicationArgument instances given a simple primary key.
     * @param predicationArgumentId
     */
    public PredicationArgument(java.lang.Long predicationArgumentId)
    {
        super(predicationArgumentId);
    }
    
    /**
     * Constructor of PredicationArgument instance from an xml element.
     * @param predicationArgumentId
     */    
    public PredicationArgument(Element e) {
    	this.setConceptSemtype(new ConceptSemtype(e.getChild("ConceptSemtype")));
    	this.setType(e.getChildTextTrim("Type")); 	
    }      

    /* Add customized code below */
    /**
     * Returns String representation.
     * @return String a String containing instance data.
     */     
    public String toString() {   
    	return getConceptSemtype().toString() + "-" + getType();     	
    }
    
    /**
     * Converts the instance to an XML element.
     * @param lang if the instance has translation, the language, otherwise null.
     * @return Element an XML element
     */       
    public Element toXml(String lang) {
    	Element paNode = new Element("PredicationArgument"); 	
    	Element concNode = getConceptSemtype().toXml(lang);
    	Element typeNode = new Element("Type");
    	typeNode.setText(getType());  	
    	
    	paNode.addContent(concNode);
    	paNode.addContent(typeNode);
    	
    	return paNode;
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
        if (! (rhs instanceof PredicationArgument))
            return false;
        PredicationArgument that = (PredicationArgument) rhs;
        if (this.getPredication() == null || that.getPredication() == null)
            return false;   
        if (this.getConceptSemtype() == null || that.getConceptSemtype() == null)
            return false;    
        if (this.getType() == null || that.getType() == null)
            return false;         
        return (this.getPredication().equals(that.getPredication()) &&
        		this.getConceptSemtype().equals(that.getConceptSemtype()) &&
        		this.getType().equals(that.getType()));
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
            int predicationArgumentIdValue = this.getPredicationArgumentId() == null ? 0 : this.getPredicationArgumentId().hashCode();
            result = result * 37 + predicationArgumentIdValue;
            this.hashValue = result;
        }
        return this.hashValue;
    }    

}
