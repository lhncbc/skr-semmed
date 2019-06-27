package gov.nih.nlm.semmed.servlet;

import gov.nih.nlm.semmed.model.SemMedDocument;
import gov.nih.nlm.semmed.struts.action.SearchAction;
import gov.nih.nlm.semmed.util.ArticleDataSource;
import gov.nih.nlm.semmed.util.MedlineSource;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.ObjectOutputStream;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

/**
 *
 * @author rodriguezal
 *
 */
public class GraphServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(GraphServlet.class);
	private static Map<UUID,Graph> graphs = new HashMap<UUID,Graph>();;

	@Override
	public void init() throws ServletException{
		Cleaner cleaner = new Cleaner();
		cleaner.setDaemon(true);
		new Cleaner().start();
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException{
		doPost(req,resp);
	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException{

		String key = req.getParameter("key");
		String grantIDs = req.getParameter("getCitation");
		String[] grantStrArray = null;
		HashSet<Long> grantSet = null;
		StringBuffer query = new StringBuffer();
		log("key : " + key);
		log("grant ID : " + grantIDs);
		if(key == null) {
			if(req.getParameter("getCitation") != null) {
				try {
					/* query.append("Select PMID, TYPE, SENTENCE from SENTENCE where PMID in (");
					for(int i = 0; i < grantArray.length; i++) {
						if(i < grantArray.length-1 && grantArray[i].compareTo("") != 0)
							query.append(grantArray[i] + ",");
						else if (grantArray[i].compareTo("") != 0)
							query.append(grantArray[i] + ")");
						else
							query.append(")");
					}
					Context ctx = new InitialContext();
			          DataSource ds =
			              (DataSource)ctx.lookup("java:comp/env/jdbc/SemMedDB");
			        Connection con = ds.getConnection();
			  		Statement s = con.createStatement();
			  		log.debug("DB query = " + query.toString());
			  		ResultSet rs = s.executeQuery(query.toString());
			  		citationBuf.append("<CitationSet>\n");
			  		String currPMID = null;
			  		String PrevPMID = null;
			  		int numberOfSentInAbstract = 0;
			  		boolean InAbstract = false;
			  		boolean alreadyTitleFound = false;
			  		// List citationList = new ArrayList();
			  		while(rs.next()) {
			  			if(PrevPMID == null && currPMID == null) {
			  				citationBuf.append("<Citation>\n<ID>" + rs.getString(1) + "</ID>\n"); // First citation
			  				currPMID = rs.getString(1).trim();
			  				PrevPMID = currPMID;
			  				if(rs.getString(2).trim().compareTo("ti") == 0)
		  						citationBuf.append("<Title>\n" + escapeXML(rs.getString(3)) + "</Title>\n");
			  			} else if(currPMID != null && rs.getString(1).trim().compareTo(currPMID) != 0) { // New PMID occurred
			  				PrevPMID = currPMID;
			  				currPMID = rs.getString(1).trim();
			  				alreadyTitleFound = false; // reset the variable already title found
			  				InAbstract = false;
			  				if(numberOfSentInAbstract > 0) { // if there is abstract sentence in the previous citation
			  					citationBuf.append("</Abstract>\n</Citation>\n<Citation>\n<ID>" + rs.getString(1) + "</ID>\n");
				  				numberOfSentInAbstract = 0;
			  				} else  {// if there is not an abstract sentence in the previous citation
			  					citationBuf.append("\n</Citation><Citation>\n<ID>" + rs.getString(1) + "</ID>\n");
			  					// citationList.add(citationBuf.toString());
			  					// citationBuf.delete(0,citationBuf.length());
			  					// citationBuf.append("<Citation>\n<ID>" + rs.getString(1) + "</ID>\n");
			  				}
			  				if(rs.getString(2).trim().compareTo("ti") == 0) {
			  						citationBuf.append("<Title>\n" + escapeXML(rs.getString(3)) + "</Title>\n");
			  				} else {
			  					if(InAbstract == false) {
			  						citationBuf.append("<Abstract>\n" + escapeXML(rs.getString(3)));
			  						InAbstract = true;
			  						numberOfSentInAbstract ++;
			  					} else {
			  						citationBuf.append(escapeXML(rs.getString(3) + " "));
			  						numberOfSentInAbstract ++;
			  					}
			  				}
			  			} else if(currPMID != null && rs.getString(1).trim().compareTo(currPMID) == 0) { // Same PMID occurred
			  				if(rs.getString(2).trim().compareTo("ti") == 0) {
			  						citationBuf.append("<Title>\n" + escapeXML(rs.getString(3)) + "</Title>\n");
			  				} else {
			  					if(InAbstract == false) {
			  						citationBuf.append("<Abstract>\n" + escapeXML(rs.getString(3)));
			  						InAbstract = true;
			  						numberOfSentInAbstract ++;
			  					} else {
			  						citationBuf.append(escapeXML(rs.getString(3)+ " "));
			  						numberOfSentInAbstract ++;
			  					}
			  				}
			  			}

			  		}
			  		if(numberOfSentInAbstract == 0)
			  			citationBuf.append("</Citation>\n</CitationSet>");
			  		else
			  			citationBuf.append("</Abstract>\n</Citation>\n</CitationSet>");
			  		// citationList.add(citationBuf.toString()); // Add the last citation
			  		resp.setContentType("application/xml");
			  		log.debug("Citation = \n " + citationBuf.toString());
			  		ServletOutputStream out =  resp.getOutputStream();
					// out.writeObject(citationList);
					out.print(citationBuf.toString()); */

					// using Essie instead of database
					grantIDs = req.getParameter("getCitation");
					grantStrArray = grantIDs.split("EON");
					// grantArray = new long[grantStrArray.length];
					// log.debug("Creating citation reading sentence from OPASI Source");
					StringBuffer xmlOutputBuf = new StringBuffer();
					grantSet = new HashSet<Long>();
					for(int i = 0; i < grantStrArray.length; i++) {
						grantSet.add(new Long(grantStrArray[i]));
					}
					ArticleDataSource ml = ArticleDataSource.getInstance(ArticleDataSource.SourceType.OPASI);
					ml.displayDocTextList(req,  resp, grantSet);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		else {
			Graph g = graphs.get(UUID.fromString(key));
			if (g!=null){
				String s;

				if (req.getParameter("doc")==null)
					s = g.graphXML;
				else
					s = g.relevantXML;

				resp.setContentType("application/xml");
				ServletOutputStream out = resp.getOutputStream();
				out.print(s);
			}
		}
	}

	public static UUID addGraph(HttpServletRequest request, String xml,List<Integer> relevant,List<Integer> nonRelevant){
		UUID id = UUID.randomUUID();
		graphs.put(id, new Graph(request, System.currentTimeMillis(),xml,relevant,nonRelevant));
		return id;
	}

	private static class Graph{
		public String graphXML;
		public String relevantXML;
		public long time;

		public Graph(HttpServletRequest request, long time,String graphXML,List<Integer> relevant,List<Integer> nonRelevant){
			this.time = time;
			this.graphXML = graphXML;

			StringWriter sw = new StringWriter();

			sw.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			sw.append("<citations>");
			sw.append("<relevants>");
			if (relevant!=null){
				try{
					HttpSession session = request.getSession();
					int mainOrTest = (Integer) session.getAttribute("predsource");
					List<? extends SemMedDocument> docs = null;
					if(mainOrTest ==1) {
						docs = ((MedlineSource)ArticleDataSource.getInstance(ArticleDataSource.SourceType.MEDLINE)).fetchTitle(relevant);

					} else {
						docs = ((MedlineSource)ArticleDataSource.getInstance(ArticleDataSource.SourceType.MEDLINE)).fetchTitleFromTest(relevant);
					}
					for(SemMedDocument d:docs)
						sw.append("<citation data=\""+d.getId()+"\" label=\""+ stringToHTMLString(d.getTitleText())+"\"/>");
				}catch(Exception e){
					for(Integer i:relevant)
						sw.append("<citation data=\""+i+"\" label=\""+i+"\"/>");
					e.printStackTrace();
				}
			}
			sw.append("</relevants>");
			sw.append("<nonrelevants>");
			if (nonRelevant!=null)
				try{
					// List<? extends SemMedDocument> docs = ((MedlineSource)ArticleDataSource.getInstance(ArticleDataSource.SourceType.MEDLINE)).fetchTitle(nonRelevant);
					HttpSession session = request.getSession();
					int mainOrTest = (Integer) session.getAttribute("predsource");
					List<? extends SemMedDocument> docs = null;
					if(mainOrTest ==1) {
						docs = ((MedlineSource)ArticleDataSource.getInstance(ArticleDataSource.SourceType.MEDLINE)).fetchTitle(nonRelevant);

					} else {
						docs = ((MedlineSource)ArticleDataSource.getInstance(ArticleDataSource.SourceType.MEDLINE)).fetchTitleFromTest(nonRelevant);
					}
					for(SemMedDocument d:docs)
						sw.append("<citation data=\""+d.getId()+"\" label=\""+ stringToHTMLString(d.getTitleText())+"\"/>");
				}catch(Exception e){
					e.printStackTrace();
					for(Integer i:nonRelevant)
						sw.append("<citation data=\""+i+"\" label=\""+i+"\"/>");
				}
			sw.append("</nonrelevants>");

			sw.append("</citations>");
			relevantXML = sw.toString();
			// log.debug("relevant citations --- ");
			// log.debug(relevantXML);
		}
	}

	private class Cleaner extends Thread{
		public void run(){
			while(true){
				long now = System.currentTimeMillis();
				Iterator<Entry<UUID,Graph>> it = graphs.entrySet().iterator();
				while(it.hasNext()){
					Entry<UUID,Graph> e = it.next();
					if (now-e.getValue().time>30000){
						it.remove();
					}
				}
				try{
					sleep(10000);
				}catch(InterruptedException e){
				}
			}
		}
	}

	public static String stringToHTMLString(String string) {
	    StringBuffer sb = new StringBuffer(string.length());
	    // true if last char was blank
	    boolean lastWasBlankChar = false;
	    int len = string.length();
	    char c;

	    for (int i = 0; i < len; i++)
	        {
	        c = string.charAt(i);
	        if (c == ' ') {
	            // blank gets extra work,
	            // this solves the problem you get if you replace all
	            // blanks with &nbsp;, if you do that you loss
	            // word breaking
	            if (lastWasBlankChar) {
	                lastWasBlankChar = false;
	                sb.append("&nbsp;");
	                }
	            else {
	                lastWasBlankChar = true;
	                sb.append(' ');
	                }
	            }
	        else {
	            lastWasBlankChar = false;
	            //
	            // HTML Special Chars
	            if (c == '"')
	                sb.append("&quot;");
	            else if (c == '&')
	                sb.append("&amp;");
	            else if (c == '<')
	                sb.append("&lt;");
	            else if (c == '>')
	                sb.append("&gt;");
	            else if (c == '\n')
	                // Handle Newline
	                sb.append("&lt;br/&gt;");
	            else {
	                int ci = 0xffff & c;
	                if (ci < 160 )
	                    // nothing special only 7 Bit
	                    sb.append(c);
	                else {
	                    // Not 7 Bit use the unicode system
	                    sb.append("&#");
	                    sb.append(new Integer(ci).toString());
	                    sb.append(';');
	                    }
	                }
	            }
	        }
	    return sb.toString();
	}

	public static String escapeXML(String string) {
	    StringBuffer sb = new StringBuffer(string.length());
	    // true if last char was blank
	    int len = string.length();
	    char c;

	    for (int i = 0; i < len; i++) {
	        c = string.charAt(i);
	            //
	            // HTML Special Chars
	            if (c == '"')
	                sb.append("&quot;");
	            else if (c == '&')
	                sb.append("&amp;");
	            else if (c == '<')
	                sb.append("&lt;");
	            else if (c == '>')
	                sb.append("&gt;");
	            else {
	                int ci = 0xffff & c;
	                if (ci < 160 )
	                    // nothing special only 7 Bit
	                    sb.append(c);
	                else {
	                    // Not 7 Bit use the unicode system
	                    sb.append("&#");
	                    sb.append(new Integer(ci).toString());
	                    sb.append(';');
	                    }
	                }
	    }
	    return sb.toString();
	}
}
