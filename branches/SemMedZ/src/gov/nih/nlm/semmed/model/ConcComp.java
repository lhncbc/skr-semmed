package gov.nih.nlm.semmed.model;

import java.util.Comparator;

public class ConcComp implements Comparator {

	public int compare(Object o1, Object o2) {
		ConcDegree cd1 = (ConcDegree) o1;
		ConcDegree cd2 = (ConcDegree) o2;
		if(cd1.degree > cd2.degree)
			return -1;
		else if(cd1.degree == cd2.degree)
			return 0;
		else
			return 1;
	}
}
