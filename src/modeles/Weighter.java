package modeles;
import java.io.IOException;
import java.util.*;

import indexation.Index;

public abstract class Weighter {
	
	protected Index index;
	private HashMap<String, Double> sumWeightsForStems;
	private Double sumWeightsInCorpus;
	private HashMap<String, Double> sumWeightsForDocs;

	String weighterName;
	
	public String getName(){
		return this.weighterName;
	}
	public Weighter(Index index){
		this.index = index;
		this.sumWeightsInCorpus = null;
		this.sumWeightsForStems = new HashMap<String, Double>();
		this.sumWeightsForDocs = new HashMap<String, Double>();
	}
	
	
		
	// retourne les poids des termes du documents idDoc
	
	public abstract HashMap<String, Double> getDocWeightsForDoc(String idDoc) throws IOException;
	
	// retourne les points du terme @stem das tous les docus qui le contiennent
	public abstract HashMap<String, Double> getDocWeightsForStem(String stem) throws IOException;
	
	// les poids des termes dont les tf sont passé en paramètres
	public abstract HashMap<String, Double> getWeightsForQuery(HashMap<String, Integer> query) throws IOException;
	
	public double getSumWeightsForStemInCorpus(String stem) throws IOException  {
		if (!this.sumWeightsForStems.containsKey(stem))
		{
			double sum = .0;
			HashMap<String, Double> docWeightsForStem = this.getDocWeightsForStem(stem);
			if (docWeightsForStem == null) {
				this.sumWeightsForStems.put(stem, null);
			}
			else {
				for (HashMap.Entry<String, Double> entry : docWeightsForStem.entrySet()){
					sum += entry.getValue();
				}
				this.sumWeightsForStems.put(stem, sum);
			}
		}
		return this.sumWeightsForStems.get(stem);
	}
	
	public double getSumWeightsInCorpus() throws IOException  {
		if (this.sumWeightsInCorpus == null){
			this.sumWeightsInCorpus = 0.;
			for (String docId : this.index.getDocs()){
				for (HashMap.Entry<String, Double> entry : this.getDocWeightsForDoc(docId).entrySet())
					this.sumWeightsInCorpus += entry.getValue();
			}
		}
		return this.sumWeightsInCorpus;
	}
	
	public double getSumWeightsForDocInCorpus(String docId) throws IOException  {
		if (!this.sumWeightsForDocs.containsKey(docId))
		{
			double sum = .0;
			for (HashMap.Entry<String, Double> entry : this.getDocWeightsForDoc(docId).entrySet())
				sum += entry.getValue();
			this.sumWeightsForDocs.put(docId, sum);
		}
		return this.sumWeightsForDocs.get(docId);
	}

	
}
