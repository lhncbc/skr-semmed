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
import gov.nih.nlm.semmed.model.TestPredicationList;
import gov.nih.nlm.semmed.struts.form.SummaryForm;
import gov.nih.nlm.semmed.util.ArticleDataSource;
import gov.nih.nlm.semmed.util.Constants;
import gov.nih.nlm.semmed.util.HibernateSessionFactory;
import gov.nih.nlm.semmed.util.XMLUtils;
import gov.nih.nlm.semmed.summarization.*;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	private static Log log = LogFactory.getLog(SummaryAction.class);

	protected Map<String, String> getKeyMethodMap() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("summary.button.process", "process");
		map.put("summary.button.processTest", "processTest");
		map.put("summary.button.upload", "upload");
		map.put("summary.button.uploadCitation", "uploadCitation");
		map.put("summary.button.export", "exportToXml");
		map.put("summary.button.relevance", "findRelevant");

		return map;
	}

	@Override
	public ActionForward unspecified(ActionMapping mapping,
            ActionForm form,
            javax.servlet.http.HttpServletRequest request,
            javax.servlet.http.HttpServletResponse response)
		throws PubmedException, EssieException, SemMedException, Exception {
		if ("process".equals(request.getParameter("method")))
				return process(mapping,form,request,response);
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
		log.debug("Summary type:" + summaryType);

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

			}

			session.setAttribute("selectedSeed", request.getParameter("seed"));
			log.debug("seed Concept:" + request.getParameter("seed"));

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


		session.setAttribute("executionTime", System.currentTimeMillis()
				- startAll);
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
		log.debug("Relevant Conc Size:"
				+ ((List) session.getAttribute("relevantConcs")).size());
		if (relevantConcepts.size() > 0)
			session.setAttribute("selectedSeed",
					((LabelValueBean) relevantConcepts.get(0)).getLabel());
		log.debug("Seed: " + ((String) session.getAttribute("selectedSeed")));
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
		log.debug("Summary type:" + summaryType);

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
			log.debug("seed Concept:" + request.getParameter("seed"));

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


		session.setAttribute("executionTime", System.currentTimeMillis()
				- startAll);
		return mapping.findForward("success");
	}
}
