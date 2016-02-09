/* ===============================================
	CLASSE DE FEATURER CONTENANT LES FEATURES RELATIVES AUX MODELS
================================================== */


package features;

import indexation.Index;

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
import java.util.TreeMap;

import modeles.IRModel;
import modeles.LanguageModel;
import modeles.Okapi;
import modeles.Vectoriel;
import modeles.Weighter1;
import modeles.Weighter2;
import modeles.Weighter3;

public class FeaturerModel extends Featurer {

	private String model;
	private List<IRModel>models =new ArrayList <IRModel>();

	private  HashMap<HashMap<String,String>,HashMap<String,Double>> featuresModel=new HashMap<HashMap<String,String>,HashMap<String,Double>>() ;

	public FeaturerModel(Index idx) throws FileNotFoundException, IOException, ClassNotFoundException {
		super(idx);
		File fichier =  new File("cacm_featuresModel.ser") ;
		if(!fichier.exists()){
			ObjectOutputStream oos =  new ObjectOutputStream(new FileOutputStream(fichier)) ;
			oos.writeObject(featuresModel);
		}
		else{
			ObjectInputStream ois =  new ObjectInputStream(new FileInputStream(fichier));
			HashMap<HashMap<String, String>, HashMap<String, Double>> tmp=(HashMap<HashMap<String, String>, HashMap<String, Double>>)ois.readObject();
			this.featuresModel=tmp;
		}
		//models.add(new Vectoriel(new Weighter1(this.idx)));
		//models.add(new Vectoriel(new Weighter2(this.idx)));
		models.add(new Vectoriel(new Weighter3(this.idx)));
		//models.add(new Okapi(new Weighter1(this.idx),1.9,0.6));
		models.add(new LanguageModel(new Weighter1(this.idx),0.95));
	}
	
	


	@Override
	public List <Double> getFeatures(String docId, String query) throws IOException {

		HashMap<String,String> couple=new HashMap<String,String>();
		couple.put(docId,query);
		if(!this.featuresModel.containsKey(couple)){
			HashMap<String,Integer> queryStemm=this.idx.stemmQuery(query);
			int cpt=1;
			HashMap<String,Double> f=new HashMap<String,Double>();
			for(IRModel mod:models){
				Double s=mod.getRanking(queryStemm).get(docId);
				f.put(mod.getName()+" ",s);
				featuresModel.put(couple,f);
				cpt++;	
			}
			File fichier =  new File("cacm_featuresModel.ser") ;
			ObjectOutputStream oos =  new ObjectOutputStream(new FileOutputStream(fichier)) ;
			oos.writeObject(featuresModel);
		}
//		List<Double> res= new ArrayList<Double>();
//		for(int j=0;j<featuresModel.get(couple).values().toArray().length;j++){
//			res.add((Double) featuresModel.get(couple).values().toArray()[j]);
//		}
//		return res; 
		HashMap<String,Double> tmpFM=featuresModel.get(couple);
		double resVect=0.0;
		double resLangue=0.0;
		for(int j=0;j<tmpFM.values().toArray().length;j++){
			if((Double) tmpFM.values().toArray()[j]<0)
				resLangue=(double) tmpFM.values().toArray()[j];
			else
				resVect=(double) tmpFM.values().toArray()[j];
		}
		List<Double> res= new ArrayList<Double>();
		res.add(resVect);
		res.add(resLangue);
		return res;
	
	}

	public HashMap<HashMap<String, String>, HashMap<String, Double>> getFeaturesModel(){
		return this.featuresModel;
	}

}
