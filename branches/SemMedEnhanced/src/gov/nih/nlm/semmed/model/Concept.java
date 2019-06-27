/*
 * Created on Wed Jan 18 17:26:55 EST 2006 by MyEclipse Hibernate Tool.
 */ 
package gov.nih.nlm.semmed.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
import org.jdom.Element;

/**
 * A class that represents a row in the 'CONCEPT' table. 
 * This class may be customized as it is never re-generated 
 * after being created.
 */
public class Concept
    extends AbstractConcept
    implements Serializable, Comparable
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** The cached hash code value for this instance.  Settting to 0 triggers re-calculation. */
    private int hashValue = 0;	
//	private static Log log = LogFactory.getLog(Concept.class);	
    
    /**
     * Simple constructor of Concept instances.
     */
    public Concept()
    {
    }
    
    /**
     * Concept constructor from an XML element.
     * @param e an XML element containing data for Concept instance.
     */        
    public Concept(Element e){
    	this.setConceptId(Long.valueOf(e.getChildTextTrim("ConceptId")));
    	this.setCui(e.getChildTextTrim("CUI")); 
    	this.setPreferredName(e.getChildTextTrim("PreferredName")); 
    	this.setType(e.getChildTextTrim("Type"));
    	this.setGhr(e.getChildTextTrim("Ghr"));
    	this.setOmim(e.getChildTextTrim("Omim"));

    	Set<ConceptTranslation> cts = new HashSet<ConceptTranslation>();    	
    	if (e.getChild("ConceptTranslation") != null) {
    		cts.add(new ConceptTranslation(e.getChild("ConceptTranslation")));
    	}
    	this.setConceptTranslationSet(cts);
    }

    /**
     * Constructor of Concept instances given a simple primary key.
     * @param conceptId
     */
    public Concept(java.lang.Long conceptId)
    {
        super(conceptId);
    }

    /* Add customized code below */
    /**
     * Returns String representation.
     * @return String a String containing instance data.
     */    
    public String toString() {
    	return getCui() + " " + getPreferredName() + " " + 
    		getType() + " " + getGhr() + " " + getOmim();
    }
    
    /**
     * Converts the instance to an XML element.
     * @param lang if the instance has translation, the language, otherwise null.
     * @param nodeName the node name of the return element.
     * @return Element an XML element
     */    
    public Element toXml(String nodeName, String lang) {    	
    	Element conceptNode = new Element(nodeName);
    	Element idNode = new Element("ConceptId");
    	idNode.setText(Long.toString(getConceptId().longValue()));
    	Element cuiNode = new Element("CUI");
    	cuiNode.setText(getCui());
    	Element preferredNameNode = new Element("PreferredName"); 
    	preferredNameNode.setText(getPreferredName());
    	Element typeNode = new Element("Type");
    	typeNode.setText(getType());
    	Element ghrNode = new Element("Ghr");
    	ghrNode.setText(getGhr());
    	Element omimNode = new Element("Omim");
    	omimNode.setText(getOmim());
    	    	
    	conceptNode.addContent(idNode);
    	conceptNode.addContent(cuiNode);
    	conceptNode.addContent(preferredNameNode); 
    	conceptNode.addContent(typeNode); 
    	conceptNode.addContent(ghrNode); 
    	conceptNode.addContent(omimNode);
    	
    	if (lang == null) 
    		return conceptNode;
    	
    	for(Object o : getConceptTranslationSet()){
    		ConceptTranslation ct = (ConceptTranslation)o;
    		if (lang.equals(ct.getLanguage())) {
    			Element ctNode = ct.toXml("ConceptTranslation");
    			conceptNode.addContent(ctNode);
    			break;
    		}
    	}
    	
    	return conceptNode;
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
        if (! (rhs instanceof Concept))
            return false;
        Concept that = (Concept) rhs;
        if (this.getCui() == null || that.getCui() == null)
        	return false;
        if (this.getPreferredName() == null || that.getPreferredName() == null)
        	return false;
        if (this.getType() == null || that.getType() == null)
        	return false;    
        return (this.getCui().equals(that.getCui()) &&
        		this.getPreferredName().equals(that.getPreferredName()) &&
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
            int conceptIdValue = this.getConceptId() == null ? 0 : this.getConceptId().hashCode();
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
    	Concept c = (Concept)o;
    	return getPreferredName().compareToIgnoreCase(c.getPreferredName());
    }
}
