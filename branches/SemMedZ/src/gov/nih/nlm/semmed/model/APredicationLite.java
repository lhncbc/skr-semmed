package gov.nih.nlm.semmed.model;
public class APredicationLite {
	String subj;
	String subjST;
	String relation;
	String obj;
	String objST;

	public APredicationLite(String subj, String subjST, String relation, String obj, String objST) {
		this.subj = subj;
		this.subjST = subjST;
		this.obj = obj;
		this.objST = objST;
		this.relation = relation;
	}

	public String getSubj() {
		return this.subj;
	}
	public void setSubj(String subj) {
		this.subj = subj;
	}
	public String getSubjST() {
		return this.subjST;
	}
	public void setSubjST(String subjST) {
		this.subjST = subjST;
	}
	public String getRelation() {
		return relation;
	}
	public void setRelation(String relation) {
		this.relation = relation;
	}
	public String getObj() {
		return this.obj;
	}
	public void setObj(String obj) {
		this.obj = obj;
	}
	public String getObjST() {
		return objST;
	}
	public void setObjST(String objST) {
		this.objST = objST;
	}

	public boolean equals(Object o) {
		APredicationLite ap = (APredicationLite) o;
		if(subj.equals(ap.subj) && subjST.equals(ap.subjST) &&
				obj.equals(ap.obj) && objST.equals(ap.objST) && relation.equals(ap.relation))
			return true;
		else return false;
	}

	public int hashCode() {
		int num =  relation.hashCode()*57 + subj.hashCode()* 47 + subjST.hashCode() * 37 + obj.hashCode()*17 + objST.hashCode()*7;
		return num;
	}

	public String toString() {
		// return subj + " | " + subjST + " - " + obj + " | " + objST;
		return subj + " | " + subjST + " | " + relation + " | " + obj + " | " + objST;
	}
}
