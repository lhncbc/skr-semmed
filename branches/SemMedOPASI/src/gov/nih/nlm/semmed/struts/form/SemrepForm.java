/**
 *
 */
package gov.nih.nlm.semmed.struts.form;

import gov.nih.nlm.semmed.model.APredicationList;
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
public class SemrepForm extends ActionForm {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	// --------------------------------------------------------- Instance Variables
//	private static Log log = LogFactory.getLog(SemrepForm.class);

	/** inputType property */
	private String inputType;
	/** uploadFilename property */
	private String uploadFilename;
	/** uploadFile property */
	private FormFile uploadFile;
	/** predicationTypes property */
	private ArrayList predicationTypes;
	/** predicationType property */
	private String predicationType;
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
		String method = request.getParameter("method");
		if ((uploadFile == null || uploadFile.getFileName().trim().length() == 0) &&
			method.equals("Upload File")) {
			errors.add("uploadFilename", new ActionError("error.uploadfile.required"));
		}
		if (method.equals("Get Predications")){
			boolean someCitations = false;
			// for(String source : new String[]{"OPASI"})
				if (request.getSession().getAttribute("citationIDsOPASI")==null)
					errors.add("process", new ActionError("error.empty.citlist"));
			//		break;
			//	}
			/* if(!someCitations) {
				if(request.getSession().getAttribute("grantLow") != null); // If given sampling query, pass the validation
				someCitations = true;
			}
			if (!someCitations)
				errors.add("process", new ActionError("error.empty.citlist")); */
		}

		if (method.equals("Export to XML")) {
			APredicationList srs = (APredicationList)request.getSession().getAttribute("predications");
			if (srs == null || srs.size() == 0)
				errors.add("exportToXml", new ActionError("error.empty.predications"));
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
	 * @return Returns the predicationType.
	 */
	public String getPredicationType() {
		return predicationType;
	}

	/**
	 * @param predicationType The predicationType to set.
	 */
	public void setPredicationType(String predicationType) {
		this.predicationType = predicationType;
	}

	/**
	 * @return Returns the predicationTypes.
	 */
	public ArrayList getPredicationTypes() {
		return predicationTypes;
	}

	/**
	 * @param predicationTypes The predicationTypes to set.
	 */
	public void setPredicationTypes(ArrayList predicationTypes) {
		this.predicationTypes = predicationTypes;
	}

}
