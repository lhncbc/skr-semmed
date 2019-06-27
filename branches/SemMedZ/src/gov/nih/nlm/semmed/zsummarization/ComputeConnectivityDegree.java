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

public class ComputeConnectivityDegree {
	public static void main(String[] args) {

		try {
			BufferedReader in
			 = new BufferedReader(new FileReader(args[0]));
			// = new BufferedReader(new FileReader("C:\\LHC_Projects\\ZhangHan\\Summarize\\Marcelo\\cadio.semrep.novel"));
			PrintWriter out1
			 //  = new PrintWriter(new BufferedWriter(new FileWriter("C:\\LHC_Projects\\ZhangHan\\Summarize\\Marcelo\\NodeDegree.txt")));
			 = new PrintWriter(new BufferedWriter(new FileWriter(args[1])));

			/* PrintWriter out1
			   = new PrintWriter(new BufferedWriter(new FileWriter(args[1] + "\\Connectivity.txt"))); */
			Hashtable ConceptTable = new Hashtable();
			String aLine = null;

			while((aLine = in.readLine()) != null) {
					String component[] = aLine.split("\\|");
					if(component != null && component.length > 4 && component[1].equals("relation") && (component[2].compareTo("compared_with") != 0)) { // do the following for the predication
							// PMID = Integer.parseInt(component[0].substring(0,8).trim());

							String SubjConcept = component[3].trim();
							String ObjConcept = component[10].trim();
							String SubjST = component[5].trim();
							String ObjST = component[12].trim();
							// System.out.println("Subj: " + SubjConcept + "|" + SubjST + "\t Obj: " + ObjConcept + "|" + ObjST);
							HashSet adjacentSet1 = null;
							String css = new String(SubjConcept + "|" + SubjST);
							String cso = new String(ObjConcept + "|" + ObjST);

							if(SubjConcept != null && SubjConcept.compareTo("") != 0 && (adjacentSet1 = (HashSet) ConceptTable.get(css)) != null) {
								// System.out.println(css.toString() + "is already found");
								if(!adjacentSet1.contains(cso)) {
									adjacentSet1.add(cso);
									// System.out.println(cso.toString() + "is already found");
								}
							} else if (SubjConcept != null && SubjConcept.compareTo("") != 0) {
								adjacentSet1 = new HashSet();
								adjacentSet1.add(cso);
								ConceptTable.put(css,adjacentSet1);
							}

							HashSet adjacentSet2 = null;
							if(ObjConcept != null && ObjConcept.compareTo("") != 0 && (adjacentSet2 = (HashSet) ConceptTable.get(cso)) != null) {
								 // System.out.println(cso.toString() + "is already found");
								if(!adjacentSet2.contains(css)) {
									adjacentSet2.add(css);
									// System.out.println(css.toString() + "is already found");
								}
							} else if (ObjConcept != null && ObjConcept.compareTo("") != 0) {
								adjacentSet2 = new HashSet();
								adjacentSet2.add(css);
								ConceptTable.put(cso,adjacentSet2);
							}

					} // if
			} // end while


			Set concSet = (Set) ConceptTable.keySet();
			List concList = new ArrayList(concSet);
			Iterator itConc = concList.iterator();
			int i = 0;
			int totalDegree = 0;
			List concDegreeList = new ArrayList();
			while(itConc.hasNext()) {
				String concST = (String) itConc.next();
				HashSet adjSet = (HashSet) ConceptTable.get(concST);
				totalDegree = totalDegree + adjSet.size();
				ConcDegree cd = new ConcDegree(concST, new Integer(adjSet.size()));
				concDegreeList.add(cd);
				// System.out.println(concST.concept	+ " | " + concST.ST +  "\t | \t" + adjSet.size());
				i++;
			}

			Collections.sort(concDegreeList, new ConcComp());
			Iterator itConcDegree = concDegreeList.iterator();
			float accumperc = 0;
			while(itConcDegree.hasNext()) {
				ConcDegree cd = (ConcDegree) itConcDegree.next();
				float perc = ((float)(cd.degree*100.0)/(float) totalDegree);
				accumperc = accumperc + perc;
				out1.println(cd.concWithST	 + " | " + cd.degree + " | " + perc + " | " + accumperc);
			}

			out1.close();
			System.out.println("Total degree = " + totalDegree);
			System.out.println("Connectivity computation completed!!");
			System.out.println("The number of unique concepts is " + i);

		} catch(FileNotFoundException fnf) {
			System.out.println(fnf);
		}catch(IOException ioe) {
			System.out.println(ioe);
		}
	}
}

class ConcWithST {
	String concept;
	String ST;

	ConcWithST(String c, String st) {
		concept = c;
		ST = st;
	}

	public boolean equals(ConcWithST c) {
		if(concept.compareTo(c.concept) ==0 && ST.compareTo(c.ST) ==0)
			return true;
		else return false;
	}

	public int hashCode() {
		int num = concept.hashCode()* 37 + ST.hashCode();
		return num;
	}

	public String toString() {
		return concept + " | " + ST;
	}
}



