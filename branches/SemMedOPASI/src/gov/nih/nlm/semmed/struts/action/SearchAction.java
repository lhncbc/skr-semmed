//Created by MyEclipse Struts
// XSL source (default): platform:/plugin/com.genuitec.eclipse.cross.easystruts.eclipse_4.0.1/xslt/JavaClass.xsl

package gov.nih.nlm.semmed.struts.action;

import gov.nih.nlm.semmed.exception.EssieException;
import gov.nih.nlm.semmed.exception.PubmedException;
import gov.nih.nlm.semmed.exception.SemMedException;
import gov.nih.nlm.semmed.exception.XMLException;
import gov.nih.nlm.semmed.model.APredication;
import gov.nih.nlm.semmed.model.APredicationList;
import gov.nih.nlm.semmed.model.Query;
import gov.nih.nlm.semmed.model.SemMedDocument;
import gov.nih.nlm.semmed.struts.form.SearchForm;
import gov.nih.nlm.semmed.util.ArticleDataSource;
import gov.nih.nlm.semmed.util.XMLUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

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
 * MyEclipse Struts Creation date: 12-02-2005
 *
 * XDoclet definition:
 *
 * @struts.action path="/Search" name="SearchForm" input="/jsp/welcome.jsp"
 *                scope="request" validate="true"
 * @struts.action-forward name="success" path="/jsp/SearchResult.jsp"
 *                        contextRelative="true"
 * @struts.action-forward name="failure" path="/jsp/welcome.jsp"
 *                        contextRelative="true"
 */
public class SearchAction extends LookupDispatchAction {
	private static final String QUERY_PREFIX_TITLE = "SELECT PMID FROM TITLES WHERE PMID >= ";
	private static Log log = LogFactory.getLog(SearchAction.class);
	protected Map<String,String> getKeyMethodMap() {
	      Map<String,String> map = new HashMap<String,String>();
	      map.put("search.button.search", "search");
	      map.put("search.button.reset", "reset");
	      map.put("search.button.uploadcitation", "uploadCitation");
	      map.put("search.button.update", "update");
	      map.put("search.button.selectall", "selectAll");
	      map.put("search.button.unselectall", "unselectAll");
	      map.put("search.button.export", "exportToXml");
	      map.put("search.toggle", "toggle");
	      map.put("search.button.sampling", "sampling");
	      return map;
	  }


	@Override
	public ActionForward unspecified(ActionMapping mapping,
            ActionForm form,
            javax.servlet.http.HttpServletRequest request,
            javax.servlet.http.HttpServletResponse response)
	throws PubmedException, EssieException, SemMedException, Exception {
			return search(mapping,form,request,response);
	}

	public ActionForward toggleLimits(ActionMapping mapping,ActionForm form,
			HttpServletRequest request,HttpServletResponse response){

		SearchForm searchForm = (SearchForm) form;

		HttpSession session = request.getSession();
		String source = searchForm.getSelectedSource();
		Query q = (Query)session.getAttribute("query");
		String term = searchForm.getTerm().trim();

		if (q==null){
			q = new Query(term,source);
			q.setDefaultValues();
			session.setAttribute("query",q);
		}

		if ("Show".equals(request.getParameter("pubmedExtra")))
			term = processPubmedOptionsFromSession(term, request);
		else
			term = processPubmedOptionsFromUser(term, request);

		if ("Show".equals(request.getParameter("pubmedExtra"))){
			session.setAttribute("pubmedExtraOptions", new Object());
		}else
			session.removeAttribute("pubmedExtraOptions");

		return mapping.findForward("success");
	}

	/**
	 * Method search
	 *
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 *
	 *
	 * Sets the session attribute 'citlist' to a list of PubmedArticle
	 *
	 *
	 */
	public ActionForward search(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws PubmedException, EssieException, SemMedException
	{
		SearchForm searchForm = (SearchForm) form;

		long startAll = System.nanoTime();
		HttpSession session = request.getSession();
		// HLJ add PMID parameter check
		String tmpPMID = request.getParameter("PMID");
		long PMID = -1;
		if (tmpPMID != null)
		{
			try
			{
				PMID = Long.parseLong(request.getParameter("PMID"));
				ArticleDataSource ml = ArticleDataSource.getInstance(ArticleDataSource.SourceType.OPASI);
				ml.displayDocText(request,  response, PMID);
				return mapping.findForward("success");

			}
			catch (Exception e)
			{
				log.debug("PMID is not valid.");
				throw new PubmedException(e);
			}
		}

		Query q = (Query)session.getAttribute("query");

		session.removeAttribute("displayedCitationsMedline");
		session.removeAttribute("displayedCitationsClinicalTrials");
		if(session.getAttribute("selectedGrantRange") != null)
			session.removeAttribute("selectedGrantRange");
		if(session.getAttribute("selectedGrantSize") != null)
			session.removeAttribute("selectedGrantSize");
		// HLJ
		session.removeAttribute("displayedCitationsOPASI");

		// TODO HLJ when q can be null?
		if (request.getParameter("p")==null || q==null)
		{

			String source = searchForm.getSelectedSource();
			String sourceNames[];

			String term = searchForm.getTerm().trim();
			log.debug("search term: " + term);
			if ("medline".equals(source))
				sourceNames = new String[]{"Medline"};
			else if ("ctrials".equals(source))
				sourceNames = new String[]{"ClinicalTrials"};
			else if ("both".equals(source))
				sourceNames = new String[]{"ClinicalTrials","Medline"};
			else if ("opasi".equals(source))
				sourceNames = new String[]{"OPASI"};
			else
				sourceNames = new String[0];

			// TODO HLJ again, when q can be null?
			if (q==null){
				q = new Query(term,source);
				q.setDefaultValues();
				session.setAttribute("query",q);
			}

			for(String sourceName : sourceNames){

				ArticleDataSource.SourceType sourceType = null;
				String startDate = null;
				String endDate = null;
				String citCount = null;

				if ("Medline".equals(sourceName)){
					sourceType = ArticleDataSource.SourceType.MEDLINE;
					startDate = searchForm.getStartDate();
					endDate = searchForm.getEndDate();
					citCount = searchForm.getSelectedCitCount();
					q.setOption("pubmedStartDate", startDate);
					q.setOption("pubmedEndDate", endDate);
					q.setOption("pubmedMax", citCount);

					if (session.getAttribute("pubmedExtraOptions")!=null)
						term = processPubmedOptionsFromUser(term, request);
					else
						term = processPubmedOptionsFromSession(term, request);

				}else if("ClinicalTrials".equals(sourceName)){
					sourceType = ArticleDataSource.SourceType.CLINICAL_TRIALS;
					citCount = searchForm.getSelectedMaxRank();
					q.setOption("clinicalTrialMax", citCount);
				} else if("OPASI".equals(sourceName)){
					sourceType = ArticleDataSource.SourceType.OPASI;
					citCount = searchForm.getSelectedMaxRank();
					q.setOption("OPASIMax", citCount);
					q.setTerm(term);
				}

				List<? extends SemMedDocument> displayedCitations = null;

				ArticleDataSource ml = null;
				int[] ids = null;
				try{
					if ("OPASI".equals(sourceName))
					{
						ml = ArticleDataSource.getInstance(sourceType);
						ids = ml.search(session,term, startDate, endDate, citCount.equals("Inf")?0:Integer.parseInt(citCount));
						displayedCitations = ml.fetch(session,0);
						session.setAttribute("count"+sourceName, ids.length);
						// log.debug("The number of search result = " + ids.length);
					}
					else
					{
						ml = ArticleDataSource.getInstance(sourceType);
						ids = ml.search(session,term, startDate, endDate, citCount.equals("Inf")?0:Integer.parseInt(citCount));
						displayedCitations = ml.fetch(session,0);
						session.setAttribute("count"+sourceName, ids.length);
					}
				}
				catch(Exception e)
				{
					throw new PubmedException(e);
				}
				session.setAttribute("displayedCitations", new Object());
				session.setAttribute("displayedCitations"+sourceName, displayedCitations);
				session.setAttribute("citationIDs"+sourceName, ids);
				session.setAttribute("pageNumber"+sourceName, 0);
				session.setAttribute("currentSource", sourceName);

			}

			session.removeAttribute("predications");
			session.removeAttribute("summaryPredications");
			session.removeAttribute("relevantQuestionrPredications");
			session.removeAttribute("relevantQuestionrCitations");
			session.removeAttribute("relevantQuestionnrPredications");
			session.removeAttribute("relevantQuestionnrCitations");
			session.removeAttribute("key");

		}
		else
		{

			String sourceName = request.getParameter("s");

			int page;

			try
			{
				page = Integer.parseInt(request.getParameter("p"));
			}
			catch(Exception e)
			{
				page = 0;
			}

			ArticleDataSource.SourceType sourceType = null;

			if ("Medline".equals(sourceName))
				sourceType = ArticleDataSource.SourceType.MEDLINE;
			else if("ClinicalTrials".equals(sourceName))
				sourceType = ArticleDataSource.SourceType.CLINICAL_TRIALS;
			else if("OPASI".equals(sourceName))
				sourceType = ArticleDataSource.SourceType.OPASI;
			else
				return mapping.findForward("failure");


			List<? extends SemMedDocument> displayedCitations = null;
			try
			{
				// Start from second page
				displayedCitations = ArticleDataSource.getInstance(sourceType).fetch(session,page);
			}
			catch(Exception e)
			{
				throw new PubmedException(e);
			}
			session.setAttribute("displayedCitations"+sourceName, displayedCitations);
			session.setAttribute("pageNumber"+sourceName, page);
			session.setAttribute("currentSource", sourceName);
		}

		session.setAttribute("executionTime", System.nanoTime()-startAll);
		// log.debug("User name = " + request.getRemoteUser());
		// if(request.isUserInRole("OPASIUser")) {			// If the user is OPASI power user, do the work for semrep tab shown to the power user
			log.debug("User belongs to OPASIUser");
			String sourceNames[] = new String[]{"Medline","ClinicalTrials", "OPASI"};
			int page = 0;
			// if (request.getParameter("p")==null){
				List<APredication> allPredications = null;
				try {
						for(String sourceName : sourceNames) {
							if (session.getAttribute("citationIDs"+sourceName)==null)
								continue;

							ArticleDataSource.SourceType type = null;
							if ("Medline".equals(sourceName))
								type = ArticleDataSource.SourceType.MEDLINE;
							else if ("ClinicalTrials".equals(sourceName))
								type = ArticleDataSource.SourceType.CLINICAL_TRIALS;
							else if ("OPASI".equals(sourceName))
								type = ArticleDataSource.SourceType.OPASI;

							// log.debug("Source name = " + sourceName);
							int[] ids = (int[])session.getAttribute("citationIDs"+sourceName);
							List<APredication> predications = new APredicationList(ids,type);
							if (allPredications==null)
								allPredications = predications;
							else
								allPredications.addAll(predications);
						} // for
					// } // else
				}catch(Exception e){
					e.printStackTrace();
					throw new SemMedException(e);
				}
				session.setAttribute("predications",allPredications);
				session.setAttribute("predsource", 1);
				session.setAttribute("pageNumberSemrep", 0);
				session.removeAttribute("summaryPredications");
			/* }else{

				try{
					page = Integer.parseInt(request.getParameter("p"));
				}catch(Exception e){
					page = 0;
				}
			} */

			List<APredication> predications = (APredicationList)session.getAttribute("predications");
			// log.debug("The number of predication = " + predications.size());

			int[] PIDs = new int[Math.min(20, predications.size()-page*20)];
			int[] SIDs = new int[PIDs.length];
			ArticleDataSource.SourceType[] sources = new ArticleDataSource.SourceType[PIDs.length];

			for(int i=0;i<PIDs.length;i++){
				PIDs[i] = predications.get(i+page*20).PID;
				SIDs[i] = predications.get(i+page*20).SID;
				sources[i] = predications.get(i+page*20).source;
				// log.debug("PIDs[" + i + "] =" + PIDs[i]);
				// log.debug("SIDs[" + i + "] =" + SIDs[i]);
				// log.debug("sources[" + i + "] =" + sources[i]);
			}
			// log.debug("PID length = " + PIDs.length);

			try{
				// if (session.getAttribute("grantLow") == null)
				List<APredication> displayedPredications = new APredicationList(PIDs,SIDs,sources);
				session.setAttribute("displayedPredications",displayedPredications);
				// log.debug("The number of displayed predication = " + displayedPredications.size());
				// else
				//	session.setAttribute("displayedPredications",new APredicationList(PIDs,SIDs,null));
			}catch(Exception e){
				e.printStackTrace();
				throw new SemMedException(e);
			}
			session.setAttribute("pageNumberSemrep", page);
			// if (session.getAttribute("predications") != null) {
				String predType = (String)session.getAttribute("predicationType");
				if (session.getAttribute("summaryType") == null) {
					if ("genetic".equals(predType)) session.setAttribute("summaryType", "genetic");
					else session.setAttribute("summaryType", "treatment");
				}

				APredicationList preds = (APredicationList)session.getAttribute("predications");
				List<LabelValueBean> relevantConcepts = null;
				String summaryType = (String)session.getAttribute("summaryType");
				if (summaryType.equals("diagnosis"))
					relevantConcepts = preds.getRelevantConcepts(APredicationList.Type.DIAGNOSIS);
				else if (summaryType.equals("pharmacogenomics"))
					relevantConcepts = preds.getRelevantConcepts(APredicationList.Type.PHARMACOGENOMICS);
				else if (summaryType.equals("genetic"))
					relevantConcepts = preds.getRelevantConcepts(APredicationList.Type.GENETIC);
				else if (summaryType.equals("interaction"))
					relevantConcepts = preds.getRelevantConcepts(APredicationList.Type.INTERATCION);
				else
					relevantConcepts = preds.getRelevantConcepts(APredicationList.Type.TREATMENT);

				if (relevantConcepts != null && relevantConcepts.size()>0) {
					session.setAttribute("relevantConcs", relevantConcepts);
					// log.debug("Relevant Conc Size:" + relevantConcepts.size());
					session.setAttribute("selectedSeed", ((LabelValueBean)relevantConcepts.get(0)).getLabel());
				}
			// }
			session.setAttribute("executionTime", System.nanoTime()-startAll);
		// } else if(request.isUserInRole("OPASITester"))
		//	log.debug("User belongs to OPASITester");
		// else
		//	log.debug("User does not belong to OPASIUser, nor OPASITester");
		return mapping.findForward("success");
	}

	/**
	 * Method reset
	 *
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 *
	 *
	 * This method seems to perform the same as InitializeSearchAction [Alejandro]
	 */
	public ActionForward reset(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) {
		HttpSession session = request.getSession();

		session.removeAttribute("citationIDsMedline");
		session.removeAttribute("citationIDsClinicalTrials");
		session.removeAttribute("displayedCitations");
		// TODO HLJ maybe change to a more generic name such as "SearchResult"
		session.removeAttribute("OpasiSearchResult");
		session.removeAttribute("citationIDsOPASI");
		session.removeAttribute("displayedCitationsMedline");
		session.removeAttribute("displayedCitationsClinicalTrials");
		session.removeAttribute("displayedCitationsOPASI"); // HLJ
		session.removeAttribute("countMedline");
		session.removeAttribute("countClinicalTrials");
		session.removeAttribute("predications");
		session.removeAttribute("predicationType");
		session.removeAttribute("relevantConcs");
		session.removeAttribute("saliency");
		session.removeAttribute("selectedSaliencyType");
		session.removeAttribute("summaryPredications");
		session.removeAttribute("selectedSeed");
		session.removeAttribute("summaryType");
		session.removeAttribute("lang");
		session.removeAttribute("translateSelectedSeed");
		session.removeAttribute("pubmedExtraOptions");


		// HLJ Query q = new Query("","medline");
		Query q = new Query("","opasi");
	    q.setDefaultValues();

	    session.setAttribute("query",q);

		return mapping.findForward("success");
	}

	public ActionForward sampling(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws PubmedException, EssieException, SemMedException {
			HttpSession session = request.getSession();
			SearchForm searchForm = (SearchForm) form;
			if(session.getAttribute("countOPASI") != null)
				session.removeAttribute("countOPASI");

			// Get sampling for OPASI grants whose id is greater than low and smaller than high
			// int low = Integer.parseInt(request.getParameter("SelectedGrantRange"));
			// int size = Integer.parseInt(request.getParameter("SelectedGrantSize"));
			if (request.getParameter("p")==null)
			{
				int low = Integer.parseInt(searchForm.getSelectedGrantRange());
				int size = Integer.parseInt(searchForm.getSelectedGrantSize());
				session.setAttribute("selectedGrantRange", searchForm.getSelectedGrantRange());
				session.setAttribute("selectedGrantSize", searchForm.getSelectedGrantSize());
				// log.debug("In sampling: low =" + low + ", size = " + size);
				try {
					Context ctx = new InitialContext();
					DataSource ds =
						(DataSource)ctx.lookup("java:comp/env/jdbc/SemMedDB");
					ServletContext context = getServlet().getServletContext();
					List<LabelValueBean> grantList = (List<LabelValueBean>) context.getAttribute("grantRange");
					ArticleDataSource ml = ArticleDataSource.getInstance(ArticleDataSource.SourceType.OPASI);
					int tempHigh = 0;
					Iterator iter = grantList.iterator();
					while (iter.hasNext()) {
						LabelValueBean e = (LabelValueBean)iter.next();
						int grantNumber = Integer.parseInt(e.getValue());
						if(grantNumber > low) {
							tempHigh = grantNumber;
							break;
						}
					}
					StringBuffer query = new StringBuffer(QUERY_PREFIX_TITLE);
					if (tempHigh >= low)
						query.append(low + " and PMID <= " + tempHigh);
					else
						query.append(low);

					// log.debug("DB query = " + query.toString());
					Connection con = ds.getConnection();
					Statement s = con.createStatement();
					ResultSet rs = s.executeQuery(query.toString());
					// ArrayList<Integer> PMIDList = new ArrayList<Integer>();
					int[] newList = new int[size];
					int i = 0;
					while (rs.next()){
						if(i < size)
							newList[i] = (rs.getInt(1));
						else
							break;
						i++;
					}
					// log.debug("Size of the retrieved citation = " + size);
					session.setAttribute("countOPASI", size);
					List<? extends SemMedDocument> displayedCitations = null; // TODO: DW displaying citations for sampling, not from Essie
					session.setAttribute("citationIDsOPASI", newList);
					session.setAttribute("displayedCitations", new Object());
					session.setAttribute("displayedCitationsOPASI", displayedCitations);
					session.setAttribute("pageNumberOPASI", 0);
					session.setAttribute("currentSource", "OPASI");
					if(session.getAttribute("query") != null) // If there is a previous search query, remove it, since it doesn't belong to sampling
						session.removeAttribute("query");

					Query	q = new Query("","OPASI");
					q.setDefaultValues();
					session.setAttribute("query",q);
				} catch(Exception e){
					e.printStackTrace();
					throw new SemMedException(e);
				}
			} else {
				// String sourceName = request.getParameter("s");
				int page;
				try
				{
					page = Integer.parseInt(request.getParameter("p"));
				}
				catch(Exception e)
				{
					page = 0;
				}
				List<? extends SemMedDocument> displayedCitations = null;
				try
				{
					// Start from second page
					displayedCitations = ArticleDataSource.getInstance(ArticleDataSource.SourceType.OPASI).fetch(session,page);
				}
				catch(Exception e)
				{
					throw new PubmedException(e);
				}
				session.setAttribute("displayedCitationsOPASI", displayedCitations);
				session.setAttribute("pageNumberOPASI", page);
			}

			if(request.isUserInRole("OPASIUser")) {			// If the user is OPASI power user, do the work for semrep tab shown to the power user
				String sourceNames[] = new String[]{"OPASI"};
				int page = 0;
				// if (request.getParameter("p")==null){
					List<APredication> allPredications = null;
					try {
							for(String sourceName : sourceNames) {
								if (session.getAttribute("citationIDs"+sourceName)==null)
									continue;

								ArticleDataSource.SourceType type = null;
								if ("Medline".equals(sourceName))
									type = ArticleDataSource.SourceType.MEDLINE;
								else if ("ClinicalTrials".equals(sourceName))
									type = ArticleDataSource.SourceType.CLINICAL_TRIALS;
								else if ("OPASI".equals(sourceName))
									type = ArticleDataSource.SourceType.OPASI;

								// log.debug("Source name = " + sourceName);
								int[] ids = (int[])session.getAttribute("citationIDs"+sourceName);
								List<APredication> predications = new APredicationList(ids,type);
								if (allPredications==null)
									allPredications = predications;
								else
									allPredications.addAll(predications);
							} // for
						// } // else
					}catch(Exception e){
						e.printStackTrace();
						throw new SemMedException(e);
					}
					session.setAttribute("predications",allPredications);
					session.setAttribute("predsource", 1);
					session.setAttribute("pageNumberSemrep", 0);
					session.removeAttribute("summaryPredications");
				/* }else{

					try{
						page = Integer.parseInt(request.getParameter("p"));
					}catch(Exception e){
						page = 0;
					}
				} */

				List<APredication> predications = (APredicationList)session.getAttribute("predications");
				// log.debug("The number of predication = " + predications.size());

				int[] PIDs = new int[Math.min(20, predications.size()-page*20)];
				int[] SIDs = new int[PIDs.length];
				ArticleDataSource.SourceType[] sources = new ArticleDataSource.SourceType[PIDs.length];

				for(int i=0;i<PIDs.length;i++){
					PIDs[i] = predications.get(i+page*20).PID;
					SIDs[i] = predications.get(i+page*20).SID;
					sources[i] = predications.get(i+page*20).source;
					// log.debug("PIDs[" + i + "] =" + PIDs[i]);
					// log.debug("SIDs[" + i + "] =" + SIDs[i]);
					// log.debug("sources[" + i + "] =" + sources[i]);
				}
				// log.debug("PID length = " + PIDs.length);

				try{
					// if (session.getAttribute("grantLow") == null)
					List<APredication> displayedPredications = new APredicationList(PIDs,SIDs,sources);
					session.setAttribute("displayedPredications",displayedPredications);
					// log.debug("The number of displayed predication = " + displayedPredications.size());
					// else
					//	session.setAttribute("displayedPredications",new APredicationList(PIDs,SIDs,null));
				}catch(Exception e){
					e.printStackTrace();
					throw new SemMedException(e);
				}
				session.setAttribute("pageNumberSemrep", page);
				// if (session.getAttribute("predications") != null) {
					String predType = (String)session.getAttribute("predicationType");
					if (session.getAttribute("summaryType") == null) {
						if ("genetic".equals(predType)) session.setAttribute("summaryType", "genetic");
						else session.setAttribute("summaryType", "treatment");
					}

					APredicationList preds = (APredicationList)session.getAttribute("predications");
					List<LabelValueBean> relevantConcepts = null;
					String summaryType = (String)session.getAttribute("summaryType");
					if (summaryType.equals("diagnosis"))
						relevantConcepts = preds.getRelevantConcepts(APredicationList.Type.DIAGNOSIS);
					else if (summaryType.equals("pharmacogenomics"))
						relevantConcepts = preds.getRelevantConcepts(APredicationList.Type.PHARMACOGENOMICS);
					else if (summaryType.equals("genetic"))
						relevantConcepts = preds.getRelevantConcepts(APredicationList.Type.GENETIC);
					else if (summaryType.equals("interaction"))
						relevantConcepts = preds.getRelevantConcepts(APredicationList.Type.INTERATCION);
					else
						relevantConcepts = preds.getRelevantConcepts(APredicationList.Type.TREATMENT);

					if (relevantConcepts != null && relevantConcepts.size()>0) {
						session.setAttribute("relevantConcs", relevantConcepts);
						// log.debug("Relevant Conc Size:" + relevantConcepts.size());
						session.setAttribute("selectedSeed", ((LabelValueBean)relevantConcepts.get(0)).getLabel());
					}
			}
			return mapping.findForward("success");
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
	public ActionForward exportToXml(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws XMLException {
		HttpSession session = request.getSession();
        try {
			Document doc = XMLUtils.exportToXml(session); //TODO [Alejandro] This method will not work since the session
														  //does not hold all the information about the citations
			response.setContentType("application/octet-stream");
			response.setHeader("Content-disposition", "attachment;filename=search-results.xml");
			Writer out = new OutputStreamWriter(response.getOutputStream(),"UTF8");
    		XMLOutputter serializer = new XMLOutputter(Format.getPrettyFormat());
    		serializer.output(doc,out);
        } catch (Exception e) {
        	e.printStackTrace();
        	throw new XMLException(e);
        }
        return null;
	}


	private List<String> getAuthorList(HttpServletRequest request){
		List<String> authors = new ArrayList<String>();

		Enumeration enumeration = request.getParameterNames();
		while(enumeration.hasMoreElements()){
			String s = (String)enumeration.nextElement();
			if (s.startsWith("author_") && s.length()>7 &&
					request.getParameter(s).trim().length()>0)
				authors.add(request.getParameter(s).trim());
		}
		return authors;
	}

	private List<String> getJournalList(HttpServletRequest request){
		List<String> journals = new ArrayList<String>();

		Enumeration enumeration = request.getParameterNames();
		while(enumeration.hasMoreElements()){
			String s = (String)enumeration.nextElement();
			if (s.startsWith("journal_") && s.length()>8 &&
					request.getParameter(s).trim().length()>0){
				String journal = request.getParameter(s).trim();
				if (journal.startsWith("\""))
					journals.add(journal);
				else
					journals.add("\""+journal+"\"");
			}
		}
		return journals;
	}

	@SuppressWarnings("unchecked")
	private String processPubmedOptionsFromSession(String term, HttpServletRequest request){
		StringBuffer sb = new StringBuffer();

		Query q = (Query)request.getSession().getAttribute("query");

		if (term.length()>0 && !hasMatchedParenthesis(term)){
			sb.append("(");
			sb.append(term);
			sb.append(")");
		}else
			sb.append(term);

		//////////////////////////////////////////////
		//authors
		List<String> authors = (List<String>)q.getOptions().get("authors");

		addAuthors(authors,sb,q,request);

		List<String> journals = (List<String>)q.getOptions().get("journals");

		addJournals(journals,sb,q,request);


		q.setTerm(sb.toString());

		///////////////////////////////////////////////
		//full text, free text, abstract
		if (q.getOption("fullText")!=null && ((Boolean)q.getOption("fullText")).booleanValue()){
			if (sb.length()>0)
				sb.append(" AND ");
			sb.append("full text[sb]");
		}
		if (q.getOption("freeFullText")!=null && ((Boolean)q.getOption("freeFullText")).booleanValue()){
			if (sb.length()>0)
				sb.append(" AND ");
			sb.append("free full text[sb]");
		}
		if (q.getOption("hasAbstract")!=null && ((Boolean)q.getOption("hasAbstract")).booleanValue()){
			if (sb.length()>0)
				sb.append(" AND ");
			sb.append("hasabstract[text]");
		}

		///////////////////////////////////////////////
		//human, animals
		if ((q.getOption("humans")!=null && q.getOption("animals")!=null) &&
				(((Boolean)q.getOption("humans")).booleanValue() && ((Boolean)q.getOption("animals")).booleanValue())){
			if (sb.length()>0)
				sb.append(" AND ");
			sb.append("(Humans[Mesh] OR Animals[Mesh:noexp])");
		}else if (q.getOption("humans")!=null && ((Boolean)q.getOption("humans")).booleanValue()){
			if (sb.length()>0)
				sb.append(" AND ");
			sb.append("(Humans[Mesh])");
		}else if (q.getOption("animals")!=null && ((Boolean)q.getOption("animals")).booleanValue()){
			if (sb.length()>0)
				sb.append(" AND ");
			sb.append("(Animals[Mesh:noexp])");
		}

		///////////////////////////////////////////////
		//male, female
		if ((q.getOption("male")!=null && q.getOption("female")!=null) &&
				(((Boolean)q.getOption("male")).booleanValue() &&
				((Boolean)q.getOption("female")).booleanValue())){
			if (sb.length()>0)
				sb.append(" AND ");
			sb.append("(Male[MeSH Terms] OR Female[MeSH Terms])");
		}else if (q.getOption("male")!=null && ((Boolean)q.getOption("male")).booleanValue()){
			if (sb.length()>0)
				sb.append(" AND ");
			sb.append("(Male[MeSH Terms])");
		}else if (q.getOption("female")!=null && ((Boolean)q.getOption("female")).booleanValue()){
			if (sb.length()>0)
				sb.append(" AND ");
			sb.append("(Female[MeSH Terms])");
		}

		List<String> languages = (List<String>)q.getOption("languages");
		List<String> journalSets = (List<String>)q.getOption("journalSets");
		List<String> topicSets = (List<String>)q.getOption("topicSets");
		List<String> publications = (List<String>)q.getOption("publications");
		List<String> ages = (List<String>)q.getOption("ages");

		///////////////////////////////////////////////
		//languages
		addLanguages(languages,sb,q);

		///////////////////////////////////////////////
		//journal subsets
		addJournalSets(journalSets,sb,q);


		///////////////////////////////////////////////
		//topic subsets
		addTopicSets(topicSets,sb,q);


		///////////////////////////////////////////////
		//publication types
		addPublications(publications,sb,q);


		///////////////////////////////////////////////
		//ages
		addAges(ages,sb,q);


		return sb.toString();
	}

	private void addAuthors(List<String> authors,StringBuffer sb, Query q,HttpServletRequest request){
		if (authors!=null && authors.size()>0){
			String connector = " "+request.getParameter("pmfilter_AuthOp")+" ";

			if (sb.length()>0)
				sb.append(" AND ");

			sb.append("(");
			sb.append(authors.get(0));
			sb.append("[Auth]");

			for(int i=1;i<authors.size();i++){
				sb.append(connector);
				sb.append(authors.get(i));
				sb.append("[Auth]");
			}
			sb.append(")");
		}
		q.setOption("authors", authors);
	}

	private void addAges(List<String> ages,StringBuffer sb,Query q){
		if (ages!=null && ages.size()>0){
			if (sb.length()>0)
				sb.append(" AND (");
			sb.append(ages.get(0));

			for(int i=1;i<ages.size();i++){
				sb.append(" OR ");
				sb.append(ages.get(i));
			}
			sb.append(")");
		}
		q.setOption("ages", ages);
	}

	private void addJournals(List<String> journals,StringBuffer sb, Query q, HttpServletRequest request){
		if (journals!=null && journals.size()>0){
			if (sb.length()>0)
				sb.append(" AND ");

			sb.append("(");
			sb.append(journals.get(0));
			sb.append("[Jour]");

			for(int i=1;i<journals.size();i++){
				sb.append(" OR ");
				sb.append(journals.get(i));
				sb.append("[Jour]");
			}

			sb.append(")");
		}
		q.setOption("journals", journals);
	}

	private void addPublications(List<String> publications,StringBuffer sb,Query q){
		if (publications!=null && publications.size()>0){
			if (sb.length()>0)
				sb.append(" AND (");
			sb.append(publications.get(0));
			sb.append("[ptyp]");

			for(int i=1;i<publications.size();i++){
				sb.append(" OR ");
				sb.append(publications.get(i));
				sb.append("[ptyp]");
			}
			sb.append(")");
		}
		q.setOption("publications", publications);
	}

	private void addLanguages(List<String> languages,StringBuffer sb,Query q){
		if (languages!=null && languages.size()>0){
			if (sb.length()>0)
				sb.append(" AND (");
			sb.append(languages.get(0));
			sb.append("[lang]");

			for(int i=1;i<languages.size();i++){
				sb.append(" OR ");
				sb.append(languages.get(i));
				sb.append("[lang]");
			}
			sb.append(")");
		}
		q.setOption("languages", languages);
	}

	private void addJournalSets(List<String> journalSets,StringBuffer sb,Query q){
		if (journalSets!=null && journalSets.size()>0){
			if (sb.length()>0)
				sb.append(" AND (");
			sb.append(journalSets.get(0));
			sb.append("[text]");

			for(int i=1;i<journalSets.size();i++){
				sb.append(" OR ");
				sb.append(journalSets.get(i));
				sb.append("[text]");
			}
			sb.append(")");
			q.setOption("journalSets", journalSets);
		}
	}

	private void addTopicSets(List<String> topicSets,StringBuffer sb,Query q){
		if (topicSets!=null && topicSets.size()>0){
			if (sb.length()>0)
				sb.append(" AND (");
			sb.append(topicSets.get(0));
			sb.append("[sb]");

			for(int i=1;i<topicSets.size();i++){
				sb.append(" OR ");
				sb.append(topicSets.get(i));
				sb.append("[sb]");
			}
			sb.append(")");
			q.setOption("topicSets", topicSets);
		}
	}

	private String processPubmedOptionsFromUser(String term, HttpServletRequest request){

		StringBuffer sb = new StringBuffer();

		Query q = (Query)request.getSession().getAttribute("query");

		if (term.length()>0 && !hasMatchedParenthesis(term)){
			sb.append("(");
			sb.append(term);
			sb.append(")");
		}else
			sb.append(term);


		//////////////////////////////////////////////
		//authors
		List<String> authors = getAuthorList(request);

		addAuthors(authors,sb,q,request);


		//////////////////////////////////////////////
		//journals
		List<String> journals = getJournalList(request);

		addJournals(journals,sb,q,request);

		q.setTerm(sb.toString());

		///////////////////////////////////////////////
		//full text, free text, abstract
		if (request.getParameter("pmfilter_FullText")!=null){
			if (sb.length()>0)
				sb.append(" AND ");
			sb.append("full text[sb]");
			q.setOption("fullText", Boolean.TRUE);
		}else
			q.setOption("fullText", Boolean.FALSE);
		if (request.getParameter("pmfilter_FreeFullText")!=null){
			if (sb.length()>0)
				sb.append(" AND ");
			sb.append("free full text[sb]");
			q.setOption("freeFullText", Boolean.TRUE);
		}else
			q.setOption("freeFullText", Boolean.FALSE);
		if (request.getParameter("pmfilter_HasAbstract")!=null){
			if (sb.length()>0)
				sb.append(" AND ");
			sb.append("hasabstract[text]");
			q.setOption("hasAbstract", Boolean.TRUE);
		}else
			q.setOption("hasAbstract", Boolean.FALSE);

		///////////////////////////////////////////////
		//human, animals
		if (request.getParameter("pmfilter_StudyH")!=null && request.getParameter("pmfilter_StudyA")!=null){
			if (sb.length()>0)
				sb.append(" AND ");
			sb.append("(Humans[Mesh] OR Animals[Mesh:noexp])");
			q.setOption("humans", Boolean.TRUE);
			q.setOption("animals", Boolean.TRUE);
		}else if (request.getParameter("pmfilter_StudyH")!=null){
			if (sb.length()>0)
				sb.append(" AND ");
			sb.append("(Humans[Mesh])");
			q.setOption("humans", Boolean.TRUE);
			q.setOption("animals", Boolean.FALSE);
		}else if (request.getParameter("pmfilter_StudyA")!=null){
			if (sb.length()>0)
				sb.append(" AND ");
			sb.append("(Animals[Mesh:noexp])");
			q.setOption("animals", Boolean.TRUE);
			q.setOption("humans", Boolean.FALSE);
		}else{
			q.setOption("humans", Boolean.FALSE);
			q.setOption("animals", Boolean.FALSE);
		}


		///////////////////////////////////////////////
		//male, female
		if (request.getParameter("pmfilter_SexM")!=null && request.getParameter("pmfilter_SexF")!=null){
			if (sb.length()>0)
				sb.append(" AND ");
			sb.append("(Male[MeSH Terms] OR Female[MeSH Terms])");
			q.setOption("male", Boolean.TRUE);
			q.setOption("female", Boolean.TRUE);
		}else if (request.getParameter("pmfilter_SexM")!=null){
			if (sb.length()>0)
				sb.append(" AND ");
			sb.append("(Male[MeSH Terms])");
			q.setOption("male", Boolean.TRUE);
			q.setOption("female", Boolean.FALSE);
		}else if (request.getParameter("pmfilter_SexF")!=null){
			if (sb.length()>0)
				sb.append(" AND ");
			sb.append("(Female[MeSH Terms])");
			q.setOption("female", Boolean.TRUE);
			q.setOption("male", Boolean.FALSE);
		}else{
			q.setOption("male", Boolean.FALSE);
			q.setOption("female", Boolean.FALSE);
		}


		List<String> languages = new ArrayList<String>();
		List<String> journalSets = new ArrayList<String>();
		List<String> topicSets = new ArrayList<String>();
		List<String> publications = new ArrayList<String>();
		List<String> ages = new ArrayList<String>();

		Enumeration enumeration = request.getParameterNames();
		while(enumeration.hasMoreElements()){
			String s= (String)enumeration.nextElement();
			if (s.startsWith("pmfilter_Language_"))
				languages.add(request.getParameter(s));
			else if (s.startsWith("pmfilter_JournalSubsets_"))
				journalSets.add(request.getParameter(s));
			else if (s.startsWith("pmfilter_TopicSubsets_"))
				topicSets.add(request.getParameter(s));
			else if (s.startsWith("pmfilter_ExtraSubsets_"))
				topicSets.add(request.getParameter(s));
			else if (s.startsWith("pmfilter_PubType_"))
				publications.add(request.getParameter(s));
			else if (s.startsWith("pmfilter_Age_"))
				ages.add(request.getParameter(s));

		}

		///////////////////////////////////////////////
		//languages
		addLanguages(languages,sb,q);

		///////////////////////////////////////////////
		//journal subsets
		addJournalSets(journalSets,sb,q);


		///////////////////////////////////////////////
		//topic subsets
		addTopicSets(topicSets,sb,q);


		///////////////////////////////////////////////
		//publication types
		addPublications(publications,sb,q);


		///////////////////////////////////////////////
		//ages
		addAges(ages,sb,q);


		return sb.toString();
	}

	private boolean hasMatchedParenthesis(String s){
		if (s.charAt(0)!='(' || s.charAt(s.length()-1)!=')')
			return false;

		int count=1;
		for(int i=1;i<s.length()-1;i++){
			if (s.charAt(i)=='(')
				count++;
			if (s.charAt(i)==')')
				count--;

			if (count==0)
				return false;
		}

		return --count==0;
	}

	/**
	 * Method search
	 *
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 *
	 *
	 * Sets the session attribute 'citlist' to a list of PubmedArticle
	 *
	 *
	 */
	public ActionForward uploadCitation(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws PubmedException, EssieException, SemMedException {
		HttpSession session = request.getSession();
		SearchForm searchForm = (SearchForm) form;
		FormFile file = searchForm.getUploadCitationFile();
		// String source = searchForm.getSelectedSource();
		String source = "Medline";

		try {
			PrintWriter out
			   = new PrintWriter(new BufferedWriter(new FileWriter("C:\\Projects\\SemMedDebug\\PMIDListEcho.out")));
			InputStream is   = file.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			int[] ids = new int[3000];
			String aLine = null;
			int lineNum = 0;
			while((aLine = br.readLine()) != null) {
				ids[lineNum] = Integer.parseInt(aLine.trim());
				lineNum++;
				out.println(Integer.parseInt(aLine.trim()));
				// System.out.println(Integer.parseInt(aLine.trim()));
			}
			out.close();
			// log.trace("Read lines from citation file: " + lineNum);
			if(lineNum < 3000) {
				ids[lineNum] = 0;
				lineNum++;
			}
			session.setAttribute("citationIDs"+source, ids);

		} catch(Exception e) {  }
		return mapping.findForward("success");
	}
}



