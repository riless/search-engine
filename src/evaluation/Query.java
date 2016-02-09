package evaluation;

import java.util.HashMap;

public class Query {
	public String id;
	public String text;
	public HashMap<String,Double> relevants; // DocId, indice de pertinance
	public HashMap<String,String> subtopics; // DocId, subTopics

	public Query(String id, String text, HashMap<String, Double> relevants, HashMap<String, String> subtopics){
		this(id, text, relevants);
		this.subtopics = subtopics;
	}
	
	public boolean isRelevant(String id_doc){
		return this.relevants.containsKey(id_doc);
	}
	
	public Integer getNbSubtopics(){
		return this.subtopics.size();
	}
	
	public String getIdSubtopics(String docId){
		return this.subtopics.get(docId);
		
	}
	
	public Query(String id, String text, HashMap<String, Double> relevants){
		this.id = id; 
		this.text = text;
		this.relevants = relevants;
	}
	
	public String getId(){
		return this.id;
	}
	
	public String getText(){
		return this.text;
	}
	
	public HashMap<String, Double> getRelevants(){
		return this.relevants;
	}

	public String getSubTopicId(String docId) {
		return this.subtopics.get(docId);
	}
	

}
