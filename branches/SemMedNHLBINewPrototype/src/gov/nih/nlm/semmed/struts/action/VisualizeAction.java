//Created by MyEclipse Struts
// XSL source (default): platform:/plugin/com.genuitec.eclipse.cross.easystruts.eclipse_4.0.1/xslt/JavaClass.xsl

package gov.nih.nlm.semmed.struts.action;

import gov.nih.nlm.semmed.exception.SemMedException;
import gov.nih.nlm.semmed.exception.UploadException;
import gov.nih.nlm.semmed.exception.XMLException;
import gov.nih.nlm.semmed.model.APredication;
import gov.nih.nlm.semmed.model.APredicationList;
import gov.nih.nlm.semmed.model.PubmedArticle;
import gov.nih.nlm.semmed.model.TestPredicationList;
import gov.nih.nlm.semmed.model.SemrepResultSet;
import gov.nih.nlm.semmed.model.SentencePredication;
import gov.nih.nlm.semmed.servlet.GraphServlet;
import gov.nih.nlm.semmed.struts.form.VisualizeForm;
import gov.nih.nlm.semmed.summarization.FilterFactory;
import gov.nih.nlm.semmed.summarization.Filter;
import gov.nih.nlm.semmed.summarization.Summarizer;
import gov.nih.nlm.semmed.util.ArticleDataSource;
import gov.nih.nlm.semmed.util.Constants;
import gov.nih.nlm.semmed.util.GraphUtils;
import gov.nih.nlm.semmed.util.HibernateSessionFactory;
import gov.nih.nlm.semmed.util.XMLUtils;
import org.hibernate.cfg.Configuration;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.LookupDispatchAction;
import org.apache.struts.upload.FormFile;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * MyEclipse Struts Creation date: 02-21-2006
 *
 * XDoclet definition:
 *
 * @struts.action path="VisualizeAction" name="VisualizeForm"
 *                input="/jsp/visualize.jsp" parameter="method" scope="request"
 *                validate="true"
 * @struts.action-forward name="success" path="/jsp/visualize.jsp"
 *                        contextRelative="true"
 * @struts.action-forward name="failure" path="/jsp/visualize.jsp"
 *                        contextRelative="true"
 */
public class VisualizeAction extends LookupDispatchAction {

	// --------------------------------------------------------- Instance
	// Variables
	private static Log log = LogFactory.getLog(VisualizeAction.class);

	public static int LIMIT = 1000;

	// --------------------------------------------------------- Methods
	protected Map<String, String> getKeyMethodMap() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("visualize.button.process", "process");
		map.put("visualize.button.processTest", "processTest");
		map.put("visualize.button.upload", "upload");
		map.put("visualize.button.exportCitationToXml", "exportCitationToXml");
		return map;
	}

	/**
	 * Method process
	 *
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 * @throws SemMedException
	 */
	@SuppressWarnings("unchecked")
	public ActionForward process(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws SemMedException {

		try {
			HttpSession session = request.getSession();

			APredicationList preds = new APredicationList(
					(List<APredication>) session
							.getAttribute("summaryPredications"));

			ArrayList<Filter> filters = new ArrayList<Filter>();

			filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.PRELIMINARY));
			filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.NHLBI_RELEVANCE));
			filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.CONNECTIVITY));
			filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.NHLBI_NOVELTY));


			if (request.getParameter("saliency") == null
					|| !request.getParameter("saliency").equals("off")){
				filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.PREDICATION_SALIENCY));
				session.setAttribute("saliency", Boolean.TRUE);
			}

			APredicationList summarizedPredications = new APredicationList(
					Summarizer.summarize(filters.toArray(new Filter[filters.size()]),
							preds,
							new String[] { Constants.CAUSES, Constants.DIAGNOSES,
									Constants.ISA },
									preds.getRelevantConcepts(APredicationList.Type.TREATMENT).get(0).getLabel()));

			Session hb_session = HibernateSessionFactory.currentSession();
			hb_session.clear();

			List<Long> idsStrings = summarizedPredications.getSentencePredicationIDs();
			// List<Long> idsStrings = preds.getSentencePredicationIDs();


			//TODO [Alejandro] This query is VERY slow
			//Incorporate the required information into scc1 so we can use
			//the much faster APredicationList constructor to get the predications
			//and eventually the XML file
			String queryString = "select distinct sp from SentencePredication as sp "
					+ "inner join fetch sp.sentence s "
					+ "inner join fetch sp.predication p "
					+ "inner join fetch p.predicationArgumentSet pa "
					+ "where sp.sentencePredicationId in (:ids)";

			Query q = hb_session.createQuery(queryString);
			q.setCacheable(true);
			q.setParameterList("ids", idsStrings);

			SemrepResultSet srs = new SemrepResultSet(q.list());

			List<SentencePredication> sps = srs.getSentencePredications();
			List<SentencePredication> displayedPredications;
			//TODO [Alejandro] Hard-coded max, find better way to deal with this
			if (sps.size()>LIMIT){
				displayedPredications = new ArrayList<SentencePredication>(LIMIT);
				for(int i=0;i<LIMIT;i++)
					displayedPredications.add(sps.get(i));
				session.setAttribute("maxedPredications", sps.size());
			}else{
				displayedPredications = sps;
				session.removeAttribute("maxedPredications");
			}
			// log.debug("seed in process() in VisualizeAction: "+ (String) session.getAttribute("selectedSeed"));
			// log.debug("Number of sentence predication : " + sps.size());
			// log.debug("Number of summarized predication : " + sps.size());
			Document graphXml = GraphUtils.parse(sps,
					null, null, session.getServletContext(), false,
					// (String) session.getAttribute("selectedSeed"));
					preds.getRelevantConcepts(APredicationList.Type.TREATMENT).get(0).getLabel());
			XMLOutputter serializer = new XMLOutputter();
			String graphString = serializer.outputString(graphXml);
			// log.debug("Graph String: " + graphString);

			session.removeAttribute("predsource");
			session.setAttribute("predsource", 1);
			UUID key = GraphServlet.addGraph(request, graphString, (List) session
					.getAttribute("relevantQuestionrCitations"), (List) session
					.getAttribute("relevantQuestionnrCitations"));

			session.setAttribute("key", key.toString());

		} catch (SQLException e) {
			throw new SemMedException(e);
		}

		return mapping.findForward("success");
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
		VisualizeForm VisualizeForm = (VisualizeForm) form;
		HttpSession session = request.getSession();
		FormFile file = VisualizeForm.getUploadFile();
		log.debug("Request type: " + request.getContentType());
		log.debug("File type: " + file.getContentType());
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
	 * Method process
	 *
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 * @throws SemMedException
	 */
	@SuppressWarnings("unchecked")
	public ActionForward processTest(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws SemMedException {
		log.debug("Visualize()");
		try {
			HttpSession session = request.getSession();

			TestPredicationList preds = new TestPredicationList(
					(List<APredication>) session
							.getAttribute("summaryPredications"));

			ArrayList<Filter> filters = new ArrayList<Filter>();

			filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.PRELIMINARY));
			filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.NHLBI_RELEVANCE));
			filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.CONNECTIVITY));
			filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.NHLBI_NOVELTY));


			if (request.getParameter("saliency") == null
					|| !request.getParameter("saliency").equals("off")){
				filters.add(FilterFactory.getFilter(FilterFactory.BatchFilter.PREDICATION_SALIENCY));
				session.setAttribute("saliency", Boolean.TRUE);
			}

			TestPredicationList summarizedPredications = new TestPredicationList(
					Summarizer.summarize(filters.toArray(new Filter[filters.size()]),
							preds,
							new String[] { Constants.CAUSES, Constants.DIAGNOSES,
									Constants.ISA },
									preds.getRelevantConcepts(APredicationList.Type.TREATMENT).get(0).getLabel()));

			Configuration configuration = new Configuration().configure();
			// configuration.setProperty("hibernate.connection.url", "jdbc:mysql://indlx3:3306/semmed2006test");
			configuration.setProperty("hibernate.connection.datasource", "java:/comp/env/jdbc/SemMedTestDB");
			SessionFactory sessionFactory = configuration.buildSessionFactory();
			Session hb_session = sessionFactory.openSession();

			List<Long> idsList = summarizedPredications.getSentencePredicationIDs();
			// List<Long> idsList = preds.getSentencePredicationIDs();

			//TODO [Alejandro] This query is VERY slow
			//Incorporate the required information into scc1 so we can use
			//the much faster APredicationList constructor to get the predications
			//and eventually the XML file
			String queryString = "select distinct sp from SentencePredication as sp "
					+ "inner join fetch sp.sentence s "
					+ "inner join fetch sp.predication p "
					+ "inner join fetch p.predicationArgumentSet pa "
					+ "where sp.sentencePredicationId in (:ids)";


			Query q = hb_session.createQuery(queryString);
			q.setCacheable(true);
			q.setParameterList("ids", idsList);

			SemrepResultSet srs = new SemrepResultSet(q.list());
			/* TestPredicationList AList = new TestPredicationList();
			List<SentencePredication> sentencePredications = AList.init(idsList, true);
			for(int i = 0; i < idsList.size(); i++) {
				sentencePredications.add(new SentencePredication(idsList.get(i)));
			}
			SemrepResultSet srs = new SemrepResultSet(sentencePredications); */

			List<SentencePredication> sps = srs.getSentencePredications();
			List<SentencePredication> displayedPredications;
			//TODO [Alejandro] Hard-coded max, find better way to deal with this
			if (sps.size()>LIMIT){
				displayedPredications = new ArrayList<SentencePredication>(LIMIT);
				for(int i=0;i<LIMIT;i++)
					displayedPredications.add(sps.get(i));
				session.setAttribute("maxedPredications", sps.size());
			}else{
				displayedPredications = sps;
				session.removeAttribute("maxedPredications");
			}
			// log.debug("seed in processTest() in VisualizeAction: "+ (String) session.getAttribute("selectedSeed"));
			Document graphXml = GraphUtils.parseTest(sps,
					null, null, session.getServletContext(), false,
					// (String) session.getAttribute("selectedSeed"), hb_session);
					preds.getRelevantConcepts(APredicationList.Type.TREATMENT).get(0).getLabel(), hb_session);
			XMLOutputter serializer = new XMLOutputter();
			String graphString = serializer.outputString(graphXml);
			// log.debug(graphString);
			session.removeAttribute("predsource");
			session.setAttribute("predsource", 2);

			UUID key = GraphServlet.addGraph(request, graphString, (List) session
					.getAttribute("relevantQuestionrCitations"), (List) session
					.getAttribute("relevantQuestionnrCitations"));

			session.setAttribute("key", key.toString());
			hb_session.close();

		} catch (SQLException e) {
			throw new SemMedException(e);
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
	public ActionForward exportCitationToXml(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws XMLException {

		HttpSession session = request.getSession();
		try {
			// rCitations
			List<PubmedArticle> allrCits = (List<PubmedArticle>) session.getAttribute("allrCitations");
			List<PubmedArticle> allnrCits = (List<PubmedArticle>) session.getAttribute("allnrCitations");
			if(allrCits != null)
				session.setAttribute("relevantCitations", allrCits);
			else {
				List<Integer> rcitations = (List<Integer>) session.getAttribute("relevantQuestionrCitations");
				log.debug("Size of relevant citations : " + rcitations.size());
				// TODO [Alejandro] I actually need different sources for different citations!
				ArticleDataSource ds = ArticleDataSource
						.getInstance(ArticleDataSource.SourceType.MEDLINE);
				session.setAttribute("relevantCitations", ds.fetch(session,
						rcitations));
			}
			if(allnrCits != null)
				session.setAttribute("nonrelevantCitations", allnrCits);
			else {
				List<Integer> nrcitations = (List<Integer>) session .getAttribute("relevantQuestionnrCitations");
				ArticleDataSource ds = ArticleDataSource
				.getInstance(ArticleDataSource.SourceType.MEDLINE);
				session.setAttribute("nonrelevantCitations", ds.fetch(session,
						nrcitations));
			}
			Document doc = XMLUtils.exportCitationToXml(session);
			response.setContentType("application/octet-stream");
			response.setHeader("Content-disposition",
					"attachment;filename=citation-results.xml");
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


}
