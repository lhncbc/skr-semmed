package gov.nih.nlm.semmed.model;

import gov.nih.nlm.semmed.struts.action.QuestionAction;
import gov.nih.nlm.semmed.util.ArticleDataSource;
import gov.nih.nlm.semmed.util.Constants;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.util.LabelValueBean;


/**
 * A list of predications.
 *
 *
 * @author shindongwoo
 *
 */
public class LitePredicationList extends ArrayList<APredication> {

	private static Log log = LogFactory.getLog(LitePredicationList.class);
	//private static Set<Integer> existingDocuments;
	public LitePredicationList() {
	}
	public LitePredicationList(List<APredication> alist) {
		super(alist);
	}

	public LitePredicationList getSubList(String idString) {
		LitePredicationList subList = new LitePredicationList();
		APredication apred = null;
		int id = Integer.parseInt(idString);
		// log.debug("Size of the list = " + this.size());
		 for(int i = 0; i < this.size(); i++) {
			 apred = (APredication) this.get(i);
			if(apred.PMID == id )
				subList.add(apred);
		}
		return subList;
	}

	public String toHTMLString() {
		StringBuffer resultStr = new StringBuffer();
		resultStr.append("<table><tr><th> PMID </th><th>Sentence</th><th>Subject</th><th>Predicate</th><th>Object</th></tr>");
		APredication apred = null;
		// log.debug("Size of the list = " + this.size());
		for(int i = 0; i < this.size(); i++) {
			apred = (APredication) this.get(i);
			resultStr.append("<tr><td>" + apred.PMID + "</td><td>" + apred.sentence + "</td><td>" + apred.subject + "</td><td>" +
					apred.predicate + "</td><td>" + apred.object + "</td></td>");
		}
		resultStr.append("</table>");
		return resultStr.toString();
	}


}
