package gov.nih.nlm.semmed.model;

import gov.nih.nlm.semmed.util.ArticleDataSource;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;

/**
 *
 * A more concise, faster representation for a Predication
 *
 * @author rodriguezal
 *
 */
public class APredication implements Serializable {

	private static final long serialVersionUID = 1L;

	public String predicate = "";

	public String sentence;

	public String PMID;

	public int PID;

	public int SID;

	public List<String> subjectCUI = new ConceptList(1);

	public List<String> objectCUI = new ConceptList(1);

	public List<String> subject = new ConceptList(1);

	public List<String> object = new ConceptList(1);

	public List<String> subjectSemtype = new ConceptList(1);

	public List<String> objectSemtype = new ConceptList(1);

	public boolean novelSubject;

	public boolean novelObject;

	public ArticleDataSource.SourceType source;

	public void addObject(String s) {
		// if (!object.contains(s))
		// April 30 2014
		// To cope with the change made to PREDICATION_AGGREGATE table From SemMedDB 24_2
		// String[] ss = s.split("\\|\\|\\|");
		String[] ss = s.split("\\|");
		for (String i : ss)
			object.add(i);
	}

	public void addObjectCUI(String s) {
		// if (!objectCUI.contains(s))
		// String[] ss = s.split("\\|\\|\\|");
		String[] ss = s.split("\\|");
		for (String i : ss)
			objectCUI.add(i);
	}

	public void addObjectSemtype(String s) {
		// if (!objectSemtype.contains(s))\
		// String[] ss = s.split("\\|\\|\\|");
		String[] ss = s.split("\\|");
		for (String i : ss)
			objectSemtype.add(i);
	}

	public void addSubject(String s) {
		// if (!subject.contains(s))
		// String[] ss = s.split("\\|\\|\\|");
		String[] ss = s.split("\\|");
		for (String i : ss)
			subject.add(i);
	}

	public void addSubjectCUI(String s) {
		// if (!subject.contains(s))
		// String[] ss = s.split("\\|\\|\\|");
		String[] ss = s.split("\\|");
		for (String i : ss)
			subjectCUI.add(i);
	}

	public void addSubjectSemtype(String s) {
		// if (!subjectSemtype.contains(s))
		// String[] ss = s.split("\\|\\|\\|");
		String[] ss = s.split("\\|");
		for (String i : ss)
			subjectSemtype.add(i);
	}

	public String toString() {
		return "{PMID:" + PMID + ";PID:" + PID + ";SID:" + SID + ";S:"
				+ sentence + "}";
	}

	public boolean equals(Object p) {
		if (p == null || !(p instanceof APredication))
			return false;

		return predicate.equals(((APredication) p).predicate)
				&& subjectCUI.equals(((APredication) p).subjectCUI)
				&& objectCUI.equals(((APredication) p).objectCUI);
	}

	public int hashCode() {
		int hash = 1;
		hash = hash * 31 + predicate.hashCode();
		hash = hash * 31 + subjectCUI.hashCode();
		hash = hash * 31 + objectCUI.hashCode();
		return hash;
	}

	private static class ConceptList extends ArrayList<String> {

		private static final long serialVersionUID = 1L;

		public ConceptList(int i) {
			super(i);
		}

		public String toString() {
			if (size() == 1)
				return get(0);
			else {
				StringBuffer sb = new StringBuffer();
				Set<String> s = new HashSet<String>();
				sb.append(get(0));
				s.add(get(0));
				for (int i = 1; i < size(); i++)
					if (!s.contains(get(i))) {
						sb.append(" | ");
						sb.append(get(i));
						s.add(get(i));
					}
				return sb.toString();
			}
		}
	}

    // Added for calculating Saliency
	  /**
     * Convenience method for finding best subject arguments.
     * Best arguments are determined as following:
     * - If Metathesaurus concept exists for the argument, take that.
     * - Otherwise, take *all* EntrezGene concepts.
     * @return Set a set of subject arguments. (ConceptSemtype instances)
     */
    public Set getBestSubjectSemtypes() {
    	Set subjects = new TreeSet();
    	Iterator iter = subjectSemtype.iterator();
    	while (iter.hasNext()) {
    		ConceptSemtype cs = (ConceptSemtype)iter.next();
    		if (!("ENTREZ".equals(cs.getConcept().getType())))
    			subjects.add(cs);
    	}
    	//no Metathesaurus concept
    	if (subjects.size() == 0) {
        	iter = subjectSemtype.iterator();
        	while (iter.hasNext()) {
        		ConceptSemtype cs = (ConceptSemtype)iter.next();
        		if ("ENTREZ".equals(cs.getConcept().getType()))
        			subjects.add(cs);
        	}
    	}
    	return subjects;
    }


    // Added for calculating Saliency
    /**
     * Convenience method for finding best object arguments.
     * Best arguments are determined as following:
     * - If Metathesaurus concept exists for the argument, take that.
     * - Otherwise, take *all* EntrezGene concepts.
     * @return Set a set of object arguments. (ConceptSemtype instances)
     */
    public Set getBestObjectSemtypes() {
    	// Set o = (Set) objectSemtype;
    	Set objects = new TreeSet();
    	Iterator iter = objectSemtype.iterator();
    	while (iter.hasNext()) {
    		ConceptSemtype cs = (ConceptSemtype)iter.next();
    		if (!("ENTREZ".equals(cs.getConcept().getType())))
    			objects.add(cs);
    	}
    	//no Metathesaurus concept
    	if (objects.size() == 0) {
        	iter = objectSemtype.iterator();
        	while (iter.hasNext()) {
        		ConceptSemtype cs = (ConceptSemtype)iter.next();
        		if ("ENTREZ".equals(cs.getConcept().getType()))
        			objects.add(cs);
        	}
    	}
    	return objects;
    }

}
