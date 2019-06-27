package gov.nih.nlm.semmed.rules;

import gov.nih.nlm.semmed.model.APredication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;


public class Filter {

	public static void filterNonRelevants(List<APredication> inputPredications,Predicate rule,List<APredication> relevantPredications,
			List<Integer> relevantCitations, List<APredication> nonRelevantPredications,List<Integer> nonRelevantCitations){
		
		if (inputPredications.size()==0)
			return;
		
		List<APredication> sortedPredications = new ArrayList<APredication>(inputPredications);
		
		Collections.sort(sortedPredications, new Comparator<APredication>(){
			public int compare(APredication p1,APredication p2){
				return p1.PMID-p2.PMID;
			}
		});


		Iterator<APredication> iterator = sortedPredications.iterator();

		List<APredication> currentList = new ArrayList<APredication>();
		APredication temp = iterator.next();

		int currentPMID = temp.PMID;
		currentList.add(temp);


		while(iterator.hasNext()){
			temp = iterator.next();
			
			if (temp.PMID!=currentPMID){
				if (rule.eval(currentList)){
					relevantCitations.add(currentPMID);
					relevantPredications.addAll(currentList);
				}else{
					nonRelevantCitations.add(currentPMID);
					nonRelevantPredications.addAll(currentList);
				}
				currentList.clear();
			}
			currentList.add(temp);
			currentPMID = temp.PMID;
		}
		if (rule.eval(currentList)){
			relevantCitations.add(currentPMID);
			relevantPredications.addAll(currentList);
		}else{
			nonRelevantCitations.add(currentPMID);
			nonRelevantPredications.addAll(currentList);
		}
	}
	
}
