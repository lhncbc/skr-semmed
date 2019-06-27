//Created by MyEclipse Struts
// XSL source (default): platform:/plugin/com.genuitec.eclipse.cross.easystruts.eclipse_4.0.1/xslt/JavaClass.xsl

package gov.nih.nlm.semmed.struts.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;


/** 
 * MyEclipse Struts
 * Creation date: 01-10-2006
 * 
 * XDoclet definition:
 * @struts.action path="/InitializeTranslate" name="TranslateForm" scope="request" validate="true"
 * @struts.action-forward name="success" path=translate.jsp" contextRelative="true"
 */
public class InitializeTranslateAction extends Action {

	// --------------------------------------------------------- Instance Variables
//	private static Log log = LogFactory.getLog(InitializeTranslateAction.class);

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
	
//		TranslateForm TranslateForm = (TranslateForm) form;
//		HttpSession session = request.getSession();		
		
	    /*if (session.getAttribute("lang") == null) {
	    	ServletContext context = session.getServletContext();
	    	ArrayList langs = (ArrayList)context.getAttribute("languages");
	    	session.setAttribute("lang",((LabelValueBean)langs.get(0)).getValue());
	    } */
		return mapping.findForward("success");
	}

}