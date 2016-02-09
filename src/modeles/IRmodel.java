package modeles;

import indexation.Index;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public abstract class IRmodel {
	protected Index index;
	String modelName = "Default Model";
	
	public void setName(String model_name){
		this.modelName = model_name;
	}
	
	public String getName(){
		return this.modelName;
	}

	// retourne les scores des documents pour une requete passé en paramètre
	public abstract HashMap<String,Double> getScores(HashMap<String, Integer> query) throws IOException;
	
	// liste de couples (document-score) ordonnée par score decroissant
	public List<Entry<String, Double>> getRanking(HashMap<String, Integer> query) throws IOException {
		
		HashMap<String, Double> scores = getScores(query);
		Set<Entry<String, Double>> set = scores.entrySet();
	    List<Entry<String, Double>> list = new ArrayList<Entry<String, Double>>(set);
	    Collections.sort( list, new Comparator<Map.Entry<String, Double>>()
	    {
	        public int compare( Map.Entry<String, Double> o1, Map.Entry<String, Double> o2 )
	        {
	            // return (o1.getValue()).compareTo( o2.getValue() );//Ascending order
	            return (o2.getValue()).compareTo( o1.getValue() );//Descending order
	        }
	    } );
//	    for(Map.Entry<String, Integer> entry:list){
//	        System.out.println(entry.getKey()+" ==== "+entry.getValue());
//	    }
	    return list; 
	}
	/*La liste contient tous les documents de la collection ...*/
	
}
