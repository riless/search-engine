package evaluation;

import java.util.Map.Entry;

public class MesureAP extends EvalMesure {

	public MesureAP(){
		super.mesureName = "MesureAP";
	}
	
	@Override
	public Double eval(IRList l) {
		
		Double mesure = 0.;
		

		Integer nbRel = 0; // nombre de docs pertinants
		Integer nbProcessedDocs = 0; // nombre de docs
//		System.out.println(l.getRelevants());
		for (Entry<String, Double> doc: l.getRelevants().entrySet() ) { // for each doc
			// System.out.println( doc.getKey()+" | "+l.getRelevants() );
			nbProcessedDocs++;
			
			if (l.getRelevance(doc.getKey()) > 0){
				nbRel++;
//				System.out.println( "nbRel++" );
				mesure += (float) nbRel / nbProcessedDocs;
			}
//			if ( nbProcessedDocs >= 20){
//				break; 
//			}
		}
		
		// normaliser
		if (nbRel>0){
			mesure = 1.0 * mesure / nbRel;
		}
		
		return mesure;
		
	}

}
