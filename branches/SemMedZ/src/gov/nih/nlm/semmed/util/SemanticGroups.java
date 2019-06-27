/**
 *
 */
package gov.nih.nlm.semmed.util;

import java.util.Arrays;
import java.util.List;
import java.util.HashSet;
import java.util.Hashtable;

/**
 * @author hkilicoglu
 *
 */
public class SemanticGroups {

	public static final HashSet<String> ACTIVITY = new HashSet(Arrays.asList("acty", "bhvr", "dora", "evnt", "gora", "inbe", "mcha", "ocac", "socb"));
	public static final HashSet<String> ANATOMY = new HashSet(Arrays.asList("anst", "bdsu", "bdsy", "blor", "bpoc", "bsoj", "cell", "celc", "emst", "ffas", "tisu"));
	public static final HashSet<String> CHEMICALS = new HashSet(Arrays.asList("aapp","antb", "bacs", "bodm", "carb", "chem", "chvf", "chvs", "clnd", "eico", "elii", "gngm", "hops", "horm", "imft", "irda", "inch", "lipd", "nsba", "nnon", "opco", "orch", "phsu", "rcpt", "strd", "vita"));
	public static final HashSet<String> CONCEPTS = new HashSet(Arrays.asList("clas", "cnce", "ftcn", "grpa", "idcn", "inpr", "lang", "qnco", "qlco", "rnlw", "spco", "tmco"));
	public static final HashSet<String> DEVICES = new HashSet(Arrays.asList("medd", "resd"));
	public static final HashSet<String> DISORDER = new HashSet(Arrays.asList("acab", "anab", "cgab", "comd", "dsyn", "emod", "fndg", "inpo","mobd", "neop", "patf", "sosy"));
	public static final HashSet<String> GENES = new HashSet(Arrays.asList("aapp", "crbs", "gngm", "mosq", "nusq"));
	public static final HashSet<String> GEOGRAPHICS = new HashSet(Arrays.asList("geoa"));
	public static final HashSet<String> LIVINGBEING = new HashSet(Arrays.asList("aggp", "alga", "amph", "anim", "arch", "bact", "bird", "famg", "fish", "fngs", "grup", "humn", "invt", "mamm", "orgm", "podg", "plnt", "popg", "rept", "rich", "vtbt", "virs"));
	public static final HashSet<String> PHYSIOLOGY = new HashSet(Arrays.asList("celf", "clna", "genf", "menp", "moft", "ortf", "orga", "orgf", "phsf"));
	public static final HashSet<String> PHENOMENA = new HashSet(Arrays.asList("biof", "eehu", "hcpp", "lbtr", "npop", "phpr"));
	public static final HashSet<String> PROCEDURE = new HashSet(Arrays.asList("diap", "edac", "hlca", "lbpr", "mbrt", "resa", "topp"));


	// Dongwook, 04/08/2008
	// Added new Dominant_predicate list for Internaction and Pharmacogenomics saliency
	public static final String[] INTERPHARMA_DEMOTED_PREDICATES = {Constants.TREATS, Constants.ISA, Constants.PROCESS_OF};
	public static final List INTERPHARMA_DEMOTED_PREDICATE_LIST = Arrays.asList(INTERPHARMA_DEMOTED_PREDICATES);

	public static final String ADMINISTERED_TO = "ADMINISTERED_TO";
	public static final String AFFECTS = "AFFECTS";
	public static final String ASSOCIATED_WITH = "ASSOCIATED_WITH";
	public static final String AUGMENTS = "AUGMENTS";
	public static final String CAUSES = "CAUSES";
	public static final String COEXISTS_WITH = "COEXISTS_WITH";
	public static final String CONVERTS_TO = "CONVERTS_TO";
	public static final String COMPLICATES = "COMPLICATES";
	public static final String DIAGNOSES = "DIAGNOSES";
	public static final String DISRUPTS = "DISRUPTS";
	public static final String INHIBITS = "INHIBITS";
	public static final String INTERACTS_WITH = "INTERACTS_WITH";
	public static final String ISA = "ISA";
	public static final String LOCATION_OF = "LOCATION_OF";
	public static final String MANIFESTATION_OF = "MANIFESTATION_OF";
	public static final String PART_OF = "PART_OF";
	public static final String PREDISPOSES = "PREDISPOSES";
	public static final String PREVENTS = "PREVENTS";
	public static final String PROCESS_OF = "PROCESS_OF";
	public static final String PRODUCES = "PRODUCES";
	public static final String STIMULATES = "STIMULATES";
	public static final String TREATS = "TREATS";
	public static final String COMPARE = "COMPARE";

	public static final Hashtable PREDICATETYPE = createPREDICATETYPE();

	private static Hashtable createPREDICATETYPE() {
		Hashtable predTable = new Hashtable();
		predTable.put(AFFECTS, AFFECTS);
		predTable.put(AUGMENTS, AFFECTS);
		predTable.put(DISRUPTS, AFFECTS);
		predTable.put(ASSOCIATED_WITH, ASSOCIATED_WITH);
		predTable.put(CAUSES, ASSOCIATED_WITH);
		predTable.put(PREDISPOSES, ASSOCIATED_WITH);
		predTable.put(AUGMENTS, ASSOCIATED_WITH);
		predTable.put(INTERACTS_WITH, INTERACTS_WITH);
		predTable.put(INHIBITS, INTERACTS_WITH);
		predTable.put(STIMULATES, INTERACTS_WITH);
		predTable.put(LOCATION_OF, LOCATION_OF);
		predTable.put(PART_OF, LOCATION_OF);
		predTable.put(PRODUCES, LOCATION_OF);
		predTable.put(COEXISTS_WITH, COEXISTS_WITH);
		predTable.put(TREATS, TREATS);
		predTable.put(PREVENTS, TREATS);
		predTable.put(COMPARE, COMPARE);

		return predTable;
	}

	public static String getPredicateType(String input) {
		String type = (String) PREDICATETYPE.get(input);
		if(type != null)
			return new String(type);
		else return input;
	}

}
