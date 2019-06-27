//Created by MyEclipse Struts
// XSL source (default): platform:/plugin/com.genuitec.eclipse.cross.easystruts.eclipse_4.0.1/xslt/JavaClass.xsl

package gov.nih.nlm.semmed.struts.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;


/** 
 * MyEclipse Struts
 * Creation date: 01-10-2006
 * 
 * XDoclet definition:
 * @struts.action path="/InitializeSemrep" name="SemrepForm" scope="request" validate="true"
 * @struts.action-forward name="success" path="semrep.jsp" contextRelative="true"
 * 
 * 
 * This class doesn't do anything at the moment [Alejandro]
 * 
 * 
 */
public class InitializeSemrepAction extends Action {

	// --------------------------------------------------------- Instance Variables
//	private static Log log = LogFactory.getLog(InitializeSemrepAction.class);

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
		return mapping.findForward("success");
	}

}

