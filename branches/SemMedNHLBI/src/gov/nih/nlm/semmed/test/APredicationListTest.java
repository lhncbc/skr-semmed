package gov.nih.nlm.semmed.test;

import gov.nih.nlm.semmed.model.APredicationList;
import gov.nih.nlm.semmed.util.ArticleDataSource;
import junit.framework.TestCase;

public class APredicationListTest extends TestCase {

	private String[] subjects = new String[]{"Breast cancer metastatic",
			"THPO",
			"Metalloproteases",
			"TIMP1 protein, human",
			"Control Groups",
			"TIMP1 protein, human",
			"TIMP1 protein, human",
			"Serum",
			"TIMP1 protein, human",
			"TIMP1 protein, human"};
	
	
	private String[] objects = new String[]{"Woman",
			"Patients",
			"Breast cancer metastatic",
			"Patients",
			"Ethylenediamine",
			"Woman",
			"Control Groups",
			"Plasma",
			"Woman",
			"Breast cancer metastatic"};
	
	private String[] predicate = new String[]{"PROCESS_OF",
			"PART_OF",
			"DISRUPTS",
			"PART_OF",
			"LOCATION_OF",
			"ADMINISTERED_TO",
			"PART_OF",
			"PART_OF",
			"PART_OF",
			"ASSOCIATED_WITH"};
			
	private boolean[] s_novel = new boolean[]{true,true,true,true,true,true,true,true,true,true};
	
	private boolean[] o_novel = new boolean[]{true,false,true,false,true,true,true,true,true,true};
	
	public void testAPredicationList() {
		
		APredicationList ap = null;
		
		try{
			ap = new APredicationList(new int[]{17407159},ArticleDataSource.SourceType.MEDLINE);
		}catch(Exception e){
			fail(e.toString());
		}
		assertNotNull(ap);
		
		for(int i=0;i<ap.size();i++){
			System.out.println(i);
			assertEquals(17407159,ap.get(i).PMID);
			assertEquals(subjects[i],ap.get(i).subject.get(0));
			assertEquals(objects[i],ap.get(i).object.get(0));
			assertEquals(predicate[i],ap.get(i).predicate);
			assertEquals(s_novel[i],ap.get(i).novelSubject);
			assertEquals(o_novel[i],ap.get(i).novelObject);			
		}		
	}

	

}
