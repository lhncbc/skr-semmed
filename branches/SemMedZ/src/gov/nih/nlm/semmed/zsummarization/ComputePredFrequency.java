package gov.nih.nlm.semmed.zsummarization;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.File;
import java.lang.NumberFormatException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.Enumeration;
import java.util.List;
import java.util.Collections;
import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Set;
import java.util.Comparator;

public class ComputePredFrequency {
	public static void main(String[] args) {

		try {
			BufferedReader in
			 // = new BufferedReader(new FileReader(args[0]));
			 = new BufferedReader(new FileReader("C:\\LHC_Projects\\ZhangHan\\Programs\\FindClique\\heartdisease.typic11"));
			PrintWriter out1
			   = new PrintWriter(new BufferedWriter(new FileWriter("C:\\LHC_Projects\\ZhangHan\\Programs\\FindClique\\smallSample.token")));
			PrintWriter out2
			   = new PrintWriter(new BufferedWriter(new FileWriter("C:\\LHC_Projects\\ZhangHan\\Programs\\FindClique\\smallSample.typic")));
			 // = new PrintWriter(new BufferedWriter(new FileWriter(args[1])));

			/* PrintWriter out1
			   = new PrintWriter(new BufferedWriter(new FileWriter(args[1] + "\\Connectivity.txt"))); */
			Hashtable predTableToken = new Hashtable();
			Hashtable predTableTypi = new Hashtable();
			String aLine = null;
			int PMID = 0;
			int prevPMID = 0;
			int numOfLine = 0;
			Hashtable predTablePerPMID = new Hashtable();

			while((aLine = in.readLine()) != null) {

					if(aLine.startsWith("-----")) {
						prevPMID = PMID;
						int dotIndex = aLine.indexOf(" ", 16);
						if(dotIndex < 0)
							PMID = Integer.parseInt(aLine.substring(14,23).trim());
						else {
							System.out.println("Index = " + dotIndex + ", " + aLine.substring(14,dotIndex).trim());
							PMID = Integer.parseInt(aLine.substring(14,dotIndex).trim());
						}
							if(PMID != prevPMID && prevPMID != 0) {
							Enumeration predEnum = predTablePerPMID.keys();
							while(predEnum.hasMoreElements()) { // Do loop for all the predications found in the citation
								APredication ap = (APredication) predEnum.nextElement();
								Integer freqPerPMID = (Integer) predTablePerPMID.get(ap);
								Integer freq1 = (Integer) predTableToken.get(ap);
								if(freq1 != null) {
									freq1 = freq1+freqPerPMID; // Increment frequence by the number of prequence of the predication
									predTableToken.remove(ap);
									predTableToken.put(ap, new Integer(freq1));
								} else
									predTableToken.put(ap, new Integer(freqPerPMID));

								// Integer freq3 = (Integer) predTableToken.get(ap);
								Integer freq2 = (Integer) predTableTypi.get(ap);
								if(freq2 != null) {
									freq2 = freq2+1;
									predTableTypi.remove(ap);
									predTableTypi.put(ap, new Integer(freq2));
								} else
									predTableTypi.put(ap, new Integer(1)); // Increment the freuqncy by 1
							}
							predTablePerPMID = null;
							predTablePerPMID = new Hashtable();
						}
					} else {
						String component[] = aLine.split("\\|");
						if(component != null && component.length > 4 && component[1].equals("relation") && (component[2].compareTo("compared_with") != 0)) { // do the following for the predication
							// prevPMID = PMID;
							// PMID = Integer.parseInt(component[0].substring(0,8).trim());
							String SubjConcept = component[3].trim();
							String ObjConcept = component[10].trim();
							String SubjST = component[5].trim();
							String ObjST = component[12].trim();
							String Relation = component[8].trim();
							APredication curPred = new APredication(SubjConcept, SubjST, Relation, ObjConcept, ObjST);
							Integer freq1 = (Integer) predTablePerPMID.get(curPred);
							if(freq1 != null) {
								freq1 = freq1+1; // Increment frequence by the number of prequence of the predication
								predTablePerPMID.remove(curPred);
								predTablePerPMID.put(curPred, new Integer(freq1));
							} else
								predTablePerPMID.put(curPred, new Integer(1));
						} // if
					} // else
			} // end while
				Enumeration predEnum = predTablePerPMID.keys();
				while(predEnum.hasMoreElements()) { // Do loop for all the predications found in the citation
					APredication ap = (APredication) predEnum.nextElement();
					Integer freqPerPMID = (Integer) predTablePerPMID.get(ap);
					Integer freq1 = (Integer) predTableToken.get(ap);
					if(freq1 != null) {
						freq1 = freq1+freqPerPMID; // Increment frequence by the number of prequence of the predication
						predTableToken.remove(ap);
						predTableToken.put(ap, new Integer(freq1));
					} else
						predTableToken.put(ap, new Integer(freqPerPMID));

					// Integer freq3 = (Integer) predTableToken.get(ap);
					Integer freq2 = (Integer) predTableTypi.get(ap);
					if(freq2 != null) {
						freq2 = freq2+1;
						predTableTypi.remove(ap);
						predTableTypi.put(ap, new Integer(freq2));
					} else
						predTableTypi.put(ap, new Integer(1)); // Increment the freuqncy by 1
				}
				predTablePerPMID = null;
				predTablePerPMID = new Hashtable();


			Set predTokenSet = (Set) predTableToken.keySet();
			List predTokenList = new ArrayList(predTokenSet);
			Iterator itTokenPred = predTokenList.iterator();
			int i = 0;
			List predTokenFreqList = new ArrayList();
			while(itTokenPred.hasNext()) {
				APredication pred = (APredication) itTokenPred.next();
				Integer freq1 = (Integer) predTableToken.get(pred);
				APredicationWithFreq predFreq = new APredicationWithFreq(pred.subj,pred.subjST, pred.relation, pred.obj, pred.objST, new Integer(freq1));
				predTokenFreqList.add(predFreq);
				// out1.println(predFreq.toString() + " | " +  freq1);
				// System.out.println(pred.toString() + " | " + freq1);
				i++;
			}
			System.out.println("# of predications for Token= " + i);

			Collections.sort(predTokenFreqList, new PredComp());
			Iterator itpredFreqToken = predTokenFreqList.iterator();
			while(itpredFreqToken.hasNext()) {
				APredicationWithFreq predFreq = (APredicationWithFreq) itpredFreqToken.next();
				out1.println(predFreq.toString() + " | " +  predFreq.freq);
				System.out.println(predFreq.toString() + " | " +  predFreq.freq);
			}
			out1.close();
			i=0;

			Set predTypiSet = (Set) predTableTypi.keySet();
			List predTypiList = new ArrayList(predTypiSet);
			Iterator itTypiPred = predTypiList.iterator();
			List predTypiFreqList = new ArrayList();
			while(itTypiPred.hasNext()) {
				APredication pred = (APredication) itTypiPred.next();
				Integer freq2 = (Integer) predTableTypi.get(pred);
				APredicationWithFreq predFreq = new APredicationWithFreq(pred.subj,pred.subjST, pred.relation, pred.obj, pred.objST, new Integer(freq2));
				predTypiFreqList.add(predFreq);
				// out2.println(predFreq.toString() + " | " +  freq2);
				// System.out.println(concST.concept	+ " | " + concST.ST +  "\t | \t" + adjSet.size());
				i++;
			}
			System.out.println("# of predications for Typicality = " + i);

			Collections.sort(predTypiFreqList, new PredComp());
			Iterator itpredFreqTypi = predTypiFreqList.iterator();
			while(itpredFreqTypi.hasNext()) {
				APredicationWithFreq predFreq = (APredicationWithFreq) itpredFreqTypi.next();
				out2.println(predFreq.toString() + " | " +  predFreq.freq);
				System.out.println(predFreq.toString() + " | " +  predFreq.freq);
			}
			out2.close();

		} catch(FileNotFoundException fnf) {
			System.out.println(fnf);
		}catch(IOException ioe) {
			System.out.println(ioe);
		}
	}
}




