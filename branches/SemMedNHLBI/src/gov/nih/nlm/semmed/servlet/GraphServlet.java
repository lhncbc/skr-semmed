package gov.nih.nlm.semmed.servlet;

import gov.nih.nlm.semmed.model.SemMedDocument;
import gov.nih.nlm.semmed.model.PubmedArticle;
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

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.DoubleArrayDataRow;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.ModelApplier;
import com.rapidminer.operator.Operator;
import com.rapidminer.tools.OperatorService;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
		log("key : " + key);
		log("relevantString : " + relevantString);
		if(key == null) {
			if(req.getParameter("uploadString") != null) {
				relevantString = req.getParameter("uploadString");
				log.debug("Upload relevant document");
				log.debug(relevantString);
				int ran = generator.nextInt();
				String ranString = Integer.toString(ran);
				ServletConfig config = getServletConfig();
				ServletContext ctx = config.getServletContext();
				String xmlurl = req.getScheme( ) + "://" + req.getServerName( ) + req.getContextPath( );

				String filename = "/relevant" + ranString + ".xml";
				String xmlcontextpath = xmlurl + filename;
				String xmlrealpath = ctx.getRealPath(filename);
				log("XML context path " + xmlcontextpath);
				log("XML real path " + xmlrealpath);
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
				log.debug("Download string to Flash : " + s);
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
			HttpSession session = request.getSession();
			HashMap<String, Double> ifhashmap = null; // Hashmap for ImpactFactor
			HashMap<String, String> issnhashmap = null; // Hashmap for ISSN/ESSN map
			if(ifhashmap == null) {
				ifhashmap = (HashMap) request.getSession().getServletContext().getAttribute("ifhashmap");
			}
			if(issnhashmap == null) {
				issnhashmap = (HashMap) request.getSession().getServletContext().getAttribute("issnhashmap");
			}
			List attributes = (List) request.getSession().getServletContext().getAttribute("rapidMinerAttributes");

			sw.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			sw.append("<citations>");
			sw.append("<relevants>");
			if (relevant!=null){
				try{
					int mainOrTest = (Integer) session.getAttribute("predsource");
					List<? extends SemMedDocument> docs = null;
					if(mainOrTest ==1) {
						// docs = ((MedlineSource)ArticleDataSource.getInstance(ArticleDataSource.SourceType.MEDLINE)).fetchTitle(relevant);
						docs = ((MedlineSource)ArticleDataSource.getInstance(ArticleDataSource.SourceType.MEDLINE)).fetch(null, relevant);

					} else {
						// docs = ((MedlineSource)ArticleDataSource.getInstance(ArticleDataSource.SourceType.MEDLINE)).fetchTitleFromTest(relevant);
						docs = ((MedlineSource)ArticleDataSource.getInstance(ArticleDataSource.SourceType.MEDLINE)).fetch(null, relevant);
					}
					IOContainer container = (IOContainer) request.getSession().getServletContext().getAttribute("rapidMinerContainer");
					MemoryExampleTable table = new MemoryExampleTable(attributes);
					for(SemMedDocument d:docs) {
						// sw.append("<citation data=\""+d.getId()+"\" label=\""+stringToHTMLString(d.getTitleText())+"\"/>");
						PubmedArticle p = (PubmedArticle) d;
						Set metadata = (HashSet)p.getMetadata();
						log.debug("PMID: " + p.getId());
						// log.debug("Mesh Heading:" + metadata.toString());
						double[] data = new double[attributes.size()];
						for ( int a=0; a<attributes.size(); a++) {
							String name = ((Attribute)attributes.get(a)).getName();
							if (metadata.contains(name)) {
								data[a] = 1.0;
							} else { data[a] = 0.0; }
						}
						table.addDataRow(new DoubleArrayDataRow(data));
						String issn1 = p.getIssn();
						if(ifhashmap.containsKey(issn1))
							p.setImpactFactor((double)ifhashmap.get(issn1));
						else {
							if(issnhashmap.containsKey(issn1) && ifhashmap.containsKey(issnhashmap.get(issn1)))
								p.setImpactFactor(ifhashmap.get(issnhashmap.get(issn1)));
						}
					}
					ExampleSet exampleSet = table.createExampleSet();
					// apply the model
					Operator modelApp =  OperatorService .createOperator(ModelApplier.class );
					container = container.append(new IOObject[] {exampleSet});
					container = modelApp.apply(container);
					ExampleSet resultSet = container.get(ExampleSet.class);
					Iterator eiter = resultSet.iterator();
					Iterator citer = docs.iterator();
					//java.text.DecimalFormat df = new java.text.DecimalFormat("##.##");
					int i = 1;
					while (eiter.hasNext() && citer.hasNext()) {
						Example e = (Example)eiter.next();
						PubmedArticle p = (PubmedArticle) citer.next();
						p.setQualityEvidence(e.getPredictedLabel());
						// predicted label=1.0 (POSITIVE) 0.0 (NEGATIVE), confidence denote probabilities.
						// log.debug("PMID: " + p.getId() + ", Prediction:" + e.getPredictedLabel() + " " + e.getConfidence("POS") + " " + e.getConfidence("NEG"));
						// String ifStr = df.format(p.getImpactFactor()).toString();
						sw.append("<citation rank =\""+i+"\" position=\""+i+"\" data=\""+p.getId()+"\" impact=\"" + String.format("%2.3f", p.getImpactFactor()) + "\" quality=\"" + new Double(p.getQualityEvidence()).intValue() + "\" label=\""+stringToHTMLString(p.getTitleText())+"\"/>");
						i++;
					}
				}catch(Exception e){
					for(Integer i:relevant)
						sw.append("<citation rank =\""+i+"\" position=\""+i+"\" data=\""+i+"\" label=\""+i+"\"/>");
					e.printStackTrace();
				}
			}
			sw.append("</relevants>");
			sw.append("<nonrelevants>");
			/* if (nonRelevant!=null)
				try{
					// List<? extends SemMedDocument> docs = ((MedlineSource)ArticleDataSource.getInstance(ArticleDataSource.SourceType.MEDLINE)).fetchTitle(nonRelevant);
					int mainOrTest = (Integer) session.getAttribute("predsource");
					List<? extends SemMedDocument> docs = null;
					if(mainOrTest ==1) {
						docs = ((MedlineSource)ArticleDataSource.getInstance(ArticleDataSource.SourceType.MEDLINE)).fetch(null,nonRelevant);

					} else {
						// docs = ((MedlineSource)ArticleDataSource.getInstance(ArticleDataSource.SourceType.MEDLINE)).fetchTitleFromTest(nonRelevant);
						docs = ((MedlineSource)ArticleDataSource.getInstance(ArticleDataSource.SourceType.MEDLINE)).fetch(null,nonRelevant);
					}
					IOContainer container = (IOContainer) request.getSession().getServletContext().getAttribute("rapidMinerContainer");
					MemoryExampleTable table = new MemoryExampleTable(attributes);
					for(SemMedDocument d:docs) {
						// sw.append("<citation data=\""+d.getId()+"\" label=\""+stringToHTMLString(d.getTitleText())+"\"/>");
						PubmedArticle p = (PubmedArticle) d;
						Set metadata = (HashSet)p.getMetadata();
						log.debug("PMID: " + p.getId());
						// log.debug("Mesh Heading:" + metadata.toString());
						double[] data = new double[attributes.size()];
						for ( int a=0; a<attributes.size(); a++) {
							String name = ((Attribute)attributes.get(a)).getName();
							if (metadata.contains(name)) {
								data[a] = 1.0;
							} else { data[a] = 0.0; }
						}
						table.addDataRow(new DoubleArrayDataRow(data));
						String issn1 = p.getIssn();
						if(ifhashmap.containsKey(issn1))
							p.setImpactFactor((double)ifhashmap.get(issn1));
						else {
							if(issnhashmap.containsKey(issn1) && ifhashmap.containsKey(issnhashmap.get(issn1)))
								p.setImpactFactor(ifhashmap.get(issnhashmap.get(issn1)));
						}
					}
					ExampleSet exampleSet = table.createExampleSet();
					// apply the model
					Operator modelApp =  OperatorService .createOperator(ModelApplier.class );
					container = container.append(new IOObject[] {exampleSet});
					container = modelApp.apply(container);
					ExampleSet resultSet = container.get(ExampleSet.class);
					Iterator eiter = resultSet.iterator();
					Iterator citer = docs.iterator();
					java.text.DecimalFormat df = new java.text.DecimalFormat("##.##");
					int i =1;
					while (eiter.hasNext() && citer.hasNext()) {
						Example e = (Example)eiter.next();
						PubmedArticle p = (PubmedArticle) citer.next();
						p.setQualityEvidence(e.getPredictedLabel());
						// predicted label=1.0 (POSITIVE) 0.0 (NEGATIVE), confidence denote probabilities.
						log.debug("PMID: " + p.getId() + ", Prediction:" + e.getPredictedLabel() + " " + e.getConfidence("POS") + " " + e.getConfidence("NEG"));
						// String ifStr = df.format(p.getImpactFactor()).toString();
						sw.append("<citation rank =\""+i+"\" data=\""+p.getId()+"\" impact=\"" + String.format("%2.3f", p.getImpactFactor()) + "\" quality=\"" + new Double(p.getQualityEvidence()).intValue() + "\" label=\""+stringToHTMLString(p.getTitleText())+"\"/>");
						i++;
					}
				}catch(Exception e){
					e.printStackTrace();
					for(Integer i:nonRelevant)
						sw.append("<citation rank =\""+i+"\" data=\""+i+"\" label=\""+i+"\"/>");
				} */
			sw.append("</nonrelevants>");
			sw.append("</citations>");
			relevantXML = sw.toString();
			log.debug("relevant citations --- ");
			log.debug(relevantXML);
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
