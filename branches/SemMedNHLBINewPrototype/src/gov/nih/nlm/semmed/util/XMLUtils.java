/**
 *
 */
package gov.nih.nlm.semmed.util;

import gov.nih.nlm.semmed.exception.SemMedException;
import gov.nih.nlm.semmed.model.ClinicalStudy;
import gov.nih.nlm.semmed.model.PubmedArticle;
import gov.nih.nlm.semmed.model.Query;
import gov.nih.nlm.semmed.model.SemMedDocument;
import gov.nih.nlm.semmed.model.SemrepResultSet;
import gov.nih.nlm.semmed.model.SentencePredication;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.util.LabelValueBean;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

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

/**
 * @author hkilicoglu
 *
 */
public class XMLUtils {

	// --------------------------------------------------------- Instance Variables
	private static Log log = LogFactory.getLog(XMLUtils.class);
	private static HashMap<String, Double> ifhashmap = null; // Hashmap for ImpactFactor
	private static HashMap<String, String> issnhashmap = null; // Hashmap for ISSN/ESSN map

	/**
	 * Builds a document from an InputStream.
	 *
	 * @param isr the input stream used to build the document.
	 * @return the document built.
	 * @throws IOException
	 * @throws JDOMException
	 */
	public static Document buildXml(InputStreamReader isr) throws IOException, JDOMException {
		SAXBuilder builder = new SAXBuilder();
		return builder.build(isr);
	}

	/**
	 * Export a session to XML.
	 *
	 * @param session session to export.
	 * @return an XML document that contains the session data.
	 */
	public static Document exportToXml(HttpSession session) {
    	Element root = new Element("SemanticMedline");
    	Document doc = new Document(root);

    	//DocType doctype = new DocType("SemanticMedline",
        //        "http://pax.nlm.nih.gov/SemMed/dtd/semantic_medline.dtd");
    	//doc.setDocType(doctype);

    	Query q = (Query)session.getAttribute("query");
		List cits = (List)session.getAttribute("citlist");
		String source = new String();
    	if (q != null) {
//    		root.addContent(q.toXml("Query"));
    		source = q.getSource();
    	}

    	if (cits != null && cits.size() > 0) {
    		if ("medline".equals(source))
    			root.addContent(MedlineSource.buildMedlineXml(cits));
    		else if ("ctrials".equals(source))
    			root.addContent(ClinicalStudySource.buildStudyXml(cits));
    		// ** THIS IS A HACK TO ALLOW PROCESSING PUBMED XML FILES DIRECTLY **
    		// ** THEY DON'T HAVE QUERY INFO.
    		//else if ("both".equals(q.getSource())) {
    		else {
    			Iterator cititer = cits.iterator();
    			List<SemMedDocument> pubmedCits = new ArrayList<SemMedDocument>();
    			List<SemMedDocument> clinStudies = new ArrayList<SemMedDocument>();
    			while (cititer.hasNext()) {
    				SemMedDocument semmedDoc = (SemMedDocument)cititer.next();
    				if (semmedDoc instanceof ClinicalStudy) {
    					clinStudies.add(semmedDoc);
    				} else if (semmedDoc instanceof PubmedArticle) {
    					pubmedCits.add(semmedDoc);
    				}
    			}
    			if (pubmedCits.size() > 0)
    				root.addContent(MedlineSource.buildMedlineXml(pubmedCits));
    			if (clinStudies.size() > 0)
    				root.addContent(ClinicalStudySource.buildStudyXml(clinStudies));
    		}
    	}

    	SemrepResultSet preds = (SemrepResultSet)session.getAttribute("predications");
    	if (preds != null) {
    		List sps = preds.getSentencePredications();
    		if (sps != null && sps.size() > 0 ) {
	        	Element predTypeNode = new Element("PredicationType");
	        	predTypeNode.setText((String)session.getAttribute("predicationType"));
	        	root.addContent(predTypeNode);
	        	Element resultsNode = new Element("SemrepResultSet");
	    		ListIterator spIter = sps.listIterator();
	    		while (spIter.hasNext()) {
	    			SentencePredication sp = (SentencePredication)spIter.next();
	    			resultsNode.addContent(sp.toXml(null));
	    		}
	    		root.addContent(resultsNode);
    		}
    	}

    	SemrepResultSet summaryPreds = (SemrepResultSet)session.getAttribute("summaryPredications");
    	if (summaryPreds != null) {
    		List sps = summaryPreds.getSentencePredications();
    		if (sps != null && sps.size() > 0) {
    			Element summTypeNode = new Element("SummaryType");
    			summTypeNode.setText((String)session.getAttribute("summaryType"));
    			root.addContent(summTypeNode);

    			String seed = (String)session.getAttribute("selectedSeed");
    			Element seedNode = new Element("SelectedSeed");
    			seedNode.setText(seed);
    			root.addContent(seedNode);

    			Element saliencyNode = new Element("Saliency");
    			saliencyNode.setText(((Boolean)session.getAttribute("saliency")).toString());
    			root.addContent(saliencyNode);

    			Element saliencyTypeNode = new Element("SelectedSaliencyType");
    			saliencyTypeNode.setText((String)session.getAttribute("selectedSaliencyType"));
    			root.addContent(saliencyTypeNode);

    			String lang = (String)session.getAttribute("lang");
    			if (lang != null) {
    				Element langNode = new Element("Language");
    				langNode.setText(lang);
    				root.addContent(langNode);
    			}

    			String trSelectedSeed = (String)session.getAttribute("translateSelectedSeed");
    			if (trSelectedSeed != null) {
    				Element trSeedNode = new Element("SelectedSeedTranslation");
    				trSeedNode.setText(trSelectedSeed);
    				root.addContent(trSeedNode);
    			}

    			Element sumResultNode = new Element("SummaryResultSet");
    			ListIterator sumIter = sps.listIterator();
    			while (sumIter.hasNext()) {
    				SentencePredication sp = (SentencePredication)sumIter.next();
    				sumResultNode.addContent(sp.toXml(lang));
    			}
    			root.addContent(sumResultNode);
    		}
    	}

    	return doc;
	}

	/**
	 * Export a session to XML.
	 *
	 * @param session session to export.
	 * @return an XML document that contains the session data.
	 */
	public static Document exportCitationToXml(HttpSession session) {
    	Element root = new Element("Citations");
    	Document doc = new Document(root);
    	int rankingsource = 0;
    	List<PubmedArticle> rcits = (List<PubmedArticle>)session.getAttribute("relevantCitations");
    	List<PubmedArticle> nrcits = (List<PubmedArticle>)session.getAttribute("nonrelevantCitations");
    	List<PubmedArticle> rcitsWithZeroQE = new ArrayList<PubmedArticle>();
    	List<PubmedArticle> nrcitsWithZeroQE = new ArrayList<PubmedArticle>();
    	try {
    	if( session.getAttribute("rankingsource") != null) {
    		rankingsource = ((Integer)session.getAttribute("rankingsource")).intValue();
    		if(rankingsource == 2) { // rank with quality evidence, so impact factor was not calculated
    			if(ifhashmap == null) {
    				ifhashmap = (HashMap) session.getServletContext().getAttribute("ifhashmap");
    			}
    			if(issnhashmap == null) {
    				issnhashmap = (HashMap) session.getServletContext().getAttribute("issnhashmap");
    			}
    		} else {
    			List attributes = (List) session.getServletContext().getAttribute("rapidMinerAttributes");
    			IOContainer container = (IOContainer) session.getServletContext().getAttribute("rapidMinerContainer");
    			MemoryExampleTable table = new MemoryExampleTable(attributes);
    			for (int i=0; i < rcits.size(); i++) {
					Set metadata = (HashSet)(rcits.get(i)).getMetadata();
					log.debug("PMID: " + rcits.get(i).getId());
					log.debug("Mesh Heading:" + metadata.toString());
					double[] data = new double[attributes.size()];
					for ( int a=0; a<attributes.size(); a++) {
						String name = ((Attribute)attributes.get(a)).getName();
						if (metadata.contains(name)) {
							data[a] = 1.0;
						} else { data[a] = 0.0; }
					}
					table.addDataRow(new DoubleArrayDataRow(data));
				}
				ExampleSet exampleSet = table.createExampleSet();
				// apply the model
				Operator modelApp =  OperatorService .createOperator(ModelApplier.class );
				container = container.append(new IOObject[] {exampleSet});
				container = modelApp.apply(container);

				// print results, iterate through input docs and get the predictions
				ExampleSet resultSet = container.get(ExampleSet.class);
				Iterator eiter = resultSet.iterator();
				Iterator citer = rcits.iterator();
				while (eiter.hasNext() && citer.hasNext()) {
					Example e = (Example)eiter.next();
					PubmedArticle p = (PubmedArticle) citer.next();
					p.setQualityEvidence(e.getPredictedLabel());
					// predicted label=1.0 (POSITIVE) 0.0 (NEGATIVE), confidence denote probabilities.
					log.debug("PMID: " + p.getId() + ", Prediction:" + e.getPredictedLabel() + " " + e.getConfidence("POS") + " " + e.getConfidence("NEG"));
				}

    			MemoryExampleTable ntable = new MemoryExampleTable(attributes);
    			for (int i=0; i < nrcits.size(); i++) {
					Set metadata = (HashSet)(nrcits.get(i)).getMetadata();
					log.debug("PMID: " + nrcits.get(i).getId());
					log.debug("Mesh Heading:" + metadata.toString());
					double[] data = new double[attributes.size()];
					for ( int a=0; a<attributes.size(); a++) {
						String name = ((Attribute)attributes.get(a)).getName();
						if (metadata.contains(name)) {
							data[a] = 1.0;
						} else { data[a] = 0.0; }
					}
					ntable.addDataRow(new DoubleArrayDataRow(data));
				}
				ExampleSet nexampleSet = ntable.createExampleSet();
				// apply the model
				Operator nmodelApp =  OperatorService .createOperator(ModelApplier.class );
				IOContainer ncontainer = (IOContainer) session.getServletContext().getAttribute("rapidMinerContainer");
				ncontainer = ncontainer.append(new IOObject[] {nexampleSet});
				ncontainer = modelApp.apply(ncontainer);

				// print results, iterate through input docs and get the predictions
				ExampleSet nresultSet = ncontainer.get(ExampleSet.class);
				Iterator neiter = nresultSet.iterator();
				Iterator nciter = nrcits.iterator();
				while (neiter.hasNext() && nciter.hasNext()) {
					Example e = (Example)neiter.next();
					PubmedArticle p = (PubmedArticle) nciter.next();
					p.setQualityEvidence(e.getPredictedLabel());
					// predicted label=1.0 (POSITIVE) 0.0 (NEGATIVE), confidence denote probabilities.
					log.debug("PMID: " + p.getId() + ", Prediction:" + e.getPredictedLabel() + " " + e.getConfidence("POS") + " " + e.getConfidence("NEG"));
				}
    		}
    	}
		} catch (Exception e) {
			e.printStackTrace();
		}

    	Element relCitsNode = new Element("RelevantCitationsWithOneQE");
    	root.addContent(relCitsNode);
    	if (rcits != null && rcits.size() > 0) {
    			Iterator rcititer = rcits.iterator();
    			while (rcititer.hasNext()) {
    				PubmedArticle particle = (PubmedArticle)rcititer.next();
    				if(particle.getQualityEvidence() != 0.0) {
    						Element relNode = new Element("Citation");
    						String pmid = particle.getId();
    						String title = particle.getTitleText();
    						double impactFactor = 0;
    						double qualityEvidence = 0;
    						if(rankingsource == 2) { // impact factor was not populated yet
    								String issn1 = particle.getIssn();
    								if(ifhashmap.containsKey(issn1))
    									impactFactor = (double)ifhashmap.get(issn1);
    								else {
    									if(issnhashmap.containsKey(issn1) && ifhashmap.containsKey(issnhashmap.get(issn1)))
    										impactFactor = ifhashmap.get(issnhashmap.get(issn1));
    									else impactFactor = 0.0;
    								}
    								particle.ifExist = true;
    								particle.setImpactFactor(impactFactor);
    								qualityEvidence = particle.getQualityEvidence();
    						}	else {
    							impactFactor = particle.getImpactFactor();
    							qualityEvidence = particle.getQualityEvidence();
    						}
    						Element pmidNode = new Element("PMID");
    						pmidNode.setText(pmid);
    						pmidNode.setAttribute("QualityEvidence", new Double(qualityEvidence).toString());
    						pmidNode.setAttribute("ImpactFactor", new Double(impactFactor).toString());
    						relNode.addContent(pmidNode);
    						Element titleNode = new Element("Title");
    						titleNode.setText(title);
    						relNode.addContent(titleNode);
    						relCitsNode.addContent(relNode);
    				} else
    					rcitsWithZeroQE.add(particle);
    			} // while
    	}
    	Element relCitsWithZeroQENode = new Element("RelevantCitationsWithZeroQE");
    	root.addContent(relCitsWithZeroQENode);
    	if (rcitsWithZeroQE != null && rcitsWithZeroQE.size() > 0) {
    			Iterator rcititer = rcitsWithZeroQE.iterator();
    			while (rcititer.hasNext()) {
    				PubmedArticle particle = (PubmedArticle)rcititer.next();
    						Element relNode = new Element("Citation");
    						String pmid = particle.getId();
    						String title = particle.getTitleText();
    						double impactFactor = 0;
    						double qualityEvidence = 0;
    						if(rankingsource == 2) { // impact factor was not populated yet
    								String issn1 = particle.getIssn();
    								if(ifhashmap.containsKey(issn1))
    									impactFactor = (double)ifhashmap.get(issn1);
    								else {
    									if(issnhashmap.containsKey(issn1) && ifhashmap.containsKey(issnhashmap.get(issn1)))
    										impactFactor = ifhashmap.get(issnhashmap.get(issn1));
    									else impactFactor = 0.0;
    								}
    								particle.ifExist = true;
    								particle.setImpactFactor(impactFactor);
    								qualityEvidence = particle.getQualityEvidence();
    						}	else {
    							impactFactor = particle.getImpactFactor();
    							qualityEvidence = particle.getQualityEvidence();
    						}
    						Element pmidNode = new Element("PMID");
    						pmidNode.setText(pmid);
    						pmidNode.setAttribute("QualityEvidence", new Double(qualityEvidence).toString());
    						pmidNode.setAttribute("ImpactFactor", new Double(impactFactor).toString());
    						relNode.addContent(pmidNode);
    						Element titleNode = new Element("Title");
    						titleNode.setText(title);
    						relNode.addContent(titleNode);
    						relCitsWithZeroQENode.addContent(relNode);
    			} // while
    	}
    	Element nrelCitsNode = new Element("NonrelevantCitationsWithOneQE");
    	root.addContent(nrelCitsNode);
    	if (nrcits != null && nrcits.size() > 0) {
			Iterator nrcititer = nrcits.iterator();
			while (nrcititer.hasNext()) {
				PubmedArticle particle = (PubmedArticle)nrcititer.next();
				if(particle.getQualityEvidence() != 0.0) {
					Element relNode = new Element("Citation");
					String pmid = particle.getId();
					String title = particle.getTitleText();
					double impactFactor = 0;
					double qualityEvidence = 0;
					if(rankingsource == 2) { // impact factor was not populated yet
							String issn1 = particle.getIssn();
							if(ifhashmap.containsKey(issn1))
								impactFactor = (double)ifhashmap.get(issn1);
							else {
								if(issnhashmap.containsKey(issn1) && ifhashmap.containsKey(issnhashmap.get(issn1)))
									impactFactor = ifhashmap.get(issnhashmap.get(issn1));
								else impactFactor = 0.0;
							}
							particle.ifExist = true;
							particle.setImpactFactor(impactFactor);
							qualityEvidence = particle.getQualityEvidence();
					}	else {
						impactFactor = particle.getImpactFactor();
						qualityEvidence = particle.getQualityEvidence();
					}
					Element pmidNode = new Element("PMID");
					pmidNode.setText(pmid);
					pmidNode.setAttribute("QualityEvidence", new Double(qualityEvidence).toString());
					pmidNode.setAttribute("ImpactFactor", new Double(impactFactor).toString());
					relNode.addContent(pmidNode);
					Element titleNode = new Element("Title");
					titleNode.setText(title);
					relNode.addContent(titleNode);
					nrelCitsNode.addContent(relNode);
				} else
					nrcitsWithZeroQE.add(particle);
			} // while
    	}
    	Element nrelCitsWithZeroQENode = new Element("NonrelevantCitationsWithZeroQE");
    	root.addContent(nrelCitsWithZeroQENode);
    	if (nrcitsWithZeroQE != null && nrcitsWithZeroQE.size() > 0) {
    			Iterator rcititer = nrcitsWithZeroQE.iterator();
    			while (rcititer.hasNext()) {
    				PubmedArticle particle = (PubmedArticle)rcititer.next();
    						Element relNode = new Element("Citation");
    						String pmid = particle.getId();
    						String title = particle.getTitleText();
    						double impactFactor = 0;
    						double qualityEvidence = 0;
    						if(rankingsource == 2) { // impact factor was not populated yet
    								String issn1 = particle.getIssn();
    								if(ifhashmap.containsKey(issn1))
    									impactFactor = (double)ifhashmap.get(issn1);
    								else {
    									if(issnhashmap.containsKey(issn1) && ifhashmap.containsKey(issnhashmap.get(issn1)))
    										impactFactor = ifhashmap.get(issnhashmap.get(issn1));
    									else impactFactor = 0.0;
    								}
    								particle.ifExist = true;
    								particle.setImpactFactor(impactFactor);
    								qualityEvidence = particle.getQualityEvidence();
    						}	else {
    							impactFactor = particle.getImpactFactor();
    							qualityEvidence = particle.getQualityEvidence();
    						}
    						Element pmidNode = new Element("PMID");
    						pmidNode.setText(pmid);
    						pmidNode.setAttribute("QualityEvidence", new Double(qualityEvidence).toString());
    						pmidNode.setAttribute("ImpactFactor", new Double(impactFactor).toString());
    						relNode.addContent(pmidNode);
    						Element titleNode = new Element("Title");
    						titleNode.setText(title);
    						relNode.addContent(titleNode);
    						nrelCitsWithZeroQENode.addContent(relNode);
    			} // while
    	}
    	return doc;
	}

	/**
	 * upload session data from an XML file.
	 * @param fileData content of the XML file.
	 * @param session  session to add the content to.
	 * @return updated session object.
	 * @throws JDOMException
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	//TODO [Alejandro] This won't work at all
	public static HttpSession uploadXML(byte[] fileData, HttpSession session)
		throws JDOMException, IOException, UnsupportedEncodingException {
		List<SemMedDocument> semrepInputList = null;
		log.debug("Finished reading file.");
		log.debug("File data: " + (new String(fileData)).length());

		if (session.getAttribute("query") != null) session.removeAttribute("query");
		if (session.getAttribute("citlist") != null) session.removeAttribute("citlist");
		if (session.getAttribute("predicationType") != null) session.removeAttribute("predicationType");
		if (session.getAttribute("predications") != null) session.removeAttribute("predications");
		if (session.getAttribute("summaryType") != null) session.removeAttribute("summaryType");
		if (session.getAttribute("summaryPredications") != null) session.removeAttribute("summaryPredications");
		if (session.getAttribute("relevantConcs") != null) session.removeAttribute("relevantConcs");
		if (session.getAttribute("selectedSeed") != null) session.removeAttribute("selectedSeed");
		if (session.getAttribute("saliency") != null) session.removeAttribute("saliency");
		if (session.getAttribute("selectedSaliencyType") != null) session.removeAttribute("selectedSaliencyType");
		if (session.getAttribute("lang") != null) session.removeAttribute("lang");
		if (session.getAttribute("translateSelectedSeed") != null) session.removeAttribute("translateSelectedSeed");

		Document doc = buildXml(new InputStreamReader(new ByteArrayInputStream(fileData),"UTF8"));
		if (doc == null) return session;
		Element root = doc.getRootElement();
		if (root.getChild("PubmedArticleSet") != null)
			semrepInputList = MedlineSource.parseMedlineXml(root.getChild("PubmedArticleSet"));
		// the case where a pubmed xml file is directly uploaded -- rare
		else if (root.getName().equals("PubmedArticleSet"))
			semrepInputList = MedlineSource.parseMedlineXml(root);
		if (root.getChild("ClinicalStudySet") != null) {
			if (semrepInputList == null)
				semrepInputList = ClinicalStudySource.parseStudyXml(root.getChild("ClinicalStudySet"));
			else
				semrepInputList.addAll(ClinicalStudySource.parseStudyXml(root.getChild("ClinicalStudySet")));
		}
		session.setAttribute("citlist",semrepInputList);
		Element q = root.getChild("Query");
		if (q != null)
			session.setAttribute("query", new gov.nih.nlm.semmed.model.Query(q));

		List<SentencePredication> predicationList = new ArrayList<SentencePredication>();
		Element elSrs = root.getChild("SemrepResultSet");
		if (elSrs != null) {
			session.setAttribute("predicationType",root.getChildTextTrim("PredicationType"));
			List spList = elSrs.getChildren("SentencePredication");
			ListIterator spIter = spList.listIterator();
			while (spIter.hasNext()) {
				Element sp = (Element)spIter.next();
				SentencePredication sentPred = new SentencePredication(sp);
				predicationList.add(sentPred);
			}
			SemrepResultSet srs = new SemrepResultSet(predicationList);
			session.setAttribute("predications",srs);
			log.debug("Predication count: " + srs.getSentencePredications().size());
		}

		String sumType = root.getChildTextTrim("SummaryType");
		if (sumType == null) {
			SemrepResultSet res = (SemrepResultSet)session.getAttribute("predications");
			if (res != null) {
				List relevantConcs = res.getRelevantConcepts("treatment");
				session.setAttribute("relevantConcs",relevantConcs);
				session.setAttribute("selectedSeed", ((LabelValueBean)relevantConcs.get(0)).getLabel());
			}
		}

		List<SentencePredication> summaryPredicationList = new ArrayList<SentencePredication>();
		Element elSumSrs = root.getChild("SummaryResultSet");
		if (elSumSrs != null) {
			session.setAttribute("summaryType",sumType);
			session.setAttribute("selectedSeed",root.getChildTextTrim("SelectedSeed"));
			session.setAttribute("saliency",Boolean.valueOf(root.getChildTextTrim("Saliency")));
			session.setAttribute("selectedSaliencyType",root.getChildTextTrim("SelectedSaliencyType"));

			List sumList = elSumSrs.getChildren("SentencePredication");
			ListIterator sumIter = sumList.listIterator();
			while (sumIter.hasNext()) {
				Element sp = (Element)sumIter.next();
				SentencePredication sentPred = new SentencePredication(sp);
				summaryPredicationList.add(sentPred);
			}
			SemrepResultSet srs = new SemrepResultSet(summaryPredicationList);
			session.setAttribute("summaryPredications",srs);
			log.debug("Summary Predications count: " + srs.getSentencePredications().size());

			if (sumType != null) {
				List relevantConcs = srs.getRelevantConcepts(sumType);
				session.setAttribute("relevantConcs",relevantConcs);
			}
		}

		String lang = root.getChildTextTrim("Language");
		if (lang != null)
			session.setAttribute("lang", lang);

		String trSeed = root.getChildTextTrim("SelectedSeedTranslation");
		if (trSeed != null)
			session.setAttribute("translateSelectedSeed", trSeed);

		return session;
	}

}
