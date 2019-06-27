package gov.nih.nlm.semmed.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * Serves as a proxy for the flash visualization, since a flash movie can't communicate directly with web servers outside
 * the domain of the movie
 *
 * @author rodriguezal
 *
 */
public class FlashProxy extends HttpServlet {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(FlashProxy.class);

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException{
		doPost(req,resp);
	}
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException{

		URL content = null;
		String url = req.getParameter("url");
		StringBuffer urlBuf = new StringBuffer();
		for(int i = 0; i < url.length(); i++) {
			if(url.charAt(i) != ' ')
					urlBuf.append(url.charAt(i));
		}
		String urlWithoutBlank = urlBuf.toString();
		// log.debug("Transferred URL through Proxy = " + url);
		// log.debug("Transferred URLWithoutBlank through Proxy = " + urlWithoutBlank);

		try {
			// content = new URL(url);
			content = new URL(urlWithoutBlank);
		} catch (MalformedURLException exception) {
			throw new ServletException(exception);
		}

		URLConnection contentCon = null;
		try {
			contentCon = content.openConnection();
		} catch (IOException exception) {
			throw new ServletException("Problem opening " + url + ": "
					+ exception.toString());
		}

		//
		// Get the content type from the URLConnection and set it on the
		// response.
		//
		String contentType = contentCon.getContentType();
		resp.setContentType(contentType);

		//
		// Get and read the input stream.
		//
		try {
			InputStream in = contentCon.getInputStream();
			byte[] buffer = new byte[1024];
			ByteArrayOutputStream contentStream = new ByteArrayOutputStream();
			for (int cnt = in.read(buffer); cnt != -1; cnt = in.read(buffer))
				contentStream.write(buffer, 0, cnt);

			//
			// This is where you have the content from the request.
			//
			String contentString = contentStream.toString();
			// System.err.println(contentString);

			//
			// Now write the bytes out to the client.
			//
			byte[] contentBytes = contentString.getBytes();
			OutputStream out = resp.getOutputStream();
			out.write(contentBytes, 0, contentBytes.length);
			log.debug(" --- Beginning of Retrieved Citations from Visulization: \n");
			log.debug(contentString);
			log.debug(" --- End of Retrieved Citations from Visulization: \n");
			out.flush();
			out.close();
		} catch (Exception exception) {
			throw new ServletException("Unable to proxy request: " + exception);
		}
	}
}
