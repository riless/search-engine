package diversite;

import indexation.Index;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import java.util.AbstractMap.SimpleEntry;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.SparseInstance;
import net.sf.javaml.clustering.Clusterer;
import net.sf.javaml.clustering.KMeans;
public class ClusteringModel {
	

	public Integer k;
	public Double alpha;
	public Integer nbDocs;

	Dataset [] clusters;
	DefaultDataset dataset;
	Clusterer clusterer;
	private List<Entry<String, Double>> ranking;
	
	public ClusteringModel(Integer k, List<Entry<String, Double>> ranking){
		this.clusterer = new KMeans(k);
		this.dataset = new DefaultDataset();
		this.ranking =ranking;
	}
	
	
	public double[] toPrimitive(Double[] list){
		double[] out = new double[list.length];
	    for(int i = 0; i < list.length; i++){
	        out[i] = list[i].doubleValue();
	    }
	    return out;
	}
	



	public void setDataset(HashMap<String, HashMap<String, Double>> docsRepr) {

		 /*  Transformer la représentation des documents en Dataset utilisable par l'algo de Kmeans java-ml */
		for (Entry<String, HashMap<String, Double>> doc: docsRepr.entrySet()){
			double[] docVector= toPrimitive( doc.getValue().values().toArray(new Double[0]) );
			// System.out.println( doc.getKey());
			SparseInstance docInstance = new SparseInstance(docVector, doc.getKey());
			this.dataset.add(docInstance);
		}

	}

	public void setAlpha(Double alpha) {
		this.alpha = alpha;
	}

	public void setNbDocs(Integer nbDocs) {
		this.nbDocs = nbDocs;
	}

	private List<Entry<String, Double>> ClustersToRanking(Dataset[] clusters){
		
		
		HashMap<Integer, Integer> sortedDataset = new HashMap<Integer, Integer>();
		for (int i=0; i<clusters.length; i++) {
			sortedDataset.put( i,clusters[i].size() );
		}
		Set<Integer> sortedIndexes = MapUtil.sortByValue( sortedDataset ).keySet();
//		System.out.println(sortedIndexes);
//		System.out.println(sortedDataset);
		
		  List<Entry<String, Double>> ranking = new  ArrayList<Entry<String, Double>>();

		  Integer index = 0;
		  while (true){
			  boolean stop = true; 
			  for (Integer clusterIndex: sortedIndexes) {
				  // System.out.println(clusterIndex);
				  Dataset cluster = clusters[clusterIndex];
//				  System.out.println("Cluster size: " +  String.valueOf(cluster.size()) );
//				  System.out.println(index);
				  if ( index < cluster.size() ){
					    stop = false;
					  	String  docId = String.valueOf( cluster.get(index).classValue() );
						SimpleEntry<String, Double> doc = new SimpleEntry<String, Double>(docId, this.ranking.get(index).getValue());
						ranking.add(doc);
				  }
			  }
			  index++;
//			  System.out.println("\n");
			  if (stop){
				  break;
			  }
		}
		return ranking;
	}
	
	public List<Entry<String, Double>> train() {
		// lancer le clustering
		Dataset[] clusters = this.clusterer.cluster(this.dataset);
		return ClustersToRanking(clusters);

	}

}
