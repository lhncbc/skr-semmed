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
 * @struts.form name="QuestionForm"
 */
public class QuestionForm extends ActionForm {

	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(QuestionForm.class);
	/** term property */
	private String questionNumber;
	/** uploadFilename property */
	/** uploading citation File */
	private FormFile uploadIncludeFile;
	private FormFile uploadExcludeFile;


	/**
	 * Method validate
	 * @param mapping
	 * @param request
	 * @return ActionErrors
	 */
	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
		ActionErrors errors = new ActionErrors();
		String method = request.getParameter("method");
		log.debug("Method name in Struts framework: " + method);
		if (method.equals("Find Relevant Citations") && (request.getSession().getAttribute("predications")==null ||
				((APredicationList)request.getSession().getAttribute("predications")).size() == 0)) {
				errors.add("question", new ActionError("error.empty.predications"));
			}

		if(method.equals("Upload") && (uploadIncludeFile == null || uploadIncludeFile.getFileName().trim().length() == 0)) {
				errors.add("question", new ActionError("error.includefile.required"));
		}
		if(method.equals("Upload") && (uploadExcludeFile == null || uploadExcludeFile.getFileName().trim().length() == 0)) {
				errors.add("question", new ActionError("error.excludefile.required"));
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

	/**
	 * @return Returns the uploadFile.
	 */
	public FormFile getUploadIncludeFile() {
		return uploadIncludeFile;
	}

	/**
	 * @param uploadFile The uploadFile to set.
	 */
	public void setUploadIncludeFile(FormFile uploadIncludeFile) {
		this.uploadIncludeFile = uploadIncludeFile;
	}

	/**
	 * @return Returns the uploadFile.
	 */
	public FormFile getUploadExcludeFile() {
		return uploadExcludeFile;
	}

	/**
	 * @param uploadFile The uploadFile to set.
	 */
	public void setUploadExcludeFile(FormFile uploadExcludeFile) {
		this.uploadExcludeFile = uploadExcludeFile;
	}

}