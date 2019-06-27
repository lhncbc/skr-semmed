package gov.nih.nlm.semmed.summarization;

import gov.nih.nlm.semmed.model.APredication;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class RelevanceFilter implements Filter {
	private static Log log = LogFactory.getLog(RelevanceFilter.class);
	private SingleRowFilter filter;

	public RelevanceFilter(SingleRowFilter filter){
		this.filter = filter;
	}

	public List<APredication> filter(List<APredication> preliminaryList,
			String[] predicateList, List<APredication> listIn, String seed) {

		List<APredication> outList = new ArrayList<APredication>();

		//TODO [Alejandro] Add the semgem check (if p.type==semgem discard)
		for(APredication p : listIn) {
			if ((p.subject.contains(seed) || p.object.contains(seed))) {
				// log.debug(p.predicate.toString() + "\t# " + p.subject.toString() +"\t# " + p.object.toString() + "\t# " + p.subjectSemtype.toString() + "\t# " + p.objectSemtype.toString());
				/* log.debug("subject:" + p.subject.toString());
				log.debug("object:" + p.object.toString());
				log.debug("subjectSemType :" + p.subjectSemtype.toString());
				log.debug("objectSemType :" + p.objectSemtype.toString()); */
				if(filter.filter(p.predicate, p.subjectSemtype,p.objectSemtype)) {
					outList.add(p);
				}
			}
		}
		return outList;
	}
}
