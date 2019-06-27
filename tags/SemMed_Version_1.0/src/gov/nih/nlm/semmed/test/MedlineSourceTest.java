package gov.nih.nlm.semmed.test;

import gov.nih.nlm.semmed.util.MedlineSource;

import java.util.List;

import junit.framework.TestCase;

public class MedlineSourceTest extends TestCase {

	public void testFetchHttpSessionListOfInteger() throws Exception{
		List<Integer> ids = java.util.Arrays.asList(15591475,15613431,15671619,15705920,15716688,15736120,15756217,15977132,1618778,16208285);
	
		MedlineSource.getInstance().fetch(null,ids);
	}

}
