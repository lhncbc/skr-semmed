package gov.nih.nlm.semmed.summarization;

import gov.nih.nlm.semmed.model.APredication;
import gov.nih.nlm.semmed.struts.action.SummaryAction;

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
		/* static PrintWriter out1 = null;
		static PrintWriter out2 = null;
		static PrintWriter out3 = null;
		static PrintWriter out4 = null;
		static PrintWriter out5 = null; */

		static {
			/* try {
				// out = new PrintWriter(new BufferedWriter(new FileWriter("C:\\Projects\\SemMedDebug\\breastcancer\\SemRepPredication.out")));
				out1	= new PrintWriter(new BufferedWriter(new FileWriter("C:\\Projects\\SemMedDebug\\breastcancer\\SemRepPredication.out")));
				out2	= new PrintWriter(new BufferedWriter(new FileWriter("C:\\Projects\\SemMedDebug\\breastcancer\\SemRepRelevance.out")));
				out3  = new PrintWriter(new BufferedWriter(new FileWriter("C:\\Projects\\SemMedDebug\\breastcancer\\SemRepConnectivity.out")));
				out4 = new PrintWriter(new BufferedWriter(new FileWriter("C:\\Projects\\SemMedDebug\\breastcancer\\SemRepNovelty.out")));
				out5 = new PrintWriter(new BufferedWriter(new FileWriter("C:\\Projects\\SemMedDebug\\breastcancer\\SemRepSalience.out")));
				// out5 = new PrintWriter(new BufferedWriter(new FileWriter("C:\\Projects\\SemMedDebug\\breastcancer\\SemRepstep5.out")));
			} catch(FileNotFoundException fnf) {
				System.out.println(fnf);
			}catch(IOException ioe) {
				System.out.println(ioe);
			} */
		}

	public static List<APredication> summarize(Filter[] filters,List<APredication> listIn,String[] predicates, String seed){

		List<APredication> tempIn = listIn;
		List<APredication> preliminaryList = new ArrayList<APredication>();

		int loop = 0;
		 // try {
			 // out1	= new PrintWriter(new BufferedWriter(new FileWriter("C:\\Projects\\SemMedDebug\\breastcancer\\SemRepPredication.out")));
			 // out2	= new PrintWriter(new BufferedWriter(new FileWriter("C:\\Projects\\SemMedDebug\\breastcancer\\SemRepRelevance.out")));
			 // out3  = new PrintWriter(new BufferedWriter(new FileWriter("C:\\Projects\\SemMedDebug\\breastcancer\\SemRepConnectivity.out")));
			 // out4 = new PrintWriter(new BufferedWriter(new FileWriter("C:\\Projects\\SemMedDebug\\breastcancer\\SemRepNovelty.out")));
			 // out5 = new PrintWriter(new BufferedWriter(new FileWriter("C:\\Projects\\SemMedDebug\\breastcancer\\SemRepSalience.out")));

			 for(Filter filter : filters) {
				 loop++;
				 tempIn = filter.filter(preliminaryList, predicates, tempIn, seed);
				 // System.out.println("# of filtering = " + loop);
				 // System.out.println("Filter Name: " + filter.getClass().getName());
				 // System.out.println("After this iteration, "+tempIn.size());
				 if(loop == 1) {
					 for(int i = 0 ; i < tempIn.size(); i++) {
						 APredication ap = tempIn.get(i);
						 // out1.println(ap.PMID + "\t#" + ap.predicate + "\t#" + ap.subject + "\t#" + ap.subjectSemtype + "\t#" + ap.object + "\t#" + ap.objectSemtype + "\t#" + ap.novelSubject + "\t#" + ap.novelObject);
					 }
				 }
				 if(loop == 2) {
				for(int i = 0 ; i < tempIn.size(); i++) {
					APredication ap = tempIn.get(i);
					// out2.println(ap.PMID + "\t#" + ap.predicate + "\t#" + ap.subject + "\t#" + ap.subjectSemtype + "\t#" + ap.object + "\t#" + ap.objectSemtype + "\t#" + ap.novelSubject + "\t#" + ap.novelObject);
				}
				 }
				 if(loop == 3) {
					for(int i = 0 ; i < tempIn.size(); i++) {
						APredication ap = tempIn.get(i);
						// out3.println(ap.PMID + "\t#" + ap.predicate + "\t#" + ap.subject + "\t#" + ap.subjectSemtype + "\t#" + ap.object + "\t#" + ap.objectSemtype + "\t#" + ap.novelSubject + "\t#" + ap.novelObject);
					}
				 }
				 else if(loop == 4) {
					for(int i = 0 ; i < tempIn.size(); i++) {
						APredication ap = tempIn.get(i);

					}
				 }
				 else if(loop == 5)  {
					for(int i = 0 ; i < tempIn.size(); i++) {
						APredication ap = tempIn.get(i);
					}
				 }
			 }

			/* } catch(FileNotFoundException fnf) {
				System.out.println(fnf);
			}catch(IOException ioe) {
				System.out.println(ioe);
			} */
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
