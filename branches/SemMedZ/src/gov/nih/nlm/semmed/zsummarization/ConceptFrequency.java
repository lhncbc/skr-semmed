package gov.nih.nlm.semmed.zsummarization;

public class ConceptFrequency {
	public String Concept;
	public int STId;
	public int Freq;

	public ConceptFrequency(String concept, int stid, int freq) {
		Concept = concept;
		STId = stid;
		Freq = freq;
	}
}
