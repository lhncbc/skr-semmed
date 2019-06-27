//Created by MyEclipse Struts
// XSL source (default): platform:/plugin/com.genuitec.eclipse.cross.easystruts.eclipse_4.0.1/xslt/JavaClass.xsl

package gov.nih.nlm.semmed.struts.action;

import gov.nih.nlm.semmed.exception.EssieException;
import gov.nih.nlm.semmed.exception.PubmedException;
import gov.nih.nlm.semmed.exception.SemMedException;
import gov.nih.nlm.semmed.exception.UploadException;
import gov.nih.nlm.semmed.exception.XMLException;
import gov.nih.nlm.semmed.model.APredication;
import gov.nih.nlm.semmed.model.APredicationList;
import gov.nih.nlm.semmed.model.APredicationLite;
import gov.nih.nlm.semmed.model.APredicationWOR;
import gov.nih.nlm.semmed.model.ConcDegree;
import gov.nih.nlm.semmed.model.ConcComp;
import gov.nih.nlm.semmed.model.TestPredicationList;
import gov.nih.nlm.semmed.model.SemrepResultSet;
import gov.nih.nlm.semmed.model.SentencePredication;
import gov.nih.nlm.semmed.struts.form.SummaryForm;
import gov.nih.nlm.semmed.util.ArticleDataSource;
import gov.nih.nlm.semmed.util.Constants;
import gov.nih.nlm.semmed.util.HibernateSessionFactory;
import gov.nih.nlm.semmed.util.XMLUtils;
import gov.nih.nlm.semmed.summarization.*;
import gov.nih.nlm.semmed.util.GraphUtils;
import gov.nih.nlm.semmed.util.SemanticGroups;
import gov.nih.nlm.semmed.servlet.GraphServlet;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.UUID;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.LookupDispatchAction;
import org.apache.struts.upload.FormFile;
import org.apache.struts.util.LabelValueBean;
import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;


/**
 * MyEclipse Struts Creation date: 01-24-2006
 *
 * XDoclet definition:
 *
 * @struts.action path="/Summary" name="SummaryForm" input="/jsp/summary.jsp"
 *                parameter="method" scope="request" validate="true"
 * @struts.action-forward name="success" path="/jsp/summary.jsp"
 * @struts.action-forward name="failure" path="/jsp/summary.jsp"
 *                        contextRelative="true"
 */
public class SummaryAction extends LookupDispatchAction {
	  static int LIMIT = 1000;
	private static Log log = LogFactory.getLog(SummaryAction.class);
	private static int noDistance = 100;

	protected Map<String, String> getKeyMethodMap() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("summary.button.process", "process");
		map.put("summary.button.processz", "processz");
		map.put("summary.button.processTest", "processTest");
		map.put("summary.button.processzTest", "processzTest");
		map.put("summary.button.upload", "upload");
		map.put("summary.button.uploadCitation", "uploadCitation");
		map.put("summary.button.export", "exportToXml");
		map.put("summary.button.relevance", "findRelevant");

		return map;
	}

	protected Set<String> getFilteredConceptSet() {
		Set<String> filteredSet = new HashSet<String>();
		filteredSet.add("individual");
		filteredSet.add("homo sapiens");
		filteredSet.add("child");
		filteredSet.add("family");
		filteredSet.add("woman");
		filteredSet.add("cohort");
		filteredSet.add("paptients");
		filteredSet.add("person");
		filteredSet.add("control group");
		filteredSet.add("high risk populations");
		filteredSet.add("adult");
		filteredSet.add("ethnic group");
		filteredSet.add("parent");
		filteredSet.add("mus");
		filteredSet.add("mice, nude");
		filteredSet.add("kouse mice");
		filteredSet.add("infraclass eutheria");
		filteredSet.add("rats, wistar");
		filteredSet.add("rattus");
		filteredSet.add("canis, familiaris");
		filteredSet.add("scid mice");
		filteredSet.add("human");
		filteredSet.add("outpatients");
		filteredSet.add("Participant");
		filteredSet.add("Mammals");
		filteredSet.add("Control Groups");
		filteredSet.add("network");
		filteredSet.add("general population");
		filteredSet.add("student");
		filteredSet.add("dependent");
		filteredSet.add("cells");
		return filteredSet;
	}


	@Override
	public ActionForward unspecified(ActionMapping mapping,
            ActionForm form,
            javax.servlet.http.HttpServletRequest request,
            javax.servlet.http.HttpServletResponse response)
		throws PubmedException, EssieException, SemMedException, Exception {
		if ("process".equals(request.getParameter("method")))
				return process(mapping,form,request,response);
		else if ("processz".equals(request.getParameter("method")))
			return processz(mapping,form,request,response);
		else if ("processTest".equals(request.getParameter("method")))
				return processTest(mapping,form,request,response);
		else
			return super.unspecified(mapping, form, request, response);
	}

	/**
	 * Method process
	 *
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 */
	@SuppressWarnings("unchecked")
	public ActionForward process(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws PubmedException, EssieException, SemMedException, Exception {

		long startAll = System.currentTimeMillis();
		SummaryForm SummaryForm = (SummaryForm) form;
		HttpSession session = request.getSession();

		String summaryType = SummaryForm.getSummaryType();
		// log.debug("Summary type:" + summaryType);

		session.removeAttribute("lang");
		session.removeAttribute("translateSelectedSeed");

		int page;

		if (request.getParameter("p") == null) {

			ArrayList<Filter> filters = new ArrayList<Filter>();

			filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.PRELIMINARY));

			if ("nhlbi".equals(summaryType)) {
				filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.NHLBI_RELEVANCE));
				filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.CONNECTIVITY));
				filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.NHLBI_NOVELTY));
			} else if ("diagnosis".equals(summaryType)) {
				filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.DIAGNOSIS_RELEVANCE));
				filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.CONNECTIVITY));
				filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.DIAGNOSIS_NOVELTY));
			} else if ("interaction".equals(summaryType)) {
				filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.INTERACTION_RELEVANCE));
				filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.INTERACTION_CONNECTIVITY));
				filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.INTERACTION_NOVELTY));
			} else if ("treatment".equals(summaryType)) {
				filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.TREATMENT_RELEVANCE));
				filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.CONNECTIVITY));
				filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.TREATMENT_NOVELTY));
			} else if ("pharmacogenomics".equals(summaryType)) {
				filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.PHARMACOGENOMICS_RELEVANCE));
				filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.CONNECTIVITY));
				filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.PHARMACOGENOMICS_NOVELTY));
			}

			/* if (request.getParameter("saliency") != null
					&& request.getParameter("saliency").equals("on")) {
				if ("predication".equals(SummaryForm.getSelectedSaliencyType()))
					filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.PREDICATION_SALIENCY));
				else if ("relation".equals(SummaryForm.getSelectedSaliencyType()))
					filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.RELATION_SALIENCY));
				else if ("concept".equals(SummaryForm.getSelectedSaliencyType()))
					filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.CONCEPT_SALIENCY));

				session.setAttribute("saliency", Boolean.TRUE);
				session.setAttribute("selectedSaliencyType", SummaryForm.getSelectedSaliencyType());

			} */
			// 07/11/2008, Dongwook Shin
			// In SemMed OPASI version, change "saliency" into "More Relation" and remove the SaliencyTyp, so that the meaning of check becomes opposite
			if(request.isUserInRole("SemMedUser")) {
				if (request.getParameter("saliency") != null
					&& request.getParameter("saliency").equals("on")) {
				/* if ("predication".equals(SummaryForm.getSelectedSaliencyType()))
					filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.PREDICATION_SALIENCY));
				else if ("relation".equals(SummaryForm.getSelectedSaliencyType()))
					filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.RELATION_SALIENCY));
				else if ("concept".equals(SummaryForm.getSelectedSaliencyType()))
					filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.CONCEPT_SALIENCY)); */

				session.setAttribute("saliency", Boolean.FALSE);
				// session.setAttribute("selectedSaliencyType", SummaryForm.getSelectedSaliencyType());

				} else {
					filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.PREDICATION_SALIENCY));
					session.setAttribute("saliency", Boolean.TRUE);
					session.setAttribute("selectedSaliencyType", SummaryForm.getSelectedSaliencyType());
				}
			} else if(request.isUserInRole("SemMedTester")) { // If OPASI tester role, keep the original setting
				if (request.getParameter("saliency") != null
						&& request.getParameter("saliency").equals("on")) {
					if ("predication".equals(SummaryForm.getSelectedSaliencyType()))
						filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.PREDICATION_SALIENCY));
					else if ("relation".equals(SummaryForm.getSelectedSaliencyType()))
						filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.RELATION_SALIENCY));
					else if ("concept".equals(SummaryForm.getSelectedSaliencyType()))
						filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.CONCEPT_SALIENCY));

					session.setAttribute("saliency", Boolean.TRUE);
					session.setAttribute("selectedSaliencyType", SummaryForm.getSelectedSaliencyType());

				} else {
						// filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.PREDICATION_SALIENCY));
						session.setAttribute("saliency", Boolean.FALSE);
						// session.setAttribute("selectedSaliencyType", SummaryForm.getSelectedSaliencyType());
				}
			}

			session.setAttribute("selectedSeed", request.getParameter("seed"));
			// log.debug("seed Concept:" + request.getParameter("seed"));

			List<APredication> summarizedPredications = Summarizer.summarize(
					filters.toArray(new Filter[filters.size()]),
					(APredicationList) session.getAttribute("predications"),
					new String[] { Constants.CAUSES, Constants.DIAGNOSES,
							Constants.ISA }, SummaryForm.getSeed());

			page = 0;
			session.setAttribute("summaryPredications", summarizedPredications);
			session.setAttribute("summaryType", summaryType);
			session.removeAttribute("predsource");
			session.setAttribute("predsource", 1);
			session.removeAttribute("key");
		}else{
			try{
				page = Integer.parseInt(request.getParameter("p"));
			}catch(Exception e){
				page = 0;
			}
		}


		List<APredication> predications = (List<APredication>)session.getAttribute("summaryPredications");

		int[] PIDs = new int[Math.min(20, predications.size()-page*20)];
		int[] SIDs = new int[PIDs.length];
		ArticleDataSource.SourceType[] sources = new ArticleDataSource.SourceType[PIDs.length];

		for(int i=0;i<PIDs.length;i++){
			PIDs[i] = predications.get(i+page*20).PID;
			SIDs[i] = predications.get(i+page*20).SID;
			sources[i] = predications.get(i+page*20).source;
		}

		try{
			session.setAttribute("displayedSummaryPredications",new APredicationList(PIDs,SIDs,sources));
		}catch(Exception e){
			e.printStackTrace();
			throw new PubmedException(e);
		}
		session.setAttribute("pageNumberSummary", page);
	    session.removeAttribute("cluster");


		session.setAttribute("executionTime", System.currentTimeMillis()
				- startAll);
		return mapping.findForward("success");
	}

	public ActionForward processz(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
    throws PubmedException, EssieException, SemMedException, Exception
  {
    HttpSession session = request.getSession();
    ArrayList filters = new ArrayList();
    filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.TREATMENT_NOVELTY));
    String[] nullList = new String("").split("|");
    // APredicationList aPredicationList = (APredicationList)session.getAttribute("predications");
    /* List predications = gov.nih.nlm.semmed.zsummarization.Summarizer.summarize(
      (gov.nih.nlm.semmed.zsummarization.Filter[])filters.toArray(new gov.nih.nlm.semmed.zsummarization.Filter[filters.size()]), aPredicationList, nullList);
      */
    List<APredication> listIn = (List<APredication>) session.getAttribute("predications");
    List<APredication> predications = new ArrayList<APredication>();
	  for(APredication p : listIn)
		if ( p.novelSubject && p.novelObject && p.predicate.compareTo("PROCESS_OF") != 0 && p.predicate.compareTo("higher_than") != 0  && p.predicate.compareTo("lower_than")  != 0)
				predications.add(p);

	/* List<APredication> predications = Summarizer.summarize(
			(Filter[]) filters.toArray(new Filter[filters.size()]),
			(APredicationList) session.getAttribute("predications"),
			new String[] {Constants.PROCESS_OF}, null); */
	log.debug("# of noble predications = " + predications.size());
    Hashtable predTableToken = new Hashtable();
    Hashtable predTablePerPMID = new Hashtable();
    Hashtable predTableTypi = new Hashtable();
    Hashtable conceptTable = new Hashtable();
    Hashtable conceptRevTable = new Hashtable();
    Hashtable predtable = new Hashtable();
    int PMID = 0;
    int prevPMID = 0;
    int concId = 0;
    Hashtable ConnectivityTable = new Hashtable();
    Hashtable findPredInfoTable = new Hashtable();
    Set filteredConceptSet = getFilteredConceptSet(); // get predefined filtered concept set
    log.debug("-------- Beginning of subject Object pair added ------------");
    for (int i = 0; i < predications.size(); i++) {
      APredication apred = (APredication)predications.get(i);
      String SubjConcept = (String)apred.subject.get(0);
      String ObjConcept = (String)apred.object.get(0);
      String SubjST = (String)apred.subjectSemtype.get(0);
      String ObjST = (String)apred.objectSemtype.get(0);
      String predicate = apred.predicate;
      // Ignore the following semantic types
      if(SubjST.equals("mamm") || SubjST.equals("humn") || ObjST.equals("mamm") || ObjST.equals("humn") || predicate.equals("PROCESS_OF") || filteredConceptSet.contains(SubjConcept.toLowerCase()) || filteredConceptSet.contains(ObjConcept.toLowerCase()))
    	  continue;

      APredicationWOR ap = new APredicationWOR(SubjConcept, SubjST, ObjConcept, ObjST);
      HashSet predSet = null;
      if ((predSet = (HashSet)predtable.get(ap)) != null) {
        predSet.add(apred);
      } else {
        predSet = new HashSet();
        predSet.add(apred);
        predtable.put(ap, predSet);
      }

      String subjWithST = new String(SubjConcept + "|" + SubjST);
      String objWithST = new String(ObjConcept + "|" + ObjST);
      String subjObjPair = new String(subjWithST + "-" + objWithST);
      if (!conceptTable.containsKey(subjWithST)) {
        conceptTable.put(subjWithST, Integer.valueOf(concId));
        conceptRevTable.put(Integer.valueOf(concId), subjWithST);
        concId++;
      }
      if (!conceptTable.containsKey(objWithST)) {
        conceptTable.put(objWithST, Integer.valueOf(concId));
        conceptRevTable.put(Integer.valueOf(concId), objWithST);
        concId++;
      }

      Hashtable predList = (Hashtable) findPredInfoTable.get(subjObjPair);
      if (predList == null) {
    	  // ConcDegree cd = new ConcDegree(predicate, 1); // Make a pair with predicate name and its frequence
    	  predList = new Hashtable();
    	  predList.put(predicate, 1); // Add the first predicate connecting subject and object
    	  findPredInfoTable.put(subjObjPair,predList);
    	  // log.debug(subjObjPair);
      } else {
    	  Integer predFreq = (Integer) predList.remove(predicate);
    	  if(predFreq == null) { // subject and object are found, but the predicate name is not found
    		  predList.put(predicate, 1); // Add the first predicate connecting subject and object
    	  } else { // same predicate
    		  Integer newPredFreq = predFreq + 1;
    		  predList.put(predicate, newPredFreq);
    	  }
      }

      prevPMID = PMID;
      PMID = ((APredication)predications.get(i)).PMID;
      if ((PMID != prevPMID) && (prevPMID != 0)) {
        Enumeration predEnum = predTablePerPMID.keys();
        while (predEnum.hasMoreElements()) {
          APredicationLite ap2 = (APredicationLite)predEnum.nextElement();
          Integer freqPerPMID = (Integer)predTablePerPMID.get(ap2);
          Integer freq1 = (Integer)predTableToken.get(ap2);
          if (freq1 != null) {
            freq1 = Integer.valueOf(freq1.intValue() + freqPerPMID.intValue());
            predTableToken.remove(ap2);
            predTableToken.put(ap2, new Integer(freq1.intValue()));
          }
          else {
            predTableToken.put(ap2, new Integer(freqPerPMID.intValue()));
          }

          Integer freq2 = (Integer)predTableTypi.get(ap2);
          if (freq2 != null) {
            freq2 = Integer.valueOf(freq2.intValue() + 1);
            predTableTypi.remove(ap2);
            predTableTypi.put(ap2, new Integer(freq2.intValue()));
          } else {
            predTableTypi.put(ap2, new Integer(1));
          }
        }
        predTablePerPMID = null;
        predTablePerPMID = new Hashtable();
      }
      APredicationLite curPred = new APredicationLite(SubjConcept, SubjST, predicate, ObjConcept, ObjST);
      Integer freq1 = (Integer)predTablePerPMID.get(curPred);
      if (freq1 != null) {
        freq1 = Integer.valueOf(freq1.intValue() + 1);
        predTablePerPMID.remove(curPred);
        predTablePerPMID.put(curPred, new Integer(freq1.intValue()));
      } else {
        predTablePerPMID.put(curPred, new Integer(1));
      }
    }
    // log.debug("----- End of Subject Object pair added ---------");

    double cutOff = findCutOff(predTableTypi);
    // double cutOff = 2.6;
    // log.debug("Cut off value = " + cutOff);
    Enumeration predEnum = predTableTypi.keys();
    HashSet survivedSet = new HashSet();
    int numOfSurvived = 0;
    int biggestFreq = 0;
    APredicationLite biggestPred = null;
    // Hashtable ConnectivityTable = new Hashtable();
    Hashtable wConnectivityTable = new Hashtable();
    while (predEnum.hasMoreElements()) {
      APredicationLite ap = (APredicationLite)predEnum.nextElement();
      Integer freq = (Integer)predTableTypi.get(ap);

      if (freq.intValue() >= cutOff) {
          // log.debug("survived from frequency cutoff : " + ap.toString() + ", freq = " + freq);
    	  survivedSet.add(ap);
        String SubjConcept = ap.getSubj();
        String SubjST = ap.getSubjST();
        String ObjConcept = ap.getObj();
        String ObjST = ap.getObjST();
        // Finding the adjancect list of a node
        HashSet adjacentSet1 = null;
        ArrayList connectedConceptList = null;
  		String css = new String(SubjConcept + "|" + SubjST);
  		String cso = new String(ObjConcept + "|" + ObjST);

  		if(SubjConcept != null && SubjConcept.compareTo("") != 0 && (adjacentSet1 = (HashSet) ConnectivityTable.get(css)) != null) {
  			// System.out.println(css.toString() + "is already found");
  			connectedConceptList = (ArrayList) wConnectivityTable.get(css);
  			connectedConceptList.add(cso);
  			if(!adjacentSet1.contains(cso)) {
  				adjacentSet1.add(cso);
  				// System.out.println(cso.toString() + "is already found");
  			}
  		} else if (SubjConcept != null && SubjConcept.compareTo("") != 0) {
  			adjacentSet1 = new HashSet();
  			adjacentSet1.add(cso);
  			ConnectivityTable.put(css,adjacentSet1);
  			connectedConceptList = new ArrayList();
  			connectedConceptList.add(cso);
  			wConnectivityTable.put(css,connectedConceptList); // need to calculated the weighted connectivity

  		}

  		HashSet adjacentSet2 = null;
  		if(ObjConcept != null && ObjConcept.compareTo("") != 0 && (adjacentSet2 = (HashSet) ConnectivityTable.get(cso)) != null) {
  			 // System.out.println(cso.toString() + "is already found");
  			connectedConceptList = (ArrayList) wConnectivityTable.get(cso);
  			connectedConceptList.add(css);
  			if(!adjacentSet2.contains(css)) {
  				adjacentSet2.add(css);
  				// System.out.println(css.toString() + "is already found");
  			}
  		} else if (ObjConcept != null && ObjConcept.compareTo("") != 0) {
  			adjacentSet2 = new HashSet();
  			adjacentSet2.add(css);
  			ConnectivityTable.put(cso,adjacentSet2);
  			connectedConceptList = new ArrayList();
  			connectedConceptList.add(css);
  			wConnectivityTable.put(cso,connectedConceptList); // need to calculated the weighted connectivity
  		}

        // log.debug("survived -- " + ap.toString() + ", freq = " + freq);
        numOfSurvived++;
        if(freq > biggestFreq) {
        	biggestFreq = freq;
        	biggestPred = ap;
        }
      }
    }
    // log.debug("---- biggest pred = " + biggestPred.toString() + " freq = " + biggestFreq);
    String centerConcept = biggestPred.getObj();
    // log.debug("---- Num of survived Predication = " + numOfSurvived);

    // log.debug("---- Num of filtered Predication = " + numOfFiltered);
    // Filter the concept connectivity less than the mean _ deviation
    double connectCutOff = findCutOffConnectivity(ConnectivityTable);
    // log.debug("connectivity cutoff : " + connectCutOff);
    HashSet survivedSet2 = new HashSet();
    Enumeration cenum = ConnectivityTable.keys();
	List concDegreeList = new ArrayList();
    while (cenum.hasMoreElements()) {
        String concept = (String)cenum.nextElement();
        HashSet aSet = (HashSet)ConnectivityTable.get(concept);
        List aList = (List)wConnectivityTable.get(concept);
        // log.debug("survived concepts : " + concept + ", connectivity : " + aSet.size());
        // int aSize = aSet.size();
        int aSize = aList.size();
        if(aSize >= connectCutOff) {
        	survivedSet2.add(concept);
			// System.out.println(concST.concept	+ " | " + concST.ST +  "\t | \t" + adjSet.size());
			ConcDegree cd = new ConcDegree(concept, new Integer(aSize));
			concDegreeList.add(cd);
            log.debug("\t------ finally survived concepts : " + concept + ", connectivity : " + aSet.size());
            // log.debug("\t\t\t weighted connectivity : " + aList.size());
        }
      }
	Collections.sort(concDegreeList, new ConcComp()); // Sort the concepts in the order of the number of connectivity

    int numOfConcepts = concId;
    boolean[][] relationTable = new boolean[numOfConcepts][numOfConcepts];
    for (int i = 0; i < numOfConcepts; i++) {
      for (int j = 0; j < numOfConcepts; j++) {
        relationTable[i][j] = false;
      }
    }

    // log.debug("Creating edge relations for # of concepts = " + numOfConcepts);
    // Enumeration apList = predtable.keys();
    Iterator apIt = survivedSet.iterator();
    // log.debug("# of elements in predtable = " + predtable.size());
    while (apIt.hasNext()) {
      APredicationLite apred = (APredicationLite)apIt.next();
      String subjWithST = new String(apred.getSubj() + "|" + apred.getSubjST());
      Integer subjIndex = (Integer)conceptTable.get(subjWithST);
      String objWithST = new String(apred.getObj() + "|" + apred.getObjST());
      Integer objIndex = (Integer)conceptTable.get(objWithST);
      if (subjIndex != objIndex && survivedSet2.contains(subjWithST) && survivedSet2.contains(objWithST)) {
        relationTable[subjIndex.intValue()][objIndex.intValue()] = true;
        relationTable[objIndex.intValue()][subjIndex.intValue()] = true;
        // log.debug("rel[" + subjIndex.intValue() + "][" + objIndex.intValue() + "] = true " );
        // log.debug("rel[" + subjIndex.intValue() + "," + subjWithST + "][" + objIndex.intValue() + ", " + objWithST + "] = true " );
      }
    }
    boolean[] nodesListForClique = new boolean[numOfConcepts];
    for (int i = 0; i < numOfConcepts; i++) {
      nodesListForClique[i] = false;
    }

    Vector cliqueVec = new Vector();
    HashSet alreadyFoundClique = new HashSet();

	for(int firstI = 0; firstI< numOfConcepts; firstI++) {
		for(int secondI = 0; secondI < numOfConcepts; secondI++) {
			if(relationTable[firstI][secondI] == true) { // if firstI <-> secondI connected
				for(int thirdI = 0; thirdI< numOfConcepts; thirdI++) {
					if(relationTable[firstI][thirdI] == true && relationTable[secondI][thirdI] ==true) {
						if(firstI != secondI  && secondI != thirdI && thirdI != firstI) { // If the three are unique concepts
							String curClique = constructValidClique(firstI, secondI, thirdI);
							if(!cliqueVec.contains(curClique)) { // if the click is not added already
								// String firstConc = (String) conceptRevTable.get(firstI);
								// String secondConc = (String) conceptRevTable.get(secondI);
								// String thirdConc = (String) conceptRevTable.get(thirdI);
								// log.debug("***** new clique : " + firstConc + " - " + secondConc + " - " + thirdConc);
								cliqueVec.add(curClique);
								// out3.println(curClique);
							}
						}

					}
				}
			}
		}
    }

    int numOfThreeNodeClique = cliqueVec.size();
    // log.debug("The number of clique with three nodes found = " + numOfThreeNodeClique);
    Vector finalCliqueVec = new Vector();
    Document graphXml = null;

    if(numOfThreeNodeClique > 0) {	// If there is more than 0 three node click
    while (cliqueVec.size() > 0) {
      String exClique = (String)cliqueVec.remove(0);
      String[] compo = exClique.split("\\|");
      boolean foundNextLevelClique = false;
      for (int j = 0; j < numOfConcepts; j++) {
        boolean[] connected = new boolean[compo.length];
        for (int k = 0; k < compo.length; k++) {
          connected[k] = false;
        }
        for (int k = 0; k < compo.length; k++) {
          int nodeInt = Integer.parseInt(compo[k]);
          if ((k == 0) && (j != nodeInt) && (relationTable[j][nodeInt] == true))
            connected[k] = true;
          else if ((k > 0) && (j != Integer.parseInt(compo[k])) && (relationTable[j][nodeInt] == true) && (connected[(k - 1)] == true)) {
            connected[k] = true;
          }
        }
        if (connected[(compo.length - 1)] == true) {
          String newClique = constructValidClique(compo, j);

          cliqueVec.add(newClique);
          foundNextLevelClique = true;
          break;
        }
      }
      if ((foundNextLevelClique) || (finalCliqueVec.contains(exClique)))
        continue;
      finalCliqueVec.add(exClique);
    }

    log.debug("*************** Final Clicques ******************" + finalCliqueVec.size());
    ArrayList clusterResult = new ArrayList(200);
    ArrayList firstClusterList = new ArrayList(finalCliqueVec.size() - 1);
    int numOfCurClusters = 0;
    for (int j = finalCliqueVec.size() - 1; j >= 0; j--) {
      String exClique = (String)finalCliqueVec.elementAt(j);
      // log.debug(exClique);
      String[] compo = exClique.split("\\|");
      StringBuffer curClickBuf = new StringBuffer();
      StringBuffer curClickBuf1 = new StringBuffer();
      if (compo.length >= 3) {
        numOfCurClusters++;
        int[] curClick = new int[compo.length];
        for (int index = 0; index < compo.length; index++) {
          curClick[index] = Integer.parseInt(compo[index].trim());
          String curCon = (String) conceptRevTable.get(Integer.parseInt(compo[index].trim()));
          String[] compon = curCon.split("\\|");
          curClickBuf.append(curCon + " | ");
          curClickBuf1.append(compon[0] + " | ");
        }
        log.debug(curClickBuf1.toString());
        firstClusterList.add(curClick);
      }
    }
    // log.debug("*************** Clicques ******************");
    clusterResult.add(firstClusterList);
    int clusterListIndex = 0;
    int prevClusterSize = 0;
    int curClusterSize = 0;
    while (true) {
      ArrayList currentClusterList = (ArrayList)clusterResult.get(clusterListIndex);

      // log.debug("Previous # of clusters = " + prevClusterSize);
      curClusterSize = currentClusterList.size();
      // log.debug("Current # of clusters = " + curClusterSize);
      if (curClusterSize == prevClusterSize || curClusterSize <= 1) {
          // log.debug("Final number after syntic clustering = " + curClusterSize);
    	  break;
      }
      prevClusterSize = curClusterSize;
      int lowestDistance = noDistance;
      int[][] distance = new int[numOfCurClusters][numOfCurClusters];
      for (int clicki = 0; clicki < curClusterSize; clicki++) {
        for (int clickj = clicki + 1; clickj < curClusterSize; clickj++) {
          if (clicki != clickj) {
            // log.debug("clicki = " + clicki + ", clickj = " + clickj);
        	  int[] click1 = (int[])currentClusterList.get(clicki);
            int[] click2 = (int[])currentClusterList.get(clickj);
            int size1 = click1.length;
            int size2 = click2.length;
            int match = 0;
            int movei = 0;
            int movej = 0;
            while ((movei < size1) && (movej < size2)) {
              if (click1[movei] == click2[movej]) {
                match++;
                movei++;
                movej++;
              } else if (click1[movei] > click2[movej]) {
                movej++;
              } else if (click1[movei] < click2[movej]) {
                movei++;
              }
            }
            if(match > 0) {
            	distance[clicki][clickj] = (click1.length + click2.length)/match;
            } else
            	distance[clicki][clickj] = noDistance;
           if (distance[clicki][clickj] < lowestDistance)
             lowestDistance = distance[clicki][clickj];
           // log.debug("lowest distance = " + lowestDistance);
          }
        }
      }
      int currentSize = currentClusterList.size();
      boolean[] used = new boolean[currentSize];
      for (int clicki = 0; clicki < currentSize; clicki++) {
        used[clicki] = false;
      }
      // log.debug("Start merging ther clusters with lowest distance = " + lowestDistance);
      ArrayList nextClusterList = new ArrayList();
      for (int clicki = 0; clicki < currentSize; clicki++) {
        for (int clickj = clicki + 1; clickj < currentSize; clickj++) {
          if (distance[clicki][clickj] <= lowestDistance && used[clicki] == false && used[clickj] == false) {
            int[] click1 = (int[]) currentClusterList.get(clicki);
            int[] click2 = (int[]) currentClusterList.get(clickj);
            // log.debug("--- old cluster for " + clicki + "(size = " + click1.length +  ") and " + clickj + "(size = " + click2.length + ")");
            int[] newCluster = mergeCluster(click1, click2);
            // log.debug("--- New cluster size = " + newCluster.length);
            nextClusterList.add(newCluster);
            used[clicki] = true;
            used[clickj] = true;
            // lowestDistance = distance[clicki][clickj];
          }
        }
      }
      for (int i = 0; i < currentSize; i++) {
        if (used[i] == false)
          nextClusterList.add(currentClusterList.get(i));
      }
      clusterResult.add(nextClusterList);
      clusterListIndex++;
    }

    int semanticClusterIndex = 0; // Start semantic clustering in half of the position
    // ArrayList  nextSemanticClusterList = null;
    ArrayList semanticClusterResult = new ArrayList();
	log.debug("*** The number of steps of syntactic clustering = " + clusterListIndex);
	ArrayList  curSemanticClusterList = (ArrayList) clusterResult.get(clusterListIndex/2); // Semantic clustering starts at the middle steps of syntact clustering

    log.debug("*************** The start of semantic Cluster info ******************");
    int sizeCluster  = curSemanticClusterList.size();
    for (int ii = 0; ii < sizeCluster; ii++) {
    	// log.debug("****************** Cluster " + ii + " ********************");
      int[] cluster = (int[]) curSemanticClusterList.get(ii);
      // log.debug(exClique);
      StringBuffer curClickBuf = new StringBuffer();
      for (int j = 0; j < cluster.length; j++) {
          int intPair1 = cluster[j];
          String curPair = (String)conceptRevTable.get(intPair1);
          	String[] comp1 = curPair.split("\\|");
          	String subj = comp1[0].trim();
          	// String subjST = comp1[1].trim();
          	// log.debug(subj);
      }
    }
    // log.debug("*************** The end of semantic Cluster info  ******************");

	semanticClusterResult.add(curSemanticClusterList);
	boolean semanticClusterChanged = true;
	Hashtable semanticMeaningTable = null;
	Vector semanticMeaningVector = null;
	while(semanticClusterChanged == true) {
    	 // ArrayList  thisClusterList = (ArrayList) clusterResult.get(semanticClusterIndex);
	     ArrayList  nextSemanticClusterList = new ArrayList();
    	 int thisSize = curSemanticClusterList.size();
    	 // log.debug("*** The size of first semantic cluster = " + thisSize);
    	 semanticMeaningTable = new Hashtable();
    	 semanticMeaningVector = new Vector();
	     boolean[] used = new boolean[thisSize];
	     for (int clicki = 0; clicki < thisSize; clicki++) {
	        used[clicki] = false;
	     }

    	 for(int i =0; i < thisSize; i++) {
 			String mostFreqSemanticTriple = null; // this will define the semantics of the cluster
    		 int[] curCluster = (int[])curSemanticClusterList.get(i);
    	      HashSet curClusterSet = new HashSet();
    	      int csize = curCluster.length;
    	      // log.debug("--- Current cluster list " + i + "---");
    	      Hashtable clusterSemanticTable = new Hashtable();
    	      for (int j = 0; j < csize; j++) {
    	        int intPair1 = curCluster[j];
    	        String curPair = (String)conceptRevTable.get(intPair1);
    	        for (int k = 0; k < csize; k++) {
    	        	if(j != k) {
    	        		int intPair2 = curCluster[k];
    	        		String nextPair = (String)conceptRevTable.get(intPair2);
    	        		String[] comp1 = curPair.split("\\|");
    	        		String[] comp2 = nextPair.split("\\|");
    	        		String subj = comp1[0].trim();
    	        		String subjST = comp1[1].trim();
    	        		Integer subjIndex = (Integer)conceptTable.get(subj + "|" + subjST);
    	        		String obj = comp2[0].trim();
    	        		String objST = comp2[1].trim();
    	        		Integer objIndex = (Integer)conceptTable.get(obj + "|" + objST);
    	        		if(relationTable[subjIndex][objIndex]) {
    	        			String subjSG = findSemanticGroup(subjST);
    	        			String objSG = findSemanticGroup(objST);
    	        			String subjObj = new String (subj + "|" + subjST + "-" + obj + "|" + objST);
    	        			// log.debug("Subject and Object pair = " + subjObj);
    	        			Hashtable hTable = (Hashtable) findPredInfoTable.get(subjObj);
    	        			boolean pairReversed = false;
    	        			if(hTable == null) {
    	        				subjObj = new String (obj + "|" + objST + "-" + subj + "|" + subjST);
    	        				// log.debug("****************** reverse of subj and obj tried: " + subjObj);
    	        				hTable = (Hashtable) findPredInfoTable.get(subjObj);
    	        				pairReversed = true;
    	        			}
    	        			Enumeration keys = hTable.keys();
    	        			Integer mostFreqNum = 0;
    	        			Integer thisNum = 0;
    	        			String mostFreqPred = null;
    	        			while(keys.hasMoreElements()) {
    	        				String nextKey = (String) keys.nextElement();
    	        				thisNum = (Integer) hTable.get(nextKey);
    	        				if(!nextKey.equals("ISA") && thisNum > mostFreqNum) {
    	        					mostFreqPred = nextKey; // replace the most frequentpredicate
    	        					mostFreqNum = thisNum; // replace the number
    	        				}
    	        			} // find most frequent predicate having the same Subject and Object
    	        			if(mostFreqPred != null) {
    	        				String predSG = (String) SemanticGroups.getPredicateType(mostFreqPred);
    	        				String semanticTriple = null;
    	        				String realPredicate = null;
    	        				if(pairReversed == false) {
    	        					semanticTriple = new String(subjSG + "-" + predSG + "-" + objSG);
    	        					realPredicate = new String(subj + "|" + subjST + "-" + mostFreqPred + "-" + obj + "|" + objST);
    	        					// log.debug("semantic triple : " + realPredicate);
    	        					// log.debug("\t real predicate : " + realPredicate);
    	        				}
    	        				else {
    	        					semanticTriple = new String(objSG + "-" + predSG + "-" + subjSG);
    	        					realPredicate = new String(obj + "|" + objST + "-" + mostFreqPred + "-" + subj + "|" + subjST);
    	        					// log.debug("*** reversed semantic triple : " + semanticTriple);
    	        					// log.debug("\t real predicate : " + realPredicate);
    	        				}
    	        				Integer semanticFreq = (Integer) clusterSemanticTable.remove(semanticTriple);
    	        				if(semanticFreq == null) { // if semantic triple occurs first
    	        					clusterSemanticTable.put(semanticTriple, mostFreqNum);
    	        					log.debug("semantic triple : " + realPredicate + ", frequency : " + mostFreqNum);
    	        				} else {
    	        					Integer accumSemanticFreq = semanticFreq + mostFreqNum;
    	        					clusterSemanticTable.put(semanticTriple, accumSemanticFreq); // add up the semantic frequence
    	        					log.debug("semantic triple : " + realPredicate + ", frequency : " + mostFreqNum);
    	        				}
    	        			}
    	        		} // if
    	        	} // for
    	        } // for
    			Enumeration keys = clusterSemanticTable.keys();
    			Integer mostFreqNum = 0;
    			Integer thisNum = 0;

    			while(keys.hasMoreElements()) {
    				String nextKey = (String) keys.nextElement();
    				thisNum = (Integer) clusterSemanticTable.get(nextKey);
    				if(thisNum > mostFreqNum) {
    					mostFreqSemanticTriple = nextKey; // replace the most frequent predicate
    					mostFreqNum = thisNum; // replace the number
    				}
    			} // find most frequent semantic triple in the cluster
    	      } // for


    	      // log.debug("****   " + i + "th semantic triple = " + mostFreqSemanticTriple);
    	      HashSet sameMeaningClusters = (HashSet) semanticMeaningTable.get(mostFreqSemanticTriple);
    	      if(sameMeaningClusters == null) { // There is no cluster having the same semantic meaning yet
    	    	  sameMeaningClusters = new HashSet();
    	    	  sameMeaningClusters.add(i); // Add cluster i in a new set
    	    	  semanticMeaningTable.put(mostFreqSemanticTriple, sameMeaningClusters); // add the singleton set to the hashtable
    	    	  semanticMeaningVector.add(mostFreqSemanticTriple);
    	      } else { // Found other cluster having the same meaning
    	    	  Iterator hi = sameMeaningClusters.iterator();
    	    	  String iList = new String();
    	    	  while(hi.hasNext()) {
    	    		  Integer curI = (Integer) hi.next();
    	    		  iList = iList + "," + curI;
    	    	  }
    	    	  sameMeaningClusters.add(i);
    	    	  // used[i] = true;
    	    	  // log.debug(mostFreqSemanticTriple + "---" +   iList  + " and " + i + "-th semantic cluster grouped");
    	    	  // log.debug(mostFreqSemanticTriple + "---" + i + "-th semantic cluster added");
    	    	  //log.debug(i + "-th semantic cluster added");
    	      }
    	 } // End of each step of calculation of semantic clustering

    	 Enumeration semanticValues = semanticMeaningTable.elements();
		 boolean thisClusterChanged = false;
    	 while(semanticValues.hasMoreElements()) {
    		 HashSet sameMeaningSet = (HashSet) semanticValues.nextElement();
    		 if(sameMeaningSet.size() > 1) { // If there is more than one clusters having the same semantic meaning, merge it semantically
    			 Iterator hi = sameMeaningSet.iterator();
    			 Integer curI = -1;
    			 Integer prevI = -1;
    			 int[] prevCluster = null;
    			 int[] newCluster = null;
    			 int[] curCluster = null;
    			 while(hi.hasNext()) {
    				 prevI = curI;
    				 curI = (Integer)hi.next();
    				 used[curI] = true;
    				 // log.debug("cluster " + curI + " is ");
					 curCluster = (int[])curSemanticClusterList.get(curI);
    				 if( prevCluster != null) {
    					 newCluster = mergeCluster(prevCluster, curCluster);
    					 prevCluster = newCluster;
    					 // log.debug("cluster " + prevI + " and " + curI + " is merged");
    					 // nextSemanticClusterList.add(newCluster);
    				 } else
    					 prevCluster = curCluster;
    			 }
				 nextSemanticClusterList.add(newCluster);
				 thisClusterChanged = true;
    		 }
    	 }
    	 // Copy the untouched clusters to the next semenatic clusters set
         for (int j = 0; j < thisSize; j++) {
             if (used[j] == false) {
            	 nextSemanticClusterList.add(curSemanticClusterList.get(j));
            	 // log.debug("cluster " + j+ " is not changed and added as is");
             }
         }

    	 semanticClusterChanged = thisClusterChanged;
    	 if(semanticClusterChanged) { // If the clusters are changed with semantic clustering
    		 // log.debug("Semantic cluster changed and the current one is added!");
    		 semanticClusterResult.add(nextSemanticClusterList);
    		 semanticClusterIndex++;
    		 curSemanticClusterList = null;
    		 curSemanticClusterList = nextSemanticClusterList;
    		 // log.debug("Semantic Cluster Index = " + semanticClusterIndex);
    	 }
    }
    // End of semantic clustering

    // ArrayList  finalClusterList = (ArrayList) clusterResult.get(clusterListIndex);
    ArrayList  finalClusterList = (ArrayList) semanticClusterResult.get(semanticClusterIndex);
    ArrayList finalClusterList2 = new ArrayList();
    int clsize = finalClusterList.size();
    log.debug("Final # of clusters = " + clsize);
    // log.debug(" ********************************************************** ");
    // log.debug(" ***********Finally survived predications ***************** ");
    for (int i = 0; i < clsize; i++) {
      int[] curCluster = (int[])finalClusterList.get(i);
      HashSet curClusterSet = new HashSet();
      int csize = curCluster.length;
      // log.debug("--- Current cluster list " + i + "---");
      for (int j = 0; j < csize; j++) {
        int intPair1 = curCluster[j];
        String curPair = (String)conceptRevTable.get(intPair1);
        for (int k = 0; k < csize; k++) {
        	if(j != k) {
        	int intPair2 = curCluster[k];
        	String nextPair = (String)conceptRevTable.get(intPair2);
        	String[] comp1 = curPair.split("\\|");
        	String[] comp2 = nextPair.split("\\|");
        	String subj = comp1[0].trim();
        	String subjST = comp1[1].trim();
        	Integer subjIndex = (Integer)conceptTable.get(subj + "|" + subjST);
        	String obj = comp2[0].trim();
        	String objST = comp2[1].trim();
        	Integer objIndex = (Integer)conceptTable.get(obj + "|" + objST);
        	if(relationTable[subjIndex][objIndex]) {
        		APredicationWOR ap1 = new APredicationWOR(subj, subjST, obj, objST);
        		// log.debug(ap1.toString());
        		// // for (int l = 0; l < filteredPredications.size(); l++) {
        		HashSet curSet = (HashSet)predtable.get(ap1);
        		if(curSet != null)
        			curClusterSet.addAll(curSet);
            } else {
            	// log.debug("***Eliminate the predicates : " + subjST + " - " + objST);
            }

        	}
        }
      }
      finalClusterList2.add(curClusterSet);
    }
    // log.debug(" ***********End of Finally survived predications ***************** ");
    ArrayList finalClusterList3 = checkSubsume(finalClusterList2);


    // String queryString = "select distinct sp from SentencePredication as sp inner join fetch sp.sentence s inner join fetch sp.predication p inner join fetch p.predicationArgumentSet pa where sp.sentencePredicationId in (:ids)";

	String queryString = "select distinct sp from SentencePredication as sp "
		+ "inner join fetch sp.sentence s "
		+ "inner join fetch sp.predication p "
		+ "inner join fetch p.predicationArgumentSet pa "
		+ "where sp.sentencePredicationId in (:ids)";
    session.setAttribute("zsummaryPredications", finalClusterList3);

    Session hb_session = HibernateSessionFactory.currentSession();
    hb_session.clear();
    Query q = hb_session.createQuery(queryString);
    q.setCacheable(true);
    ArrayList clusteredSPS = new ArrayList();

    for (int i = 0; i < finalClusterList3.size(); i++) {
      HashSet curClusterSet = (HashSet)finalClusterList3.get(i);
      ArrayList curClusterList = new ArrayList(curClusterSet);
      // APredicationList spCurCluster = new APredicationList(curClusterList);
      // log.debug("# of predications BEFORE EXPANSION in the cluster -" + i + " - = " + curClusterList.size());
      // List idsClusterList = spCurCluster.getSentencePredicationIDs();
      List expandedPredList = extractPredicationList(curClusterList, predications);
      APredicationList spCurCluster = new APredicationList(expandedPredList);
      List idsClusterList = spCurCluster.getSentencePredicationIDs();

      q.setParameterList("ids", idsClusterList);
      // log.debug("idsString = " + idsClusterList.toString());
      SemrepResultSet srs = new SemrepResultSet(q.list());

      List sps = srs.getSentencePredications();
      List displayedPredications;
      /* if (sps.size() > LIMIT) {
        displayedPredications = new ArrayList(LIMIT);
        for (int j = 0; i < LIMIT; i++)
          displayedPredications.add((SentencePredication)sps.get(j));
        session.setAttribute("maxedPredications", Integer.valueOf(sps.size()));
      } else {
        displayedPredications = sps;
        session.removeAttribute("maxedPredications");
      } */
      displayedPredications = sps;
      session.removeAttribute("maxedPredications");
      // log.debug("# of predications AFTER EXPANSION in the cluster -" + i + "- = " + displayedPredications.size());
      clusteredSPS.add(displayedPredications);
    }

    // log.debug("Center concept = " +  centerConcept);
    graphXml = GraphUtils.parseZ(clusteredSPS, semanticMeaningVector, ConnectivityTable, concDegreeList,
      null, null, session.getServletContext(), false,
      centerConcept);
    } else {
    	ArrayList clusteredSPS = new ArrayList();
    	graphXml = GraphUtils.parseZ(clusteredSPS, new Vector(), ConnectivityTable, concDegreeList,
    		      null, null, session.getServletContext(), false,
    		      centerConcept);
    }
    XMLOutputter serializer = new XMLOutputter();
    String graphString = serializer.outputString(graphXml);
    log.debug("Graph XML in SummaryAction () : " + graphString);

    session.removeAttribute("predsource");
    session.setAttribute("predsource", Integer.valueOf(1));
    UUID key = GraphServlet.addGraph(request, graphString, (List)
      session.getAttribute("relevantQuestionrCitations"), (List)session
      .getAttribute("relevantQuestionnrCitations"));

    session.setAttribute("cluster", "true");
    session.setAttribute("key", key.toString());
    return mapping.findForward("successz");
  }

  double findCutOff(Hashtable predTable) {
    float mean = 0.0F;
    float deviation = 0.0F;
    int numOfList = 0;
    double sumOfSquare = 0.0F;
    Enumeration predEnum = predTable.keys();
    ArrayList valueList = new ArrayList();
    ArrayList subtList = new ArrayList();
    ArrayList squareList = new ArrayList();
    while (predEnum.hasMoreElements()) {
      APredicationLite ap = (APredicationLite)predEnum.nextElement();
      Integer freq = (Integer)predTable.get(ap);
      numOfList++;
      mean = mean + freq;
      valueList.add(freq);
    }
    mean = mean / numOfList;

    for (int i = 0; i < valueList.size(); i++) {
      double newValue = ((Integer)valueList.get(i) - mean) * ((Integer)valueList.get(i) - mean) ;
      sumOfSquare = sumOfSquare + newValue;
      // subtList.add(Double.valueOf(newValue));
    }
    /* for (int i = 0; i < subtList.size(); i++) {
      float newValue = ((Float)subtList.get(i)).floatValue();
      sumOfSquare += newValue * newValue;
    } */
    double total = sumOfSquare / numOfList;
    double devi = Math.sqrt(total);
    log.debug("sumOfSquare = " + sumOfSquare + " , numOfList = " + numOfList);
    log.debug("Frequency Mean = " + mean + " , Frequency Deviation = " + devi);
    log.debug("cutoff for frequency = " + mean + devi/2);
    return mean + devi/2;
    // return mean + devi;

  }

  double findCutOffConnectivity(Hashtable connectTable) {
	    float mean = 0.0F;
	    float deviation = 0.0F;
	    int numOfList = 0;
	    double sumOfSquare = 0.0F;
	    Enumeration predEnum = connectTable.keys();
	    ArrayList valueList = new ArrayList();
	    int biggest = 0;
	    while (predEnum.hasMoreElements()) {
	      String concept = (String)predEnum.nextElement();
	      Set aSet = (Set)connectTable.get(concept);
	      numOfList++;
	      if(aSet.size() > biggest)
	    	  biggest = aSet.size();
	      mean = mean + aSet.size();
	      valueList.add(aSet.size());
	        // log.debug("In findCutOffConnectivity() -  concepts : " + concept + ", connectivity : " + aSet.size());
	    }
	    mean = (mean-biggest)/ (numOfList-1);

	    for (int i = 0; i < valueList.size(); i++) {
	    	double newValue = 0F;
	    	if((Integer)valueList.get(i) < biggest) {
	    		newValue = ((Integer)valueList.get(i) - mean) * ((Integer)valueList.get(i) - mean) ;
	    		sumOfSquare = sumOfSquare + newValue;
	    	}
	      // subtList.add(Double.valueOf(newValue));
	    }
	    /* for (int i = 0; i < subtList.size(); i++) {
	      float newValue = ((Float)subtList.get(i)).floatValue();
	      sumOfSquare += newValue * newValue;
	    } */
	    double total = sumOfSquare / numOfList;
	    double devi = Math.sqrt(total);
	    log.debug("sumOfSquare = " + sumOfSquare + " , numOfList = " + numOfList);
	    log.debug("Connectivity Mean = " + mean + " , Connectivity Deviation = " + devi);
	    double central = mean + devi/2;
	    log.debug("Cut-off for centrality = " + central);
	    return central;
	    // return mean + 2*devi/3;
	  }

  static String constructValidClique(int first, int second, int third)
  {
    StringBuffer pathBuf = new StringBuffer();
    if ((first < second) && (first < third)) {
      pathBuf.append(first + "|");
      if (second < third)
        pathBuf.append(second + "|" + third);
      else
        pathBuf.append(third + "|" + second);
    } else if ((second < first) && (second < third)) {
      pathBuf.append(second + "|");
      if (first < third)
        pathBuf.append(first + "|" + third);
      else
        pathBuf.append(third + "|" + first);
    } else if ((third < first) && (third < second)) {
      pathBuf.append(third + "|");
      if (first < second)
        pathBuf.append(first + "|" + second);
      else
        pathBuf.append(second + "|" + first);
    }
    return pathBuf.toString();
  }

  static String constructValidClique(String[] compo, int index) {
    StringBuffer pathBuf = new StringBuffer();
    boolean alreadyInserted = false;
    for (int i = 0; i < compo.length; i++) {
      int curIndex = Integer.parseInt(compo[i]);
      if (curIndex < index) {
        pathBuf.append(curIndex + "|");
      } else if ((index < curIndex) && (!alreadyInserted)) {
        pathBuf.append(index + "|" + curIndex + "|");
        alreadyInserted = true;
      } else if ((index < curIndex) && (alreadyInserted)) {
        pathBuf.append(curIndex + "|");
      }
    }

    if (!alreadyInserted) {
      pathBuf.append(index + "|");
    }
    return pathBuf.delete(pathBuf.length() - 1, pathBuf.length()).toString();
  }

  int[] mergeCluster(int[] list1, int[] list2) {
    ArrayList<Integer> out = new ArrayList<Integer>();
    int movei = 0;
    int movej = 0;
    // int minValue = Math.min(list1.length, list2.length);
    while ((movei < list1.length) && (movej < list2.length)) {
      if (list1[movei] == list2[movej]) {
        out.add(list2[movej]);
        movei++;
        movej++;
      } else if (list1[movei] > list2[movej]) {
        out.add(list2[movej]);
        movej++;
      } else if (list1[movei] < list2[movej]) {
        out.add(list1[movei]);
        movei++;
      }
    }
    if(movei < list1.length) {
    	while (movei < list1.length) {
    	      out.add(list1[movei]);
    	      movei++;
    	}
    }
    if(movej < list2.length) {
        while (movej < list2.length) {
        	  out.add(list2[movej]);
        	  movej++;
        }
   }
    int[] outInt = new int[out.size()];
    // log.debug("New cluster size in mergeCluster() = " + out.size());
    for(int i=0; i < out.size(); i++)
    	outInt[i] = out.get(i);
    return   outInt;
  }

  public List extractPredicationList(List inList, List predList) throws Exception {
		  List outList = new ArrayList();
		  for(int i = 0; i < inList.size(); i++) {
			  outList.add(inList.get(i));
			  int pid1 = ((APredication) inList.get(i)).PID;
			  for(int j = 0; j < predList.size(); j++) {
				  int pid2 = ((APredication) predList.get(j)).PID;
				  if(pid1 == pid2)
					  outList.add(predList.get(j));
			  }
		  }
	  return outList;
  }

  public String findSemanticGroup(String semType) {
	  if(SemanticGroups.ACTIVITY.contains(semType)) return new String("acti");
	  else if(SemanticGroups.ANATOMY.contains(semType)) return new String("anat");
	  else if(SemanticGroups.CHEMICALS.contains(semType)) return new String("chem");
	  else if(SemanticGroups.CONCEPTS.contains(semType)) return new String("conc");
	  else if(SemanticGroups.DEVICES.contains(semType)) return new String("devi");
	  else if(SemanticGroups.DISORDER.contains(semType)) return new String("diso");
	  else if(SemanticGroups.GENES.contains(semType)) return new String("gene");
	  else if(SemanticGroups.GEOGRAPHICS.contains(semType)) return new String("geog");
	  else if(SemanticGroups.LIVINGBEING.contains(semType)) return new String("livb");
	  else if(SemanticGroups.PHENOMENA.contains(semType)) return new String("phen");
	  else if(SemanticGroups.PHYSIOLOGY.contains(semType)) return new String("phys");
	  else if(SemanticGroups.PROCEDURE.contains(semType)) return new String("proc");
	  else return new String("other");
  }

  public ArrayList checkSubsume(ArrayList inList) {
	  // ArrayList outList = new ArrayList();
	  ArrayList thisList = inList;
	  Boolean changed = true;
	  while (changed) {
		  Boolean everChanged = false;
		  log.debug(" --- Start subsume check ----");
		  for(int i = 0; i < thisList.size(); i++) {
			  for(int j = 0; j < thisList.size(); j++) {
				  if(i != j) {
					  HashSet me = (HashSet) thisList.get(i);
					  HashSet you = (HashSet) thisList.get(j);
					  if(me.containsAll(you)) {
						  thisList.remove(j);
						  everChanged = true;
						  log.debug(" -----------Cluster " + i + " subsume cluster " + j + ", so remove cluster " + j);
					  }
				  }
			  }
		  }
		  if(everChanged)
			  changed = true;
		  else
			  changed = false;
	  }

	  return thisList;
  }

	/**
	 * Method upload
	 *
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 */
	public ActionForward upload(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws UploadException {
		SummaryForm SummaryForm = (SummaryForm) form;
		HttpSession session = request.getSession();
		FormFile file = SummaryForm.getUploadFile();

		try {
			byte[] fileData = file.getFileData();
			session = XMLUtils.uploadXML(fileData, session);
			if (HibernateSessionFactory.isSessionValid())
				HibernateSessionFactory.closeSession();

			return mapping.findForward("success");
		} catch (Exception e) {
			e.printStackTrace();
			throw new UploadException(e);
		}
	}

	/**
	 * Method exportToXml
	 *
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 */
	public ActionForward exportToXml(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws XMLException {

		HttpSession session = request.getSession();
		try {
			Document doc = XMLUtils.exportToXml(session);
			response.setContentType("application/octet-stream");
			response.setHeader("Content-disposition",
					"attachment;filename=summary-results.xml");
			Writer out = new OutputStreamWriter(response.getOutputStream(),
					"UTF8");
			XMLOutputter serializer = new XMLOutputter(Format.getPrettyFormat());
			serializer.output(doc, out);
		} catch (Exception e) {
			e.printStackTrace();
			throw new XMLException(e);
		}
		return null;
	}

	/**
	 * Method findRelevant
	 *
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 */
	public ActionForward findRelevant(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {
		SummaryForm SummaryForm = (SummaryForm) form;
		// TODO Need to add summary-specific information to the XML
		String summaryType = SummaryForm.getSummaryType();
		HttpSession session = request.getSession();
		APredicationList preds = (APredicationList) session
				.getAttribute("predications");
		List<LabelValueBean> relevantConcepts = null;
		if (summaryType.equals("diagnosis"))
			relevantConcepts = preds
					.getRelevantConcepts(APredicationList.Type.DIAGNOSIS);
		else if (summaryType.equals("pharmacogenomics"))
			relevantConcepts = preds
					.getRelevantConcepts(APredicationList.Type.PHARMACOGENOMICS);
		else if (summaryType.equals("genetic"))
			relevantConcepts = preds
					.getRelevantConcepts(APredicationList.Type.GENETIC);
		else if (summaryType.equals("interaction"))
			relevantConcepts = preds
					.getRelevantConcepts(APredicationList.Type.INTERATCION);
		else
			relevantConcepts = preds
					.getRelevantConcepts(APredicationList.Type.TREATMENT);
		session.setAttribute("relevantConcs", relevantConcepts);
		session.setAttribute("summaryType", summaryType);
		/* log.debug("Relevant Conc Size:"
				+ ((List) session.getAttribute("relevantConcs")).size()); */
		if (relevantConcepts.size() > 0)
			session.setAttribute("selectedSeed",
					((LabelValueBean) relevantConcepts.get(0)).getLabel());
		// log.debug("Seed: " + ((String) session.getAttribute("selectedSeed")));
		return mapping.findForward("success");
	}

	/**
	 * Method processTest
	 *
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 */
	@SuppressWarnings("unchecked")
	public ActionForward processTest(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws PubmedException, EssieException, SemMedException, Exception {

		long startAll = System.currentTimeMillis();
		SummaryForm SummaryForm = (SummaryForm) form;
		HttpSession session = request.getSession();

		String summaryType = SummaryForm.getSummaryType();
		// log.debug("Summary type:" + summaryType);

		session.removeAttribute("lang");
		session.removeAttribute("translateSelectedSeed");

		int page;

		if (request.getParameter("p") == null) {

			ArrayList<Filter> filters = new ArrayList<Filter>();

			filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.PRELIMINARY));

			if ("nhlbi".equals(summaryType)) {
				filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.NHLBI_RELEVANCE));
				filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.CONNECTIVITY));
				filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.NHLBI_NOVELTY));
			} else if ("diagnosis".equals(summaryType)) {
				filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.DIAGNOSIS_RELEVANCE));
				filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.CONNECTIVITY));
				filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.DIAGNOSIS_NOVELTY));
			} else if ("interaction".equals(summaryType)) {
				filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.INTERACTION_RELEVANCE));
				// filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.CONNECTIVITY));
				// Dongwook, 04/07/2008, Has separate Connectivity for interaction in summarize_interaction.pl
				filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.INTERACTION_CONNECTIVITY));
				filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.INTERACTION_NOVELTY));
			} else if ("treatment".equals(summaryType)) {
				filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.TREATMENT_RELEVANCE));
				filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.CONNECTIVITY));
				filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.TREATMENT_NOVELTY));
			} else if ("pharmacogenomics".equals(summaryType)) {
				filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.PHARMACOGENOMICS_RELEVANCE));
				// filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.CONNECTIVITY));
				// Dongwook, 04/07/2008 Has separate Connectivity for pharmacogenomics in summarize_pharmgen.pl
				filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.PHARMACOGENOMICS_CONNECTIVITY));
				filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.PHARMACOGENOMICS_NOVELTY));
			}

			if (request.getParameter("saliency") != null
					&& request.getParameter("saliency").equals("on")) {
				if ("predication".equals(SummaryForm.getSelectedSaliencyType()))
					// Dongwook, 04/08/2008
					// Substance Interaction and Pharmacogenomics have different saliency filter since they have different DEMOTED_PREDICATE_LIST
					if("pharmacogenomics".equals(summaryType))
						filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.INTERPHARMA_PREDICATION_SALIENCY));
					else if("interaction".equals(summaryType))
						filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.INTERPHARMA_PREDICATION_SALIENCY));
					else
						filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.PREDICATION_SALIENCY));
				else if ("relation".equals(SummaryForm.getSelectedSaliencyType()))
					filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.RELATION_SALIENCY));
				else if ("concept".equals(SummaryForm.getSelectedSaliencyType()))
					filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.CONCEPT_SALIENCY));

				session.setAttribute("saliency", Boolean.TRUE);
				session.setAttribute("selectedSaliencyType", SummaryForm.getSelectedSaliencyType());

			}

			session.setAttribute("selectedSeed", request.getParameter("seed"));
			// log.debug("seed Concept:" + request.getParameter("seed"));

			List<APredication> summarizedPredications = Summarizer.summarize(
					filters.toArray(new Filter[filters.size()]),
					(TestPredicationList) session.getAttribute("predications"),
					new String[] { Constants.CAUSES, Constants.DIAGNOSES,
							Constants.ISA }, SummaryForm.getSeed());

			page = 0;
			session.setAttribute("summaryPredications", summarizedPredications);
			session.setAttribute("summaryType", summaryType);
			session.removeAttribute("predsource");
			session.setAttribute("predsource",2);
			session.removeAttribute("key");
		}else{
			try{
				page = Integer.parseInt(request.getParameter("p"));
			}catch(Exception e){
				page = 0;
			}
		}


		List<APredication> predications = (List<APredication>)session.getAttribute("summaryPredications");

		int[] PIDs = new int[Math.min(20, predications.size()-page*20)];
		int[] SIDs = new int[PIDs.length];
		ArticleDataSource.SourceType[] sources = new ArticleDataSource.SourceType[PIDs.length];

		for(int i=0;i<PIDs.length;i++){
			PIDs[i] = predications.get(i+page*20).PID;
			SIDs[i] = predications.get(i+page*20).SID;
			sources[i] = predications.get(i+page*20).source;
		}

		try{
			session.setAttribute("displayedSummaryPredications",new TestPredicationList(PIDs,SIDs,sources));
		}catch(Exception e){
			e.printStackTrace();
			throw new PubmedException(e);
		}
		session.setAttribute("pageNumberSummary", page);
	    session.removeAttribute("query");

		session.setAttribute("executionTime", System.currentTimeMillis()
				- startAll);
		return mapping.findForward("success");
	}

	public ActionForward processzTest(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
    throws PubmedException, EssieException, SemMedException, Exception
  {
    HttpSession session = request.getSession();
    ArrayList filters = new ArrayList();
    filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.TREATMENT_NOVELTY));
    String[] nullList = new String("").split("|");
    // APredicationList aPredicationList = (APredicationList)session.getAttribute("predications");
    /* List predications = gov.nih.nlm.semmed.zsummarization.Summarizer.summarize(
      (gov.nih.nlm.semmed.zsummarization.Filter[])filters.toArray(new gov.nih.nlm.semmed.zsummarization.Filter[filters.size()]), aPredicationList, nullList);
      */
    List<APredication> listIn = (List<APredication>) session.getAttribute("predications");
    List<APredication> predications = new ArrayList<APredication>();
	  for(APredication p : listIn)
		if ( p.novelSubject && p.novelObject && p.predicate.compareTo("PROCESS_OF") != 0 && p.predicate.compareTo("higher_than") != 0  && p.predicate.compareTo("lower_than")  != 0)
				predications.add(p);

	/* List<APredication> predications = Summarizer.summarize(
			(Filter[]) filters.toArray(new Filter[filters.size()]),
			(APredicationList) session.getAttribute("predications"),
			new String[] {Constants.PROCESS_OF}, null); */
	log.debug("# of noble predications = " + predications.size());
    Hashtable predTableToken = new Hashtable();
    Hashtable predTablePerPMID = new Hashtable();
    Hashtable predTableTypi = new Hashtable();
    Hashtable conceptTable = new Hashtable();
    Hashtable conceptRevTable = new Hashtable();
    Hashtable predtable = new Hashtable();
    int PMID = 0;
    int prevPMID = 0;
    int concId = 0;
    Hashtable ConnectivityTable = new Hashtable();
    Hashtable findPredInfoTable = new Hashtable();
    Set filteredConceptSet = getFilteredConceptSet(); // get predefined filtered concept set
    log.debug("-------- Beginning of subject Object pair added ------------");
    for (int i = 0; i < predications.size(); i++) {
      APredication apred = (APredication)predications.get(i);
      String SubjConcept = (String)apred.subject.get(0);
      String ObjConcept = (String)apred.object.get(0);
      String SubjST = (String)apred.subjectSemtype.get(0);
      String ObjST = (String)apred.objectSemtype.get(0);
      String predicate = apred.predicate;
      // Ignore the following semantic types
      if(SubjST.equals("mamm") || SubjST.equals("humn") || ObjST.equals("mamm") || ObjST.equals("humn") || predicate.equals("PROCESS_OF") || filteredConceptSet.contains(SubjConcept.toLowerCase()) || filteredConceptSet.contains(ObjConcept.toLowerCase()))
    	  continue;

      APredicationWOR ap = new APredicationWOR(SubjConcept, SubjST, ObjConcept, ObjST);
      HashSet predSet = null;
      if ((predSet = (HashSet)predtable.get(ap)) != null) {
        predSet.add(apred);
      } else {
        predSet = new HashSet();
        predSet.add(apred);
        predtable.put(ap, predSet);
      }

      String subjWithST = new String(SubjConcept + "|" + SubjST);
      String objWithST = new String(ObjConcept + "|" + ObjST);
      String subjObjPair = new String(subjWithST + "-" + objWithST);
      if (!conceptTable.containsKey(subjWithST)) {
        conceptTable.put(subjWithST, Integer.valueOf(concId));
        conceptRevTable.put(Integer.valueOf(concId), subjWithST);
        concId++;
      }
      if (!conceptTable.containsKey(objWithST)) {
        conceptTable.put(objWithST, Integer.valueOf(concId));
        conceptRevTable.put(Integer.valueOf(concId), objWithST);
        concId++;
      }

      Hashtable predList = (Hashtable) findPredInfoTable.get(subjObjPair);
      if (predList == null) {
    	  // ConcDegree cd = new ConcDegree(predicate, 1); // Make a pair with predicate name and its frequence
    	  predList = new Hashtable();
    	  predList.put(predicate, 1); // Add the first predicate connecting subject and object
    	  findPredInfoTable.put(subjObjPair,predList);
    	  // log.debug(subjObjPair);
      } else {
    	  Integer predFreq = (Integer) predList.remove(predicate);
    	  if(predFreq == null) { // subject and object are found, but the predicate name is not found
    		  predList.put(predicate, 1); // Add the first predicate connecting subject and object
    	  } else { // same predicate
    		  Integer newPredFreq = predFreq + 1;
    		  predList.put(predicate, newPredFreq);
    	  }
      }

      prevPMID = PMID;
      PMID = ((APredication)predications.get(i)).PMID;
      if ((PMID != prevPMID) && (prevPMID != 0)) {
        Enumeration predEnum = predTablePerPMID.keys();
        while (predEnum.hasMoreElements()) {
          APredicationLite ap2 = (APredicationLite)predEnum.nextElement();
          Integer freqPerPMID = (Integer)predTablePerPMID.get(ap2);
          Integer freq1 = (Integer)predTableToken.get(ap2);
          if (freq1 != null) {
            freq1 = Integer.valueOf(freq1.intValue() + freqPerPMID.intValue());
            predTableToken.remove(ap2);
            predTableToken.put(ap2, new Integer(freq1.intValue()));
          }
          else {
            predTableToken.put(ap2, new Integer(freqPerPMID.intValue()));
          }

          Integer freq2 = (Integer)predTableTypi.get(ap2);
          if (freq2 != null) {
            freq2 = Integer.valueOf(freq2.intValue() + 1);
            predTableTypi.remove(ap2);
            predTableTypi.put(ap2, new Integer(freq2.intValue()));
          } else {
            predTableTypi.put(ap2, new Integer(1));
          }
        }
        predTablePerPMID = null;
        predTablePerPMID = new Hashtable();
      }
      APredicationLite curPred = new APredicationLite(SubjConcept, SubjST, predicate, ObjConcept, ObjST);
      Integer freq1 = (Integer)predTablePerPMID.get(curPred);
      if (freq1 != null) {
        freq1 = Integer.valueOf(freq1.intValue() + 1);
        predTablePerPMID.remove(curPred);
        predTablePerPMID.put(curPred, new Integer(freq1.intValue()));
      } else {
        predTablePerPMID.put(curPred, new Integer(1));
      }
    }
    // log.debug("----- End of Subject Object pair added ---------");

    double cutOff = findCutOff(predTableTypi);
    // double cutOff = 2.6;
    // log.debug("Cut off value = " + cutOff);
    Enumeration predEnum = predTableTypi.keys();
    HashSet survivedSet = new HashSet();
    int numOfSurvived = 0;
    int biggestFreq = 0;
    APredicationLite biggestPred = null;
    // Hashtable ConnectivityTable = new Hashtable();
    Hashtable wConnectivityTable = new Hashtable();
    while (predEnum.hasMoreElements()) {
      APredicationLite ap = (APredicationLite)predEnum.nextElement();
      Integer freq = (Integer)predTableTypi.get(ap);

      if (freq.intValue() >= cutOff) {
          // log.debug("survived from frequency cutoff : " + ap.toString() + ", freq = " + freq);
    	  survivedSet.add(ap);
        String SubjConcept = ap.getSubj();
        String SubjST = ap.getSubjST();
        String ObjConcept = ap.getObj();
        String ObjST = ap.getObjST();
        // Finding the adjancect list of a node
        HashSet adjacentSet1 = null;
        ArrayList connectedConceptList = null;
  		String css = new String(SubjConcept + "|" + SubjST);
  		String cso = new String(ObjConcept + "|" + ObjST);

  		if(SubjConcept != null && SubjConcept.compareTo("") != 0 && (adjacentSet1 = (HashSet) ConnectivityTable.get(css)) != null) {
  			// System.out.println(css.toString() + "is already found");
  			connectedConceptList = (ArrayList) wConnectivityTable.get(css);
  			connectedConceptList.add(cso);
  			if(!adjacentSet1.contains(cso)) {
  				adjacentSet1.add(cso);
  				// System.out.println(cso.toString() + "is already found");
  			}
  		} else if (SubjConcept != null && SubjConcept.compareTo("") != 0) {
  			adjacentSet1 = new HashSet();
  			adjacentSet1.add(cso);
  			ConnectivityTable.put(css,adjacentSet1);
  			connectedConceptList = new ArrayList();
  			connectedConceptList.add(cso);
  			wConnectivityTable.put(css,connectedConceptList); // need to calculated the weighted connectivity

  		}

  		HashSet adjacentSet2 = null;
  		if(ObjConcept != null && ObjConcept.compareTo("") != 0 && (adjacentSet2 = (HashSet) ConnectivityTable.get(cso)) != null) {
  			 // System.out.println(cso.toString() + "is already found");
  			connectedConceptList = (ArrayList) wConnectivityTable.get(cso);
  			connectedConceptList.add(css);
  			if(!adjacentSet2.contains(css)) {
  				adjacentSet2.add(css);
  				// System.out.println(css.toString() + "is already found");
  			}
  		} else if (ObjConcept != null && ObjConcept.compareTo("") != 0) {
  			adjacentSet2 = new HashSet();
  			adjacentSet2.add(css);
  			ConnectivityTable.put(cso,adjacentSet2);
  			connectedConceptList = new ArrayList();
  			connectedConceptList.add(css);
  			wConnectivityTable.put(cso,connectedConceptList); // need to calculated the weighted connectivity
  		}

        // log.debug("survived -- " + ap.toString() + ", freq = " + freq);
        numOfSurvived++;
        if(freq > biggestFreq) {
        	biggestFreq = freq;
        	biggestPred = ap;
        }
      }
    }
    // log.debug("---- biggest pred = " + biggestPred.toString() + " freq = " + biggestFreq);
    String centerConcept = biggestPred.getObj();
    // log.debug("---- Num of survived Predication = " + numOfSurvived);

    // log.debug("---- Num of filtered Predication = " + numOfFiltered);
    // Filter the concept connectivity less than the mean _ deviation
    double connectCutOff = findCutOffConnectivity(ConnectivityTable);
    // log.debug("connectivity cutoff : " + connectCutOff);
    HashSet survivedSet2 = new HashSet();
    Enumeration cenum = ConnectivityTable.keys();
	List concDegreeList = new ArrayList();
    while (cenum.hasMoreElements()) {
        String concept = (String)cenum.nextElement();
        HashSet aSet = (HashSet)ConnectivityTable.get(concept);
        List aList = (List)wConnectivityTable.get(concept);
        // log.debug("survived concepts : " + concept + ", connectivity : " + aSet.size());
        // int aSize = aSet.size();
        int aSize = aList.size();
        if(aSize >= connectCutOff) {
        	survivedSet2.add(concept);
			// System.out.println(concST.concept	+ " | " + concST.ST +  "\t | \t" + adjSet.size());
			ConcDegree cd = new ConcDegree(concept, new Integer(aSize));
			concDegreeList.add(cd);
            log.debug("\t------ finally survived concepts : " + concept + ", connectivity : " + aSet.size());
            // log.debug("\t\t\t weighted connectivity : " + aList.size());
        }
      }
	Collections.sort(concDegreeList, new ConcComp()); // Sort the concepts in the order of the number of connectivity

    int numOfConcepts = concId;
    boolean[][] relationTable = new boolean[numOfConcepts][numOfConcepts];
    for (int i = 0; i < numOfConcepts; i++) {
      for (int j = 0; j < numOfConcepts; j++) {
        relationTable[i][j] = false;
      }
    }

    // log.debug("Creating edge relations for # of concepts = " + numOfConcepts);
    // Enumeration apList = predtable.keys();
    Iterator apIt = survivedSet.iterator();
    // log.debug("# of elements in predtable = " + predtable.size());
    while (apIt.hasNext()) {
      APredicationLite apred = (APredicationLite)apIt.next();
      String subjWithST = new String(apred.getSubj() + "|" + apred.getSubjST());
      Integer subjIndex = (Integer)conceptTable.get(subjWithST);
      String objWithST = new String(apred.getObj() + "|" + apred.getObjST());
      Integer objIndex = (Integer)conceptTable.get(objWithST);
      if (subjIndex != objIndex && survivedSet2.contains(subjWithST) && survivedSet2.contains(objWithST)) {
        relationTable[subjIndex.intValue()][objIndex.intValue()] = true;
        relationTable[objIndex.intValue()][subjIndex.intValue()] = true;
        // log.debug("rel[" + subjIndex.intValue() + "][" + objIndex.intValue() + "] = true " );
        // log.debug("rel[" + subjIndex.intValue() + "," + subjWithST + "][" + objIndex.intValue() + ", " + objWithST + "] = true " );
      }
    }
    boolean[] nodesListForClique = new boolean[numOfConcepts];
    for (int i = 0; i < numOfConcepts; i++) {
      nodesListForClique[i] = false;
    }

    Vector cliqueVec = new Vector();
    HashSet alreadyFoundClique = new HashSet();

	for(int firstI = 0; firstI< numOfConcepts; firstI++) {
		for(int secondI = 0; secondI < numOfConcepts; secondI++) {
			if(relationTable[firstI][secondI] == true) { // if firstI <-> secondI connected
				for(int thirdI = 0; thirdI< numOfConcepts; thirdI++) {
					if(relationTable[firstI][thirdI] == true && relationTable[secondI][thirdI] ==true) {
						if(firstI != secondI  && secondI != thirdI && thirdI != firstI) { // If the three are unique concepts
							String curClique = constructValidClique(firstI, secondI, thirdI);
							if(!cliqueVec.contains(curClique)) { // if the click is not added already
								String firstConc = (String) conceptRevTable.get(firstI);
								String secondConc = (String) conceptRevTable.get(secondI);
								String thirdConc = (String) conceptRevTable.get(thirdI);
								log.debug("***** new clique : " + firstConc + " - " + secondConc + " - " + thirdConc);
								cliqueVec.add(curClique);
								// out3.println(curClique);
							}
						}

					}
				}
			}
		}
    }

    int numOfThreeNodeClique = cliqueVec.size();
    // log.debug("The number of clique with three nodes found = " + numOfThreeNodeClique);
    Vector finalCliqueVec = new Vector();
    Document graphXml = null;

    if(numOfThreeNodeClique > 0) {	// If there is more than 0 three node click
    while (cliqueVec.size() > 0) {
      String exClique = (String)cliqueVec.remove(0);
      String[] compo = exClique.split("\\|");
      boolean foundNextLevelClique = false;
      for (int j = 0; j < numOfConcepts; j++) {
        boolean[] connected = new boolean[compo.length];
        for (int k = 0; k < compo.length; k++) {
          connected[k] = false;
        }
        for (int k = 0; k < compo.length; k++) {
          int nodeInt = Integer.parseInt(compo[k]);
          if ((k == 0) && (j != nodeInt) && (relationTable[j][nodeInt] == true))
            connected[k] = true;
          else if ((k > 0) && (j != Integer.parseInt(compo[k])) && (relationTable[j][nodeInt] == true) && (connected[(k - 1)] == true)) {
            connected[k] = true;
          }
        }
        if (connected[(compo.length - 1)] == true) {
          String newClique = constructValidClique(compo, j);

          cliqueVec.add(newClique);
          foundNextLevelClique = true;
          break;
        }
      }
      if ((foundNextLevelClique) || (finalCliqueVec.contains(exClique)))
        continue;
      finalCliqueVec.add(exClique);
    }

    log.debug("*************** Final Cliques ******************" + finalCliqueVec.size());
    ArrayList clusterResult = new ArrayList(200);
    ArrayList firstClusterList = new ArrayList(finalCliqueVec.size() - 1);
    int numOfCurClusters = 0;
    for (int j = finalCliqueVec.size() - 1; j >= 0; j--) {
      String exClique = (String)finalCliqueVec.elementAt(j);
      // log.debug(exClique);
      String[] compo = exClique.split("\\|");
      StringBuffer curClickBuf = new StringBuffer();
      StringBuffer curClickBuf1 = new StringBuffer();
      if (compo.length >= 3) {
        numOfCurClusters++;
        int[] curClick = new int[compo.length];
        for (int index = 0; index < compo.length; index++) {
          curClick[index] = Integer.parseInt(compo[index].trim());
          String curCon = (String) conceptRevTable.get(Integer.parseInt(compo[index].trim()));
          String[] compon = curCon.split("\\|");
          curClickBuf.append(curCon + " | ");
          curClickBuf1.append(compon[0] + " | ");
        }
        log.debug(curClickBuf1.toString());
        firstClusterList.add(curClick);
      }
    }
    // log.debug("*************** Clicques ******************");
    clusterResult.add(firstClusterList);
    int clusterListIndex = 0;
    int prevClusterSize = 0;
    int curClusterSize = 0;
    while (true) {
      ArrayList currentClusterList = (ArrayList)clusterResult.get(clusterListIndex);

      // log.debug("Previous # of clusters = " + prevClusterSize);
      curClusterSize = currentClusterList.size();
      // log.debug("Current # of clusters = " + curClusterSize);
      if (curClusterSize == prevClusterSize || curClusterSize <= 1) {
          // log.debug("Final number after syntic clustering = " + curClusterSize);
    	  break;
      }
      prevClusterSize = curClusterSize;
      int lowestDistance = noDistance;
      int[][] distance = new int[numOfCurClusters][numOfCurClusters];
      for (int clicki = 0; clicki < curClusterSize; clicki++) {
        for (int clickj = clicki + 1; clickj < curClusterSize; clickj++) {
          if (clicki != clickj) {
            // log.debug("clicki = " + clicki + ", clickj = " + clickj);
        	  int[] click1 = (int[])currentClusterList.get(clicki);
            int[] click2 = (int[])currentClusterList.get(clickj);
            int size1 = click1.length;
            int size2 = click2.length;
            int match = 0;
            int movei = 0;
            int movej = 0;
            while ((movei < size1) && (movej < size2)) {
              if (click1[movei] == click2[movej]) {
                match++;
                movei++;
                movej++;
              } else if (click1[movei] > click2[movej]) {
                movej++;
              } else if (click1[movei] < click2[movej]) {
                movei++;
              }
            }
            if(match > 0) {
            	distance[clicki][clickj] = (click1.length + click2.length)/match;
            } else
            	distance[clicki][clickj] = noDistance;
           if (distance[clicki][clickj] < lowestDistance)
             lowestDistance = distance[clicki][clickj];
           // log.debug("lowest distance = " + lowestDistance);
          }
        }
      }
      int currentSize = currentClusterList.size();
      boolean[] used = new boolean[currentSize];
      for (int clicki = 0; clicki < currentSize; clicki++) {
        used[clicki] = false;
      }
      // log.debug("Start merging ther clusters with lowest distance = " + lowestDistance);
      ArrayList nextClusterList = new ArrayList();
      for (int clicki = 0; clicki < currentSize; clicki++) {
        for (int clickj = clicki + 1; clickj < currentSize; clickj++) {
          if (distance[clicki][clickj] <= lowestDistance && used[clicki] == false && used[clickj] == false) {
            int[] click1 = (int[]) currentClusterList.get(clicki);
            int[] click2 = (int[]) currentClusterList.get(clickj);
            // log.debug("--- old cluster for " + clicki + "(size = " + click1.length +  ") and " + clickj + "(size = " + click2.length + ")");
            int[] newCluster = mergeCluster(click1, click2);
            // log.debug("--- New cluster size = " + newCluster.length);
            nextClusterList.add(newCluster);
            used[clicki] = true;
            used[clickj] = true;
            // lowestDistance = distance[clicki][clickj];
          }
        }
      }
      for (int i = 0; i < currentSize; i++) {
        if (used[i] == false)
          nextClusterList.add(currentClusterList.get(i));
      }
      clusterResult.add(nextClusterList);
      clusterListIndex++;
    }

    int semanticClusterIndex = 0; // Start semantic clustering in half of the position
    // ArrayList  nextSemanticClusterList = null;
    ArrayList semanticClusterResult = new ArrayList();
	log.debug("*** The number of steps of syntactic clustering = " + clusterListIndex);
	ArrayList  curSemanticClusterList = (ArrayList) clusterResult.get(clusterListIndex/2); // Semantic clustering starts at the middle steps of syntact clustering

    log.debug("*************** The start of semantic Cluster info ******************");
    int sizeCluster  = curSemanticClusterList.size();
    for (int ii = 0; ii < sizeCluster; ii++) {
    	// log.debug("****************** Cluster " + ii + " ********************");
      int[] cluster = (int[]) curSemanticClusterList.get(ii);
      // log.debug(exClique);
      StringBuffer curClickBuf = new StringBuffer();
      for (int j = 0; j < cluster.length; j++) {
          int intPair1 = cluster[j];
          String curPair = (String)conceptRevTable.get(intPair1);
          	String[] comp1 = curPair.split("\\|");
          	String subj = comp1[0].trim();
          	// String subjST = comp1[1].trim();
          	// log.debug(subj);
      }
    }
    log.debug("*************** The end of semantic Cluster info  ******************");

	semanticClusterResult.add(curSemanticClusterList);
	boolean semanticClusterChanged = true;
	Hashtable semanticMeaningTable = null;
	Vector semanticMeaningVector = null;
	while(semanticClusterChanged == true) {
    	 // ArrayList  thisClusterList = (ArrayList) clusterResult.get(semanticClusterIndex);
	     ArrayList  nextSemanticClusterList = new ArrayList();
    	 int thisSize = curSemanticClusterList.size();
    	 // log.debug("*** The size of first semantic cluster = " + thisSize);
    	 semanticMeaningTable = new Hashtable();
    	 semanticMeaningVector = new Vector();
	     boolean[] used = new boolean[thisSize];
	     for (int clicki = 0; clicki < thisSize; clicki++) {
	        used[clicki] = false;
	     }

    	 for(int i =0; i < thisSize; i++) {
 			String mostFreqSemanticTriple = null; // this will define the semantics of the cluster
    		 int[] curCluster = (int[])curSemanticClusterList.get(i);
    	      HashSet curClusterSet = new HashSet();
    	      int csize = curCluster.length;
    	      log.debug("--- Current cluster list " + i + "---");
    	      Hashtable clusterSemanticTable = new Hashtable();
    	      for (int j = 0; j < csize; j++) {
    	        int intPair1 = curCluster[j];
    	        String curPair = (String)conceptRevTable.get(intPair1);
    	        for (int k = 0; k < csize; k++) {
    	        	if(j != k) {
    	        		int intPair2 = curCluster[k];
    	        		String nextPair = (String)conceptRevTable.get(intPair2);
    	        		String[] comp1 = curPair.split("\\|");
    	        		String[] comp2 = nextPair.split("\\|");
    	        		String subj = comp1[0].trim();
    	        		String subjST = comp1[1].trim();
    	        		Integer subjIndex = (Integer)conceptTable.get(subj + "|" + subjST);
    	        		String obj = comp2[0].trim();
    	        		String objST = comp2[1].trim();
    	        		Integer objIndex = (Integer)conceptTable.get(obj + "|" + objST);
    	        		if(relationTable[subjIndex][objIndex]) {
    	        			String subjSG = findSemanticGroup(subjST);
    	        			String objSG = findSemanticGroup(objST);
    	        			String subjObj = new String (subj + "|" + subjST + "-" + obj + "|" + objST);
    	        			log.debug("Subject and Object pair = " + subjObj);
    	        			Hashtable hTable = (Hashtable) findPredInfoTable.get(subjObj);
    	        			boolean pairReversed = false;
    	        			if(hTable == null) {
    	        				subjObj = new String (obj + "|" + objST + "-" + subj + "|" + subjST);
    	        				log.debug("****************** reverse of subj and obj tried: " + subjObj);
    	        				hTable = (Hashtable) findPredInfoTable.get(subjObj);
    	        				pairReversed = true;
    	        			}
    	        			Enumeration keys = hTable.keys();
    	        			Integer mostFreqNum = 0;
    	        			Integer thisNum = 0;
    	        			String mostFreqPred = null;
    	        			while(keys.hasMoreElements()) {
    	        				String nextKey = (String) keys.nextElement();
    	        				thisNum = (Integer) hTable.get(nextKey);
    	        				// if(!nextKey.equals("ISA") && thisNum > mostFreqNum) {
    	        				if(thisNum > mostFreqNum) {
    	        					mostFreqPred = nextKey; // replace the most frequentpredicate
    	        					mostFreqNum = thisNum; // replace the number
    	        				}
    	        			} // find most frequent predicate having the same Subject and Object
    	        			if(mostFreqPred != null) {
    	        				String predSG = (String) SemanticGroups.getPredicateType(mostFreqPred);
    	        				String semanticTriple = null;
    	        				String realPredicate = null;
    	        				if(pairReversed == false) {
    	        					semanticTriple = new String(subjSG + "-" + predSG + "-" + objSG);
    	        					realPredicate = new String(subj + "|" + subjST + "-" + mostFreqPred + "-" + obj + "|" + objST);
    	        					log.debug("semantic triple : " + semanticTriple);
    	        					// log.debug("\t real predicate : " + realPredicate);
    	        				}
    	        				else {
    	        					semanticTriple = new String(objSG + "-" + predSG + "-" + subjSG);
    	        					realPredicate = new String(obj + "|" + objST + "-" + mostFreqPred + "-" + subj + "|" + subjST);
    	        					log.debug("*** reversed semantic triple : " + semanticTriple);
    	        					// log.debug("\t real predicate : " + realPredicate);
    	        				}
    	        				Integer semanticFreq = (Integer) clusterSemanticTable.remove(semanticTriple);
    	        				if(semanticFreq == null) { // if semantic triple occurs first
    	        					clusterSemanticTable.put(semanticTriple, mostFreqNum);
    	        					log.debug("semantic triple : " + realPredicate + ", frequency : " + mostFreqNum);
    	        				} else {
    	        					Integer accumSemanticFreq = semanticFreq + mostFreqNum;
    	        					clusterSemanticTable.put(semanticTriple, accumSemanticFreq); // add up the semantic frequence
    	        					log.debug("semantic triple : " + realPredicate + ", frequency : " + mostFreqNum);
    	        				}
    	        			}
    	        		} // if
    	        	} // for
    	        } // for
    			Enumeration keys = clusterSemanticTable.keys();
    			Integer mostFreqNum = 0;
    			Integer thisNum = 0;
    			log.debug("----- Final semantic triple search -----");
    			while(keys.hasMoreElements()) {
    				String nextKey = (String) keys.nextElement();
    				thisNum = (Integer) clusterSemanticTable.get(nextKey);
    				log.debug("Final semantic triple : " + nextKey + " , " + thisNum);
    				if(thisNum > mostFreqNum) {
    					mostFreqSemanticTriple = nextKey; // replace the most frequentpredicate
    					mostFreqNum = thisNum; // replace the number
    				}
    			} // find most frequent semantic triple in the cluster
    	      } // for


    	      log.debug("****   " + i + "th semantic triple = " + mostFreqSemanticTriple);
    	      HashSet sameMeaningClusters = (HashSet) semanticMeaningTable.get(mostFreqSemanticTriple);
    	      if(sameMeaningClusters == null) { // There is no cluster having the same semantic meaning yet
    	    	  sameMeaningClusters = new HashSet();
    	    	  sameMeaningClusters.add(i); // Add cluster i in a new set
    	    	  semanticMeaningTable.put(mostFreqSemanticTriple, sameMeaningClusters); // add the singleton set to the hashtable
    	    	  semanticMeaningVector.add(mostFreqSemanticTriple);
    	      } else { // Found other cluster having the same meaning
    	    	  Iterator hi = sameMeaningClusters.iterator();
    	    	  String iList = new String();
    	    	  while(hi.hasNext()) {
    	    		  Integer curI = (Integer) hi.next();
    	    		  iList = iList + "," + curI;
    	    	  }
    	    	  sameMeaningClusters.add(i);
    	    	  // used[i] = true;
    	    	  // log.debug(mostFreqSemanticTriple + "---" +   iList  + " and " + i + "-th semantic cluster grouped");
    	    	  // log.debug(mostFreqSemanticTriple + "---" + i + "-th semantic cluster added");
    	    	  //log.debug(i + "-th semantic cluster added");
    	      }
    	 } // End of each step of calculation of semantic clustering

    	 Enumeration semanticValues = semanticMeaningTable.elements();
		 boolean thisClusterChanged = false;
    	 while(semanticValues.hasMoreElements()) {
    		 HashSet sameMeaningSet = (HashSet) semanticValues.nextElement();
    		 if(sameMeaningSet.size() > 1) { // If there is more than one clusters having the same semantic meaning, merge it semantically
    			 Iterator hi = sameMeaningSet.iterator();
    			 Integer curI = -1;
    			 Integer prevI = -1;
    			 int[] prevCluster = null;
    			 int[] newCluster = null;
    			 int[] curCluster = null;
    			 while(hi.hasNext()) {
    				 prevI = curI;
    				 curI = (Integer)hi.next();
    				 used[curI] = true;
    				 // log.debug("cluster " + curI + " is ");
					 curCluster = (int[])curSemanticClusterList.get(curI);
    				 if( prevCluster != null) {
    					 newCluster = mergeCluster(prevCluster, curCluster);
    					 prevCluster = newCluster;
    					 // log.debug("cluster " + prevI + " and " + curI + " is merged");
    					 // nextSemanticClusterList.add(newCluster);
    				 } else
    					 prevCluster = curCluster;
    			 }
				 nextSemanticClusterList.add(newCluster);
				 thisClusterChanged = true;
    		 }
    	 }
    	 // Copy the untouched clusters to the next semenatic clusters set
         for (int j = 0; j < thisSize; j++) {
             if (used[j] == false) {
            	 nextSemanticClusterList.add(curSemanticClusterList.get(j));
            	 // log.debug("cluster " + j+ " is not changed and added as is");
             }
         }

    	 semanticClusterChanged = thisClusterChanged;
    	 if(semanticClusterChanged) { // If the clusters are changed with semantic clustering
    		 // log.debug("Semantic cluster changed and the current one is added!");
    		 semanticClusterResult.add(nextSemanticClusterList);
    		 semanticClusterIndex++;
    		 curSemanticClusterList = null;
    		 curSemanticClusterList = nextSemanticClusterList;
    		 // log.debug("Semantic Cluster Index = " + semanticClusterIndex);
    	 }
    }
    // End of semantic clustering

    // ArrayList  finalClusterList = (ArrayList) clusterResult.get(clusterListIndex);
    ArrayList  finalClusterList = (ArrayList) semanticClusterResult.get(semanticClusterIndex);
    ArrayList finalClusterList2 = new ArrayList();
    int clsize = finalClusterList.size();
    log.debug("Final # of clusters = " + clsize);
    // log.debug(" ********************************************************** ");
    // log.debug(" ***********Finally survived predications ***************** ");
    for (int i = 0; i < clsize; i++) {
      int[] curCluster = (int[])finalClusterList.get(i);
      HashSet curClusterSet = new HashSet();
      int csize = curCluster.length;
      // log.debug("--- Current cluster list " + i + "---");
      for (int j = 0; j < csize; j++) {
        int intPair1 = curCluster[j];
        String curPair = (String)conceptRevTable.get(intPair1);
        for (int k = 0; k < csize; k++) {
        	if(j != k) {
        	int intPair2 = curCluster[k];
        	String nextPair = (String)conceptRevTable.get(intPair2);
        	String[] comp1 = curPair.split("\\|");
        	String[] comp2 = nextPair.split("\\|");
        	String subj = comp1[0].trim();
        	String subjST = comp1[1].trim();
        	Integer subjIndex = (Integer)conceptTable.get(subj + "|" + subjST);
        	String obj = comp2[0].trim();
        	String objST = comp2[1].trim();
        	Integer objIndex = (Integer)conceptTable.get(obj + "|" + objST);
        	if(relationTable[subjIndex][objIndex]) {
        		APredicationWOR ap1 = new APredicationWOR(subj, subjST, obj, objST);
        		// log.debug(ap1.toString());
        		// // for (int l = 0; l < filteredPredications.size(); l++) {
        		HashSet curSet = (HashSet)predtable.get(ap1);
        		if(curSet != null)
        			curClusterSet.addAll(curSet);
            } else {
            	// log.debug("***Eliminate the predicates : " + subjST + " - " + objST);
            }

        	}
        }
      }
      finalClusterList2.add(curClusterSet);
    }
    // log.debug(" ***********End of Finally survived predications ***************** ");
    ArrayList finalClusterList3 = checkSubsume(finalClusterList2);


    // String queryString = "select distinct sp from SentencePredication as sp inner join fetch sp.sentence s inner join fetch sp.predication p inner join fetch p.predicationArgumentSet pa where sp.sentencePredicationId in (:ids)";

	String queryString = "select distinct sp from SentencePredication as sp "
		+ "inner join fetch sp.sentence s "
		+ "inner join fetch sp.predication p "
		+ "inner join fetch p.predicationArgumentSet pa "
		+ "where sp.sentencePredicationId in (:ids)";
    session.setAttribute("zsummaryPredications", finalClusterList3);
	Configuration configuration = new Configuration().configure();
	// configuration.setProperty("hibernate.connection.url", "jdbc:mysql://indlx3:3306/semmed2006test");
	configuration.setProperty("hibernate.connection.datasource", "java:/comp/env/jdbc/SemMedTestDB");
	SessionFactory sessionFactory = configuration.buildSessionFactory();
	Session hb_session = sessionFactory.openSession();
    // Session hb_session = HibernateSessionFactory.currentSession();
    hb_session.clear();
    Query q = hb_session.createQuery(queryString);
    q.setCacheable(true);
    ArrayList clusteredSPS = new ArrayList();

    for (int i = 0; i < finalClusterList3.size(); i++) {
      HashSet curClusterSet = (HashSet)finalClusterList3.get(i);
      ArrayList curClusterList = new ArrayList(curClusterSet);
      // APredicationList spCurCluster = new APredicationList(curClusterList);
      // log.debug("# of predications BEFORE EXPANSION in the cluster -" + i + " - = " + curClusterList.size());
      // List idsClusterList = spCurCluster.getSentencePredicationIDs();
      List expandedPredList = extractPredicationList(curClusterList, predications);
      // APredicationList spCurCluster = new APredicationList(expandedPredList);
      TestPredicationList spCurCluster = new TestPredicationList(expandedPredList);
      List idsClusterList = spCurCluster.getSentencePredicationIDs();

      q.setParameterList("ids", idsClusterList);
      // log.debug("idsString = " + idsClusterList.toString());
      SemrepResultSet srs = new SemrepResultSet(q.list());

      List sps = srs.getSentencePredications();
      List displayedPredications;
      /* if (sps.size() > LIMIT) {
        displayedPredications = new ArrayList(LIMIT);
        for (int j = 0; i < LIMIT; i++)
          displayedPredications.add((SentencePredication)sps.get(j));
        session.setAttribute("maxedPredications", Integer.valueOf(sps.size()));
      } else {
        displayedPredications = sps;
        session.removeAttribute("maxedPredications");
      } */
      displayedPredications = sps;
      session.removeAttribute("maxedPredications");
      // log.debug("# of predications AFTER EXPANSION in the cluster -" + i + "- = " + displayedPredications.size());
      clusteredSPS.add(displayedPredications);
    }

    // log.debug("Center concept = " +  centerConcept);
    graphXml = GraphUtils.parseZ(clusteredSPS, semanticMeaningVector, ConnectivityTable, concDegreeList,
      null, null, session.getServletContext(), false,
      centerConcept);
    } else {
    	ArrayList clusteredSPS = new ArrayList();
    	graphXml = GraphUtils.parseZ(clusteredSPS, new Vector(), ConnectivityTable, concDegreeList,
    		      null, null, session.getServletContext(), false,
    		      centerConcept);
    }
    XMLOutputter serializer = new XMLOutputter();
    String graphString = serializer.outputString(graphXml);
    log.debug("Graph XML in SummaryAction () : " + graphString);

    session.removeAttribute("predsource");
    session.removeAttribute("query");
    session.setAttribute("predsource", Integer.valueOf(1));
    UUID key = GraphServlet.addGraph(request, graphString, (List)
      session.getAttribute("relevantQuestionrCitations"), (List)session
      .getAttribute("relevantQuestionnrCitations"));

    session.setAttribute("cluster", "true");
    session.setAttribute("key", key.toString());
    return mapping.findForward("successz");
  }
}

