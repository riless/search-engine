package features;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import evaluations.Query;
import indexation.Index;

public class FeaturerQuery extends Featurer{

	public List<Query> lq;
	public int longMin=0;
	public int longMax=0;

	protected  HashMap<String,HashMap<String,Double>> featuresQuery = new HashMap<String,HashMap<String,Double>>() ;

	public FeaturerQuery(Index idx,List<Query> lq) throws FileNotFoundException, IOException, ClassNotFoundException {
		super(idx);
		File fichier =  new File("cacm_featuresQuery.ser") ;
		if(!fichier.exists()){
			ObjectOutputStream oos =  new ObjectOutputStream(new FileOutputStream(fichier)) ;
			oos.writeObject(featuresQuery);
		}
		else{
			ObjectInputStream ois =  new ObjectInputStream(new FileInputStream(fichier));
			HashMap<String,HashMap<String,Double>> tmp=(HashMap<String,HashMap<String,Double>>)ois.readObject();
			this.featuresQuery=tmp;
		}
		this.lq=lq;
		longMin=(int) (this.getLongQuery(this.lq.get(0).getId()));
		for(Query qi:this.lq){
			int tmpLong= (int) (this.getLongQuery(qi.getId()));
			if(tmpLong<longMin){
				longMin=tmpLong;
			}
			if(tmpLong>longMax){
				longMax=tmpLong;
			}
		}
	}

	@Override
	public List<Double> getFeatures(String docId, String query) throws IOException {
		if(!this.featuresQuery.containsKey(query)){
			HashMap <String,Double> f=new HashMap <String,Double>();
			f.put("longQuery",this.getLongQueryNormalized(query));
			f.put("sommeIdf",this.getSommeIdfQuery(query));
			this.featuresQuery.put(query,f);
			//mise a jour du fichier ser
			File fichier =  new File("cacm_featuresQuery.ser") ;
			ObjectOutputStream oos =  new ObjectOutputStream(new FileOutputStream(fichier)) ;
			oos.writeObject(featuresQuery);
		}
		List<Double> res= new ArrayList<Double>();
		for(int j=0;j<featuresQuery.get(query).values().toArray().length;j++){
			res.add((Double) featuresQuery.get(query).values().toArray()[j]);
		}
		return res; 
	}

	public double getLongQueryNormalized(String docId) throws IOException{
		return (this.getLongQuery(docId)-longMin)/(longMax-longMin);
	}

	public double getLongQuery(String query) throws IOException{
		return idx.stemmQuery(getTextQueryFromText(query)).size();
	}


	public double getSommeIdfQuery(String query) throws IOException{
		double s=0;
		HashMap<String, Integer>q=idx.stemmQuery(getTextQueryFromText(query));
		for(String t: q.keySet()){
			s+=idf(t);
		}
		return s;

	}
	
	public String getTextQueryFromText(String queryId){
		String text="";
		for(Query q:this.lq){
			if(q.getId().equals(queryId)){
				text=q.getText();
			}
		}
		return text;
	}

	public double df(String terme) throws IOException{
		int cpt=0;
		for(String docid: idx.getDocsId()){
			if( idx.getTfsForDoc(docid).containsKey(terme))
				cpt++;
		}
		return cpt;
	}

	public double idf(String terme) throws IOException{
		int N=idx.getDocsId().size();
		return Math.log((1.0*(1+N))/(1+this.df(terme)));
	}

	public HashMap<String,HashMap<String,Double>> getFeaturesQuery(){
		return this.featuresQuery;
	}


}
