package gov.nih.nlm.semmed.util;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author rodriguezal
 *
 */
public class PubMedIDParser extends DefaultHandler {

	private boolean inQueryKey = false;
	private boolean inWebEnv = false;
	private boolean inID = false;
	private boolean inCount = false;

	private StringBuffer sb = new StringBuffer();
	private int[] ids;
	private String webEnv;
	private String queryKey;
	private int count = -1;
	private int current = 0;
	private int maxReturn = Integer.MAX_VALUE;

	public PubMedIDParser(int max){
		if(max>0)
			maxReturn=max;
	}

	public int[] getIds(){
		return ids;
	}

	public String getWebEnv(){
		return "WebEnv="+webEnv+"&query_key="+queryKey;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException{
		String eName = localName; // element name
        if ("".equals(eName)) eName = qName; // namespaceAware = false

        if (eName.equalsIgnoreCase("id")) {
        	sb = new StringBuffer();
        	inID = true;
	} else if (eName.equalsIgnoreCase("webenv"))
        	inWebEnv = true;
        else if (eName.equalsIgnoreCase("querykey"))
        	inQueryKey = true;
        else if (count<0 && eName.equalsIgnoreCase("count"))
        	inCount = true;
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException{
		String eName = localName; // element name
        if ("".equals(eName)) eName = qName; // namespaceAware = false

        if (eName.equalsIgnoreCase("id")) {
        	ids[current++] = Integer.parseInt(sb.toString().trim());
        	inID = false;
        }
        else if (eName.equalsIgnoreCase("webenv")){
        	inWebEnv = false;
        	webEnv = sb.toString().trim();
        }else if (eName.equalsIgnoreCase("querykey"))
        	inQueryKey = false;
        else if (inCount && eName.equalsIgnoreCase("count")){
        	ids = new int[Math.min(count,maxReturn)];
        	inCount = false;
        }
	}

	@Override
	public void characters(char[] ch,int start,int length) throws SAXException{
		if (inID){
			if (current>=ids.length)
				return;
			// ids[current++] = Integer.parseInt(new String(ch,start,length));
			sb.append(ch,start,length);
		}else if (inWebEnv)
			sb.append(ch,start,length);
        else if (inQueryKey)
        	queryKey = new String(ch,start,length);
        else if (inCount){
        	count = Integer.parseInt(new String(ch,start,length));
        }
	}

//	public static String forHTMLTag(String aTagFragment){
//	    final StringBuilder result = new StringBuilder();
//	    final StringCharacterIterator iterator = new StringCharacterIterator(aTagFragment);
//	    char character =  iterator.current();
//	    while (character != CharacterIterator.DONE ){
//	      if (character == '<') {
//	        result.append("&lt;");
//	      }
//	      else if (character == '>') {
//	        result.append("&gt;");
//	      }
//	      else if (character == '\"') {
//	        result.append("&quot;");
//	      }
//	      else if (character == '\'') {
//	        result.append("&#039;");
//	      }
//	      else if (character == '\\') {
//	         result.append("&#092;");
//	      }
//	      else if (character == '&') {
//	         result.append("&amp;");
//	      }
//	      else if ((character >= 'a' && character <= 'z') ||
//	    		   (character >= 'A' && character <= 'Z') ||
//	    		   (character >= '0' && character <= '9')){
//	    	  result.append(character);
//	      }
//	      else {
//	    	  result.append("&#"+Character.+";");
//	      }
//	      character = iterator.next();
//	    }
//	    return result.toString();
//	  }
}
