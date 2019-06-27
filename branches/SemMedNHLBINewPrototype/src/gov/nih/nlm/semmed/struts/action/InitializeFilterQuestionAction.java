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
 * @struts.action path="/Welcome" scope="request"
 * @struts.action-forward name="success" path="/jsp/filterquestion.jsp" contextRelative="true"
 *
 *
 *
 * If there is no query in session, creates and adds one and removes all query
 * attributes from the session    [shindongwoo]
 *
 *
 */
public class InitializeFilterQuestionAction extends Action {

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

		return mapping.findForward("success");
	}

}

