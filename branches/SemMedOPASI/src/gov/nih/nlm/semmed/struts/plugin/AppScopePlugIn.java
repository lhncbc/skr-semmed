package gov.nih.nlm.semmed.struts.plugin;

import gov.nih.nlm.semmed.model.Predicate;
import gov.nih.nlm.semmed.model.SemanticGroup;
import gov.nih.nlm.semmed.model.UmlsRelationPredicateMapping;
import gov.nih.nlm.semmed.util.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.action.PlugIn;
import org.apache.struts.config.ModuleConfig;
import org.apache.struts.util.LabelValueBean;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class AppScopePlugIn implements PlugIn {

	// --------------------------------------------------------- Instance Variables
	private static Log log = LogFactory.getLog(AppScopePlugIn.class);

	public AppScopePlugIn() {
		super();
		// TODO Auto-generated constructor stub
	}

	public void destroy() {
		// TODO Auto-generated method stub

	}

	public void init(ActionServlet arg0, ModuleConfig arg1)
		throws ServletException {
		ServletContext context = arg0.getServletContext();
		InputStream is = context.getResourceAsStream(Constants.DEF_FILE);
	    try
	    {
	    	if (is == null)
	    	{
	    		log.debug("Failed opening stream for this resource.");
	    	}
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(is);
			Element root = doc.getRootElement();

			context.setAttribute("questions", getKeyValuePairs(root.getChild("Questions").getChildren("Question")));
			context.setAttribute("pubmedLanguages", getKeyValuePairs(root.getChild("LanguagesPubMed").getChildren("Language")));
			context.setAttribute("pubmedExtraLanguages", getKeyValuePairs(root.getChild("MoreLanguagesPubMed").getChildren("Language")));

			context.setAttribute("pubmedArticleTypes", getKeyValuePairs(root.getChild("ArticleTypesPubMed").getChildren("ArticleType")));
			context.setAttribute("pubmedExtraArticleTypes", getKeyValuePairs(root.getChild("MoreArticleTypesPubMed").getChildren("ArticleType")));

			context.setAttribute("pubmedJournalSubsets", getKeyValuePairs(root.getChild("JournalSubsets").getChildren("Subset")));
			context.setAttribute("pubmedTopicSubsets", getKeyValuePairs(root.getChild("TopicSubsets").getChildren("Subset")));
			context.setAttribute("pubmedExtraSubsets", getKeyValuePairs(root.getChild("MoreSubsets").getChildren("Subset")));

			context.setAttribute("pubmedAges", getKeyValuePairs(root.getChild("AgeRanges").getChildren("AgeRange")));


			List ccList = root.getChild("CitationCounts").getChildren("CitationCount");
			context.setAttribute("citCounts", getKeyValuePairs(ccList));

			List seList = root.getChild("Sources").getChildren("Source");
			context.setAttribute("sources", getKeyValuePairs(seList));

			List ptList = root.getChild("PredicationTypes").getChildren("PredicationType");
			context.setAttribute("predicationTypes", getKeyValuePairs(ptList));

			List stList = root.getChild("SummaryTypes").getChildren("SummaryType");
			context.setAttribute("summaryTypes", getKeyValuePairs(stList));

			List satList = root.getChild("SaliencyTypes").getChildren("SaliencyType");
			context.setAttribute("saliencyTypes", getKeyValuePairs(satList));

			List grantList = root.getChild("GrantRange").getChildren("Range");
			context.setAttribute("grantRange", getKeyValuePairs(grantList));
			List sizeList = root.getChild("GrantSize").getChildren("Size");
			context.setAttribute("grantSize", getKeyValuePairs(sizeList));

			List langList = root.getChild("Languages").getChildren("Language");
			context.setAttribute("languages", getKeyValuePairs(langList));

			context.setAttribute("defCitCount", ((LabelValueBean)((ArrayList)context.getAttribute("citCounts")).get(0)).getLabel());
			context.setAttribute("defRecruiting", new Boolean(true));
			context.setAttribute("defSource", ((LabelValueBean)((ArrayList)context.getAttribute("sources")).get(0)).getLabel());
			context.setAttribute("defPredicationType", ((LabelValueBean)((ArrayList)context.getAttribute("predicationTypes")).get(0)).getLabel());
			context.setAttribute("defSaliencyType", ((LabelValueBean)((ArrayList)context.getAttribute("saliencyTypes")).get(0)).getLabel());
			context.setAttribute("defSummaryType", ((LabelValueBean)((ArrayList)context.getAttribute("summaryTypes")).get(0)).getLabel());
			context.setAttribute("defSaliency", new Boolean(true));
			context.setAttribute("defLanguage", ((LabelValueBean)((ArrayList)context.getAttribute("languages")).get(0)).getLabel());
			context.setAttribute("defGrantRange", ((LabelValueBean)((ArrayList)context.getAttribute("grantRange")).get(0)).getLabel());
			context.setAttribute("defGrantSize", ((LabelValueBean)((ArrayList)context.getAttribute("grantSize")).get(0)).getLabel());

			//Map colorMap = new HashMap();
			//Map translationMap = new HashMap();
			Map<String,Predicate> predicateMap = new HashMap<String,Predicate>();


			Iterator citer = root.getChild("UnusedColorSet").getChildren("Color").iterator();
			List<String> colors = new ArrayList<String>();
			while (citer.hasNext()) {
	        	//if (colorMap.containsKey("NOPRED")) {
	        		//colors = (List)colorMap.get("NOPRED");
	        	//}
	        	colors.add(((Element)citer.next()).getAttributeValue("colorCode"));
	        	//colorMap.put("NOPRED", colors);
			}

			List predicateList = root.getChild("PredicateSet").getChildren("Predicate");
			Iterator predIter = predicateList.iterator();
			//int usedColorIndex = 0;
			while (predIter.hasNext()) {
				Element predElement = (Element)predIter.next();
				String pred = predElement.getAttributeValue("name");
				String colorCode = predElement.getAttributeValue("colorCode");
				Predicate predicate = new Predicate(pred, colorCode);
				Predicate negPredicate = new Predicate("NEG_" + pred, colorCode);

				//Predicate negPredicate = new Predicate("NEG_" + pred, (String)colors.get(usedColorIndex));
				//usedColorIndex++;
				Iterator titer = predElement.getChildren("Translation").iterator();
				Map<String,String> temp = new HashMap<String,String>();
				Map<String,String> negTemp = new HashMap<String,String>();
				while (titer.hasNext()) {
					Element t = (Element)titer.next();
					temp.put(t.getAttributeValue("lang"), t.getAttributeValue("name"));
					negTemp.put(t.getAttributeValue("lang"), "NEG_" + t.getAttributeValue("name"));
				}
				predicate.setTranslationMap(temp);
				negPredicate.setTranslationMap(negTemp);
				predicateMap.put(pred, predicate);
				predicateMap.put("NEG_" + pred, negPredicate);
				//log.debug("Neg predicate: " + negPredicate.toString());
			}

			Iterator miter = root.getChild("PredicateMappingSet").getChildren("Mapping").iterator();
			Map<String,UmlsRelationPredicateMapping> mappings = new HashMap<String,UmlsRelationPredicateMapping>();
			while (miter.hasNext()) {
				Element m = (Element)miter.next();
				UmlsRelationPredicateMapping mapping = new UmlsRelationPredicateMapping(m.getAttributeValue("umlsRelation"),
						(Predicate)predicateMap.get(m.getAttributeValue("predicate")),
						Boolean.parseBoolean(m.getAttributeValue("inverse")));
				mappings.put(m.getAttributeValue("umlsRelation"),mapping);
			}

			Iterator sgiter = root.getChild("SemanticGroups").getChildren("SemanticGroup").iterator();
			Map<String,SemanticGroup> semanticTypeMappings = new HashMap<String,SemanticGroup>();
			while (sgiter.hasNext()) {
				Element s = (Element)sgiter.next();
				SemanticGroup sg = new SemanticGroup(s.getAttributeValue("colorCode"), s.getAttributeValue("name"), s.getAttributeValue("abbreviation"));
				Iterator stiter = s.getChildren("SemanticType").iterator();
				while (stiter.hasNext()) {
					String st = ((Element)stiter.next()).getAttributeValue("abbreviation");
					semanticTypeMappings.put(st, sg);
				}
			}

			//context.setAttribute("colorMap", colorMap);
			//context.setAttribute("translationMap", translationMap);
			context.setAttribute("predicateMap", predicateMap);
			context.setAttribute("predicateMappings", mappings);
			context.setAttribute("semanticTypeMappings", semanticTypeMappings);

		    is.close();

	    } catch (IOException ioe) {
	    	log.error("IO Error." + ioe.getMessage());
	        ioe.printStackTrace();
	    }	catch (JDOMException je) {
	    	log.error("JDOM Error." + je.getMessage());
	    	je.printStackTrace();
	    }
	}

	private List<LabelValueBean> getKeyValuePairs(List l) {
		Iterator iter = l.iterator();
		List<LabelValueBean> outList = new ArrayList<LabelValueBean>();
		while (iter.hasNext()) {
			Element e = (Element)iter.next();
			outList.add(new LabelValueBean(e.getAttributeValue("key"),e.getAttributeValue("value")));
		}
		return outList;

	}


}
