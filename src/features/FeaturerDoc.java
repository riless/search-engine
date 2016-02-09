/* ===============================================
	CLASSE DE FEATURER CONTENANT LES FEATURES RELATIVES AUX DOCUMENTS
================================================== */

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
import java.util.TreeMap;
import java.util.Map.Entry;

import indexation.Index;
import modeles.PageRank;

public class FeaturerDoc extends Featurer {
	
	public int longMin=0;
	public int longMax=0;
	public int nbTermeMin=0;
	public int nbTermeMax=0;
	
	protected  HashMap<String,HashMap<String,Double>> featuresDoc =  new HashMap<String,HashMap<String,Double>>() ;
	//longueur du document, nombre de termes différents dans le document, importance du document dans le graphe des hyperliens (score PageRank)
	

	public FeaturerDoc(Index idx) throws FileNotFoundException, IOException, ClassNotFoundException {
		super(idx);
		File fichier =  new File("cacm_featuresDoc.ser") ;
		if(!fichier.exists()){
			ObjectOutputStream oos =  new ObjectOutputStream(new FileOutputStream(fichier)) ;
			oos.writeObject(featuresDoc);
		}
		else{
			ObjectInputStream ois =  new ObjectInputStream(new FileInputStream(fichier));
			HashMap<String,HashMap<String,Double>> tmp=(HashMap<String,HashMap<String,Double>>)ois.readObject();
			this.featuresDoc=tmp;
		}
		nbTermeMin=(int) (this.getNbtermeDoc(idx.getDocsId().get(0)));
		longMin=(int) (this.getLongDoc(idx.getDocsId().get(0)));
		
		for(String doc:idx.getDocsId()){
			int tmpLong= (int) (this.getLongDoc(doc));
			int tmpNbterme=(int) (this.getNbtermeDoc(doc));
			
			if(tmpLong<longMin){
				longMin=tmpLong;
			}
			if(tmpLong>longMax){
				longMax=tmpLong;
			}
			
			if(tmpNbterme<nbTermeMin){
				nbTermeMin=tmpNbterme;
			}
			if(tmpNbterme>nbTermeMax){
				nbTermeMax=tmpNbterme;
			}
		}
	}

	@Override
	public List<Double> getFeatures(String docId, String query) throws IOException {
		if(!this.featuresDoc.containsKey(docId)){
			HashMap <String,Double> f=new HashMap <String,Double>();
			f.put("longDoc",this.getLongDocNormalized(docId));
			f.put("nbTermeDiff",this.getNbtermeDocNormalized(docId));
			f.put("importancePR",this.getImportancePGDoc(docId));
			this.featuresDoc.put(docId,f);
			//Mise a jour du fichier ser
			File fichier =  new File("cacm_featuresDoc.ser") ;
			ObjectOutputStream oos =  new ObjectOutputStream(new FileOutputStream(fichier)) ;
			oos.writeObject(featuresDoc);
		}
		List<Double> res= new ArrayList<Double>();
		for(int j=0;j<featuresDoc.get(docId).values().toArray().length;j++){
			res.add((Double) featuresDoc.get(docId).values().toArray()[j]);
		}
		return res; 
	}
	
	public double getNbtermeDocNormalized(String docId) throws IOException{
		return (this.getNbtermeDoc(docId)-nbTermeMin)/(nbTermeMax-nbTermeMin);
	}
	
	public double getLongDocNormalized(String docId) throws IOException{
		return (this.getLongDoc(docId)-longMin)/(longMax-longMin);
	}
	
	
	public double getNbtermeDoc(String docId) throws IOException{
		return idx.getTfsForDoc(docId).size();
	}

	public double getLongDoc(String docId) throws IOException{
		double s=0;
		for(Entry<String, Double> e:idx.getTfsForDoc(docId).entrySet())
			s+=e.getValue();
		return s;
	}

	public double getImportancePGDoc(String docId){
		PageRank pg=new PageRank(idx.getDocsId(),idx.getDocPredec(),idx.getDocLinks(),20);
		TreeMap<String,Double> importance=pg.marcheAleatoire();
		if(!importance.containsKey(docId))
			return 0.0;
		return importance.get(docId);
	}
	
	public HashMap<String,HashMap<String,Double>> getFeaturesDoc(){
		return this.featuresDoc;
	}
}
