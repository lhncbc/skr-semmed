//Created by MyEclipse Struts
// XSL source (default): platform:/plugin/com.genuitec.eclipse.cross.easystruts.eclipse_4.0.1/xslt/JavaClass.xsl

package gov.nih.nlm.semmed.struts.action;

import gov.nih.nlm.semmed.exception.DbConnectionException;
import gov.nih.nlm.semmed.exception.UploadException;
import gov.nih.nlm.semmed.exception.XMLException;
import gov.nih.nlm.semmed.model.Concept;
import gov.nih.nlm.semmed.model.PredicationArgument;

import gov.nih.nlm.semmed.model.SemrepResultSet;
import gov.nih.nlm.semmed.model.SentencePredication;
import gov.nih.nlm.semmed.struts.form.TranslateForm;

import gov.nih.nlm.semmed.util.HibernateSessionFactory;

import gov.nih.nlm.semmed.util.XMLUtils;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.SQLException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.LookupDispatchAction;
import org.apache.struts.upload.FormFile;
import org.hibernate.JDBCException;
import org.hibernate.Session;
import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/** 
 * MyEclipse Struts
 * Creation date: 05-23-2006
 * 
 * XDoclet definition:
 * @struts.action path="/Translate" name="jsp/translate.jsp" input="jsp/translate.jsp" scope="request" validate="true"
 * @struts.action-forward name="success" path="/jsp/translate.jsp" contextRelative="true"
 * @struts.action-forward name="failure" path="/jsp/translate.jsp" contextRelative="true"
 */
public class TranslateAction extends LookupDispatchAction {

	// --------------------------------------------------------- Instance Variables
	private static Log log = LogFactory.getLog(TranslateAction.class);	
	
	// --------------------------------------------------------- Methods

	protected Map<String,String> getKeyMethodMap() {
	      Map<String,String> map = new HashMap<String,String>();
	      map.put("translate.button.process", "process");
	      map.put("translate.button.upload", "upload");
	      map.put("translate.button.export", "exportToXml");
    
	      return map;
	  }
	
	/** 
	 * Method process
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 */
	public ActionForward process (
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response) throws DbConnectionException {
		TranslateForm TranslateForm = (TranslateForm) form;
		// TODO Auto-generated method stub
		HttpSession session = request.getSession();		
		String lang = TranslateForm.getLanguage();	
		session.setAttribute("lang", lang);
		
		if (!HibernateSessionFactory.isSessionValid()) {
			int retryCount = 5;
			boolean loadComplete = false;			
			Map<Long,Set> loadedConcepts = new HashMap<Long,Set>();
			do {
				try {
					Session hb_session = HibernateSessionFactory.currentSession();
					Iterator predIter = ((SemrepResultSet)session.getAttribute("summaryPredications")).getSentencePredications().iterator();
					while (predIter.hasNext()) {
						SentencePredication sp = (SentencePredication)predIter.next();
						Iterator argIter = sp.getPredication().getPredicationArgumentSet().iterator();
						while (argIter.hasNext()) {
							Concept c = ((PredicationArgument)argIter.next()).getConceptSemtype().getConcept();	
							Long id = c.getConceptId();
							log.debug("Concept: " + id);
							if (!loadedConcepts.containsKey(id)) {
								hb_session.load(c, id);
								loadedConcepts.put(id, c.getConceptTranslationSet());
							} else {
								c.setConceptTranslationSet((Set)loadedConcepts.get(id));
							}
						}
					}
					loadComplete = true;
				} catch (JDBCException je) {
					SQLException sqle = (SQLException)je.getCause();
					String sqlState = sqle.getSQLState();
					log.debug("Error:" + sqlState);
		            if ("08S01".equals(sqlState) || "40001".equals(sqlState)) {            	
		                retryCount--;
		            } else {
		                retryCount = 0;
		            }
				}
				catch (Exception e) {
					retryCount = 0;
					e.printStackTrace();	
					throw new DbConnectionException(e);
				} 
			} while (!loadComplete && retryCount > 0);
		}
		
		SemrepResultSet preds = (SemrepResultSet)session.getAttribute("summaryPredications");
		String trSeed = preds.translate((String)session.getAttribute("selectedSeed"), lang);
		log.debug("Translate Seed: " + trSeed);
		session.setAttribute("translateSelectedSeed", trSeed);
		return mapping.findForward("success");
	}
	
	/** 
	 * Method upload
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 */	
	public ActionForward upload(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws UploadException {
			TranslateForm TranslateForm = (TranslateForm) form;
			HttpSession session = request.getSession();
			FormFile file = TranslateForm.getUploadFile();
			log.debug("Request type: " + request.getContentType());
			
			try {
				byte[] fileData    = file.getFileData();
				session = XMLUtils.uploadXML(fileData, session);				
				if (HibernateSessionFactory.isSessionValid()) HibernateSessionFactory.closeSession();				
				return mapping.findForward("success");			
			} catch (Exception e) {
				e.printStackTrace();
				throw new UploadException(e);
			}
		}		

	
	/** 
	 * Method exportToXml
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 */
	public ActionForward exportToXml(
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response) throws XMLException {
		HttpSession session = request.getSession();
        try {
			Document doc = XMLUtils.exportToXml(session);        	
	        response.setContentType("application/octet-stream");
	        response.setHeader("Content-disposition", "attachment;filename=translation-results.xml");	   		     	
    		Writer out = new OutputStreamWriter(response.getOutputStream(),"UTF8");				
			XMLOutputter serializer = new XMLOutputter(Format.getPrettyFormat());
			serializer.output(doc,out); 	    		
        } catch (Exception e) {
        	e.printStackTrace();
        	throw new XMLException(e);	        	
        } 
		return null;
	}
	
}

