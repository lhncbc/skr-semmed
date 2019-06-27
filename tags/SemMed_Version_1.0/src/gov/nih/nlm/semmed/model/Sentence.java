/*
 * Created Wed Jan 18 17:23:59 EST 2006 by MyEclipse Hibernate Tool.
 */ 
package gov.nih.nlm.semmed.model;

import java.io.Serializable;

import org.jdom.Element;

/**
 * A class that represents a row in the 'SENTENCE' table. 
 * This class may be customized as it is never re-generated 
 * after being created.
 */
public class Sentence
    extends AbstractSentence
    implements Serializable
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** The cached hash code value for this instance.  Settting to 0 triggers re-calculation. */
    private int hashValue = 0;		
    /**
     * Simple constructor of Sentence instances.
     */
    public Sentence()
    {
    }

    /**
     * Constructor of Sentence instances given a simple primary key.
     * @param sentenceId
     */
    public Sentence(java.lang.Long sentenceId)
    {
        super(sentenceId);
    }
    
    public Sentence(Element e) {
    	this.setPmid(e.getChildTextTrim("PMID"));
    	this.setType(e.getChildTextTrim("Type")); 
    	this.setNumber(Integer.valueOf(e.getChildTextTrim("Number")));  
    	this.setSentence(e.getChildTextTrim("SentenceText"));    	
    }    

    /* Add customized code below */
    public String toString() {
    	return getPmid() + "." + getType() + "." + getNumber() + " " + getSentence();
    }
    
    public Element toXml() {
    	Element sentNode = new Element("Sentence");
    	Element pmidNode = new Element("PMID");
    	pmidNode.setText(getPmid());
    	Element typeNode = new Element("Type"); 
    	typeNode.setText(getType());
    	Element numberNode = new Element("Number");
    	numberNode.setText(getNumber().toString());
    	Element sentenceNode = new Element("SentenceText"); 
    	sentenceNode.setText(getSentence());
    	
    	sentNode.addContent(pmidNode);
    	sentNode.addContent(typeNode);    	
    	sentNode.addContent(numberNode); 	
    	sentNode.addContent(sentenceNode); 	    	
    	return sentNode;
    	
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
        if (! (rhs instanceof Sentence))
            return false;
        Sentence that = (Sentence) rhs;
        //if (this.getSentenceId() == null || that.getSentenceId() == null)
        //    return false;
        if (this.getPmid() == null || that.getPmid() == null)
        	return false;
        if (this.getType() == null || that.getType() == null)
        	return false;
        if (this.getNumber() == null || that.getNumber() == null)
        	return false;
        return (//this.getSentenceId().equals(that.getSentenceId()) &&
        		this.getPmid().equals(that.getPmid()) &&
        		this.getType().equals(that.getType()) &&
        		this.getNumber().intValue() == that.getNumber().intValue());
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
            int sentenceIdValue = this.getSentenceId() == null ? 0 : this.getSentenceId().hashCode();
            result = result * 37 + sentenceIdValue;
            this.hashValue = result;
        }
        return this.hashValue;
    } 
}
