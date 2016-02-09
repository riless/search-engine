package modeles;

import indexation.Index;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

public class Vectoriel extends IRmodel {
	
	private boolean normalized = true;
	private Weighter weighter;
	private Index index;
	
	public Vectoriel(Weighter weighter, Index index, boolean normalized){
		this.weighter = weighter;
		this.normalized = normalized;
		this.index = index;
	}

	public HashMap<String,Double> getScores(HashMap<String, Integer> query) throws IOException{
		
		HashMap<String, Double> normDocs = new HashMap<String, Double>();
		Double norm = 0.; 

		if ( this.normalized ){ // score cosinus ( produit scalaire entre vecteur de norme 1 ) entre la représentation des documents et celle de la requete
			// <!> calculer les normes des vecteurs des documents une bonne fois pour toute
				for (Entry<String, Double> docWeights: this.weighter.getWeightsForQuery(query).entrySet()){
					norm += docWeights.getValue() * docWeights.getValue();
				}
				norm = Math.sqrt(norm);
		}
				System.out.println("norm: " + String.valueOf(normDocs));
		
		HashMap<String,Double>  scores  = new HashMap<String,Double> ();
		HashMap<String, Double> queryWeights = this.weighter.getWeightsForQuery(query);
		
		// produit scalaire entre les poids des documents et ceux de la requete
		for (String docId: this.index.getDocs() ){
			
			Double w  = 0.;
			
			for ( Entry<String, Double> d: this.weighter.getDocWeightsForDoc(docId).entrySet() ){
				for (Entry<String, Double> q: queryWeights.entrySet()){

					if ( q.getKey().equals( d.getKey()) ){
						w += q.getValue() * d.getValue();
					}
				}
			}
			if ( this.normalized ){
				w /= norm; // if normalized then this value exists
			}
			scores.put(docId, w);
		}
			 
		return scores;
	}
	

	
}
