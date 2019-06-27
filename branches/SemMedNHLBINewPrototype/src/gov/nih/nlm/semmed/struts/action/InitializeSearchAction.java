//Created by MyEclipse Struts
// XSL source (default): platform:/plugin/com.genuitec.eclipse.cross.easystruts.eclipse_4.0.1/xslt/JavaClass.xsl

package gov.nih.nlm.semmed.struts.action;

import gov.nih.nlm.semmed.model.Query;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

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
 * attributes from the session    [Alejandro]
 * 
 * 
 */
public class InitializeSearchAction extends Action {

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
		if (session.getAttribute("query") == null) {
		    Query q = new Query("","medline");
		    q.setDefaultValues();
		    
		    session.setAttribute("query",q);		    
		    
			session.removeAttribute("citationIDsMedline");
			session.removeAttribute("citationIDsClinicalTrials");
			session.removeAttribute("displayedCitations");
			session.removeAttribute("displayedCitationsMedline");
			session.removeAttribute("displayedCitationsClinicalTrials");			
			session.removeAttribute("predications");
			session.removeAttribute("relevantConcs");
			session.removeAttribute("summaryPredications");
			session.removeAttribute("predicationType");			
			session.removeAttribute("summaryType");
			session.removeAttribute("selectedSeed");		
			session.removeAttribute("saliency");
			session.removeAttribute("selectedSaliencyType");
			session.removeAttribute("lang");
			session.removeAttribute("translateSelectedSeed");
			session.removeAttribute("executionTime");
			session.removeAttribute("pubmedExtraOptions");
		}
			
		return mapping.findForward("success");
	}

}

