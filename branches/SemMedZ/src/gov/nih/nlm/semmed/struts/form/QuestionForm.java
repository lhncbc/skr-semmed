package gov.nih.nlm.semmed.struts.form;

import gov.nih.nlm.semmed.model.APredicationList;

import javax.servlet.http.HttpServletRequest;

//import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/** 
 * MyEclipse Struts
 * Creation date: 12-02-2005
 * 
 * XDoclet definition:
 * @struts.form name="SearchForm"
 */
public class QuestionForm extends ActionForm {

	private static final long serialVersionUID = 1L;

	/** term property */
	private int questionNumber;

	/** 
	 * Method validate
	 * @param mapping
	 * @param request
	 * @return ActionErrors
	 */
	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {		
		ActionErrors errors = new ActionErrors();
		
		if (request.getSession().getAttribute("predications")==null || 
				((APredicationList)request.getSession().getAttribute("predications")).size() == 0) {
				errors.add("question", new ActionError("error.empty.predications"));
			}
			
		return errors;
	}

	public void reset(ActionMapping mapping, HttpServletRequest request) {

	}

	public int getQuestionNumber() {
		return questionNumber;
	}

	public void setQuestionNumber(int questionNumber) {
		this.questionNumber = questionNumber;
	}
		
}