package gov.nih.nlm.semmed.rules;

import gov.nih.nlm.semmed.model.APredication;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class Exists implements Predicate {

	private Set<String> subject;
	private Set<String> object;
	private Set<String> relation;
	
	public Exists(Set<String> subject,Set<String> relation,Set<String> object){
		this.subject = subject;
		this.object = object;
		this.relation = relation;
	}
	
	public boolean eval(List<APredication> atoms) {		
		for(APredication atom:atoms)
			if ((relation.size()==0 || relation.contains(atom.predicate)) &&
			    (subject.size()==0 || !Exists.isDisjoint(subject, atom.subject)) &&
				(object.size()==0 || !Exists.isDisjoint(object, atom.object)))
				return true;
		
		return false;		
	}

	private static <T> boolean isDisjoint(Set<T> set1,Collection<T> set2){
		for(T e : set2)
			if (set1.contains(e))
				return false;
		return true;
	}
	
	@Override
	public String toString(){
		return "(E "+subject+","+object+","+relation+")";
	}
}
