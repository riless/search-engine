package evaluation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class IRList {
	
	protected Query query;
	protected HashMap<String, Double> relevants; // doc_id relevance
	private List<Entry<String, Double>> ranking;
	
	public IRList(Query query, HashMap<String, Double> relevants){
		this.query = query; 
		this.relevants = relevants;
	}
	
	public IRList(Query query, List<Entry<String, Double>> ranking) {
		this.query = query; 
		this.ranking = ranking;
	}

	public Query getQuery(){
		return this.query;
	}
	
	public ArrayList<String> getDocs(){
		return new ArrayList<String>( this.relevants.keySet());
	}
	
	public HashMap<String, Double> getRelevants(){
		return this.relevants;
	}
	
	public Double getRelevance(String docId){
		return this.relevants.get(docId);
	}
	
	public String getIdSubtopic(String docId){
		return this.query.getSubTopicId(docId);
	}
	
	public int getNbSubtopics(){
		return this.query.getNbSubtopics();
	}

	public Integer size() {
		return this.relevants.size();
	}
	
	public List<Entry<String, Double>> getRanking(){
		return this.ranking;
	}
	

}
