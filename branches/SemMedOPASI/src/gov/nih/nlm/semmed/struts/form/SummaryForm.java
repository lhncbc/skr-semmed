//Created by MyEclipse Struts
// XSL source (default): platform:/plugin/com.genuitec.eclipse.cross.easystruts.eclipse_4.0.1/xslt/JavaClass.xsl

package gov.nih.nlm.semmed.struts.form;

import gov.nih.nlm.semmed.model.APredication;
import gov.nih.nlm.semmed.model.APredicationList;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;

/**
 * MyEclipse Struts
 * Creation date: 01-24-2006
 *
 * XDoclet definition:
 * @struts.form name="SummaryForm"
 */
public class SummaryForm extends ActionForm {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	// --------------------------------------------------------- Instance Variables
	private static Log log = LogFactory.getLog(SummaryForm.class);

	/** inputType property */
	private String inputType;
	/** uploadFilename property */
	private String uploadFilename;
	/** uploadFile property */
	private FormFile uploadFile;
	/** relevance property */
	//private boolean relevance;
	/** connectivity property */
	//private boolean connectivity;
	/** novelty property */
	//private boolean novelty;
	/** saliency property */
	private boolean saliency;
	/** seed concept property */
	private String seed;
	/** saliency output types */
//	private ArrayList saliencyTypes;
	/** selectedType property */
	private String selectedSaliencyType;
	/** summaryTypes property */
//	private ArrayList summaryTypes;
	/** summaryType property */
	private String summaryType;


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
		// log.debug("Method : " + method);
		if ((uploadFile == null || uploadFile.getFileName().trim().length() == 0) && method!=null &&
			method.equals("Upload File")) {
			errors.add("uploadFilename", new ActionError("error.uploadfile.required"));
		}
		if (((method!=null && method.equals("Summarize")) || method.equals("findRelevant")) &&
			(request.getSession().getAttribute("predications") == null ||
			 ((APredicationList)request.getSession().getAttribute("predications")).size() == 0)) {
			errors.add("summarize", new ActionError("error.empty.predications"));
		}

		if (method!=null && method.equals("Export to XML")) {
			List<APredication> srs = (List<APredication>)request.getSession().getAttribute("summaryPredications");
			if (srs == null || srs.size() == 0)
				errors.add("exportToXml", new ActionError("error.empty.summary.predications"));
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
/*		setRelevance(false);
		setConnectivity(false);
		setNovelty(false);*/
		setSaliency(false);
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

	/**
	 * @return Returns the connectivity.
	 */
/*	public boolean getConnectivity() {
		return connectivity;
	}*/

	/**
	 * @param connectivity The connectivity to set.
	 */
/*	public void setConnectivity(boolean connectivity) {
		this.connectivity = connectivity;
	}
*/
	/**
	 * @return Returns the novelty.
	 */
/*	public boolean getNovelty() {
		return novelty;
	}*/

	/**
	 * @param novelty The novelty to set.
	 */
/*	public void setNovelty(boolean novelty) {
		this.novelty = novelty;
	}*/

	/**
	 * @return Returns the relevance.
	 */
/*	public boolean getRelevance() {
		return relevance;
	}*/

	/**
	 * @param relevance The relevance to set.
	 */
/*	public void setRelevance(boolean relevance) {
		this.relevance = relevance;
	}*/

	/**
	 * @return Returns the saliency.
	 */
	public boolean getSaliency() {
		return saliency;
	}

	/**
	 * @param saliency The saliency to set.
	 */
	public void setSaliency(boolean saliency) {
		this.saliency = saliency;
	}

	/**
	 * @return Returns the seed.
	 */
	public String getSeed() {
		return seed;
	}

	/**
	 * @param seed The seed to set.
	 */
	public void setSeed(String seed) {
		this.seed = seed;
	}

	/**
	 * @return Returns the selectedSaliencyType.
	 */
	public String getSelectedSaliencyType() {
		return selectedSaliencyType;
	}

	/**
	 * @param selectedSaliencyType The selectedSaliencyType to set.
	 */
	public void setSelectedSaliencyType(String selectedSaliencyType) {
		this.selectedSaliencyType = selectedSaliencyType;
	}

	/**
	 * @return Returns the summaryType.
	 */
	public String getSummaryType() {
		return summaryType;
	}

	/**
	 * @param summaryType The summaryType to set.
	 */
	public void setSummaryType(String summaryType) {
		this.summaryType = summaryType;
	}

}

