package gov.nih.nlm.semmed.zsummarization;
public class ConcDegree {
		String concWithST;
		Integer degree;

		ConcDegree(String c, Integer d) {
			concWithST = c;
			degree = d;
		}

		public boolean equals(Object o) {
			ConcDegree cd = (ConcDegree) o;
			if(concWithST.compareTo(cd.concWithST) == 0)
				return true;
			else
				return false;
		}

		public int hashCode() {
			return concWithST.hashCode();
		}

}
