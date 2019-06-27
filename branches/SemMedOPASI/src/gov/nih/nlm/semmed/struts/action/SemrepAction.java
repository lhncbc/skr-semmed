//Created by MyEclipse Struts
// XSL source (default): platform:/plugin/com.genuitec.eclipse.cross.easystruts.eclipse_4.0.1/xslt/JavaClass.xsl

package gov.nih.nlm.semmed.struts.action;

import gov.nih.nlm.semmed.exception.EssieException;
import gov.nih.nlm.semmed.exception.PubmedException;
import gov.nih.nlm.semmed.exception.SemMedException;
import gov.nih.nlm.semmed.exception.UploadException;
import gov.nih.nlm.semmed.exception.XMLException;
import gov.nih.nlm.semmed.model.APredication;
import gov.nih.nlm.semmed.model.APredicationList;
import gov.nih.nlm.semmed.model.TestPredicationList;
import gov.nih.nlm.semmed.struts.form.SemrepForm;
import gov.nih.nlm.semmed.util.ArticleDataSource;
import gov.nih.nlm.semmed.util.HibernateSessionFactory;
import gov.nih.nlm.semmed.util.XMLUtils;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletContext;


import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.LookupDispatchAction;
import org.apache.struts.upload.FormFile;
import org.apache.struts.util.LabelValueBean;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * MyEclipse Struts
 * Creation date: 01-10-2006
 *
 * XDoclet definition:
 * @struts.action path="/Semrep" name="SemrepForm" input="/jsp/semrep.jsp" scope="request" validate="true"
 * @struts.action-forward name="success" path="/jsp/semrep.jsp" contextRelative="true"
 * @struts.action-forward name="failure" path="/jsp/semrep.jsp" contextRelative="true"
 */
public class SemrepAction extends LookupDispatchAction {

	private static Log log = LogFactory.getLog(SemrepAction.class);

	protected Map<String,String> getKeyMethodMap() {
	      Map<String,String> map = new HashMap<String,String>();
	      map.put("semrep.button.process", "process");
	      map.put("semrep.button.upload", "upload");
	      map.put("semrep.button.export", "exportToXml");
	      map.put("semrep.button.uploaddb", "uploaddb");

	      return map;
	  }


	@Override
	public ActionForward unspecified(ActionMapping mapping,
            ActionForm form,
            javax.servlet.http.HttpServletRequest request,
            javax.servlet.http.HttpServletResponse response)
	throws PubmedException, EssieException, SemMedException, Exception {
		if ("process".equals(request.getParameter("method")))
				return process(mapping,form,request,response);
		else if ("uploaddb".equals(request.getParameter("method")))
			return uploaddb(mapping,form,request,response);
		else
			return super.unspecified(mapping, form, request, response);

	}


	/**
	 * Method process
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 */
	public ActionForward process(
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response) throws SemMedException{

		HttpSession session = request.getSession();

		long startAll = System.nanoTime();
		String sourceNames[] = new String[]{"Medline","ClinicalTrials", "OPASI"};
		int page = 0;

		if (request.getParameter("p")==null){
			List<APredication> allPredications = null;
			try {
					for(String sourceName : sourceNames) {
						if (session.getAttribute("citationIDs"+sourceName)==null)
							continue;

						ArticleDataSource.SourceType type = null;
						if ("Medline".equals(sourceName))
							type = ArticleDataSource.SourceType.MEDLINE;
						else if ("ClinicalTrials".equals(sourceName))
							type = ArticleDataSource.SourceType.CLINICAL_TRIALS;
						else if ("OPASI".equals(sourceName))
							type = ArticleDataSource.SourceType.MEDLINE;

						// log.debug("Source name = " + sourceName);
						int[] ids = (int[])session.getAttribute("citationIDs"+sourceName);
						List<APredication> predications = new APredicationList(ids,type);
						if (allPredications==null)
							allPredications = predications;
						else
							allPredications.addAll(predications);
					} // for
				// } // else
			}catch(Exception e){
				e.printStackTrace();
				throw new SemMedException(e);
			}
			session.setAttribute("predications",allPredications);
			session.setAttribute("predsource", 1);
			session.setAttribute("pageNumberSemrep", 0);
			session.removeAttribute("summaryPredications");
		}else{

			try{
				page = Integer.parseInt(request.getParameter("p"));
			}catch(Exception e){
				page = 0;
			}
		}

		List<APredication> predications = (APredicationList)session.getAttribute("predications");
		// log.debug("The number of predication = " + predications.size());

		int[] PIDs = new int[Math.min(20, predications.size()-page*20)];
		int[] SIDs = new int[PIDs.length];
		ArticleDataSource.SourceType[] sources = new ArticleDataSource.SourceType[PIDs.length];

		for(int i=0;i<PIDs.length;i++){
			PIDs[i] = predications.get(i+page*20).PID;
			SIDs[i] = predications.get(i+page*20).SID;
			sources[i] = predications.get(i+page*20).source;
			// log.debug("PIDs[" + i + "] =" + PIDs[i]);
			// log.debug("SIDs[" + i + "] =" + SIDs[i]);
			// log.debug("sources[" + i + "] =" + sources[i]);
		}
		// log.debug("PID length = " + PIDs.length);

		try{
			// if (session.getAttribute("grantLow") == null)
			List<APredication> displayedPredications = new APredicationList(PIDs,SIDs,sources);
			session.setAttribute("displayedPredications",displayedPredications);
			// log.debug("The number of displayed predication = " + displayedPredications.size());
			// else
			//	session.setAttribute("displayedPredications",new APredicationList(PIDs,SIDs,null));
		}catch(Exception e){
			e.printStackTrace();
			throw new SemMedException(e);
		}
		session.setAttribute("pageNumberSemrep", page);

		session.setAttribute("executionTime", System.nanoTime()-startAll);
		return mapping.findForward("success");
	}


	/**
	 * Method exportToXml
	 *
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
	        response.setHeader("Content-disposition", "attachment;filename=semrep-results.xml");
    		Writer out = new OutputStreamWriter(response.getOutputStream(),"UTF8");
			XMLOutputter serializer = new XMLOutputter(Format.getPrettyFormat());
			serializer.output(doc,out);
        } catch (Exception e) {
        	e.printStackTrace();
        	throw new XMLException(e);
        }
        return null;
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
		SemrepForm SemrepForm = (SemrepForm) form;
		HttpSession session = request.getSession();
		FormFile file = SemrepForm.getUploadFile();
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
	 * Method upload
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 */
	public ActionForward uploaddb(
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response) throws UploadException {

		HttpSession session = request.getSession();
		String sourceName = "Medline";
		ArticleDataSource.SourceType type = ArticleDataSource.SourceType.MEDLINE;
		int page=0;
		List<APredication> predications = null;
		if (request.getParameter("p")==null){
			try {
				int[] ids = (int[])session.getAttribute("citationIDs"+sourceName);
				predications = new TestPredicationList(ids,type);
				session.setAttribute("predications",predications);
				session.setAttribute("predsource",2);
				session.setAttribute("pageNumberSemrep", 0);
			}catch(Exception e){
				e.printStackTrace();
				throw new UploadException(e);
			}

		}else{

			try{
				page = Integer.parseInt(request.getParameter("p"));
			}catch(Exception e){
				page = 0;
			}
		}

		predications = (APredicationList)session.getAttribute("predications");

		int[] PIDs = new int[Math.min(20, predications.size()-page*20)];
		int[] SIDs = new int[PIDs.length];
		ArticleDataSource.SourceType[] sources = new ArticleDataSource.SourceType[PIDs.length];
			for(int i=0;i<PIDs.length;i++){
				PIDs[i] = predications.get(i+page*20).PID;
				SIDs[i] = predications.get(i+page*20).SID;
				sources[i] = predications.get(i+page*20).source;
			}

			try{
				session.setAttribute("displayedPredications",new TestPredicationList(PIDs,SIDs,sources));
			}catch(Exception e){
				e.printStackTrace();
				throw new UploadException(e);
			}
			session.setAttribute("pageNumberSemrep", page);

			return mapping.findForward("success");
	}
}

