package evaluation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

public class CRatN extends EvalMesure {

	protected Integer n;
	
	public CRatN(Integer n) {
		super.mesureName = "CR@" + String.valueOf(n);
		this.n = n;
	}

	@Override
	public Double eval(IRList l) {

			Set<String> clusters = new HashSet<String>();
			List<Entry<String, Double>> ranking = l.getRanking();
			
			for (int i = 0; i < this.n && i < l.getRanking().size(); i++) {
				Entry<String, Double> doc =  ranking.get(i);
				
				if ( l.getQuery().isRelevant(doc.getKey()) ) { 
					clusters.add( l.getQuery().getSubTopicId( doc.getKey() ) );
				}
				
			}
			return 1.0 * clusters.size() / l.getNbSubtopics() ;
			
		}
		

	

}
