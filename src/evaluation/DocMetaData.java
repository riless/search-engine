package evaluation;

import java.util.HashMap;

public class DocMetaData {
	
	public HashMap<String, Double> relevants;
	public HashMap<String, String> subtopics;
	
	public DocMetaData(HashMap<String, Double> relevants, HashMap<String, String> subtopics){
		this.relevants = relevants;
		this.subtopics = subtopics;
	}
}
