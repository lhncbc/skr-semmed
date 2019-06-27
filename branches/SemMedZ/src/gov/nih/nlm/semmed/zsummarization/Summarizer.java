package gov.nih.nlm.semmed.zsummarization;

import gov.nih.nlm.semmed.model.APredication;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
// import gov.nih.nlm.semmed.summarization.Filter;

/**
 * Main class for the summarization process
 *
 * Applies a list of filters to a list of predications
 *
 * @author rodriguezal
 *
 */
public class Summarizer {

	private static Log log = LogFactory.getLog(Summarizer.class);

	public static List<APredication> summarize(Filter[] filters,List<APredication> listIn,String[] predicates){

		List<APredication> tempIn = listIn;
		List<APredication> preliminaryList = new ArrayList<APredication>();


			 for(Filter filter : filters) {
				 tempIn = filter.filter(preliminaryList, predicates, tempIn);
				 for(int i = 0 ; i < tempIn.size(); i++) {
						 APredication ap = tempIn.get(i);
				 }
			 }
			return tempIn;
	}

	public static <T> boolean intersects(Collection<T> c1,Collection<T> c2){
		for(T t:c2) {
			if (t != null && ((String)t).length() > 0) // Check the validity of t
					if ( c1.contains(t)) {
						return true;
					}
					else ;
			else log.debug("Invalid semtype in intersects: " + t);
		}
		return false;
	}
}
