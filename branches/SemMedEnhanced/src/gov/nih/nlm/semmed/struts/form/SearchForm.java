//Created by MyEclipse Struts
// XSL source (default): platform:/plugin/com.genuitec.eclipse.cross.easystruts.eclipse_4.0.1/xslt/JavaClass.xsl

package gov.nih.nlm.semmed.struts.form;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

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
 * @struts.form name="SearchForm"
 */
public class SearchForm extends ActionForm {

	private static SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	// --------------------------------------------------------- Instance Variables
	private static Log log = LogFactory.getLog(SearchForm.class);

	/** term property */
	private String term;
	/** sources property */
	private ArrayList sources;
	/** selectedSource property */
	private String selectedSource;
	/** citCounts property */
	private String[] citCounts;
	/** selectedcitCount property */
	private String selectedCitCount;
	/** startDate property */
	private String startDate;
	/** endDate property */
	private String endDate;
	/** selectedMaxRank property */
	private String selectedMaxRank;
	/** recruiting property */
	private boolean recruiting;
	/** uploadFilename property */
	private String uploadFilename;
	/** uploading citation File */
	private FormFile uploadCitationFile;
	/** uploading citation File */
	private FormFile uploadSemrepFile;
	private String emailAddress;


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
		/* Enumeration en = request.getParameterNames();
		    while (en.hasMoreElements()) {
			String paramName = (String)en.nextElement();
			log.debug(paramName +"|" + request.getParameter(paramName));
		} */
		if (method != null) {
			if (method.length() > 100) {
				errors.add("search", new ActionError("error.method.invalid"));
				return errors;
			}
			if (method.equals("Search") && (term == null || term.trim().length() == 0)) {
				errors.add("search", new ActionError("error.searchterm.required"));
			}

			if (method.equals("Search")) {
				// Ignore the selectedSource for a while, Dongwook Shin 07/21/2008
				if (request.getParameter("selectedSource").equals("medline")) {
					try {
						df.parse(startDate);
					} catch (ParseException pe) {
						errors.add("startDate", new ActionError("error.searchdate.parse",
								"Start Date"));
					} catch(NullPointerException ne){
						errors.add("startDate", new ActionError("error.searchdate.parse",
						"Start Date"));
					}
					try {
						df.parse(endDate);
					} catch (ParseException pe) {
						errors.add("endDate", new ActionError("error.searchdate.parse",
								"End Date"));
					} catch(NullPointerException ne){
						errors.add("startDate", new ActionError("error.searchdate.parse",
						"End Date"));
					}
				}
			}

			if(method.equals("Upload Citation") && (uploadCitationFile == null || uploadCitationFile.getFileName().trim().length() == 0) &&
					method.equals("Upload Citation File")) {
					errors.add("uploadFilename", new ActionError("error.uploadfile.required"));
				}
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
	 * Returns the term.
	 * @return String
	 */
	public String getTerm() {
		return term;
	}

	/**
	 * Set the term.
	 * @param search The term to set
	 */
	public void setTerm(String term) {
		this.term = term;
	}

	/**
	 * @return Returns the citCounts.
	 */
	public String[] getCitCounts() {
		return citCounts;
	}

	/**
	 * @param citCounts The citCounts to set.
	 */
	public void setCitCounts(String[] citCounts) {
		this.citCounts = citCounts;
	}

	/**
	 * @return Returns the searchTypes.
	 */
/*	public ArrayList getSearchTypes() {
		return searchTypes;
	}*/

	/**
	 * @param searchTypes The searchTypes to set.
	 */
/*	public void setSearchTypes(ArrayList searchTypes) {
		this.searchTypes = searchTypes;
	}*/

	/**
	 * @return Returns the selectedCitCount.
	 */
	public String getSelectedCitCount() {
		return selectedCitCount;
	}

	/**
	 * @param selectedCitCount The selectedCitCount to set.
	 */
	public void setSelectedCitCount(String selectedCitCount) {
		this.selectedCitCount = selectedCitCount;
	}

	/**
	 * @return Returns the selectedSearchType.
	 */
/*	public String getSelectedSearchType() {
		return selectedSearchType;
	}*/

	/**
	 * @param selectedSearchType The selectedSearchType to set.
	 */
/*	public void setSelectedSearchType(String selectedSearchType) {
		this.selectedSearchType = selectedSearchType;
	}*/

	/**
	 * @return Returns the selectedSource.
	 */
	public String getSelectedSource() {
		return selectedSource;
	}

	/**
	 * @param selectedSource The selectedSource to set.
	 */
	public void setSelectedSource(String selectedSource) {
		this.selectedSource = selectedSource;
	}

	/**
	 * @return Returns the sources.
	 */
	public ArrayList getSources() {
		return sources;
	}

	/**
	 * @param sources The sources to set.
	 */
	public void setSources(ArrayList sources) {
		this.sources = sources;
	}

	/**
	 * @return Returns the endDate.
	 */
	public String getEndDate() {
		return endDate;
	}

	/**
	 * @param endDate The endDate to set.
	 */
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	/**
	 * @return Returns the startDate.
	 */
	public String getStartDate() {
		return startDate;
	}

	/**
	 * @param startDate The startDate to set.
	 */
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	/**
	 * @return Returns the maxRank.
	 */
	public String getSelectedMaxRank() {
		return selectedMaxRank;
	}

	/**
	 * @param maxRank The maxRank to set.
	 */
	public void setSelectedMaxRank(String maxRank) {
		this.selectedMaxRank = maxRank;
	}

	/**
	 * @return Returns the recruiting.
	 */
	public boolean isRecruiting() {
		return recruiting;
	}

	/**
	 * @param recruiting The recruiting to set.
	 */
	public void setRecruiting(boolean recruiting) {
		this.recruiting = recruiting;
	}

	/**
	 * @return Returns the uploadFile.
	 */
	public FormFile getUploadCitationFile() {
		return uploadCitationFile;
	}

	/**
	 * @param uploadFile The uploadFile to set.
	 */
	public void setUploadCitationFile(FormFile uploadCitationFile) {
		this.uploadCitationFile = uploadCitationFile;
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
	 * @return Returns the uploadSemrepFile.
	 */
	public FormFile getUploadSemrepFile() {
		return uploadSemrepFile;
	}

	/**
	 * @param uploadFile The uploadSemrepFile to set.
	 */
	public void setUploadSemrepFile(FormFile uploadSemrepFile) {
		this.uploadSemrepFile = uploadSemrepFile;
	}

	/**
	 * @return Returns the emailAddress.
	 */
	public String getEmailAddress() {
		return emailAddress;
	}

	/**
	 * @param emailAddress
	 */
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

}

