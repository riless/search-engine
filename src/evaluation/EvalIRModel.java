package evaluation;
import indexation.Index;
import indexation.Stemmer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import diversite.ClusteringModel;

import modeles.IRmodel;
import modeles.LanguageModel;
import modeles.Vectoriel;
import modeles.Weighter;
import modeles.WeighterLogTfIdf;
import modeles.WeighterLogTfLogTf;
import modeles.WeighterTfIdf;
import modeles.WeighterTfInd;
import modeles.WeighterTfOne;
import modeles.WeighterTfTf;

public class EvalIRModel {

	public static Double mean(ArrayList<Double> scores){
		Double m = 0.;
		for (Double score: scores){
			m += score;
		}
		return 1.0 * m / scores.size();
	}
	
	static double variance(Double mean, ArrayList<Double> scores)
    {
        double temp = 0;
        for(double a :scores)
            temp += (mean-a)*(mean-a);
        return 1.0 * temp/scores.size();
    }
	
	 static double std(Double mean, ArrayList<Double> scores) {
	        return Math.sqrt(variance(mean, scores));
	 }
	 
	public static void main(String[] args) throws IOException, ClassNotFoundException {
//		String queryFile = "cisi/cisi.qry";

		String pathSource = "BENCHMARKS/easy235/";
		String fileSource = "easy235_text.txt";
		String queryFile = pathSource + "easy235_query.txt";
		String gtFile = pathSource + "easy235_gt_clean.txt";
		String gtSplit = " ";
		
//		String pathSource = "BENCHMARKS/easyCLEF08/";
//		String fileSource = "easyCLEF08_text.txt";
//		String queryFile = pathSource + "easyCLEF08_query.txt";
//		String gtFile = pathSource + "easyCLEF08_gt_clean.txt";
//		String gtSplit = " ";

		Index index = new Index(pathSource, fileSource);

		// mesures //
		Integer nbLevels = 20;
//		MesureAP mesureAP = new MesureAP();
//		MesurePR mesurePR = new MesurePR(nbLevels);
		PatN patn = new PatN(nbLevels);
		CRatN cratn = new CRatN(nbLevels);
		
		ArrayList<EvalMesure> mesures = new ArrayList<EvalMesure>();
//		mesures.add(mesureAP);
//		mesures.add(mesurePR);
		mesures.add(patn);
		mesures.add(cratn);
		
		ArrayList<Weighter> weighters = new ArrayList<Weighter>();
//		weighters.add(new WeighterTfInd(index));
		weighters.add(new WeighterTfTf(index));
//		weighters.add(new WeighterTfOne(index));
//		weighters.add(new WeighterTfIdf(index));
//		weighters.add(new WeighterLogTfIdf(index));
//		weighters.add(new WeighterLogTfLogTf(index));
		
		// modèles de recherches
		ArrayList<IRmodel> models = new ArrayList<IRmodel>();
		
		LanguageModel languageModel = new LanguageModel(weighters.get(0), index);
		languageModel.setName("Modèle de langue");
		models.add( languageModel );
		
//		for (int i = 0; i < weighters.size(); i++){
//			IRmodel model = new Vectoriel(weighters.get(i),index, true);
//			model.setName("Vectoriel + weighter " + weighters.get(i).getName() );
//			models.add( model );
//		}
		
	
		// Stemmer
		Stemmer stemmer = new Stemmer();
		
		// requêtes
		QueryParserCISI_CACM queryParser = new QueryParserCISI_CACM();
		Query query;
		
		System.out.println("Start evaluation");
		
		
		
		Double alpha = 0.8; // param alpha ( slide 45 )
		Integer nbDocs = index.getDocs().size();
		
for (int k=5; k<=5; k++){
		for (IRmodel model: models){ // Utiliser diffrent models de ranking
			
			ArrayList<Double> scores = new ArrayList<Double>();
			ArrayList<Double> diversityScores = new ArrayList<Double>();
			
			// Représentaion vectoriel de tout les documents
			HashMap<String, HashMap<String, Double>> docsRepr = new HashMap<String, HashMap<String, Double>>();
			for ( String docId: index.getDocs()){
				docsRepr.put( docId, weighters.get(0).getDocWeightsForDoc(docId) );
			}
			
			// System.out.println(docsRepr);
			
			
			// Utililliser la représentation des documents du premier weighter 
			
			for ( EvalMesure mesure: mesures){ // Utiliser diffrentes mesures d'évaluation du ranking.
				Integer nbQuery = 0;
				queryParser.init(queryFile, gtFile, gtSplit); 
				while( (query = queryParser.nextQuery()) != null ){ // sur toutes les requêtes
					nbQuery++;

					/***************************************/
					/********** RANKING SANS DIVERSITE ********/
					/***************************************/

					// Représenation vectoriel de query
					HashMap<String, Integer> query_repr = stemmer.getTextRepresentation(query.text);
//					System.out.println(query_repr);
//					HashMap<String, Double> query_scores = model.getScores(query_repr);
//					System.out.println(query_scores);
					List<Entry<String, Double>> rankings = model.getRanking(query_repr);
					System.out.println(rankings);
					/***************************************/
					/****EVALUATION DU MODEL SANS DIVERSITE****/
					/***************************************/
					
					// Créer un objet IRList avec la requetes et ses scores ( pour chaque document ) 
					IRList l = new IRList(query, rankings);
					Double s = mesure.eval(l);
					System.out.println(s);
					scores.add( s );
					
					/***************************************/
					/*********RANKING AVEC DIVERSITE **********/
					/***************************************/

					// Initialiser l'algo de clustering avec k et alpha et nbDocs( le nombe de clusters)
					ClusteringModel clustering_model = new ClusteringModel(k, rankings);
					clustering_model.setAlpha(alpha);
					clustering_model.setNbDocs(nbDocs);
					clustering_model.setDataset(docsRepr);

					// Calcul du ranking diversifié avec le clustering et le ranking initial
					// les clusters sont calculé en utlisant la représetation des documents.( vectoriel )
					List<Entry<String, Double>> disverted_ranking = clustering_model.train();
//					System.out.println(nbDocs);
//					System.out.println(disverted_ranking.size());
					// System.out.println(disverted_ranking);

					/***************************************/
					/**********EVALUATION DU MODEL* **********/
					/***************************************/

					IRList ld = new IRList(query, disverted_ranking);
					Double sd = mesure.eval(ld);
					diversityScores.add( sd );

				}
				
				/***************************************/
				/*****CALCUL DE LA MOYENNE DES SCORES *****/
				/***************************************/
				
				Double m = mean(scores);
//				Double st = std(m, scores);
				System.out.printf("SANS DIVERSITE: Model: %s, Mesure: %s: (%d Queries, %d Documents), k = %d, mean: %.2f\n" , model.getName() ,  mesure.getName() , nbQuery, nbDocs,k, m);
				
				Double md = mean(diversityScores);
//				Double std = std(md, diversityScores);
				System.out.printf("AVEC DIVERSITE. Model: %s, Mesure: %s:  (%d Queries, %d Documents), k = %d, mean: %.2f\n" , model.getName() ,  mesure.getName() ,  nbQuery, nbDocs,k, md);
			}
		}
}
		
		System.out.println("End.");

	}

}
