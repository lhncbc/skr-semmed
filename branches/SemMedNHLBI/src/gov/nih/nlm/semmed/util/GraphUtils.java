/**
 *
 */
package gov.nih.nlm.semmed.util;

import gov.nih.nlm.semmed.model.Concept;
import gov.nih.nlm.semmed.model.ConceptSemtype;
import gov.nih.nlm.semmed.model.ConceptTranslation;
import gov.nih.nlm.semmed.model.Predicate;
import gov.nih.nlm.semmed.model.Predication;
import gov.nih.nlm.semmed.model.PredicationArgument;
import gov.nih.nlm.semmed.model.SemanticGroup;
import gov.nih.nlm.semmed.model.Sentence;
import gov.nih.nlm.semmed.model.SentencePredication;
import gov.nih.nlm.semmed.model.UmlsRelationPredicateMapping;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 * @author hkilicoglu
 *
 */
public class GraphUtils {


	private static Log log = LogFactory.getLog(GraphUtils.class);

	// --------------------------------------------------------- Instance Variables
	// --------------------------------------------------------- Methods


	public static Document parse(List<SentencePredication> sps, String filename, String lang, ServletContext sc, boolean useUmls) {
		return GraphUtils.parse(sps,filename,lang,sc,useUmls,null);
	}

	@SuppressWarnings("unchecked")
	public static Document parse(List<SentencePredication> sps, String filename, String lang, ServletContext sc, boolean useUmls,String seed) {
		Map<Set<Concept>,Integer> concepts = new HashMap<Set<Concept>,Integer>();
		Map<Set<Concept>,Set<String>> semtypes = new HashMap<Set<Concept>,Set<String>>();
		Map<Set<Concept>,Set<Concept>> entrezgenes = new HashMap<Set<Concept>,Set<Concept>>();
		Map<Predication,Map<Sentence,Integer>> predications = new HashMap<Predication,Map<Sentence,Integer>>();

		int cnt = 0;
		Map predicateMap = (Map)sc.getAttribute("predicateMap");
		Map semanticTypeMap = (Map)sc.getAttribute("semanticTypeMappings");
		log.debug("Predicate map size:" + predicateMap.size());

		Iterator piter = predicateMap.keySet().iterator();
		while (piter.hasNext()) {
			String p = (String)piter.next();
			Predicate c = (Predicate)predicateMap.get(p);
			// log.debug("Predicate Map: " + p +"|" + c.getColorCode());
		}

		InputStream umlsStream = null;
		Map mappings = null;
		// enrich with UMLS relations
		if (useUmls) {
			umlsStream = sc.getResourceAsStream(filename);
			// log.debug("Opened umls stream.");
			mappings = (Map)sc.getAttribute("predicateMappings");
			// log.debug("Mapping size: " + mappings.size());
		}

		Element root = new Element("graph");
		Document doc = new Document(root);

		Map<String,Concept> seenCuis = new HashMap<String,Concept>();
		for(SentencePredication sp : sps){
			Predication p = sp.getPredication();
			// log.debug("predicate in sentence predication:" + p.getPredicate());
			String ptype = p.getType();
			Sentence snt = sp.getSentence();

			Set<ConceptSemtype> displaySubjects = p.getBestSubjectSemtypes();
			Set<ConceptSemtype> displayObjects = p.getBestObjectSemtypes();
			Set<Concept> displaySubjConcs = getConcepts(displaySubjects);
			Set<Concept> displayObjConcs = getConcepts(displayObjects);
			if (displayObjConcs.size() <= 0 || displaySubjConcs.size() <=0) continue;

			Set<Concept> subjEntrezIds = p.getSubjectsWithType("ENTREZ");
			if (entrezgenes.containsKey(displaySubjConcs)) {
				Set<Concept> existingSubjEntrezIds = entrezgenes.get(displaySubjConcs);
				existingSubjEntrezIds.addAll(subjEntrezIds);
				//entrezgenes.put(displaySubjConcs, existingSubjEntrezIds);
			} else
				entrezgenes.put(displaySubjConcs, subjEntrezIds);

			Set<Concept> objEntrezIds = p.getObjectsWithType("ENTREZ");
			if (entrezgenes.containsKey(displayObjConcs)) {
				Set<Concept> existingObjEntrezIds = entrezgenes.get(displayObjConcs);
				existingObjEntrezIds.addAll(objEntrezIds);
				//entrezgenes.put(displayObjConcs, existingObjEntrezIds);
			} else
				entrezgenes.put(displayObjConcs, objEntrezIds);

			seenCuis.putAll(saveConcepts(displaySubjConcs));
			seenCuis.putAll(saveConcepts(displayObjConcs));
			Set<String> subjSemtypes = getSemtypes(displaySubjects, ptype);
			Set<String> objSemtypes = getSemtypes(displayObjects, ptype);

			// need to flatten somehow
		    if (concepts.containsKey(displaySubjConcs)) {
				cnt = ((Integer)concepts.get(displaySubjConcs)).intValue();
				concepts.put(displaySubjConcs,cnt+1);
			} else {
				concepts.put(displaySubjConcs,1);
				semtypes.put(displaySubjConcs,subjSemtypes);
			}

			if (concepts.containsKey(displayObjConcs)) {
				cnt = ((Integer)concepts.get(displayObjConcs)).intValue();
				concepts.put(displayObjConcs,cnt+1);
			} else {
				concepts.put(displayObjConcs,1);
				semtypes.put(displayObjConcs,objSemtypes);
			}

			Map<Sentence,Integer> sentences;
			if (predications.containsKey(p)) {
				sentences = predications.get(p);
				if (sentences.containsKey(snt)) {
					int cn = ((Integer)sentences.get(snt)).intValue() + 1;
					sentences.put(snt, cn);
				} else {
					sentences.put(snt, 1);
				}
			}
			else {
				sentences = new HashMap<Sentence,Integer>();
				sentences.put(snt, 1);
			}
			predications.put(p,sentences);
		}

		if (umlsStream != null) {
			try {

				Session hb_session = HibernateSessionFactory.currentSession();
				Query q1 = hb_session.createQuery("select c from Concept as c inner join fetch c.conceptSemtypeSet cs where c.cui= :cui").
					setCacheable(true);
/*				SQLQuery q1 = hb_session.createSQLQuery("select {c.*}, {cs.*} from CONCEPT c inner join CONCEPT_SEMTYPE cs " +
						"on c.CONCEPT_ID=cs.CONCEPT_ID where c.CUI= :cui").addEntity("c",Concept.class).addJoin("cs","c.conceptSemtypeSet");*/
				SQLQuery q2 = hb_session.createSQLQuery("select {p.*}, {pa1.*}, {pa2.*}, {cs1.*}, {cs2.*}, {c1.*}, {c2.*} from PREDICATION p " +
				//SQLQuery q2 = hb_session.createSQLQuery("select {p.*} from PREDICATION p " +
						"inner join PREDICATION_ARGUMENT pa1 on p.PREDICATION_ID=pa1.PREDICATION_ID " +
						"inner join PREDICATION_ARGUMENT pa2 on p.PREDICATION_ID=pa2.PREDICATION_ID " +
						"inner join CONCEPT_SEMTYPE cs1 on pa1.CONCEPT_SEMTYPE_ID=cs1.CONCEPT_SEMTYPE_ID " +
						"inner join CONCEPT_SEMTYPE cs2 on pa2.CONCEPT_SEMTYPE_ID=cs2.CONCEPT_SEMTYPE_ID " +
						"inner join CONCEPT c1 on cs1.CONCEPT_ID=c1.CONCEPT_ID " +
						"inner join CONCEPT c2 on cs2.CONCEPT_ID=c2.CONCEPT_ID " +
						"where p.PREDICATE=:predicate AND pa1.TYPE='S' AND pa2.TYPE='O' AND c1.CONCEPT_ID=:subjid AND c2.CONCEPT_ID=:objid").
						addEntity("p", Predication.class).
						addJoin("pa1","p.predicationArgumentSet").
						addJoin("pa2","p.predicationArgumentSet").
						addJoin("cs1","pa1.conceptSemtype").
						addJoin("cs2","pa2.conceptSemtype").
						addJoin("c1","cs1.concept").
						addJoin("c2","cs2.concept");

				q2.setCacheable(true);

				SAXBuilder builder = new SAXBuilder();
				Document umlsDoc = builder.build(new InputStreamReader(umlsStream,"UTF8"));
				Iterator relIter = umlsDoc.getRootElement().getChildren("UmlsRelation").iterator();
				while (relIter.hasNext()) {
					Element e = (Element)relIter.next();
					String rangeCui = e.getChildTextTrim("RangeCui");
					String domainCui = e.getChildTextTrim("DomainCui");
					String rel = e.getChildTextTrim("RelationAttribute");

					//switch subject-object if necessary -- the relation has reverse direction
					UmlsRelationPredicateMapping urpm = (UmlsRelationPredicateMapping)mappings.get(rel);
					if (urpm == null) continue;
					if (urpm.isInverse()) {
						String tempCui = rangeCui;
						rangeCui = domainCui;
						domainCui = tempCui;
					}

					// log.debug("range:domain:rel: " + rangeCui + ":" + domainCui + ":" + rel);
					List<Concept> rangeConcs = new ArrayList<Concept>();
					List<Concept> domainConcs = new ArrayList<Concept>();

					if (seenCuis.containsKey(rangeCui))
						rangeConcs.add(seenCuis.get(rangeCui));
					else {
						q1.setParameter("cui",rangeCui);
						// log.debug("Querying for " + rangeCui);
						rangeConcs = (List<Concept>)q1.list();
						//seenCuis.put(rangeCui,(Concept)rangeConcs.get(0));
					}

					if (seenCuis.containsKey(domainCui))
						domainConcs.add(seenCuis.get(domainCui));
					else {
						q1.setParameter("cui",domainCui);
						// log.debug("Querying for " + domainCui);
						domainConcs = (List<Concept>)q1.list();
						//seenCuis.put(domainCui,(Concept)domainConcs.get(0));
					}

					// log.debug("Range: " + rangeConcs.get(0).toString());
					// log.debug("Domain: " + domainConcs.get(0).toString());
					Set<Concept> rangeSet = new HashSet<Concept>(rangeConcs);
					Set<Concept> domainSet = new HashSet<Concept>(domainConcs);
					if (concepts.containsKey(rangeSet)) {
						cnt = ((Integer)concepts.get(rangeSet)).intValue();
						concepts.put(rangeSet,new Integer(cnt+1));
					} else {
						concepts.put(rangeSet,new Integer(1));
						semtypes.put(rangeSet,getSemtypes(rangeConcs.get(0).getConceptSemtypeSet(),"semrep"));
					}
					if (concepts.containsKey(domainSet)) {
						cnt = ((Integer)concepts.get(domainSet)).intValue();
						concepts.put(domainSet,new Integer(cnt+1));
					} else {
						concepts.put(domainSet,new Integer(1));
						semtypes.put(domainSet,getSemtypes(domainConcs.get(0).getConceptSemtypeSet(),"semrep"));
					}

					q2.setParameter("predicate", ((UmlsRelationPredicateMapping)mappings.get(rel)).getPredicate().getPredicateText());
					q2.setParameter("subjid",((Concept)rangeConcs.get(0)).getConceptId());
					q2.setParameter("objid",((Concept)domainConcs.get(0)).getConceptId());
					// log.debug("Querying for predication " + rangeCui + "|" + rel + "|" + domainCui);

					Predication p = null;
					List qlist = q2.list();
					if (qlist.size() == 1) {
						Object[] os = (Object[])qlist.get(0);
						if (os != null)
							p = (Predication)os[0];
					} else if (qlist.size() > 1) {
						Iterator qiter = qlist.iterator();
						while (qiter.hasNext()) {
							Object[] os = (Object[])qiter.next();
							if (os != null) {
								p = (Predication)os[0];
								if (p.getPredicationArgumentSet().size() == 2) break;
							}
						}
					}

					if (p != null) {
						if (predications.get(p) == null) predications.put(p,null);
					}
					else {
						p = new Predication();
						p.setPredicate(((UmlsRelationPredicateMapping)mappings.get(rel)).getPredicate().getPredicateText());
						p.setType("semrep");
						Set<PredicationArgument> paSet = new HashSet<PredicationArgument>();

						Iterator rangeIter = rangeConcs.iterator();
						while (rangeIter.hasNext()) {
							Concept rangeConc = (Concept)rangeIter.next();
							// log.debug("Concept: " + rangeConc.toString());
							Set rangeConcSemtypeSet = rangeConc.getConceptSemtypeSet();
							Iterator semtypeIter = rangeConcSemtypeSet.iterator();
							while (semtypeIter.hasNext()) {
								ConceptSemtype cs = (ConceptSemtype)semtypeIter.next();
								PredicationArgument pa1 = new PredicationArgument();
								pa1.setType("S");
								pa1.setConceptSemtype(cs);
								paSet.add(pa1);
							}
						}

						Iterator domainIter = domainConcs.iterator();
						while (domainIter.hasNext()) {
							Concept domainConc = (Concept)domainIter.next();
							// log.debug("Concept: " + domainConc.toString());
							Set domainConcSemtypeSet = domainConc.getConceptSemtypeSet();
							Iterator semtypeIter = domainConcSemtypeSet.iterator();
							while (semtypeIter.hasNext()) {
								ConceptSemtype cs = (ConceptSemtype)semtypeIter.next();
								PredicationArgument pa2 = new PredicationArgument();
								pa2.setType("O");
								pa2.setConceptSemtype(cs);
								paSet.add(pa2);
							}
						}

						p.setPredicationArgumentSet(paSet);
						predications.put(p,null);
					}
				}
				umlsStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		for(Set<Concept> c: concepts.keySet()) {
			Set<Concept> metaConcs = getConceptType(c,"META");
			Set<Concept> entrezConcs = entrezgenes.get(c);
			Set<String> cuis = new HashSet<String>();
			if (metaConcs != null)
				cuis = getUis(metaConcs);
			Set<String> entrezids = new HashSet<String>();
			if (entrezConcs != null)
				entrezids = getUis(entrezConcs);

			Set<String> names = getNames(c,lang);

			String uiStr = new String();
			if (cuis.size() > 0) uiStr = flatten(cuis);
			if (entrezids.size() > 0) {
				if (uiStr.length() > 0)
					uiStr += ",";
				uiStr += flatten(entrezids);
			}

			//log.debug("UI String: " + uiStr);
			Element n = new Element("node");

			Set<String> sts = semtypes.get(c);
			n.setAttribute("id", flatten(getUis(c)));
			n.setAttribute("name", flatten(names));
			n.setAttribute("semtype", flatten(sts));
			n.setAttribute("size", ((Integer)concepts.get(c)).toString());

			SemanticGroup sg = (SemanticGroup)semanticTypeMap.get(sts.iterator().next());
			if (sg != null) {
				String colorCode = sg.getColorCode();
				n.setAttribute("color", colorCode);
			}
			else {
				log.debug("semantic group is null. " + flatten(names) + " " + flatten(sts) + " " + flatten(getUis(c)));
			}
			if (cuis.size() == 1)
				n.setAttribute("umlsks_url",
						"http://mor.nlm.nih.gov/perl/semnav.pl?DB_CODE=UMLS_2006AA&amp;HREL=ALL&amp;REM_TRANS=1&amp;CUI=" + flatten(cuis));

			if (entrezids.size() == 1)
				n.setAttribute("entrezgene_url", "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?db=gene&cmd=Retrieve&dopt=Graphics&list_uids=" + flatten(entrezids));
			else if (entrezids.size() > 1)
				n.setAttribute("entrezgene_url", "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?db=gene&cmd=Retrieve&list_uids=" + flatten(entrezids));

			Set<String> ghrSet = getGhrSet(c);
			// log.debug("GHR: " + flatten(ghrSet));
			Set<String> omimSet = getOmimSet(c);
			// log.debug("OMIM: " + flatten(omimSet));
			if (ghrSet.size() == 1) {
				Set sems = (Set)semtypes.get(c);
				Iterator semIter = sems.iterator();
				boolean disorder = false;
				while (semIter.hasNext()) {
					String sem = (String)semIter.next();
					if (Constants.DISORDER_SEMGROUP.contains(sem)) { disorder = true; break;}
				}
				if (disorder)
					n.setAttribute("ghr_url", "http://ghr.nlm.nih.gov/condition=" + flatten(ghrSet));
				else
					n.setAttribute("ghr_url", "http://ghr.nlm.nih.gov/gene=" + flatten(ghrSet));
			} else if (ghrSet.size() > 1) {
				n.setAttribute("ghr_url", "http://ghr.nlm.nih.gov/search/term=" + flatten(ghrSet));
			}

			if (omimSet.size() == 1)
				n.setAttribute("omim_url", "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?db=omim&cmd=Retrieve&dopt=Graphics&list_uids=" + flatten(omimSet));
			else if (omimSet.size() > 1)
				n.setAttribute("omim_url", "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?db=omim&cmd=Retrieve&list_uids=" + flatten(omimSet));

			root.addContent(n);
		}

		for(Predication p :predications.keySet()){
			String predicate = p.getPredicate();
			// log.debug("predicate for edge: " + predicate);
			Element e = new Element("edge");
			e.setAttribute("source", flatten(getUis(getConcepts(p.getBestSubjectSemtypes(),p.getType()))));
			e.setAttribute("target", flatten(getUis(getConcepts(p.getBestObjectSemtypes(), p.getType()))));
			if (lang == null)
				e.setAttribute("label", predicate);
			else {
				Map trMap = ((Predicate)predicateMap.get(predicate)).getTranslationMap();
				if (trMap == null || trMap.get(lang) == null)
					e.setAttribute("label", predicate);
				else
					e.setAttribute("label", (String)trMap.get(lang));
			}

			if (predicateMap.get(predicate)!=null)
				e.setAttribute("color", ((Predicate)predicateMap.get(predicate)).getColorCode());
			else
				e.setAttribute("color", "#000000");
			Map sentences = (Map)predications.get(p);
			if (sentences == null)
				e.setAttribute("infer_level", "1");
			else
				e.setAttribute("infer_level", "0");

			root.addContent(e);
			if (predications.get(p) != null) {
				Map preds = (Map)predications.get(p);
				Iterator sentIter = preds.keySet().iterator();
				while (sentIter.hasNext()) {
					Sentence sent = (Sentence)sentIter.next();
					Element s = new Element("sentence");
					s.setAttribute("id", sent.getPmid() + "." + sent.getType() + "." + sent.getNumber());
					s.setAttribute("text", sent.getSentence());
					s.setAttribute("size", ((Integer)preds.get(sent)).toString());
					e.addContent(s);
				}
			}
		}
		Element seedE = new Element("seed");
		if (seed==null)
			seed = "empty";
		seedE.setAttribute("value",seed);
		root.addContent(seedE);

		return doc;
	}

//	private static void printCollection(String str, Collection c) {
//		Iterator iter = c.iterator();
//		while (iter.hasNext())  {
//			log.debug(str + ((Concept)iter.next()).getPreferredName());
//		}
//	}

	private static <T> String flatten(Collection<T> c) {
		String str = new String();
		if (c.size() <= 0)
			return str;
		Iterator iter = c.iterator();
		str += iter.next();
		while (iter.hasNext()) {
			str += "," + iter.next();
		}
		return str;
	}

	private static Set<Concept> getConcepts(Set<ConceptSemtype> c, String type) {
		Set<Concept> s = new TreeSet<Concept>();
		for(ConceptSemtype cs : c){
			Concept con = cs.getConcept();
			if ("semrep".equals(type)) {
				// in upload cases, concept-->conceptSemtype association is *sort of* set here.
				if (con.getConceptSemtypeSet() == null) {
					Set<ConceptSemtype> csSet = new HashSet<ConceptSemtype>();
					csSet.add(cs);
					con.setConceptSemtypeSet(csSet);
				}
				s.add(con);
			}
		}
		return s;
	}

	public static Set<Concept> getConcepts(Set<ConceptSemtype> c) {
		Set<Concept> s = new TreeSet<Concept>();
		for(ConceptSemtype cs :c){
			Concept con = cs.getConcept();
			// in upload cases, concept-->conceptSemtype association is *sort of* set here.
			if (con.getConceptSemtypeSet() == null) {
				Set<ConceptSemtype> csSet = new HashSet<ConceptSemtype>();
				csSet.add(cs);
				con.setConceptSemtypeSet(csSet);
			}
			s.add(con);
		}
		return s;
	}

	private static Set<Concept> getConceptType(Set<Concept> c, String type) {
		Set<Concept> s = new HashSet<Concept>();
		for(Concept conc : c)
			if (type.equals(conc.getType()))
				s.add(conc);

		return s;
	}

	private static Set<String> getSemtypes(Set<ConceptSemtype> c, String type) {
		Set<String> s = new HashSet<String>();
		for(ConceptSemtype cs : c){
			if ("semrep".equals(type))
				s.add(cs.getSemtype());
		}
		return s;
	}

	private static Set<String> getUis(Set<Concept> c) {
		Set<String> s = new HashSet<String>();
		for(Concept conc:c)
			s.add(conc.getCui());
		return s;
	}

	private static Set<String> getNames(Set<Concept> c, String lang) {
		Set<String> s = new TreeSet<String>();
		for(Concept conc:c)
			if (lang == null)
				s.add(conc.getPreferredName());
			else {
				Iterator triter = conc.getConceptTranslationSet().iterator();
				boolean trFound = false;
				while (triter.hasNext()) {
					ConceptTranslation ct = (ConceptTranslation)triter.next();
					if (lang.equals(ct.getLanguage())) {
						s.add(ct.getTranslation());
						trFound = true;
						break;
					}
				}
				if (!trFound) s.add(conc.getPreferredName());
			}
		return s;
	}

	private static Set<String> getGhrSet(Set<Concept> c) {
		Set<String> s = new HashSet<String>();
		for(Concept cc : c) {
			String ghrStr = cc.getGhr();
			if (ghrStr == null || ghrStr.equals("")) continue;
			s.add(ghrStr);
		}
		return s;
	}

	private static Set<String> getOmimSet(Set<Concept> c) {
		Set<String> s = new HashSet<String>();
		Iterator iter = c.iterator();
		while (iter.hasNext()) {
			Concept conc = (Concept)iter.next();
			// log.debug("Concept:" + conc.toString());
			String omimStr = conc.getOmim();
			if (omimStr == null || omimStr.equals("")) continue;
			// log.debug("OMIM str: " + omimStr);
			String omims[] = omimStr.split("[:]");
			Collections.addAll(s,omims);
		}
		return s;
	}

	public static void printMap(Map<? extends Object, ? extends Object> map) {
		log.debug("Map size: "  + map.size());
		for(Map.Entry<? extends Object, ? extends Object> entry : map.entrySet()){
			log.debug(entry.getKey().toString() + "_" + entry.getValue().toString());
		}
	}

	private static Map<String,Concept> saveConcepts(Set<Concept> s) {
		Map<String,Concept> m = new HashMap<String,Concept>();
		for(Concept c :s)
			m.put(c.getCui(), c);
		return m;
	}

	@SuppressWarnings("unchecked")
	public static Document parseTest(List<SentencePredication> sps, String filename, String lang, ServletContext sc, boolean useUmls,String seed, Session hb_session) {
		Map<Set<Concept>,Integer> concepts = new HashMap<Set<Concept>,Integer>();
		Map<Set<Concept>,Set<String>> semtypes = new HashMap<Set<Concept>,Set<String>>();
		Map<Set<Concept>,Set<Concept>> entrezgenes = new HashMap<Set<Concept>,Set<Concept>>();
		Map<Predication,Map<Sentence,Integer>> predications = new HashMap<Predication,Map<Sentence,Integer>>();

		int cnt = 0;
		Map predicateMap = (Map)sc.getAttribute("predicateMap");
		Map semanticTypeMap = (Map)sc.getAttribute("semanticTypeMappings");
		// log.debug("Predicate map size:" + predicateMap.size());

		Iterator piter = predicateMap.keySet().iterator();
		while (piter.hasNext()) {
			String p = (String)piter.next();
			Predicate c = (Predicate)predicateMap.get(p);
			// log.debug("Predicate Map: " + p +"|" + c.getColorCode());
		}

		InputStream umlsStream = null;
		Map mappings = null;
		// enrich with UMLS relations
		if (useUmls) {
			umlsStream = sc.getResourceAsStream(filename);
			// log.debug("Opened umls stream.");
			mappings = (Map)sc.getAttribute("predicateMappings");
			// log.debug("Mapping size: " + mappings.size());
		}

		Element root = new Element("graph");
		Document doc = new Document(root);

		Map<String,Concept> seenCuis = new HashMap<String,Concept>();
		for(SentencePredication sp : sps){
			// log.debug("sentence predication: " + sp.toString());
			Predication p = sp.getPredication();
			// log.debug("predicate in sentence predication:" + p.getPredicate());
			String ptype = p.getType();
			Sentence snt = sp.getSentence();

			Set<ConceptSemtype> displaySubjects = p.getBestSubjectSemtypes();
			Set<ConceptSemtype> displayObjects = p.getBestObjectSemtypes();
			Set<Concept> displaySubjConcs = getConcepts(displaySubjects);
			Set<Concept> displayObjConcs = getConcepts(displayObjects);
			if (displayObjConcs.size() <= 0 || displaySubjConcs.size() <=0) continue;

			Set<Concept> subjEntrezIds = p.getSubjectsWithType("ENTREZ");
			if (entrezgenes.containsKey(displaySubjConcs)) {
				Set<Concept> existingSubjEntrezIds = entrezgenes.get(displaySubjConcs);
				existingSubjEntrezIds.addAll(subjEntrezIds);
				//entrezgenes.put(displaySubjConcs, existingSubjEntrezIds);
			} else
				entrezgenes.put(displaySubjConcs, subjEntrezIds);

			Set<Concept> objEntrezIds = p.getObjectsWithType("ENTREZ");
			if (entrezgenes.containsKey(displayObjConcs)) {
				Set<Concept> existingObjEntrezIds = entrezgenes.get(displayObjConcs);
				existingObjEntrezIds.addAll(objEntrezIds);
				//entrezgenes.put(displayObjConcs, existingObjEntrezIds);
			} else
				entrezgenes.put(displayObjConcs, objEntrezIds);

			seenCuis.putAll(saveConcepts(displaySubjConcs));
			seenCuis.putAll(saveConcepts(displayObjConcs));
			Set<String> subjSemtypes = getSemtypes(displaySubjects, ptype);
			Set<String> objSemtypes = getSemtypes(displayObjects, ptype);

			// need to flatten somehow
		    if (concepts.containsKey(displaySubjConcs)) {
				cnt = ((Integer)concepts.get(displaySubjConcs)).intValue();
				concepts.put(displaySubjConcs,cnt+1);
			} else {
				concepts.put(displaySubjConcs,1);
				semtypes.put(displaySubjConcs,subjSemtypes);
			}

			if (concepts.containsKey(displayObjConcs)) {
				cnt = ((Integer)concepts.get(displayObjConcs)).intValue();
				concepts.put(displayObjConcs,cnt+1);
			} else {
				concepts.put(displayObjConcs,1);
				semtypes.put(displayObjConcs,objSemtypes);
			}

			Map<Sentence,Integer> sentences;
			if (predications.containsKey(p)) {
				sentences = predications.get(p);
				if (sentences.containsKey(snt)) {
					int cn = ((Integer)sentences.get(snt)).intValue() + 1;
					sentences.put(snt, cn);
				} else {
					sentences.put(snt, 1);
				}
			}
			else {
				sentences = new HashMap<Sentence,Integer>();
				sentences.put(snt, 1);
			}
			predications.put(p,sentences);
		}

		if (umlsStream != null) {
			try {

				// Session hb_session = HibernateSessionFactory.currentSession();
				/* Configuration configuration = new Configuration().configure();
				configuration.setProperty("hibernate.connection.url", "jdbc:mysql://skr3:3306/semmed2006test");
				SessionFactory sessionFactory = configuration.buildSessionFactory();
				Session hb_session = sessionFactory.openSession(); */
				Query q1 = hb_session.createQuery("select c from Concept as c inner join fetch c.conceptSemtypeSet cs where c.cui= :cui").
					setCacheable(true);
/*				SQLQuery q1 = hb_session.createSQLQuery("select {c.*}, {cs.*} from CONCEPT c inner join CONCEPT_SEMTYPE cs " +
						"on c.CONCEPT_ID=cs.CONCEPT_ID where c.CUI= :cui").addEntity("c",Concept.class).addJoin("cs","c.conceptSemtypeSet");*/
				SQLQuery q2 = hb_session.createSQLQuery("select {p.*}, {pa1.*}, {pa2.*}, {cs1.*}, {cs2.*}, {c1.*}, {c2.*} from PREDICATION p " +
				//SQLQuery q2 = hb_session.createSQLQuery("select {p.*} from PREDICATION p " +
						"inner join PREDICATION_ARGUMENT pa1 on p.PREDICATION_ID=pa1.PREDICATION_ID " +
						"inner join PREDICATION_ARGUMENT pa2 on p.PREDICATION_ID=pa2.PREDICATION_ID " +
						"inner join CONCEPT_SEMTYPE cs1 on pa1.CONCEPT_SEMTYPE_ID=cs1.CONCEPT_SEMTYPE_ID " +
						"inner join CONCEPT_SEMTYPE cs2 on pa2.CONCEPT_SEMTYPE_ID=cs2.CONCEPT_SEMTYPE_ID " +
						"inner join CONCEPT c1 on cs1.CONCEPT_ID=c1.CONCEPT_ID " +
						"inner join CONCEPT c2 on cs2.CONCEPT_ID=c2.CONCEPT_ID " +
						"where p.PREDICATE=:predicate AND pa1.TYPE='S' AND pa2.TYPE='O' AND c1.CONCEPT_ID=:subjid AND c2.CONCEPT_ID=:objid").
						addEntity("p", Predication.class).
						addJoin("pa1","p.predicationArgumentSet").
						addJoin("pa2","p.predicationArgumentSet").
						addJoin("cs1","pa1.conceptSemtype").
						addJoin("cs2","pa2.conceptSemtype").
						addJoin("c1","cs1.concept").
						addJoin("c2","cs2.concept");

				q2.setCacheable(true);

				SAXBuilder builder = new SAXBuilder();
				Document umlsDoc = builder.build(new InputStreamReader(umlsStream,"UTF8"));
				Iterator relIter = umlsDoc.getRootElement().getChildren("UmlsRelation").iterator();
				while (relIter.hasNext()) {
					Element e = (Element)relIter.next();
					String rangeCui = e.getChildTextTrim("RangeCui");
					String domainCui = e.getChildTextTrim("DomainCui");
					String rel = e.getChildTextTrim("RelationAttribute");

					//switch subject-object if necessary -- the relation has reverse direction
					UmlsRelationPredicateMapping urpm = (UmlsRelationPredicateMapping)mappings.get(rel);
					if (urpm == null) continue;
					if (urpm.isInverse()) {
						String tempCui = rangeCui;
						rangeCui = domainCui;
						domainCui = tempCui;
					}

					// log.debug("range:domain:rel: " + rangeCui + ":" + domainCui + ":" + rel);
					List<Concept> rangeConcs = new ArrayList<Concept>();
					List<Concept> domainConcs = new ArrayList<Concept>();

					if (seenCuis.containsKey(rangeCui))
						rangeConcs.add(seenCuis.get(rangeCui));
					else {
						q1.setParameter("cui",rangeCui);
						// log.debug("Querying for " + rangeCui);
						rangeConcs = (List<Concept>)q1.list();
						//seenCuis.put(rangeCui,(Concept)rangeConcs.get(0));
					}

					if (seenCuis.containsKey(domainCui))
						domainConcs.add(seenCuis.get(domainCui));
					else {
						q1.setParameter("cui",domainCui);
						// log.debug("Querying for " + domainCui);
						domainConcs = (List<Concept>)q1.list();
						//seenCuis.put(domainCui,(Concept)domainConcs.get(0));
					}

					// log.debug("Range: " + rangeConcs.get(0).toString());
					// log.debug("Domain: " + domainConcs.get(0).toString());
					Set<Concept> rangeSet = new HashSet<Concept>(rangeConcs);
					Set<Concept> domainSet = new HashSet<Concept>(domainConcs);
					if (concepts.containsKey(rangeSet)) {
						cnt = ((Integer)concepts.get(rangeSet)).intValue();
						concepts.put(rangeSet,new Integer(cnt+1));
					} else {
						concepts.put(rangeSet,new Integer(1));
						semtypes.put(rangeSet,getSemtypes(rangeConcs.get(0).getConceptSemtypeSet(),"semrep"));
					}
					if (concepts.containsKey(domainSet)) {
						cnt = ((Integer)concepts.get(domainSet)).intValue();
						concepts.put(domainSet,new Integer(cnt+1));
					} else {
						concepts.put(domainSet,new Integer(1));
						semtypes.put(domainSet,getSemtypes(domainConcs.get(0).getConceptSemtypeSet(),"semrep"));
					}

					q2.setParameter("predicate", ((UmlsRelationPredicateMapping)mappings.get(rel)).getPredicate().getPredicateText());
					q2.setParameter("subjid",((Concept)rangeConcs.get(0)).getConceptId());
					q2.setParameter("objid",((Concept)domainConcs.get(0)).getConceptId());
					// log.debug("Querying for predication " + rangeCui + "|" + rel + "|" + domainCui);

					Predication p = null;
					List qlist = q2.list();
					if (qlist.size() == 1) {
						Object[] os = (Object[])qlist.get(0);
						if (os != null)
							p = (Predication)os[0];
					} else if (qlist.size() > 1) {
						Iterator qiter = qlist.iterator();
						while (qiter.hasNext()) {
							Object[] os = (Object[])qiter.next();
							if (os != null) {
								p = (Predication)os[0];
								if (p.getPredicationArgumentSet().size() == 2) break;
							}
						}
					}

					if (p != null) {
						if (predications.get(p) == null) predications.put(p,null);
					}
					else {
						p = new Predication();
						p.setPredicate(((UmlsRelationPredicateMapping)mappings.get(rel)).getPredicate().getPredicateText());
						p.setType("semrep");
						Set<PredicationArgument> paSet = new HashSet<PredicationArgument>();

						Iterator rangeIter = rangeConcs.iterator();
						while (rangeIter.hasNext()) {
							Concept rangeConc = (Concept)rangeIter.next();
							// log.debug("Concept: " + rangeConc.toString());
							Set rangeConcSemtypeSet = rangeConc.getConceptSemtypeSet();
							Iterator semtypeIter = rangeConcSemtypeSet.iterator();
							while (semtypeIter.hasNext()) {
								ConceptSemtype cs = (ConceptSemtype)semtypeIter.next();
								PredicationArgument pa1 = new PredicationArgument();
								pa1.setType("S");
								pa1.setConceptSemtype(cs);
								paSet.add(pa1);
							}
						}

						Iterator domainIter = domainConcs.iterator();
						while (domainIter.hasNext()) {
							Concept domainConc = (Concept)domainIter.next();
							// log.debug("Concept: " + domainConc.toString());
							Set domainConcSemtypeSet = domainConc.getConceptSemtypeSet();
							Iterator semtypeIter = domainConcSemtypeSet.iterator();
							while (semtypeIter.hasNext()) {
								ConceptSemtype cs = (ConceptSemtype)semtypeIter.next();
								PredicationArgument pa2 = new PredicationArgument();
								pa2.setType("O");
								pa2.setConceptSemtype(cs);
								paSet.add(pa2);
							}
						}

						p.setPredicationArgumentSet(paSet);
						predications.put(p,null);
					}
				}
				// hb_session.close();
				umlsStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		for(Set<Concept> c: concepts.keySet()) {
			Set<Concept> metaConcs = getConceptType(c,"META");
			Set<Concept> entrezConcs = entrezgenes.get(c);
			Set<String> cuis = new HashSet<String>();
			if (metaConcs != null)
				cuis = getUis(metaConcs);
			Set<String> entrezids = new HashSet<String>();
			if (entrezConcs != null)
				entrezids = getUis(entrezConcs);

			Set<String> names = getNames(c,lang);

			String uiStr = new String();
			if (cuis.size() > 0) uiStr = flatten(cuis);
			if (entrezids.size() > 0) {
				if (uiStr.length() > 0)
					uiStr += ",";
				uiStr += flatten(entrezids);
			}

			//log.debug("UI String: " + uiStr);
			Element n = new Element("node");

			Set<String> sts = semtypes.get(c);
			n.setAttribute("id", flatten(getUis(c)));
			n.setAttribute("name", flatten(names));
			n.setAttribute("semtype", flatten(sts));
			n.setAttribute("size", ((Integer)concepts.get(c)).toString());

			SemanticGroup sg = (SemanticGroup)semanticTypeMap.get(sts.iterator().next());
			if (sg != null) {
				String colorCode = sg.getColorCode();
				n.setAttribute("color", colorCode);
			}
			else {
				// log.debug("semantic group is null. " + flatten(names) + " " + flatten(sts) + " " + flatten(getUis(c)));
			}
			if (cuis.size() == 1)
				n.setAttribute("umlsks_url",
						"http://mor.nlm.nih.gov/perl/semnav.pl?DB_CODE=UMLS_2006AA&amp;HREL=ALL&amp;REM_TRANS=1&amp;CUI=" + flatten(cuis));

			if (entrezids.size() == 1)
				n.setAttribute("entrezgene_url", "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?db=gene&cmd=Retrieve&dopt=Graphics&list_uids=" + flatten(entrezids));
			else if (entrezids.size() > 1)
				n.setAttribute("entrezgene_url", "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?db=gene&cmd=Retrieve&list_uids=" + flatten(entrezids));

			Set<String> ghrSet = getGhrSet(c);
			// log.debug("GHR: " + flatten(ghrSet));
			Set<String> omimSet = getOmimSet(c);
			// log.debug("OMIM: " + flatten(omimSet));
			if (ghrSet.size() == 1) {
				Set sems = (Set)semtypes.get(c);
				Iterator semIter = sems.iterator();
				boolean disorder = false;
				while (semIter.hasNext()) {
					String sem = (String)semIter.next();
					if (Constants.DISORDER_SEMGROUP.contains(sem)) { disorder = true; break;}
				}
				if (disorder)
					n.setAttribute("ghr_url", "http://ghr.nlm.nih.gov/condition=" + flatten(ghrSet));
				else
					n.setAttribute("ghr_url", "http://ghr.nlm.nih.gov/gene=" + flatten(ghrSet));
			} else if (ghrSet.size() > 1) {
				n.setAttribute("ghr_url", "http://ghr.nlm.nih.gov/search/term=" + flatten(ghrSet));
			}

			if (omimSet.size() == 1)
				n.setAttribute("omim_url", "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?db=omim&cmd=Retrieve&dopt=Graphics&list_uids=" + flatten(omimSet));
			else if (omimSet.size() > 1)
				n.setAttribute("omim_url", "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?db=omim&cmd=Retrieve&list_uids=" + flatten(omimSet));

			root.addContent(n);
		}

		for(Predication p :predications.keySet()){
			String predicate = p.getPredicate();
			// log.debug("predicate for edge: " + predicate);
			Element e = new Element("edge");
			e.setAttribute("source", flatten(getUis(getConcepts(p.getBestSubjectSemtypes(),p.getType()))));
			e.setAttribute("target", flatten(getUis(getConcepts(p.getBestObjectSemtypes(), p.getType()))));
			if (lang == null)
				e.setAttribute("label", predicate);
			else {
				Map trMap = ((Predicate)predicateMap.get(predicate)).getTranslationMap();
				if (trMap == null || trMap.get(lang) == null)
					e.setAttribute("label", predicate);
				else
					e.setAttribute("label", (String)trMap.get(lang));
			}

			if (predicateMap.get(predicate)!=null)
				e.setAttribute("color", ((Predicate)predicateMap.get(predicate)).getColorCode());
			else
				e.setAttribute("color", "#000000");
			Map sentences = (Map)predications.get(p);
			if (sentences == null)
				e.setAttribute("infer_level", "1");
			else
				e.setAttribute("infer_level", "0");

			root.addContent(e);
			if (predications.get(p) != null) {
				Map preds = (Map)predications.get(p);
				Iterator sentIter = preds.keySet().iterator();
				while (sentIter.hasNext()) {
					Sentence sent = (Sentence)sentIter.next();
					Element s = new Element("sentence");
					s.setAttribute("id", sent.getPmid() + "." + sent.getType() + "." + sent.getNumber());
					s.setAttribute("text", sent.getSentence());
					s.setAttribute("size", ((Integer)preds.get(sent)).toString());
					e.addContent(s);
				}
			}
		}
		Element seedE = new Element("seed");
		if (seed==null)
			seed = "empty";
		seedE.setAttribute("value",seed);
		root.addContent(seedE);
		return doc;
	}
}
