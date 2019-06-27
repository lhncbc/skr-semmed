package gov.nih.nlm.semmed.model;

import gov.nih.nlm.semmed.struts.action.SemrepAction;
import gov.nih.nlm.semmed.util.ArticleDataSource;
import gov.nih.nlm.semmed.util.Constants;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.util.LabelValueBean;


/**
 * A list of predications. Contains methods for loading predications from a DB.
 *
 *
 * @author rodriguezal
 *
 */
public class APredicationList extends ArrayList<APredication> implements Serializable {

	public static enum Type {GENETIC,TREATMENT,INTERATCION,PHARMACOGENOMICS,DIAGNOSIS};

	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(APredicationList.class);

	//private static Set<Integer> existingDocuments;

	private static DataSource ds;
	// private static DataSource testds;
	// public DataSource ds;

	private static final String QUERY_PREFIX = "SELECT scc.PMID, s.SENTENCE, s_name, predicate, o_name FROM "+
												"{} as scc, SENTENCE s WHERE ";
	private static final String QUERY_PREFIX_SCC1 = "SELECT scc.PMID, s.SENTENCE, s_name, predicate, o_name FROM PREDICATE_AGGREGATE scc, SENTENCE s WHERE ";

	private static final String QUERY_PREFIX_TITLE = "SELECT PMID FROM TITLES WHERE PMID >= ";


	static{
		try{
			ds = setupDataSource();
		}catch(Exception e){
			System.out.println("************************************************************");
			System.out.println("************************************************************");
			System.out.println("************************************************************");
			System.out.println("************************************************************");
			System.out.println("************************************************************");
			System.err.println("Couldn't load database driver!!");
			e.printStackTrace();
		}
	}

	public static DataSource getDataSource(){
		return ds;
	}

	public APredicationList(){
		super();
	}

	public APredicationList(List<APredication> list){
		super(list);
	}

	public APredicationList(int[] pmids,ArticleDataSource.SourceType sourceType) throws SQLException{
		String query = createQuery(pmids,sourceType);
		init(query,false,new ArticleDataSource.SourceType[]{sourceType});
	}

	public APredicationList(int[] pids,int[] sids,ArticleDataSource.SourceType[] source) throws SQLException{
			String query = createQuery(pids,sids,source);
			init(query,true,source);
	}

	public APredicationList(int low,int size) throws SQLException{
		String query = createQuery(low, size);
		init(query,false);
	}

	public APredicationList(int low,int high, int size) throws SQLException{
		String query = createTitleQuery(low, high);
		initWithSize(query, size, false);
	}

	protected void init(String query,boolean includeSentence,ArticleDataSource.SourceType[] source) throws SQLException{
		if (query!=null){
			Connection con = ds.getConnection();
			Statement s = con.createStatement();
			ResultSet rs = s.executeQuery(query);
			createPredications(rs,includeSentence,source);
			rs.close();
			s.close();
			con.close();
		}
	}

	protected void init(String query,boolean includeSentence) throws SQLException{
		if (query!=null){
			Connection con = ds.getConnection();
			Statement s = con.createStatement();
			ResultSet rs = s.executeQuery(query);
			// log.debug("Create predications with DB results");
			createPredications(rs,includeSentence);
			rs.close();
			s.close();
			con.close();
		}
	}

	protected void initWithSize(String query, int size, boolean includeSentence) throws SQLException{
		if (query!=null){
			Connection con = ds.getConnection();
			Statement s = con.createStatement();
			ResultSet rs = s.executeQuery(query);
			ArrayList<Integer> PMIDList = new ArrayList<Integer>();
			int[] newList = new int[size];
			int i = 0;
			// log.debug("Inside initWithSize()");
			while (rs.next()){
				if(i < size)
					newList[i] = (rs.getInt(1));
				else
					break;
				i++;
			}

			String predQuery = createQuery(newList,ArticleDataSource.SourceType.OPASI);
			// log.debug("Create predication Query = " + predQuery);
			rs = s.executeQuery(predQuery);
			// log.debug("Create predications with DB results");
			createPredications(rs,includeSentence);
			rs.close();
			s.close();
			con.close();
		}
	}

	protected String createQuery(int[] pids,int[] sids,ArticleDataSource.SourceType[] source) throws SQLException{

		StringBuffer query = null;
		Map<ArticleDataSource.SourceType,StringBuffer> queries = new HashMap<ArticleDataSource.SourceType,StringBuffer>();

		int addedTotal=0;
		for(int i=0;i<pids.length;i++){
			StringBuffer sb = queries.get(source[i]);
			int addedIndividual = 0;
			if (sb==null){
				String table = null;
				switch(source[i]){
				case MEDLINE:
					table = "PREDICATE_AGGREGATE";
					break;
				case CLINICAL_TRIALS:
					table = "scc2";
					break;
				case OPASI:
					table = "PREDICATE_AGGREGATE";
					break;
				}
				sb = new StringBuffer(QUERY_PREFIX.replaceAll("\\{\\}", table));
				queries.put(source[i], sb);
			}else
				addedIndividual = 1;

			if (addedIndividual>0)
				sb.append(" OR ");
			sb.append("(scc.PID=");
			sb.append(pids[i]);
			sb.append(" AND scc.SID=");
			sb.append(sids[i]);
			sb.append(" AND s.SENTENCE_ID=");
			sb.append(sids[i]);
			sb.append(")");
			addedTotal++;
		}

		if (addedTotal==0)
			return null;
		else{
			Collection<StringBuffer> sbs = queries.values();
			boolean first = true;

			query = new StringBuffer();
			for(StringBuffer sb : sbs)
				if (first){
					query.append(sb);
					first = false;
				}else{
					query.append(" UNION ");
					query.append(sb);
				}
			query.append(" ORDER by PMID");
			// log.debug("DB query = " + query.toString());
			return query.toString();
		}
	}

	protected String createQuery(int[] pids,int[] sids) throws SQLException{

		StringBuffer query = null;
		Map<ArticleDataSource.SourceType,StringBuffer> queries = new HashMap<ArticleDataSource.SourceType,StringBuffer>();

		int addedTotal=0;
		StringBuffer sb = new StringBuffer(QUERY_PREFIX_SCC1);
		for(int i=0;i<pids.length;i++){
			if (i>0)
				sb.append(" OR ");
			sb.append("(scc.PID=");
			sb.append(pids[i]);
			sb.append(" AND scc.SID=");
			sb.append(sids[i]);
			sb.append(" AND s.SENTENCE_ID=");
			sb.append(sids[i]);
			sb.append(")");
			addedTotal++;
		}

		if (addedTotal==0)
			return null;
		else{
			Collection<StringBuffer> sbs = queries.values();
			sb.append(" ORDER by PMID");
			log.debug("DB query = " + sb.toString());
			return sb.toString();
		}
	}


	protected String createQuery(int[] ids,ArticleDataSource.SourceType type) throws SQLException{

		StringBuffer query = null;
		String connector = " or PMID in ";
		String source = null;

		switch(type){
		case MEDLINE:
			source = "PREDICATE_AGGREGATE";
			break;
		case CLINICAL_TRIALS:
			source = "scc2";
		case OPASI:
			source = "PREDICATE_AGGREGATE";
			break;
		}

		query = new StringBuffer("SELECT PMID, PID, SID, predicate, s_name, s_type, s_cui, s_novel, o_name, o_type, o_cui, o_novel FROM "+source+" WHERE PMID in ");

		int added=0;
		for(int i=0;i<ids.length;){
			if (true){//existingDocuments.contains(ids[i])){
				if (added>0)
					query.append(connector);
				query.append("(");
				query.append(ids[i++]);
				added++;
			}else{
				i++;
				continue;
			}
			while (added%5000!=0 && i<ids.length){
			//	if (existingDocuments.contains(ids[i])){
					query.append(",");
					query.append(ids[i++]);
					added++;
			//	}else
				//	i++;
			}
			query.append(")");
		}

		if (added==0)
			return null;
		else
			return query.toString();
	}

	protected String createQuery(int low, int high) throws SQLException{

		StringBuffer query = new StringBuffer("SELECT PMID, PID, SID, predicate, s_name, s_type, s_cui, s_novel, o_name, o_type, o_cui, o_novel FROM PREDICATE_AGGREGATE WHERE  PMID >= " );
		if (high > low)
			query.append(low + " and PMID <= " + high);
		else
			query.append(low);
		log.debug("DB query = " + query.toString());
		return query.toString();
	}

	protected String createTitleQuery(int low, int high) throws SQLException{

		StringBuffer query = new StringBuffer(QUERY_PREFIX_TITLE);
		if (high >= low)
			query.append(low + " and PMID <= " + high);
		else
			query.append(low);
		// log.debug("DB query = " + query.toString());
		return query.toString();
	}

	public List<LabelValueBean> getRelevantConcepts(Type type){
		ArrayList<LabelValueBean> relevantConcepts = null;
		if (size() > 0) {
			List<String> relevantSemTypes = new ArrayList<String>();
			switch(type){
			case GENETIC:
			case TREATMENT:
				relevantSemTypes.addAll(Constants.DISORDER_SEMGROUP); //TODO [Alejandro] change these constants into integer values so you can do binary 'or' on them
				break;
			case INTERATCION:
				relevantSemTypes.addAll(Constants.DRUG_CHEM_SEMGROUP);
				break;
			case PHARMACOGENOMICS:
				relevantSemTypes.addAll(Constants.SUBSTANCE_SEMGROUP);
				relevantSemTypes.addAll(Constants.DISORDER_SEMGROUP);
				// relevantSemTypes.addAll(Constants.HUMAN_SEMGROUP);
				break;
			case DIAGNOSIS:
				relevantSemTypes.addAll(Constants.DISORDER_SEMGROUP);
				relevantSemTypes.addAll(Constants.DIAGNOSIS_SEMGROUP);
			}

			Map<String,Integer> map = new HashMap<String,Integer>();
			for(APredication pred : this)
//				TODO [Alejandro] check the subject/object is novel
				countArgs(map, pred.subject, pred.subjectSemtype, pred.object, pred.objectSemtype, pred.novelSubject, pred.novelObject, relevantSemTypes);


			//TODO Would be better to have the map sorted from the begining? [Alejandro]
			ArrayList<Entry<String,Integer>> tempConcs = new ArrayList<Entry<String,Integer>>( map.entrySet() );
			Collections.sort( tempConcs , new Comparator<Entry<String,Integer>>() { //TODO This comparator can be a singleton [Alejandro]
	             public int compare( Entry<String,Integer> o1 , Entry<String,Integer> o2 )
	             {
	                 int first = o1.getValue();
	                 int second = o2.getValue();
	                 if (first!=second)
	                	 return second-first;
	                 return o1.toString().compareToIgnoreCase(o2.toString());
	             }
	         });

	         relevantConcepts = new ArrayList<LabelValueBean>();
	         for(Entry<String,Integer> entry : tempConcs){
				String concept = entry.getKey();
				String value =  concept + "(" + entry.getValue() + ")";
				relevantConcepts.add(new LabelValueBean(concept, value));
			}
		}
		return relevantConcepts;
	}

	protected void countArgs(Map<String,Integer> o1,List<String> subjects,List<String> subjSemtypes,
				List<String> objects,List<String> objSemtypes, boolean novelSubject, boolean novelObject, List<String> semtypes){
		if(novelSubject == true)  // Add to the relevantConcept only when the subject is novel
			for(String semtype : subjSemtypes)
				if (semtypes.contains(semtype)){
					String subj = subjects.get(0);
					Integer i = o1.get(subj);
					if (i==null)
						o1.put(subj, 1);
					else
						o1.put(subj, i.intValue()+1);
					break;
				}

		if(novelObject == true) // Add to the relevantConcept only when the object is novel
			for(String semtype : objSemtypes)
				if (semtypes.contains(semtype)){
					String obj = objects.get(0);
					Integer i = o1.get(obj);
					if (i==null)
						o1.put(obj, 1);
					else
						o1.put(obj, i.intValue()+1);
					break;
				}
	}

	protected void createPredications(ResultSet rs,boolean includeSentence,ArticleDataSource.SourceType[] source) throws SQLException{
		ArrayList<APredication> list = this;
		if (includeSentence){
			int i=0;
			while (rs.next()){
				APredication predication = new APredication();
				predication.PMID  = rs.getInt(1);
				predication.sentence = rs.getString(2);
				predication.addSubject(rs.getString(3));
				predication.predicate = rs.getString(4);
				predication.addObject(rs.getString(5));
				predication.source = source[i];
				list.add(predication);
			}
		}else
			while (rs.next()){
				APredication predication = new APredication();
				predication.PMID = rs.getInt(1);
				predication.PID = rs.getInt(2);
				predication.SID = rs.getInt(3);
				predication.predicate = rs.getString(4);
				predication.addSubject(rs.getString(5));
				predication.addSubjectSemtype(rs.getString(6));
				predication.addSubjectCUI(rs.getString(7));
				predication.novelSubject =  rs.getInt(8)==1;
				predication.addObject(rs.getString(9));
				predication.addObjectSemtype(rs.getString(10));
				predication.addObjectCUI(rs.getString(11));
				predication.novelObject = rs.getInt(12)==1;
				predication.source = source[0];
				list.add(predication);
			}
	}

	protected void createPredications(ResultSet rs, boolean includeSentence) throws SQLException{
		ArrayList<APredication> list = this;
		if (includeSentence){
			int i=0;
			while (rs.next()){
				APredication predication = new APredication();
				predication.PMID  = rs.getInt(1);
				predication.sentence = rs.getString(2);
				predication.addSubject(rs.getString(3));
				predication.predicate = rs.getString(4);
				predication.addObject(rs.getString(5));
				predication.source = ArticleDataSource.SourceType.MEDLINE;
				list.add(predication);
			}
		}else
			while (rs.next()){
				APredication predication = new APredication();
				predication.PMID = rs.getInt(1);
				predication.PID = rs.getInt(2);
				predication.SID = rs.getInt(3);
				predication.predicate = rs.getString(4);
				predication.addSubject(rs.getString(5));
				predication.addSubjectSemtype(rs.getString(6));
				predication.addSubjectCUI(rs.getString(7));
				predication.novelSubject =  rs.getInt(8)==1;
				predication.addObject(rs.getString(9));
				predication.addObjectSemtype(rs.getString(10));
				predication.addObjectCUI(rs.getString(11));
				predication.novelObject = rs.getInt(12)==1;
				predication.source = ArticleDataSource.SourceType.MEDLINE;
				list.add(predication);
			}
	}

	public List<Long> getSentencePredicationIDs() throws SQLException{
		StringBuffer query = new StringBuffer("SELECT SENTENCE_PREDICATION_ID FROM SENTENCE_PREDICATION WHERE ");

		int added=0;
		for(APredication predication : this){
			if (added>0)
				query.append(" OR ");
			query.append("(SENTENCE_ID = ");
			query.append(predication.SID);
			query.append(" AND PREDICATION_ID = ");
			query.append(predication.PID);
			query.append(")");
			added++;
		}

		if (added==0)
			return new ArrayList<Long>(0);

		Connection con = ds.getConnection();
		Statement s = con.createStatement();
		ResultSet rs = s.executeQuery(query.toString());

		List<Long> result = new ArrayList<Long>();

		while(rs.next())
			result.add((long)rs.getInt(1));

		rs.close();
		s.close();
		con.close();

		return result;
	}

	protected static DataSource setupDataSource() throws Exception{
		Context ctx = new InitialContext();
        DataSource ds =
            (DataSource)ctx.lookup("java:comp/env/jdbc/SemMedDB");
      return ds;
	}


}
