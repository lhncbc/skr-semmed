package gov.nih.nlm.semmed.zsummarization;

import java.util.Comparator;

public class PredComp implements Comparator {

	public int compare(Object o1, Object o2) {
		APredicationWithFreq pred1 = (APredicationWithFreq) o1;
		APredicationWithFreq pred2 = (APredicationWithFreq) o2;

		if(pred1.freq > pred2.freq)
			return -1;
		else if(pred1.freq == pred2.freq)
			return 0;
		else
			return 1;
	}
}
