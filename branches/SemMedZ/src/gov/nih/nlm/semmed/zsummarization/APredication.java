package gov.nih.nlm.semmed.zsummarization;
public class APredication {
	String subj;
	String subjST;
	String relation;
	String obj;
	String objST;


	APredication(String subj, String subjST, String relation, String obj, String objST) {
		this.subj = subj;
		this.subjST = subjST;
		this.obj = obj;
		this.objST = objST;
		this.relation = relation;
	}

	public boolean equals(Object o) {
		APredication ap = (APredication) o;
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
