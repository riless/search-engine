package modeles;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;



public class Okapi extends IRmodel {
	
	protected Double b;
	protected Double k1;
	public Okapi(){
		this.b = 0.75; // entre 0 et 1
		this.k1 = 1.5; // entre 1 et 2
	}
	
	public void train( HashMap<String, Integer> Qtrain, HashMap<String, Integer> Qtest){
		this.b = 0.;
		this.k1 = 0.;
	}
	
	
	@Override
	public HashMap<String, Double> getScores(HashMap<String, Integer> query)
			throws IOException {
		
		HashMap<String,Double> docsScores = new HashMap<String, Double>();
		for (String d: this.index.getDocs()){
		
//			
			Integer L_d = this.index.getDocLength(d); // longueur de d
			double L_m = L_d / this.index.getDocs().size();
//			Integer L_c = this.index.getCorpusLength(); // longueur du corpus
//			
			Double r = 0.;
			
			for (Entry<String, Integer> q: query.entrySet()){
				String t = q.getKey();
//				Integer tf_tq = q.getValue();
				Integer df_t = this.index.getDF(t, d); // get doc frequency for t
				Integer N = this.index.getDocs().size(); // nombre de documents
				double idf_tq = Math.max(0, Math.log( (N-df_t+0.5)/(df_t+0.5) ) );
				HashMap<String, Integer> tfs_t = this.index.getTfsForStem(t); // docId1: w1; docId2: w2
//				
//				Integer tf_tc = this.index.getTfForStemInCorpus(t); // tf de t dans le corpus
				Integer tf_td = tfs_t.get(d); // tf de t dans d
//				
//				double PMd_t = (double) tf_td / L_d;
//				double PMc_t = (double) tf_tc / L_c;
				r = idf_tq * ( ((this.k1+1)*tf_td)/(this.k1 * ((1-this.b)+this.b*L_d/L_m) + tf_td) );
			}
			 docsScores.put(d,r);
		}
		return docsScores;
	}

}
