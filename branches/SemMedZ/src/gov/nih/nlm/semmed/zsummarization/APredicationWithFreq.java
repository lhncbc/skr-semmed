package gov.nih.nlm.semmed.zsummarization;

public class APredicationWithFreq  {
	String subj;
	String subjST;
	String relation;
	String obj;
	String objST;
	Integer freq;

	APredicationWithFreq(String subj, String subjST, String relation, String obj, String objST, Integer freq) {
		this.subj = subj;
		this.subjST = subjST;
		this.relation = relation;
		this.obj = obj;
		this.objST = objST;
		this.freq = freq;
	}

	public boolean equals(Object o) {
		APredication ap = (APredication) o;
		if(subj.equals(ap.subj) && subjST.equals(ap.subjST) && relation.equals(ap.relation) &&
				obj.equals(ap.obj) && objST.equals(ap.objST))
			return true;
		else return false;
	}

	public int hashCode() {
		int num = subj.hashCode()* 37 + relation.hashCode() * 17 + obj.hashCode()*7;
		return num;
	}

	public String toString() {
		return subj + " | " + subjST + " | " + relation + " | " + obj + " | " + objST;
	}
}
