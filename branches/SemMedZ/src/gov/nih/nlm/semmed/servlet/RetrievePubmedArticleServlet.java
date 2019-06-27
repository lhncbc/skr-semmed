package gov.nih.nlm.semmed.servlet;

import gov.nih.nlm.semmed.model.SemMedDocument;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RetrievePubmedArticleServlet extends HttpServlet {
	private static Log log = LogFactory.getLog(RetrievePubmedArticleServlet.class);
	private static final long serialVersionUID = 1L;


	/**
	 * Constructor of the object.
	 */
	public RetrievePubmedArticleServlet() {
		super();
	}

	/**
	 * Destruction of the servlet. <br>
	 */
	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
		// Put your code here
	}

	/**
	 * The doGet method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to get.
	 *
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		String contentType = "application/x-java-serialized-object";
		response.setContentType(contentType);
		ObjectOutputStream out = new ObjectOutputStream(response.getOutputStream());
		String ids[] = request.getParameter("id").split(",");
		List articles = (List)session.getAttribute("citlist");
		Map<String,String> citMap = new HashMap<String,String>();
		Iterator citIter = articles.iterator();
		Map<String,String> selectedCitMap = new HashMap<String,String>();
		while (citIter.hasNext()) {
			SemMedDocument article = (SemMedDocument)citIter.next();
			citMap.put(article.getId(), article.toString());
			// log.debug("article id : " + article.getId());
			// log.debug(article.toString());
		}
		for (int i=0; i < ids.length; i++) {
			String id = ids[i];
			if (citMap.containsKey(id))
				selectedCitMap.put(id, citMap.get(id));
		}

		out.writeObject(selectedCitMap);
		out.flush();
		out.close();
	}

	/**
	 * The doPost method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to post.
	 *
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request,response);
	}

	/**
	 * Initialization of the servlet. <br>
	 *
	 * @throws ServletException if an error occure
	 */
	public void init() throws ServletException {
		// Put your code here
	}

}
