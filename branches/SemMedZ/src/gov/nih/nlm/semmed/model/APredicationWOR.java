package gov.nih.nlm.semmed.model;

public class APredicationWOR {
	String subj;
	String subjST;
	String obj;
	String objST;


	public APredicationWOR(String subj, String subjST,  String obj, String objST) {
		this.subj = subj;
		this.subjST = subjST;
		this.obj = obj;
		this.objST = objST;
	}

	public String getSubj() {
		return subj;
	}
	public void setSubj(String subj) {
		this.subj = subj;
	}
	public String getSubjST() {
		return subjST;
	}
	public void setSubjST(String subjST) {
		this.subjST = subjST;
	}
	public String getObj() {
		return obj;
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
		APredicationWOR ap = (APredicationWOR) o;
		if(subj.equals(ap.subj) && subjST.equals(ap.subjST) &&
				obj.equals(ap.obj) && objST.equals(ap.objST))
			return true;
		else return false;
	}

	public int hashCode() {
		int num = subj.hashCode()* 47 + subjST.hashCode() * 37 + obj.hashCode()*17 + objST.hashCode()*7;
		return num;
	}

	public String toString() {
		return subj + " | " + subjST + " - " + obj + " | " + objST;
	}
}
