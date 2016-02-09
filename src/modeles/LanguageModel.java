package modeles;

import indexation.Index;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

public class LanguageModel extends IRmodel {

	protected double lambda;
	private Index index;
	protected Weighter weighter;
	private  HashMap<String, Double> scores;
	
	public LanguageModel(Weighter weighter, Index index){
		this.index = index;
		this.weighter = weighter;
	}
	
	public void train( HashMap<String, Integer> Qtrain, HashMap<String, Integer> Qtest){
		this.lambda = 0.6;
	}
	
	public HashMap<String, Double> getScores(HashMap<String, Integer> query) throws IOException  {
	
		this.scores = new HashMap<String, Double>();
		HashMap<String, Double> wtq = weighter.getWeightsForQuery(query);
		
		for (String term : query.keySet()){
			double pct = this.weighter.getSumWeightsForStemInCorpus(term) / weighter.getSumWeightsInCorpus();
			HashMap<String, Double> docWeightsForStem = this.weighter.getDocWeightsForStem(term);
			for (String doc : index.getDocs()){
				double pdt = 0;
				if (docWeightsForStem.containsKey(doc)){
					pdt = docWeightsForStem.get(doc) / this.weighter.getSumWeightsForDocInCorpus(doc);
				}
				if (!scores.containsKey(doc)){
					scores.put(doc, 0.0);
				}
				double score = this.lambda * pdt + (1-this.lambda) * pct;
				if (score > 0){
					scores.put(doc, scores.get(doc) + wtq.get(term) * Math.log(score));
				}
			}
		}
		return scores;
	
	}
	
	public HashMap<String, Double> getScores2(HashMap<String, Integer> query)
			throws IOException {

		HashMap<String,Double> docsScores = new HashMap<String, Double>();
		Integer L_c = this.index.getCorpusLength(); // longueur du corpus

	
		for (String d: this.index.getDocs()){
			
			Integer L_d = this.index.getDocLength(d); // longueur de d

			Double r = 0.;
			
			for (Entry<String, Integer> q: query.entrySet()){
				
				String t = q.getKey();
				
				HashMap<String, Double> tfs_t = this.weighter.getDocWeightsForStem(t); // docId1: w1; docId2: w2

				Double tf_td = tfs_t.get(d); // tf de t dans d
				Integer tf_tc = this.index.getTfForStemInCorpus(t); // tf de t dans le corpus

				if ( tf_td == null ){ tf_td = 0.;}
				if ( tf_tc == null ){ tf_tc = 0;}
				
				Double PMd_t = 1. * tf_td / L_d;
				Double PMc_t = 1. * tf_tc / L_c;

				if (PMd_t>0){
					r += q.getValue() * Math.log(this.lambda * PMd_t + (1 - this.lambda) * PMc_t);
				}

			}
			docsScores.put(d,r);
		}
		// System.out.println( docsScores );
		return docsScores;
	}



}
