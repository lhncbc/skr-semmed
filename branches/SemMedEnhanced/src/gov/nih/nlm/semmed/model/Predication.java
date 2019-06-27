/*
 * Created Wed Jan 18 17:29:48 EST 2006 by MyEclipse Hibernate Tool.
 */ 
package gov.nih.nlm.semmed.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
import org.jdom.Element;

/**
 * A class that represents a row in the 'PREDICATION' table. 
 * This class may be customized as it is never re-generated 
 * after being created.
 */
public class Predication
    extends AbstractPredication
    implements Serializable
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** The cached hash code value for this instance.  Settting to 0 triggers re-calculation. */
    private int hashValue = 0;
    
//	private static Log log = LogFactory.getLog(Predication.class);    
    
    /**
     * Simple constructor of Predication instances.
     */
    public Predication()
    {
    }

    /**
     * Constructor of Predication instances given a simple primary key.
     * @param predicationId
     */
    public Predication(java.lang.Long predicationId)
    {
        super(predicationId);
    }
    
    /**
     * Predication constructor from an XML element.
     * @param e an XML element containing data for Predication instance.
     */      
    public Predication(Element e) {
    	this.setPredicationId(Long.valueOf(e.getChildTextTrim("PredicationId")));
    	this.setPredicate(e.getChildTextTrim("Predicate"));
    	this.setType(e.getChildTextTrim("Type"));
    	List paNodes = e.getChildren("PredicationArgument");
    	Set<PredicationArgument> paSet = new HashSet<PredicationArgument>();
    	Iterator nodeIter = paNodes.iterator();
    	while (nodeIter.hasNext()) {
    		PredicationArgument pa = new PredicationArgument((Element)nodeIter.next());
    		pa.setPredication(this);
    		paSet.add(pa);    	
    	}
    	this.setPredicationArgumentSet(paSet); 	
    }     

    /* Add customized code below */
    /**
     * Returns String representation.
     * @return String a String containing instance data.
     */     
    public String toString() {
    	String str = getPredicationId() + "-" + getPredicate() + "-" + getType() + "[" ;
    	Iterator iter = getPredicationArgumentSet().iterator();
    	while (iter.hasNext()) {
    		str += "-" + ((PredicationArgument)iter.next()).toString();
    	}
    	str += "]";
    	return str;   	
    }
    
    /**
     * Converts the instance to an XML element.
     * @param lang if the instance has translation, the language, otherwise null.
     * @return Element an XML element
     */    
    public Element toXml(String lang) {
    	Element predNode = new Element("Predication");
    	Element idNode = new Element("PredicationId");
    	idNode.setText(Long.toString(getPredicationId().longValue()));
    	predNode.addContent(idNode);
    	
    	Element predicateNode = new Element("Predicate");
    	predicateNode.setText(getPredicate());
    	Element typeNode = new Element("Type");
    	typeNode.setText(getType());  	
    	
    	predNode.addContent(predicateNode);
    	predNode.addContent(typeNode);
    	
    	Iterator iter = getPredicationArgumentSet().iterator();
    	while (iter.hasNext()) {
    		PredicationArgument pa = (PredicationArgument)iter.next();
    		Element paNode = pa.toXml(lang);
    		predNode.addContent(paNode);
    	}      	
    	return predNode;
    }
    
    
    /**
     * Implementation of the equals comparison. 
     * Subject arguments and object arguments are expected to be the same.
     * @param rhs
     * @return boolean
     */
    public boolean equals(Object rhs)
    {
        if (rhs == null)
            return false;
        if (! (rhs instanceof Predication))
            return false;
        Predication that = (Predication) rhs;
        if (this.getPredicationId() == null || that.getPredicationId() == null)
        	return false;
        if (this.getPredicationId().equals(that.getPredicationId())) return true;
        if (this.getPredicate() == null || that.getPredicate() == null)
        	return false;
        if (this.getType() == null || that.getType() == null)
        	return false; 
        if (this.getPredicate().equals(that.getPredicate()) &&
        		this.getType().equals(that.getType())) {       	

            Set thisSubjs = this.getBestSubjects();
            Set thisObjs = this.getBestObjects();
            Set thatSubjs = that.getBestSubjects();
            Set thatObjs = that.getBestObjects();
            if (thisSubjs.equals(thatSubjs) && thisObjs.equals(thatObjs)) 
            	return true;            	
        }
        return false;      
    }

    /**
     * Implementation of the hashCode method conforming to the Bloch pattern with
     * the exception of array properties (these are very unlikely primary key types).
     * @return int
     */
    public int hashCode()
    {
    	int result = 17;
    	int val = this.getPredicate().hashCode() + this.getType().hashCode();
    	Iterator subjIter = this.getBestSubjects().iterator();
        Iterator objIter = this.getBestObjects().iterator();
        int subjval = 37;
        while (subjIter.hasNext()) {
        	subjval += ((Concept)subjIter.next()).hashCode();
        }
        int objval = 47;
        while (objIter.hasNext()) {
        	objval += ((Concept)objIter.next()).hashCode();
        }  
        result = result * 27 + val + 7 * subjval + 17* objval;
        this.hashValue = result;
        return this.hashValue;
    }
    
    /**
     * Convenience method for finding all subject arguments.
     * @return Set a set of subject arguments.(ConceptSemtype instances)
     */
    public Set getSubjectSemtypes() {
    	Iterator iter = getPredicationArgumentSet().iterator();
    	Set<ConceptSemtype> subjects = new HashSet<ConceptSemtype>();
    	while (iter.hasNext()) {
    		PredicationArgument pa = (PredicationArgument)iter.next();
    		if ("S".equals(pa.getType())) subjects.add(pa.getConceptSemtype()); 
    	}
    	return subjects;
    }
    
    /**
     * Convenience method for finding all object arguments.
     * @return Set a set of object arguments.(ConceptSemtype instances)
     */    
    public Set<ConceptSemtype> getObjectSemtypes() {
    	
    	Set<ConceptSemtype> objects = new HashSet<ConceptSemtype>();
    	for(PredicationArgument pa : getPredicationArgumentSet()) {
    		if ("O".equals(pa.getType())) objects.add(pa.getConceptSemtype()); 
    	}
    	return objects;    	
    }
    
    /**
     * Convenience method for finding all subject arguments.
     * @return Set a set of subject arguments.(Concept instances)
     */    
    public Set getSubjects() {
    	Iterator iter = getSubjectSemtypes().iterator();
    	Set<Concept> subjects = new HashSet<Concept>();
    	while (iter.hasNext()) {
    		Concept c = ((ConceptSemtype)iter.next()).getConcept();
    		subjects.add(c); 
    	}
    	return subjects;
    }
    
    /**
     * Convenience method for finding all object arguments.
     * @return Set a set of object arguments.(Concept instances)
     */       
    public Set getObjects() {
    	Iterator iter = getObjectSemtypes().iterator();
    	Set<Concept> objects = new HashSet<Concept>();
    	while (iter.hasNext()) {
    		Concept c = ((ConceptSemtype)iter.next()).getConcept();
    		objects.add(c); 
    	}
    	return objects;   	
    }    
    
    /**
     * Convenience method for finding all subject preferred names.
     * @return Set a set of subject preferred names.
     */       
    public Set getSubjectNames() {
    	Set s = getSubjects();
    	Set<String> names = new HashSet<String>();    	
    	Iterator iter = s.iterator();
    	while (iter.hasNext()) 
    		names.add(((Concept)iter.next()).getPreferredName());
    	return names;  	 	
    }
    
    /**
     * Convenience method for finding all object preferred names.
     * @return Set a set of object preferred names.
     */      
    public Set getObjectNames() {
    	Set o = getObjects();
    	Set<String> names = new HashSet<String>();    	
    	Iterator iter = o.iterator();
    	while (iter.hasNext()) 
    		names.add(((Concept)iter.next()).getPreferredName());
    	return names;    	  	 	
    }  
    
    /**
     * Convenience method for finding all subject ids.
     * @return Set a set of subject ids.
     */      
    public Set getSubjectCuis() {
    	Set s = getSubjects();
    	Set<String> cuis = new HashSet<String>();    	
    	Iterator iter = s.iterator();
    	while (iter.hasNext()) {
    		cuis.add(((Concept)iter.next()).getCui());
    	}
    	return cuis;      	 	 	
    }
    
    /**
     * Convenience method for finding all object ids.
     * @return Set a set of object ids.
     */      
    public Set getObjectCuis() {
    	Set o = getObjects();
    	Set<String> cuis = new HashSet<String>();    	
    	Iterator iter = o.iterator();
    	while (iter.hasNext()) {
    		cuis.add(((Concept)iter.next()).getCui());
    	}
    	return cuis;    		 	
    }  
    
    /**
     * Convenience method for finding best subject arguments.
     * Best arguments are determined as following:
     * - If Metathesaurus concept exists for the argument, take that.
     * - Otherwise, take *all* EntrezGene concepts.
     * @return Set a set of subject arguments. (ConceptSemtype instances)
     */      
    public Set<ConceptSemtype> getBestSubjectSemtypes() {
    	Set s = getSubjectSemtypes(); 
    	Set<ConceptSemtype> subjects = new TreeSet<ConceptSemtype>();
    	Iterator iter = s.iterator();
    	while (iter.hasNext()) {
    		ConceptSemtype cs = (ConceptSemtype)iter.next();
    		if (!("ENTREZ".equals(cs.getConcept().getType()))) 
    			subjects.add(cs);
    	}    	
    	//no Metathesaurus concept
    	if (subjects.size() == 0) {
        	iter = s.iterator();    		
        	while (iter.hasNext()) {
        		ConceptSemtype cs = (ConceptSemtype)iter.next();
        		if ("ENTREZ".equals(cs.getConcept().getType())) 
        			subjects.add(cs);
        	}   		
    	}
    	return subjects;
    }
    
    /**
     * Convenience method for finding best object arguments.
     * Best arguments are determined as following:
     * - If Metathesaurus concept exists for the argument, take that.
     * - Otherwise, take *all* EntrezGene concepts.
     * @return Set a set of object arguments. (ConceptSemtype instances)
     */     
    public Set<ConceptSemtype> getBestObjectSemtypes() {
    	Set o = getObjectSemtypes();
    	Set<ConceptSemtype> objects = new TreeSet<ConceptSemtype>();    	
    	Iterator iter = o.iterator();
    	while (iter.hasNext()) {
    		ConceptSemtype cs = (ConceptSemtype)iter.next();
    		if (!("ENTREZ".equals(cs.getConcept().getType()))) 
    			objects.add(cs);
    	}    	
    	//no Metathesaurus concept
    	if (objects.size() == 0) {
        	iter = o.iterator();    		
        	while (iter.hasNext()) {
        		ConceptSemtype cs = (ConceptSemtype)iter.next();
        		if ("ENTREZ".equals(cs.getConcept().getType())) 
        			objects.add(cs);
        	}   		
    	}
    	return objects;   	
    }    
    
    /**
     * Convenience method for finding best subject arguments.
     * Best arguments are determined as following:
     * - If Metathesaurus concept exists for the argument, take that.
     * - Otherwise, take *all* EntrezGene concepts.
     * @return Set a set of subject arguments. (Concept instances)
     */     
    public Set<Concept> getBestSubjects() {
    	Set<ConceptSemtype> s = getBestSubjectSemtypes(); 
    	Set<Concept> subjects = new TreeSet<Concept>();
    	for(ConceptSemtype cs : s)
    		subjects.add(cs.getConcept());
    	
    	return subjects;
    }
    
    /**
     * Convenience method for finding best object arguments.
     * Best arguments are determined as following:
     * - If Metathesaurus concept exists for the argument, take that.
     * - Otherwise, take *all* EntrezGene concepts.
     * @return Set a set of object arguments. (Concept instances)
     */     
    public Set<Concept> getBestObjects() {
    	Set<ConceptSemtype> o = getBestObjectSemtypes();
    	Set<Concept> objects = new TreeSet<Concept>();    	
    	for(ConceptSemtype cs: o) 
    		objects.add(cs.getConcept()); 		
    	
    	return objects;   	
    }    

    /**
     * Convenience method for finding subject arguments of a particular type (metathesaurus or entrezgene).
     * @return Set a set of subject arguments. (Concept instances)
     */     
	public Set<Concept> getSubjectsWithType(String type) {
    	Set s = getSubjectSemtypes();
    	Set<Concept> subjects = new HashSet<Concept>();    	
    	Iterator iter = s.iterator();
    	while (iter.hasNext()) {
    		Concept c = ((ConceptSemtype)iter.next()).getConcept();
    		if (type.equals(c.getType()))
    			subjects.add(c);
    	}
		return subjects;		
	}   
	
    /**
     * Convenience method for finding object arguments of a particular type (metathesaurus or entrezgene).
     * @return Set a set of object arguments. (Concept instances)
     */ 	
	public Set<Concept> getObjectsWithType(String type) {
    	Set o = getObjectSemtypes();
    	Set<Concept> objects = new HashSet<Concept>();    	
    	Iterator iter = o.iterator();
    	while (iter.hasNext()) {
    		Concept c = ((ConceptSemtype)iter.next()).getConcept();
    		if (type.equals(c.getType()))
    			objects.add(c);
    	}
		return objects;		
	}	

}
