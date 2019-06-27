package gov.nih.nlm.semmed.summarization;

import gov.nih.nlm.semmed.model.APredication;
import gov.nih.nlm.semmed.util.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author rodriguezal
 *
 */
public class ConnectivityFilter implements Filter {

	private static Log log = LogFactory.getLog(ConnectivityFilter.class);
	private static ConnectivityFilter myInstance = new ConnectivityFilter();

	public static ConnectivityFilter getInstance(){
		return myInstance;
	}

	private ConnectivityFilter(){}

	/*
	 * The predicateList must be sorted
	 * (non-Javadoc)
	 * @see gov.nih.nlm.semmed.summarization.Filter#filter(java.util.List, java.lang.String[], java.util.List, java.lang.String)
	 */
	public List<APredication> filter(List<APredication> preliminaryList,
			String[] predicateList, List<APredication> listIn, String seed) {

		List<APredication> outList = new ArrayList<APredication>();
		Set<String> potentialConcepts = new HashSet<String>();

		for(APredication p : listIn){
			if (p.predicate.trim().equals(Constants.LOCATION_OF) == false && p.predicate.trim().equals(Constants.PROCESS_OF) == false && p.predicate.trim().equals(Constants.COEXISTS_WITH) == false)
			if (predicateList!=null ||
			   (p.predicate.startsWith("NEG_") && (Arrays.binarySearch(predicateList, p.predicate.substring(4))>=0)) ||
			   Arrays.binarySearch(predicateList, p.predicate)>=0){
					if (p.subject.contains(seed))
						potentialConcepts.addAll(p.object);
					if (p.object.contains(seed))
						potentialConcepts.addAll(p.subject);
			}
		}

		for(APredication p : preliminaryList) {
			if (listIn.contains(p))
				outList.add(p);
			else if ((Summarizer.intersects(potentialConcepts,p.subject) || Summarizer.intersects(potentialConcepts,p.object)))
				outList.add(p);
		}
		return outList;
	}


}
