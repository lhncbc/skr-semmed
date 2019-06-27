//Created by MyEclipse Struts
// XSL source (default): platform:/plugin/com.genuitec.eclipse.cross.easystruts.eclipse_4.0.1/xslt/JavaClass.xsl

package gov.nih.nlm.semmed.struts.form;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;

/**
 * MyEclipse Struts
 * Creation date: 02-21-2006
 *
 * XDoclet definition:
 * @struts.form name="VisualizeForm"
 */
public class VisualizeForm extends ActionForm {

	// --------------------------------------------------------- Instance Variables

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/** visualizationType property */
	private String visualizationType;

	/** inputType property */
	private String inputType;
	/** uploadFilename property */
	private String uploadFilename;
	/** uploadFile property */
	private FormFile uploadFile;
	/** seed property */
	private String seed;


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

		ActionErrors errors = new ActionErrors();
		/* if ((uploadFile == null || uploadFile.getFileName().trim().length() == 0) &&
				request.getParameter("method")!=null && request.getParameter("method").equals("Upload File")) {
				errors.add("uploadFilename", new ActionError("error.uploadfile.required"));
		} */
		if (request.getParameter("method")!=null && request.getParameter("method").equals("Visualize") &&
			(request.getSession().getAttribute("summaryPredications") == null ||
			(((List)request.getSession().getAttribute("summaryPredications")).size() <= 0))) {
			errors.add("visualize", new ActionError("error.empty.summary.predications"));
		}
		return errors;
	}

	/**
	 * Method reset
	 * @param mapping
	 * @param request
	 */
	public void reset(ActionMapping mapping, HttpServletRequest request) {

		// TODO Auto-generated method stub
	}

	/**
	 * Returns the visualizationType.
	 * @return String
	 */
	public String getVisualizationType() {
		return visualizationType;
	}

	/**
	 * Set the visualizationType.
	 * @param visualizationType The visualizationType to set
	 */
	public void setVisualizationType(String visualizationType) {
		this.visualizationType = visualizationType;
	}

	/**
	 * Returns the inputType.
	 * @return String
	 */
	public String getInputType() {
		return inputType;
	}

	/**
	 * Set the inputType.
	 * @param inputType The inputType to set
	 */
	public void setInputType(String inputType) {
		this.inputType = inputType;
	}

	/**
	 * Returns the seed.
	 * @return String
	 */
	public String getSeed() {
		return seed;
	}

	/**
	 * Set the seed.
	 * @param seed The seed to set
	 */
	public void setSeed(String seed) {
		this.seed = seed;
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
	 * @return Returns the uploadFilename.
	 */
	public String getUploadFilename() {
		return uploadFilename;
	}

	/**
	 * @param uploadFilename The uploadFilename to set.
	 */
	public void setUploadFilename(String uploadFilename) {
		this.uploadFilename = uploadFilename;
	}

}

