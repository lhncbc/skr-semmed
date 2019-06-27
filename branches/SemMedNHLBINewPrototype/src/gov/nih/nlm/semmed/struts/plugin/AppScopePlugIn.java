package gov.nih.nlm.semmed.struts.plugin;

import gov.nih.nlm.semmed.model.Predicate;
import gov.nih.nlm.semmed.model.SemanticGroup;
import gov.nih.nlm.semmed.model.UmlsRelationPredicateMapping;
import gov.nih.nlm.semmed.util.Constants;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.tools.Ontology;

import com.rapidminer.RapidMiner;
import com.rapidminer.RapidMinerCommandLine;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DoubleArrayDataRow;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.ModelApplier;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.io.ModelLoader;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.OperatorService;

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
		InputStream isif = context.getResourceAsStream(Constants.IF_FILE);
		InputStream isissnmap = context.getResourceAsStream(Constants.ISSNESSNMAP_FILE);
	    try
	    {
	    	if (is == null)
	    	{
	    		log.debug("Failed opening stream for this resource.");
	    	}
	    	if (isif == null)
	    	{
	    		log.debug("Failed opening stream for the impact factor xml file.");
	    	}
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(is);
			Element root = doc.getRootElement();

			context.setAttribute("questions", getKeyValuePairs(root.getChild("Questions").getChildren("Question")));
			context.setAttribute("questionMapping", getQuestionMapping(root.getChild("Questions").getChildren("Question")));
			context.setAttribute("questionNumberMapping", getQuestionMapping(root.getChild("QuestionNumbers").getChildren("Question")));
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

			List riskFactorList = root.getChild("QuestionVariables").getChild("RiskFactor").getChildren("Instance");
			context.setAttribute("riskFactorTypes", getKeyValuePairs(riskFactorList));

			List disorderList = root.getChild("QuestionVariables").getChild("Disorder").getChildren("Instance");
			context.setAttribute("disorderTypes", getKeyValuePairs(disorderList));

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
			context.setAttribute("defRiskFactorType", ((LabelValueBean)((ArrayList)context.getAttribute("riskFactorTypes")).get(0)).getLabel());
			context.setAttribute("defDisorderType", ((LabelValueBean)((ArrayList)context.getAttribute("disorderTypes")).get(0)).getLabel());
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

		    SAXBuilder builderif = new SAXBuilder();
			Document docif = builderif.build(isif);
			Element rootif = docif.getRootElement();
			Iterator citerif = rootif.getChildren("Journal").iterator();
			HashMap<String, Double> ifhashmap = new HashMap<String, Double>();
			while (citerif.hasNext()) {
	        	//if (colorMap.containsKey("NOPRED")) {
	        		//colors = (List)colorMap.get("NOPRED");
	        	//}
				Element journal = (Element)citerif.next();
				String ifString = journal.getChild("ImpactFactor").getText();
				// log.debug("ISSN: " + journal.getChild("ISSN").getText().trim() + "IF: " + journal.getChild("ImpactFactor").getText());
				// log.debug("IF: " + journal.getChild("ImpactFactor").getText());
				if(ifString != null && ifString != "")
					ifhashmap.put(journal.getChild("ISSN").getText().trim(), Double.parseDouble(journal.getChild("ImpactFactor").getText().trim()));
				else
					ifhashmap.put(journal.getChild("ISSN").getText().trim(), new Double(0.0));
			}
			context.setAttribute("ifhashmap", ifhashmap);
			isif.close();

		    SAXBuilder builderissnmap = new SAXBuilder();
			Document docissnmap = builderissnmap.build(isissnmap);
			Element rootissnmap = docissnmap.getRootElement();
			Iterator citerissnmap = rootissnmap.getChildren("Journal").iterator();
			HashMap<String, String> issnhashmap = new HashMap<String, String>();
			while (citerissnmap.hasNext()) {
				Element journal = (Element)citerissnmap.next();
				String issn = journal.getChild("ISSN").getText();
				String essn = journal.getChild("ESSN").getText();
				// log.debug("ISSN: " + journal.getChild("ISSN").getText().trim() + "IF: " + journal.getChild("ImpactFactor").getText());
				// log.debug("IF: " + journal.getChild("ImpactFactor").getText());
				if(essn != null && essn != "" && issn != null && issn != "") // In case both ISSN and ESSN are not null and not empty string
					issnhashmap.put(essn.trim(), issn.trim());
			}
			context.setAttribute("issnhashmap", issnhashmap);

		    // Initialize RapidMiner for reading
		    // String rapidhome = context.getInitParameter("rapidminer.home");
		    // log.debug("raidminer home = " + rapidhome);
		    String relativeHomePath = "/rapidminer";
			String realHomePath = context.getRealPath(relativeHomePath);
		    System.setProperty("rapidminer.home", realHomePath);
		    log.debug("Setting rapidminer home: " + realHomePath);
			RapidMiner.init();
			// load attributes from xml file
			log.debug("Load attribute...");
			String trainFilePath = context.getRealPath(Constants.TRAIN_ATT_FILE);
			List attributes = loadAttributes(trainFilePath);
			context.setAttribute("rapidMinerAttributes", attributes);

			// load the existing model (created from the training set)
			Operator modelLoader = OperatorService.createOperator(ModelLoader.class);
			log.debug("Load train model file...");
			modelLoader.setParameter(ModelLoader.PARAMETER_MODEL_FILE, context.getRealPath(Constants.TRAIN_MODEL_FILE));
			log.debug("Train file real path : " + context.getRealPath(Constants.TRAIN_MODEL_FILE));
			IOContainer container = modelLoader.apply(new IOContainer());
			// Model model = container.get(Model.class);
			context.setAttribute("rapidMinerContainer", container);

	    } catch (IOException ioe) {
	    	log.error("IO Error." + ioe.getMessage());
	        ioe.printStackTrace();
	    }	catch (JDOMException je) {
	    	log.error("JDOM Error." + je.getMessage());
	    	je.printStackTrace();
	    }	catch (Exception e) {
	    	log.error("Error." + e.getMessage());
	    	e.printStackTrace();
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

	private Map getQuestionMapping(List l) {
		Iterator iter = l.iterator();
		Map outMap = new HashMap();
		int i = 0;
		while (iter.hasNext()) {
			Element e = (Element)iter.next();
			if(e.getAttributeValue("value").compareTo("*") != 0) {
				outMap.put(new Integer(i),new LabelValueBean(e.getAttributeValue("key"),e.getAttributeValue("value")));
				i++;
			}
		}
		return outMap;

	}

	private static List loadAttributes(String path) {
		List attributes = new LinkedList();
		try {
		SAXBuilder builder = new SAXBuilder();
		org.jdom.Document doc = builder.build(new FileInputStream(path));
		List attributeList = doc.getRootElement().getChildren("attribute");
		for (int a=0; a < attributeList.size(); a++) {
			org.jdom.Element att = (org.jdom.Element)attributeList.get(a);
			attributes.add(AttributeFactory.createAttribute(att.getAttributeValue("name"), Ontology.REAL));
			// log.debug("attribute: " + att.getAttributeValue("name"));
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return attributes;
	}


}
