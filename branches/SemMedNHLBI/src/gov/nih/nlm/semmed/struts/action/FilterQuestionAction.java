//Created by MyEclipse Struts
// XSL source (default): platform:/plugin/com.genuitec.eclipse.cross.easystruts.eclipse_4.0.1/xslt/JavaClass.xsl

package gov.nih.nlm.semmed.struts.action;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import gov.nih.nlm.semmed.exception.EssieException;
import gov.nih.nlm.semmed.exception.PubmedException;
import gov.nih.nlm.semmed.exception.SemMedException;
import gov.nih.nlm.semmed.model.APredication;
import gov.nih.nlm.semmed.model.Query;
import gov.nih.nlm.semmed.model.TestPredicationList;
import gov.nih.nlm.semmed.rules.Predicate;
import gov.nih.nlm.semmed.rules.RuleParser;
import gov.nih.nlm.semmed.struts.form.FilterQuestionForm;
import gov.nih.nlm.semmed.struts.form.SearchForm;
import gov.nih.nlm.semmed.util.ArticleDataSource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.LookupDispatchAction;
import org.apache.struts.upload.FormFile;
import org.apache.struts.util.LabelValueBean;
import org.jdom.Element;

/**
 * MyEclipse Struts
 * Creation date: 12-07-2005
 *
 * XDoclet definition:
 * @struts.action path="/Welcome" name="SearchForm" scope="request"
 * @struts.action-forward name="success" path="/jsp/welcome.jsp" contextRelative="true"
 *
 *
 *
 * If there is no query in session, creates and adds one and removes all query
 * attributes from the session    [shindongwoo]
 *
 *
 */
public class FilterQuestionAction extends LookupDispatchAction  {

	private static Log log = LogFactory.getLog(FilterQuestionAction.class);
	protected Map<String,String> getKeyMethodMap() {
	      Map<String,String> map = new HashMap<String,String>();
	      map.put("question.button.select", "select");
	      map.put("question.button.selectTest", "selectTest");
	      map.put("question.button.uploadRule", "uploadRule");
	      return map;
	  }

	@Override
	public ActionForward unspecified(ActionMapping mapping,
            ActionForm form,
            javax.servlet.http.HttpServletRequest request,
            javax.servlet.http.HttpServletResponse response)
	throws PubmedException, EssieException, SemMedException, Exception {
		if ("select".equals(request.getParameter("method")))
				return select(mapping,form,request,response);
		else
			return super.unspecified(mapping, form, request, response);
	}

	private Map<String, Predicate> rules;
	private static String ruleString = null;
	private String ruleTestString = null;
	// private  String ruleString = null; // Changed into instance variable to make the rule file uploaded and updatable
	private static String RULES_FILE = "rsc/rules.txt";

	// private void readRules(HttpServletRequest request) {
	private void readRules(HttpServletRequest request) { // Change the name since it is called when no on-the-fly rule file is uploaded
		if (ruleString == null)
			synchronized (this) {
				if (ruleString == null)
					try {
						ruleString = RuleParser.read(new FileInputStream(request
								.getSession().getServletContext().getRealPath(
										RULES_FILE)));
					} catch (IOException e) {
						System.err.println("********************");
						System.err.println("********************");
						System.err.println("********************");
						System.err.println("RULES NOT READ");
						System.err.println(request.getContextPath());

						e.printStackTrace();
					}
			}
	}

	private void readTestRules(InputStream is) { // Change the name since it is called when no on-the-fly rule file is uploaded
					try {
						ruleTestString = RuleParser.read(is);
						log.debug("Uploaded Rule: " + ruleTestString);
					} catch (IOException e) {
						System.err.println("********************");
						System.err.println("********************");
						System.err.println("RULES NOT READ");
						e.printStackTrace();
					}
	}

	private Map<String, Predicate> getRules(String ruleInstance) {
					try {
						rules = RuleParser.parse(ruleInstance);
					} catch (IOException e) {
						System.err.println("********************");
						System.err.println("********************");
						System.err.println("********************");
						System.err.println("RULES NOT LOADED");
						e.printStackTrace();
					}

		/* for(Map.Entry<String,Predicate> e : rules.entrySet()){
			log.debug("Rules "+e.getKey() +"\t" + e.getValue());
			// System.out.print("\t");
			// System.out.println(e.getValue());
		} */
		return rules;
	}

	/**
	 * Method execute
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 */
	public ActionForward select(
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response) {

		FilterQuestionForm QuestionForm = (FilterQuestionForm) form;
		HttpSession session = request.getSession();

		String qnumString = QuestionForm.getQuestionNumber();
		int qnum = Integer.parseInt(qnumString.trim());

		String[] RiskFactorArray = QuestionForm.getRiskFactor();
		String[] DisorderArray = QuestionForm.getDisorder();
		// log.debug("Question Num : " + qnum + ", RiskFactor : " + RiskFactor + ", Disorder : " + Disorder);
		log.debug("Question Num : " + qnum );

		String RiskFactor = RiskFactorArray[qnum];
		String Disorder = DisorderArray[qnum];
		log.debug("Risk Factor = " + RiskFactor);
		log.debug("Disorder = " + Disorder);

		readRules(request);
		String ruleInstanceRF = null;
		String ruleInstance = null;
		log.debug("Rule String : " + ruleString);
		String questionTemplate = null;
		String chosenQuestion = null;
		ServletContext ctx = session.getServletContext();
		// List questions = ((List<LabelValueBean>) ctx.getAttribute("questions"));
		Map questionMapping = ((Map) ctx.getAttribute("questionMapping"));
		LabelValueBean e = (LabelValueBean) questionMapping.get(new Integer(qnum));
		questionTemplate = e.getLabel();
		String questionValue = e.getValue();

		 if(RiskFactor != null) {
			if(RiskFactor.compareTo("Obesity") == 0) {
				ruleInstanceRF = ruleString.replace("_RiskFactor", "Obesity_Set");
				chosenQuestion = questionTemplate.replace("_RiskFactor", "obesity");
			} else if(RiskFactor.compareTo("Hyperlipidemia") == 0) {
				ruleInstanceRF = ruleString.replace("_RiskFactor", "Hyperlipidemia_Set");
				chosenQuestion = questionTemplate.replace("_RiskFactor", "hyperlipidemia");
			} else if(RiskFactor.compareTo("Metabolic syndrome") == 0) {
				ruleInstanceRF = ruleString.replace("_RiskFactor", "Metabolic_Syndrome_Set");
				chosenQuestion = questionTemplate.replace("_RiskFactor", "metabolic syndrome");
			} else if(RiskFactor.compareTo("Diabetes") == 0) {
				ruleInstanceRF = ruleString.replace("_RiskFactor", "Diabetes_Mellitus_Set");
				chosenQuestion = questionTemplate.replace("_RiskFactor", "diabetes");
			} else if(RiskFactor.compareTo("Inflammation") == 0) {
				ruleInstanceRF = ruleString.replace("_RiskFactor", "Inflammation_Set");
				chosenQuestion = questionTemplate.replace("_RiskFactor", "inflammation");
			} else {
				ruleInstanceRF = ruleString;
				chosenQuestion = questionTemplate;
			}
		} else {
			ruleInstanceRF = ruleString;
			chosenQuestion = questionTemplate;
		}
		if(Disorder != null) {
			if(Disorder.compareTo("Atherosclerosis") == 0) {
				ruleInstance = ruleInstanceRF.replace("_Disorder", "Atherosclerosis_Set");
				chosenQuestion = chosenQuestion.replace("_Disorder", "atherosclerosis");
			} else if(Disorder.compareTo("Cardiovascular disease") == 0) {
				ruleInstance = ruleInstanceRF.replace("_Disorder", "Cardiovascular_Disease_Set");
				chosenQuestion = chosenQuestion.replace("_Disorder", "cardiovascular disease");
			} else {
				ruleInstance = ruleInstanceRF;
			}
		} else {
			ruleInstance = ruleInstanceRF;
		}

		log.debug("Rule Instance : " + ruleInstance);
		log.debug("Chosen Question : " + chosenQuestion);
		Predicate pred = getRules(ruleInstance).get(questionValue);
		log.debug("rule selected : " + pred);
		if(session.getAttribute("rules") != null)
			session.removeAttribute("rules");
		session.setAttribute("rules", pred);
		if(session.getAttribute("chosenQuestion") != null)
			session.removeAttribute("chosenQuestion");
		session.setAttribute("chosenQuestion", chosenQuestion);
		if(session.getAttribute("questionNumber") != null)
			session.removeAttribute("questionNumber");
		session.setAttribute("questionNumber", new Integer(qnum));
		if(session.getAttribute("riskFactor") != null)
			session.removeAttribute("riskFactor");
		session.setAttribute("riskFactor", RiskFactor);
		if(session.getAttribute("disorder") != null)
			session.removeAttribute("disorder");
		session.setAttribute("disorder", Disorder);

		return mapping.findForward("success");
	}

	/**
	 * Method execute
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 */
	public ActionForward selectTest(
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response) {

		FilterQuestionForm QuestionForm = (FilterQuestionForm) form;
		HttpSession session = request.getSession();

		String qnumString = QuestionForm.getQuestionNumber();
		int qnum = Integer.parseInt(qnumString.trim());

		String[] RiskFactorArray = QuestionForm.getRiskFactor();
		String[] DisorderArray = QuestionForm.getDisorder();
		// log.debug("Question Num : " + qnum + ", RiskFactor : " + RiskFactor + ", Disorder : " + Disorder);
		log.debug("Question Num : " + qnum );

		String RiskFactor = RiskFactorArray[qnum];
		String Disorder = DisorderArray[qnum];
		log.debug("Risk Factor = " + RiskFactor);
		log.debug("Disorder = " + Disorder);

		// readTestRules(request);
		String ruleInstanceRF = null;
		String ruleInstance = null;
		log.debug("Rule String : " + ruleTestString);
		String questionTemplate = null;
		String chosenQuestion = null;
		ServletContext ctx = session.getServletContext();
		// List questions = ((List<LabelValueBean>) ctx.getAttribute("questions"));
		Map questionMapping = ((Map) ctx.getAttribute("questionMapping"));
		LabelValueBean e = (LabelValueBean) questionMapping.get(new Integer(qnum));
		questionTemplate = e.getLabel();
		String questionValue = e.getValue();

		 if(RiskFactor != null) {
			if(RiskFactor.compareTo("Obesity") == 0) {
				ruleInstanceRF = ruleTestString.replace("_RiskFactor", "Obesity_Set");
				chosenQuestion = questionTemplate.replace("_RiskFactor", "obesity");
			} else if(RiskFactor.compareTo("Hyperlipidemia") == 0) {
				ruleInstanceRF = ruleTestString.replace("_RiskFactor", "Hyperlipidemia_Set");
				chosenQuestion = questionTemplate.replace("_RiskFactor", "hyperlipidemia");
			} else if(RiskFactor.compareTo("Metabolic syndrome") == 0) {
				ruleInstanceRF = ruleTestString.replace("_RiskFactor", "Metabolic_Syndrome_Set");
				chosenQuestion = questionTemplate.replace("_RiskFactor", "metabolic syndrome");
			} else if(RiskFactor.compareTo("Diabetes") == 0) {
				ruleInstanceRF = ruleTestString.replace("_RiskFactor", "Diabetes_Mellitus_Set");
				chosenQuestion = questionTemplate.replace("_RiskFactor", "diabetes");
			} else if(RiskFactor.compareTo("Inflammation") == 0) {
				ruleInstanceRF = ruleTestString.replace("_RiskFactor", "Inflammation_Set");
				chosenQuestion = questionTemplate.replace("_RiskFactor", "inflammation");
			} else {
				ruleInstanceRF = ruleTestString;
				chosenQuestion = questionTemplate;
			}
		} else {
			ruleInstanceRF = ruleTestString;
			chosenQuestion = questionTemplate;
		}
		if(Disorder != null) {
			if(Disorder.compareTo("Atherosclerosis") == 0) {
				ruleInstance = ruleInstanceRF.replace("_Disorder", "Atherosclerosis_Set");
				chosenQuestion = chosenQuestion.replace("_Disorder", "atherosclerosis");
			} else if(Disorder.compareTo("Cardiovascular disease") == 0) {
				ruleInstance = ruleInstanceRF.replace("_Disorder", "Cardiovascular_Disease_Set");
				chosenQuestion = chosenQuestion.replace("_Disorder", "cardiovascular disease");
			} else {
				ruleInstance = ruleInstanceRF;
			}
		} else {
			ruleInstance = ruleInstanceRF;
		}
		log.debug("Rule Instance: " + ruleInstance);
		log.debug("Question Value: " + questionValue);
		// log.debug("Rule Instance : " + ruleInstance);
		// log.debug("Chosen Question : " + chosenQuestion);
		Predicate pred = getRules(ruleInstance).get(questionValue);
		log.debug("rule selected : " + pred);
		if(session.getAttribute("rules") != null)
			session.removeAttribute("rules");
		session.setAttribute("rules", pred);
		if(session.getAttribute("chosenQuestion") != null)
			session.removeAttribute("chosenQuestion");
		session.setAttribute("chosenQuestion", chosenQuestion);
		if(session.getAttribute("questionNumber") != null)
			session.removeAttribute("questionNumber");
		session.setAttribute("questionNumber", new Integer(qnum));
		if(session.getAttribute("riskFactor") != null)
			session.removeAttribute("riskFactor");
		session.setAttribute("riskFactor", RiskFactor);
		if(session.getAttribute("disorder") != null)
			session.removeAttribute("disorder");
		session.setAttribute("disorder", Disorder);

		return mapping.findForward("success");
	}

	public ActionForward uploadRule(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws PubmedException, EssieException, SemMedException {
		FilterQuestionForm questionForm = (FilterQuestionForm) form;
		FormFile file = questionForm.getUploadRuleFile();
		log.debug("Rule file is upoaded.");
		try {
			InputStream is   = file.getInputStream();
			readTestRules(is);

		} catch(Exception e) {  }
		return mapping.findForward("success");
	}

}

