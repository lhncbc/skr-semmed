/*
 * Generated by MyEclipse Struts
 * Template path: templates/java/JavaClass.vtl
 */
package gov.nih.nlm.semmed.struts.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.FileInputStream;
import java.io.IOException;

import gov.nih.nlm.semmed.exception.EssieException;
import gov.nih.nlm.semmed.exception.PubmedException;
import gov.nih.nlm.semmed.exception.SemMedException;
import gov.nih.nlm.semmed.model.APredication;
import gov.nih.nlm.semmed.model.APredicationList;
import gov.nih.nlm.semmed.model.TestPredicationList;
import gov.nih.nlm.semmed.rules.*;
import gov.nih.nlm.semmed.struts.form.QuestionForm;
import gov.nih.nlm.semmed.util.ArticleDataSource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.Action;
import org.apache.struts.actions.LookupDispatchAction;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * MyEclipse Struts Creation date: 11-15-2007
 *
 * XDoclet definition:
 *
 * @struts.action path="/Question" name="QuestionForm" input="question.jsp"
 *                scope="request" validate="true"
 */
public class QuestionAction extends LookupDispatchAction {

	protected Map<String,String> getKeyMethodMap() {
	      Map<String,String> map = new HashMap<String,String>();
	      map.put("question.button.findcitation", "findcitation");
	      map.put("question.button.findcitationfromdb", "findcitationfromdb");
	      return map;
	  }

	@Override
	public ActionForward unspecified(ActionMapping mapping,
            ActionForm form,
            javax.servlet.http.HttpServletRequest request,
            javax.servlet.http.HttpServletResponse response)
	throws PubmedException, EssieException, SemMedException, Exception {
		if ("findcitation".equals(request.getParameter("method")))
				return findcitation(mapping,form,request,response);
		else if ("findcitationfromdb".equals(request.getParameter("method")))
			return findcitationfromdb(mapping,form,request,response);
		else
			return super.unspecified(mapping, form, request, response);

	}

	private Map<String, Predicate> rules;
	private static Log log = LogFactory.getLog(QuestionAction.class);
	private static String RULES_FILE = "rsc/rules.txt";

	private Map<String, Predicate> getRules(HttpServletRequest r) {
		if (rules == null)
			synchronized (this) {
				if (rules == null)
					try {
						rules = RuleParser.parse(new FileInputStream(r
								.getSession().getServletContext().getRealPath(
										RULES_FILE)));
					} catch (IOException e) {
						System.err.println("********************");
						System.err.println("********************");
						System.err.println("********************");
						System.err.println("RULES NOT LOADED");
						System.err.println(r.getContextPath());

						e.printStackTrace();
					}
			}
		return rules;
	}

	/**
	 * Method execute
	 *
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 * @throws SemMedException
	 */
	// @SuppressWarnings("unchecked")
	public ActionForward findcitation(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws SemMedException {
		QuestionForm QuestionForm = (QuestionForm) form;
		HttpSession session = request.getSession();

		int page = 0;

		if (request.getParameter("p") == null) {

			List<APredication> relevantPredications = new ArrayList<APredication>();
			List<Integer> relevantCitations = new ArrayList<Integer>();
			List<APredication> nonRelevantPredications = new ArrayList<APredication>();
			List<Integer> nonRelevantCitations = new ArrayList<Integer>();

			Filter.filterNonRelevants((List<APredication>) session
					.getAttribute("predications"), getRules(request).get(
					"" + QuestionForm.getQuestionNumber()),
					relevantPredications, relevantCitations,
					nonRelevantPredications, nonRelevantCitations);

			nonRelevantCitations.clear();
			// HLJ for (int i : (int[]) session.getAttribute("citationIDsMedline"))
			for (int i : (int[]) session.getAttribute("citationIDsOPASI"))
				nonRelevantCitations.add(i);
			// nonRelevantCitations.addAll(Arrays.asList());
			nonRelevantCitations.removeAll(relevantCitations);

			session.setAttribute("relevantQuestionrCitations",
					relevantCitations);
			session.setAttribute("relevantQuestionrPredications",
					relevantPredications);
			session.setAttribute("relevantQuestionnrCitations",
					nonRelevantCitations);
			session.setAttribute("relevantQuestionnrPredications",
					nonRelevantPredications);
			session.setAttribute("summaryPredications", session
					.getAttribute("predications"));
			session.setAttribute("selectedQuestion", QuestionForm
					.getQuestionNumber());
			session.removeAttribute("key");
		} else {

			try {
				page = Integer.parseInt(request.getParameter("p"));
			} catch (Exception e) {
				page = 0;
			}
		}

		if (request.getParameter("display") == null) {
			showAll(session);
			// rPredications
			List<APredication> predications = (List<APredication>) session
					.getAttribute("relevantQuestionrPredications");
			log.debug("Size of relevant predications : " + predications.size());
			int[] PIDs = new int[Math.min(20, predications.size() - page * 20)];
			int[] SIDs = new int[PIDs.length];
			ArticleDataSource.SourceType[] sources = new ArticleDataSource.SourceType[PIDs.length];

			for (int i = 0; i < PIDs.length; i++) {
				PIDs[i] = predications.get(i + page * 20).PID;
				SIDs[i] = predications.get(i + page * 20).SID;
				sources[i] = predications.get(i + page * 20).source;
			}

			try {
				session.setAttribute("displayedrPredications",
						new APredicationList(PIDs, SIDs, sources));
			} catch (Exception e) {
				e.printStackTrace();
				throw new SemMedException(e);
			}
			session.setAttribute("pageNumberrPredications", page);
			session.setAttribute("currentQuestionDisplay", "rPredications");

			// rCitations
			List<Integer> PMIDs = new ArrayList<Integer>(20);
			List<Integer> citations = (List<Integer>) session.getAttribute("relevantQuestionrCitations");
			log.debug("Size of relevant citations : " + citations.size());
			for (int i = page * 20; i < citations.size() && i < (page + 1) * 20; i++)
				PMIDs.add(citations.get(i));

			try {
				// TODO [Alejandro] I actually need different sources for different citations!
				ArticleDataSource ds = ArticleDataSource
						.getInstance(ArticleDataSource.SourceType.MEDLINE);
				session.setAttribute("displayedrCitations", ds.fetch(session,
						PMIDs));
			} catch (Exception e) {
				e.printStackTrace();
			}
			session.setAttribute("pageNumberrCitations", page);

			// nrPredications

			predications = (List<APredication>) session
					.getAttribute("relevantQuestionnrPredications");
			log.debug("Size of non-relevant predications : " + predications.size());
			PIDs = new int[Math.min(20, predications.size() - page * 20)];
			SIDs = new int[PIDs.length];
			sources = new ArticleDataSource.SourceType[PIDs.length];

			for (int i = 0; i < PIDs.length; i++) {
				PIDs[i] = predications.get(i + page * 20).PID;
				SIDs[i] = predications.get(i + page * 20).SID;
				sources[i] = predications.get(i + page * 20).source;
			}

			try {
				session.setAttribute("displayednrPredications",
						new APredicationList(PIDs, SIDs, sources));
			} catch (Exception e) {
				e.printStackTrace();
				throw new SemMedException(e);
			}
			session.setAttribute("pageNumbernrPredications", page);

			// nrCitations
			PMIDs = new ArrayList<Integer>(20);
			citations = (List<Integer>) session
					.getAttribute("relevantQuestionnrCitations");
			log.debug("Size of non-relevant citations : " + citations.size());
			for (int i = page * 20; i < citations.size() && i < (page + 1) * 20; i++)
				PMIDs.add(citations.get(i));

			try {
				ArticleDataSource ds = ArticleDataSource
						.getInstance(ArticleDataSource.SourceType.MEDLINE); // TODO
				// [Alejandro]
				// I
				// actually
				// need
				// different
				// sources
				// for
				// different citations!
				session.setAttribute("displayednrCitations", ds.fetch(session,
						PMIDs));
			} catch (Exception e) {
				e.printStackTrace();
			}

			session.setAttribute("pageNumbernrCitations", page);
		} else if (request.getParameter("display").equals("rPredications")) {

			List<APredication> predications = (List<APredication>) session
					.getAttribute("relevantQuestionrPredications");
			log.debug("Size of non relevant predications : " + predications.size());
			int[] PIDs = new int[Math.min(20, predications.size() - page * 20)];
			int[] SIDs = new int[PIDs.length];
			ArticleDataSource.SourceType[] sources = new ArticleDataSource.SourceType[PIDs.length];

			for (int i = 0; i < PIDs.length; i++) {
				PIDs[i] = predications.get(i + page * 20).PID;
				SIDs[i] = predications.get(i + page * 20).SID;
				sources[i] = predications.get(i + page * 20).source;
			}

			try {
				session.setAttribute("displayedrPredications",
						new APredicationList(PIDs, SIDs, sources));
			} catch (Exception e) {
				e.printStackTrace();
				throw new SemMedException(e);
			}
			session.setAttribute("pageNumberrPredications", page);
			session.setAttribute("currentQuestionDisplay", "rPredications");
		} else if (request.getParameter("display").equals("rCitations")) {

			List<Integer> PMIDs = new ArrayList<Integer>(20);
			List<Integer> citations = (List<Integer>) session
					.getAttribute("relevantQuestionrCitations");
			log.debug("Size of relevant citations : " + citations.size());
			for (int i = page * 20; i < citations.size() && i < (page + 1) * 20; i++)
				PMIDs.add(citations.get(i));

			try {
				ArticleDataSource ds = ArticleDataSource
						.getInstance(ArticleDataSource.SourceType.MEDLINE); // TODO
				// [Alejandro]
				// I
				// actually
				// need
				// different
				// sources
				// for
				// different citations!
				session.setAttribute("displayedrCitations", ds.fetch(session,
						PMIDs));
			} catch (Exception e) {
				e.printStackTrace();
			}

			session.setAttribute("pageNumberrCitations", page);
			session.setAttribute("currentQuestionDisplay", "rCitations");
		} else if (request.getParameter("display").equals("nrPredications")) {
			List<APredication> predications = (List<APredication>) session
					.getAttribute("relevantQuestionnrPredications");
			log.debug("Size of non relevant predications : " + predications.size());
			int[] PIDs = new int[Math.min(20, predications.size() - page * 20)];
			int[] SIDs = new int[PIDs.length];
			ArticleDataSource.SourceType[] sources = new ArticleDataSource.SourceType[PIDs.length];

			for (int i = 0; i < PIDs.length; i++) {
				PIDs[i] = predications.get(i + page * 20).PID;
				SIDs[i] = predications.get(i + page * 20).SID;
				sources[i] = predications.get(i + page * 20).source;
			}

			try {
				session.setAttribute("displayednrPredications",
						new APredicationList(PIDs, SIDs, sources));
			} catch (Exception e) {
				e.printStackTrace();
				throw new SemMedException(e);
			}
			session.setAttribute("pageNumbernrPredications", page);
			session.setAttribute("currentQuestionDisplay", "nrPredications");
		} else if (request.getParameter("display").equals("nrCitations")) {
			List<Integer> PMIDs = new ArrayList<Integer>(20);
			List<Integer> citations = (List<Integer>) session
					.getAttribute("relevantQuestionnrCitations");
			log.debug("Size of non relevant citations : " + citations.size());
			for (int i = page * 20; i < citations.size() && i < (page + 1) * 20; i++)
				PMIDs.add(citations.get(i));

			try {
				ArticleDataSource ds = ArticleDataSource
						.getInstance(ArticleDataSource.SourceType.MEDLINE); // TODO
				// [Alejandro]
				// I
				// actually
				// need
				// different
				// sources
				// for
				// different citations!
				session.setAttribute("displayednrCitations", ds.fetch(session,
						PMIDs));
			} catch (Exception e) {
				e.printStackTrace();
			}

			session.setAttribute("pageNumbernrCitations", page);
			session.setAttribute("currentQuestionDisplay", "nrCitations");
		}
		session.removeAttribute("predsource");
		session.setAttribute("predsource", 1);

		return mapping.findForward("success");
	}

	/**
	 * Method execute
	 *
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 * @throws SemMedException
	 */
	// @SuppressWarnings("unchecked")
	public ActionForward findcitationfromdb(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws SemMedException {
		QuestionForm QuestionForm = (QuestionForm) form;
		HttpSession session = request.getSession();

		int page = 0;

		if (request.getParameter("p") == null) {

			List<APredication> relevantPredications = new ArrayList<APredication>();
			List<Integer> relevantCitations = new ArrayList<Integer>();
			List<APredication> nonRelevantPredications = new ArrayList<APredication>();
			List<Integer> nonRelevantCitations = new ArrayList<Integer>();

			Filter.filterNonRelevants((List<APredication>) session
					.getAttribute("predications"), getRules(request).get(
					"" + QuestionForm.getQuestionNumber()),
					relevantPredications, relevantCitations,
					nonRelevantPredications, nonRelevantCitations);

			nonRelevantCitations.clear();
			// HLJ for (int i : (int[]) session.getAttribute("citationIDsMedline")) {
			for (int i : (int[]) session.getAttribute("citationIDsOPASI")) {
				if(i > 0)
				nonRelevantCitations.add(i);
			}
			// nonRelevantCitations.addAll(Arrays.asList());
			nonRelevantCitations.removeAll(relevantCitations);

			session.setAttribute("relevantQuestionrCitations",
					relevantCitations);
			session.setAttribute("relevantQuestionrPredications",
					relevantPredications);
			session.setAttribute("relevantQuestionnrCitations",
					nonRelevantCitations);
			session.setAttribute("relevantQuestionnrPredications",
					nonRelevantPredications);
			session.setAttribute("summaryPredications", session
					.getAttribute("predications"));
			session.setAttribute("selectedQuestion", QuestionForm
					.getQuestionNumber());
			session.removeAttribute("key");
		} else {

			try {
				page = Integer.parseInt(request.getParameter("p"));
			} catch (Exception e) {
				page = 0;
			}
		}

		if (request.getParameter("display") == null) {
			showAll(session);
			// rPredications
			List<APredication> predications = (List<APredication>) session
					.getAttribute("relevantQuestionrPredications");
			log.debug("Size of relevant predications : " + predications.size());
			int[] PIDs = new int[Math.min(20, predications.size() - page * 20)];
			int[] SIDs = new int[PIDs.length];
			ArticleDataSource.SourceType[] sources = new ArticleDataSource.SourceType[PIDs.length];

			for (int i = 0; i < PIDs.length; i++) {
				PIDs[i] = predications.get(i + page * 20).PID;
				SIDs[i] = predications.get(i + page * 20).SID;
				sources[i] = predications.get(i + page * 20).source;
			}

			try {
				session.setAttribute("displayedrPredications",
						new TestPredicationList(PIDs, SIDs, sources));
			} catch (Exception e) {
				e.printStackTrace();
				throw new SemMedException(e);
			}
			session.setAttribute("pageNumberrPredications", page);
			session.setAttribute("currentQuestionDisplay", "rPredications");

			// rCitations
			List<Integer> PMIDs = new ArrayList<Integer>(20);
			List<Integer> citations = (List<Integer>) session.getAttribute("relevantQuestionrCitations");
			log.debug("Size of relevant citations : " + citations.size());
			for (int i = page * 20; i < citations.size() && i < (page + 1) * 20; i++)
				PMIDs.add(citations.get(i));

			try {
				// TODO [Alejandro] I actually need different sources for different citations!
				ArticleDataSource ds = ArticleDataSource
						.getInstance(ArticleDataSource.SourceType.MEDLINE);
				session.setAttribute("displayedrCitations", ds.fetch(session,
						PMIDs));
			} catch (Exception e) {
				e.printStackTrace();
			}
			session.setAttribute("pageNumberrCitations", page);

			// nrPredications

			predications = (List<APredication>) session
					.getAttribute("relevantQuestionnrPredications");
			log.debug("Size of non-relevant predications : " + predications.size());
			PIDs = new int[Math.min(20, predications.size() - page * 20)];
			SIDs = new int[PIDs.length];
			sources = new ArticleDataSource.SourceType[PIDs.length];

			for (int i = 0; i < PIDs.length; i++) {
				PIDs[i] = predications.get(i + page * 20).PID;
				SIDs[i] = predications.get(i + page * 20).SID;
				sources[i] = predications.get(i + page * 20).source;
			}

			try {
				session.setAttribute("displayednrPredications",
						new TestPredicationList(PIDs, SIDs, sources));
			} catch (Exception e) {
				e.printStackTrace();
				throw new SemMedException(e);
			}
			session.setAttribute("pageNumbernrPredications", page);

			// nrCitations
			PMIDs = new ArrayList<Integer>(20);
			citations = (List<Integer>) session
					.getAttribute("relevantQuestionnrCitations");
			log.debug("Size of non-relevant citations : " + citations.size());
			for (int i = page * 20; i < citations.size() && i < (page + 1) * 20; i++)
				PMIDs.add(citations.get(i));

			try {
				ArticleDataSource ds = ArticleDataSource
						.getInstance(ArticleDataSource.SourceType.MEDLINE); // TODO
				// [Alejandro]
				// I
				// actually
				// need
				// different
				// sources
				// for
				// different citations!
				session.setAttribute("displayednrCitations", ds.fetch(session,
						PMIDs));
			} catch (Exception e) {
				e.printStackTrace();
			}

			session.setAttribute("pageNumbernrCitations", page);
		} else if (request.getParameter("display").equals("rPredications")) {

			List<APredication> predications = (List<APredication>) session
					.getAttribute("relevantQuestionrPredications");
			log.debug("Size of non relevant predications : " + predications.size());
			int[] PIDs = new int[Math.min(20, predications.size() - page * 20)];
			int[] SIDs = new int[PIDs.length];
			ArticleDataSource.SourceType[] sources = new ArticleDataSource.SourceType[PIDs.length];

			for (int i = 0; i < PIDs.length; i++) {
				PIDs[i] = predications.get(i + page * 20).PID;
				SIDs[i] = predications.get(i + page * 20).SID;
				sources[i] = predications.get(i + page * 20).source;
			}

			try {
				session.setAttribute("displayedrPredications",
						new APredicationList(PIDs, SIDs, sources));
			} catch (Exception e) {
				e.printStackTrace();
				throw new SemMedException(e);
			}
			session.setAttribute("pageNumberrPredications", page);
			session.setAttribute("currentQuestionDisplay", "rPredications");
		} else if (request.getParameter("display").equals("rCitations")) {

			List<Integer> PMIDs = new ArrayList<Integer>(20);
			List<Integer> citations = (List<Integer>) session
					.getAttribute("relevantQuestionrCitations");
			log.debug("Size of relevant citations : " + citations.size());
			for (int i = page * 20; i < citations.size() && i < (page + 1) * 20; i++)
				PMIDs.add(citations.get(i));

			try {
				ArticleDataSource ds = ArticleDataSource
						.getInstance(ArticleDataSource.SourceType.MEDLINE); // TODO
				// [Alejandro]
				// I
				// actually
				// need
				// different
				// sources
				// for
				// different citations!
				session.setAttribute("displayedrCitations", ds.fetch(session,
						PMIDs));
			} catch (Exception e) {
				e.printStackTrace();
			}

			session.setAttribute("pageNumberrCitations", page);
			session.setAttribute("currentQuestionDisplay", "rCitations");
		} else if (request.getParameter("display").equals("nrPredications")) {
			List<APredication> predications = (List<APredication>) session
					.getAttribute("relevantQuestionnrPredications");
			log.debug("Size of non relevant predications : " + predications.size());
			int[] PIDs = new int[Math.min(20, predications.size() - page * 20)];
			int[] SIDs = new int[PIDs.length];
			ArticleDataSource.SourceType[] sources = new ArticleDataSource.SourceType[PIDs.length];

			for (int i = 0; i < PIDs.length; i++) {
				PIDs[i] = predications.get(i + page * 20).PID;
				SIDs[i] = predications.get(i + page * 20).SID;
				sources[i] = predications.get(i + page * 20).source;
			}

			try {
				session.setAttribute("displayednrPredications",
						new TestPredicationList(PIDs, SIDs, sources));
			} catch (Exception e) {
				e.printStackTrace();
				throw new SemMedException(e);
			}
			session.setAttribute("pageNumbernrPredications", page);
			session.setAttribute("currentQuestionDisplay", "nrPredications");
		} else if (request.getParameter("display").equals("nrCitations")) {
			List<Integer> PMIDs = new ArrayList<Integer>(20);
			List<Integer> citations = (List<Integer>) session
					.getAttribute("relevantQuestionnrCitations");
			log.debug("Size of non relevant citations : " + citations.size());
			for (int i = page * 20; i < citations.size() && i < (page + 1) * 20; i++)
				PMIDs.add(citations.get(i));

			try {
				ArticleDataSource ds = ArticleDataSource
						.getInstance(ArticleDataSource.SourceType.MEDLINE); // TODO
				// [Alejandro]
				// I
				// actually
				// need
				// different
				// sources
				// for
				// different citations!
				session.setAttribute("displayednrCitations", ds.fetch(session,
						PMIDs));
			} catch (Exception e) {
				e.printStackTrace();
			}

			session.setAttribute("pageNumbernrCitations", page);
			session.setAttribute("currentQuestionDisplay", "nrCitations");
		}
		session.removeAttribute("predsource");
		session.setAttribute("predsource", 2);
		return mapping.findForward("success");
	}


	// @SuppressWarnings("unchecked")
	private void showAll(HttpSession session) throws SemMedException {
		int page = 0;
		List<APredication> predications = (List<APredication>) session
				.getAttribute("summaryPredications");

		int[] PIDs = new int[Math.min(20, predications.size() - page * 20)];
		int[] SIDs = new int[PIDs.length];
		ArticleDataSource.SourceType[] sources = new ArticleDataSource.SourceType[PIDs.length];

		for (int i = 0; i < PIDs.length; i++) {
			PIDs[i] = predications.get(i + page * 20).PID;
			SIDs[i] = predications.get(i + page * 20).SID;
			sources[i] = predications.get(i + page * 20).source;
		}

		try {
			session.setAttribute("displayedSummaryPredications", new APredicationList(
					PIDs, SIDs, sources));
		} catch (Exception e) {
			e.printStackTrace();
			throw new SemMedException(e);
		}
		session.setAttribute("pageNumberPredications", page);
	}
}
