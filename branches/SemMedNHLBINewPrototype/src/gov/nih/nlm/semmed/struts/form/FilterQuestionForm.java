package gov.nih.nlm.semmed.struts.form;

import java.text.ParseException;

import gov.nih.nlm.semmed.model.APredicationList;
import gov.nih.nlm.semmed.struts.action.FilterQuestionAction;

import javax.servlet.http.HttpServletRequest;

//import org.apache.struts.action.ActionError;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;

/**
 * MyEclipse Struts
 * Creation date: 12-02-2005
 *
 * XDoclet definition:
 * @struts.form name="FilterQuestionForm"
 */
public class FilterQuestionForm extends ActionForm {

	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(FilterQuestionForm.class);
	/** term property */
	private String questionNumber;
	private String[] riskFactor;
	private String[] disorder;
	/** uploading rule File */
	private FormFile uploadRuleFile;


	/**
	 * Method validate
	 * @param mapping
	 * @param request
	 * @return ActionErrors
	 */
	public ActionErrors validate(
		ActionMapping mapping,
		HttpServletRequest request) {
		// TODO Auto-generated method stub
		ActionErrors errors = new ActionErrors();
		String method = request.getParameter("method");
		if (method != null) {
			if (method.equals("Select") && (questionNumber == null || questionNumber.trim().length() == 0)) {
				// errors.add("questionNumber", new ActionError("error.questionnumber.required"));
				log.debug("Question is not selected");
				errors.add("questionNumber", new ActionError("error.questionnumber.required", "questionNumber"));
			} /* else {
				if(method.equals("Select") && (riskFactor[Integer.parseInt(questionNumber)].compareTo("Select Risk Factor") == 0)) {
					log.debug("Risk Factor is not selected");
					errors.add("riskFactor", new ActionError("error.riskfactor.required", "riskFactor"));
				}
				if(method.equals("Select") && (disorder[Integer.parseInt(questionNumber)].compareTo("Select Disorder") == 0)) {
					log.debug("Disorder is not selected");
					errors.add("disorder", new ActionError("error.disorder.required", "disorder"));
				}
			} */
			if(method.equals("Upload Rule") && (uploadRuleFile == null || uploadRuleFile.getFileName().trim().length() == 0) &&
					method.equals("Upload Rule File")) {
					errors.add("uploadFilename", new ActionError("error.uploadRulefile.required"));
				}
		}
		return errors;
	}

	public void reset(ActionMapping mapping, HttpServletRequest request) {

	}

	public String getQuestionNumber() {
		return questionNumber;
	}

	public void setQuestionNumber(String questionNumber) {
		this.questionNumber = questionNumber;
	}

	public String[] getRiskFactor() {
		return riskFactor;
	}

	public void setRiskFactor(String[] riskFactor) {
		this.riskFactor = riskFactor;
	}

	public String[] getDisorder() {
		return disorder;
	}

	public void setDisorder(String[] disorder) {
		this.disorder = disorder;
	}
	/**
	 * @return Returns the uploadFile.
	 */
	public FormFile getUploadRuleFile() {
		return uploadRuleFile;
	}

	/**
	 * @param uploadFile The uploadFile to set.
	 */
	public void setUploadRuleFile(FormFile uploadRuleFile) {
		this.uploadRuleFile = uploadRuleFile;
	}

}