/*
 * Created Wed Jan 18 17:34:59 EST 2006 by MyEclipse Hibernate Tool.
 */ 
package gov.nih.nlm.semmed.model;

import java.io.Serializable;

import org.jdom.Element;

/**
 * A class that represents a row in the 'SENTENCE_PREDICATION' table. 
 * This class may be customized as it is never re-generated 
 * after being created.
 */
public class SentencePredication
    extends AbstractSentencePredication
    implements Serializable
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	/**
     * Simple constructor of SentencePredication instances.
     */
    public SentencePredication()
    {
    }

    /**
     * Constructor of SentencePredication instances given a simple primary key.
     * @param sentencePredicationId
     */
    public SentencePredication(java.lang.Long sentencePredicationId)
    {
        super(sentencePredicationId);
    }
    
    public SentencePredication(Element e) {
    	this.setSentence(new Sentence(e.getChild("Sentence")));
    	this.setPredication(new Predication(e.getChild("Predication")));
    	this.setPredicationNumber(Integer.valueOf(e.getChildTextTrim("PredicationNumber")));
    }

    /* Add customized code below */
    public String toString() {
    	
    	return this.getSentence().toString() + " " + 
    			//this.getPredication().toString() + " " + 
    			this.getPredication().getSubjectNames() + "-" + this.getPredication().getPredicate() + "-" + this.getPredication().getObjectNames() + " "  +
    			this.getPredicationNumber().toString();
    }
    
/*    public Element toXml() {
    	Element sentPredNode = new Element("SentencePredication");
    	Element sentNode = getSentence().toXml();
    	Element predNode = getPredication().toXml();
    	Element predNumberNode = new Element("PredicationNumber");
    	predNumberNode.setText(this.getPredicationNumber().toString());
    	
    	sentPredNode.addContent(sentNode);
    	sentPredNode.addContent(predNode);
    	sentPredNode.addContent(predNumberNode);
    	return sentPredNode;
    }*/
    
    public Element toXml(String lang) {
    	Element sentPredNode = new Element("SentencePredication");
    	Element sentNode = getSentence().toXml();
    	Element predNode = getPredication().toXml(lang);
    	Element predNumberNode = new Element("PredicationNumber");
    	predNumberNode.setText(this.getPredicationNumber().toString());
    	
    	sentPredNode.addContent(sentNode);
    	sentPredNode.addContent(predNode);
    	sentPredNode.addContent(predNumberNode);
    	return sentPredNode;
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
        if (! (rhs instanceof SentencePredication))
            return false;
        SentencePredication that = (SentencePredication) rhs;
        if (this.getSentence() == null || that.getSentence() == null)
            return false;
        if (this.getPredication() == null || that.getPredication() == null)
        	return false;
        if (this.getPredicationNumber() == null || that.getPredicationNumber() == null)
        	return false;        
        return (this.getSentence().equals(that.getSentence()) &&
        		this.getPredication().equals(that.getPredication()) &&
        		this.getPredicationNumber().equals(that.getPredicationNumber()));
    }    
    
    //TODO write hash method [Alejandro]
}
