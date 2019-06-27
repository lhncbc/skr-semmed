package gov.nih.nlm.semmed.summarization;

import gov.nih.nlm.semmed.model.APredication;

import java.util.ArrayList;
import java.util.List;


/**
 * 
 * @author rodriguezal
 *
 */
public class PreliminaryFilter implements Filter{

	private static PreliminaryFilter myInstance = new PreliminaryFilter();
	
	private PreliminaryFilter(){}
	
	public static PreliminaryFilter getInstance(){
		return myInstance;
	}
	
	public List<APredication> filter(List<APredication> preliminaryList,String[] predicateList, 
            List<APredication> listIn, String seed){
		
		List<APredication> outList = new ArrayList<APredication>();
		for(APredication p : listIn)
			if (!containsDummyConcept(p.subjectSemtype) && 
			    !containsDummyConcept(p.objectSemtype) && 
			    !equalArguments(p))
			outList.add(p);		

		preliminaryList.addAll(outList);
		return outList;
	}
	
	private boolean containsDummyConcept(List<String> args) {
		if (args.size() == 1 && "None".equals(args.get(0))) 
			return true;		
		return false;
	}
	
	private boolean equalArguments(APredication p) {
		return p.subject.equals(p.object); //TODO [Alejandro] Should check CUI instead of preferred names		
	}


}
