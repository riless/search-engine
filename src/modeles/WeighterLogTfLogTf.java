package modeles;

import indexation.Index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;


public class WeighterLogTfLogTf extends Weighter {
	

	public WeighterLogTfLogTf(Index index) {
		super(index);
		super.weighterName = "WeighterLogTfLogTf";
	}

	/*  */
	@Override
	public HashMap<String, Double> getDocWeightsForDoc(String idDoc) throws IOException {
		HashMap<String, Integer> mapInteger = index.getTfsForDoc(idDoc);
		HashMap<String, Double> mapDouble = new HashMap<String, Double>();
		for (Entry<String, Integer> entry : mapInteger.entrySet()) {
		    mapDouble.put(entry.getKey(), new Double(entry.getValue()) );
		}
		return mapDouble;
	}

	@Override
	public HashMap<String, Double> getDocWeightsForStem(String stem) throws IOException {
		HashMap<String, Integer> mapInteger = index.getTfsForStem(stem);
		HashMap<String, Double> tf = new HashMap<String,Double>();
		for (HashMap.Entry<String, Integer> entry : mapInteger.entrySet()) {
			// 1 + log(tft,d) si t ∈ d, 0 sinon
			Integer idf = index.getTfsForStem(entry.getKey()).size();
			tf.put(entry.getKey(), (1 + Math.log(entry.getValue())) * idf);
		}
		return tf;
	}

	@Override
	public HashMap<String, Double> getWeightsForQuery(HashMap<String, Integer> query) throws IOException {
		HashMap<String, Double> tfidf = new HashMap<String,Double>();
		for (HashMap.Entry<String, Integer> entry : query.entrySet()) {
			// w_tq = 1 si t appartient à q, 0 sinon
			Integer idf = index.getTfsForStem(entry.getKey()).size();
			tfidf.put(entry.getKey(), (1 + Math.log(entry.getValue())) * idf);	
		}
		return tfidf;
	}

}
