package evaluation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

public class MesurePR extends EvalMesure {

	private Integer nbLevels;

	public MesurePR(Integer nbLevels){
		super.mesureName = "MesurePR";
		this.nbLevels = nbLevels;
	}
	
	@Override
	public Double eval(IRList l) {
		

		Double mesure = 0.;
		// Double precision = 0.;
		Integer doc_i = 0;
		Integer pertinants_i = 0;
		Integer pertinants_t = l.getRelevants().size();
		Double rappel_i = 0.;
		Double precision_i = 0.;
				
		for (Entry<String, Double> doc: l.getRelevants().entrySet() ) { // for each doc
			
			doc_i++;
			
			if (  l.getRelevance(doc.getKey()) > 0 ){ // si doc est pertinent
				pertinants_i++;
			}
			
			rappel_i = (double) pertinants_i / pertinants_t;
			precision_i = (double) pertinants_i / doc_i;
			
			List<Double> k = new ArrayList<Double>();
			for ( int i = 1; i <= this.nbLevels; i++){
				k.add( 1.0 * i / this.nbLevels );
			}
			
			for (Double k_i:k){
				if (rappel_i >= k_i){
					if ( precision_i > mesure ){
						mesure = precision_i;
					}
				}
			}
			
//			if ( doc_i >= 20){
//				break; 
//			}
			
		}

		return mesure;
		
		
	}

}
