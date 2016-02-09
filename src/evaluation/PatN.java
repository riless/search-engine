package evaluation;

import java.util.List;
import java.util.Map.Entry;


public class PatN extends EvalMesure {
	public Integer  n;
	
	public PatN(Integer n){
		super.mesureName = "P@" + String.valueOf(n);
		this.n = n;
	}
	
	@Override
	public Double eval(IRList l) {

			double nbRel = 0;
			
//			System.out.println( "DOCS: " + docs );
//			System.out.println( "RELEVANTS: " + l.getRelevants() );
			
			List<Entry<String, Double>> ranking = l.getRanking();
//			System.out.println(ranking);
			for (int i = 0; i < this.n && i < l.getRanking().size(); i++) {
//				 System.out.print(i);
				Entry<String, Double> doc =  ranking.get(i);
				
//				System.out.print("QUERY_ID: " + l.query.id  + ", DOC_ID: "+ docId + "-->");
				if (l.getQuery().isRelevant( doc.getKey() )) {
//					System.out.print("--"+doc.getKey()+"-->REL");
					nbRel++;
				}
//				System.out.print("\n");
			}

			return nbRel/this.n;
	}
}
