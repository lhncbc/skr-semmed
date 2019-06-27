/**
 * 
 */
package gov.nih.nlm.semmed.struts.form;

import gov.nih.nlm.semmed.model.SemrepResultSet;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;

/**
 * @author hkilicoglu
 *
 */
public class TranslateForm extends ActionForm {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// --------------------------------------------------------- Instance Variables
//	private static Log log = LogFactory.getLog(TranslateForm.class);	
	
	/** inputType property */
	private String inputType;	
	/** uploadFilename property */
	private String uploadFilename;
	/** uploadFile property */
	private FormFile uploadFile;
	/** languages property */
	private ArrayList languages;	
	/** language property */
	private String language;	
	// --------------------------------------------------------- Methods
	
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
		if ((uploadFile == null || uploadFile.getFileName().trim().length() == 0) &&
			method.equals("Upload File")) {		
			errors.add("uploadFilename", new ActionError("error.uploadfile.required"));			
		}
		SemrepResultSet srs = (SemrepResultSet)request.getSession().getAttribute("summaryPredications");
		if (method.equals("Translate")) {
			if (srs == null || srs.getSentencePredications() == null || !(srs.getSentencePredications().size() >0)) 
				errors.add("process", new ActionError("error.empty.summary.predications"));
		}
		
		if (method.equals("Export to XML")) {
			if (srs == null || srs.getSentencePredications() == null || !(srs.getSentencePredications().size() >0)) 
				errors.add("exportToXml", new ActionError("error.empty.summary.predications"));
		}
		
		return errors;
	}

	/** 
	 * Method reset
	 * @param mapping
	 * @param request
	 */
	public void reset(
			ActionMapping mapping, 
			HttpServletRequest request) {
		// TODO Auto-generated method stub
	}
	
	/**
	 * @return Returns the uploadFile.
	 */
	public String getUploadFilename() {
		return uploadFilename;
	}

	/**
	 * @param uploadFile The uploadFile to set.
	 */
	public void setUploadFilename(String uploadFilename) {
		this.uploadFilename = uploadFilename;
	}

	/**
	 * @return Returns the inputType.
	 */
	public String getInputType() {
		return inputType;
	}

	/**
	 * @param inputType The inputType to set.
	 */
	public void setInputType(String inputType) {
		this.inputType = inputType;
	}

	/**
	 * @return Returns the uploadFile.
	 */
	public FormFile getUploadFile() {
		return uploadFile;
	}

	/**
	 * @param uploadFile The uploadFile to set.
	 */
	public void setUploadFile(FormFile uploadFile) {
		this.uploadFile = uploadFile;
	}

	/**
	 * @return Returns the language.
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * @param predicationType The language to set.
	 */
	public void setLanguage(String language) {
		this.language = language;
	}

	/**
	 * @return Returns the languages.
	 */
	public ArrayList getLanguages() {
		return languages;
	}

	/**
	 * @param languages The languages to set.
	 */
	public void setLanguages(ArrayList languages) {
		this.languages = languages;
	}	

}


