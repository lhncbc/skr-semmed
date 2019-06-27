package gov.nih.nlm.semmed.servlet;

import gov.nih.nlm.semmed.model.SemMedDocument;
import gov.nih.nlm.semmed.struts.action.SearchAction;
import gov.nih.nlm.semmed.util.ArticleDataSource;
import gov.nih.nlm.semmed.util.MedlineSource;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.*;
import javax.servlet.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author rodriguezal
 *
 */
public class GraphServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(GraphServlet.class);
	private static Map<UUID,Graph> graphs = new HashMap<UUID,Graph>();;
	private Random generator = new Random();

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
		String relevantString = req.getParameter("uploadString");
		// log("key : " + key);
		// log("relevantString : " + relevantString);
		if(key == null) {
			if(req.getParameter("uploadString") != null) {
				relevantString = req.getParameter("uploadString");
				HttpSession session = req.getSession();
				log.debug("Upload relevant document");
				log.debug(relevantString);
				int ran = generator.nextInt();
				String ranString = Integer.toString(ran);
				ServletConfig config = getServletConfig();
				ServletContext ctx = config.getServletContext();
				String xmlurl = req.getScheme( ) + "://" + req.getServerName( ) + req.getContextPath( );
				String usernameKN = (String) session.getAttribute("usernameKN");
				String filename = "/graph-" + usernameKN + ".xml";
				String xmlcontextpath = xmlurl + filename;
				String xmlrealpath = ctx.getRealPath(filename);
				// log("XML context path " + xmlcontextpath);
				// log("XML real path " + xmlrealpath);
				PrintWriter outfile
				= new PrintWriter(new BufferedWriter(new FileWriter(xmlrealpath)));
				outfile.println(relevantString);
				outfile.close();

				resp.setContentType("text/plain");
				// StringWriter sw = new StringWriter();

				// sw.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
				// sw.append("<downloadurl>");
				// sw.append(xmlurl);
				// sw.append("</downloadurl>");

				ServletOutputStream out = resp.getOutputStream();
				out.print(xmlcontextpath);
			}
		} else {
			Graph g = graphs.get(UUID.fromString(key));
			if (g!=null){
				String s;
				if (req.getParameter("doc")==null)
					s = g.graphXML;
				else
					s = g.relevantXML;

				resp.setContentType("application/xml");
				log.debug("graph XML in GraphServlet() = " + s);

				ServletOutputStream out = resp.getOutputStream();
				out.print(s);
			}
		}
	}

	public static UUID addGraph(HttpServletRequest request, String xml,List<Integer> relevant,List<Integer> nonRelevant){
		UUID id = UUID.randomUUID();
		graphs.put(id, new Graph(request, System.currentTimeMillis(),xml,relevant,nonRelevant));
		// log.debug("graph id : " + id);
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
						sw.append("<citation data=\""+d.getId()+"\" label=\""+stringToHTMLString(d.getTitleText())+"\"/>");
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
						sw.append("<citation data=\""+d.getId()+"\" label=\""+stringToHTMLString(d.getTitleText())+"\"/>");
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
}
