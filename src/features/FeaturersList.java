package features;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import indexation.Index;

public class FeaturersList extends Featurer {
	
	private List<Featurer> featurers=new ArrayList<Featurer>();
	private int nbFeatures=7;

	
	public FeaturersList(Index index) {
		super(index);
	}

	@Override
	public List<Double> getFeatures(String docId, String query) throws IOException {
		List<Double> res=new ArrayList<Double>();
		for(Featurer f:this.featurers){
			res.addAll(f.getFeatures(docId, query));
		}
		return res;
	}
	
	public void addFeaturer(Featurer f){
		this.featurers.add(f);
	}
	
	public List<Featurer> getfeaturers(){
		return this.featurers;
	}
	
	public int getnbFeatures(){
		return this.nbFeatures;
	}

}
