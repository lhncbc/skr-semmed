/**
 *
 */
package gov.nih.nlm.semmed.struts.action;

import gov.nih.nlm.semmed.model.APredicationList;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.LabelValueBean;

/**
 * @author hkilicoglu
 *
 */
public class InitializeSummaryAction extends Action {

	// --------------------------------------------------------- Instance Variables
	private static Log log = LogFactory.getLog(InitializeSummaryAction.class);

	// --------------------------------------------------------- Methods

	/**
	 * Method execute
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 */
	public ActionForward execute(
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response) {

		HttpSession session = request.getSession();

		session.removeAttribute("executionTime");

		if (session.getAttribute("predications") != null) {
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



}
