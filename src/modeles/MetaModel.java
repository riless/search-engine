package modeles;

import java.io.IOException;
import java.util.HashMap;
import java.util.TreeMap;

import features.FeaturersList;
import indexation.Index;

public abstract class MetaModel extends IRModel {
	
	private FeaturersList featL;
	private Index ind;
	
	public MetaModel(Index ind, FeaturersList featL){
		this.featL=featL;
		this.ind=ind;
	}
	
	public FeaturersList getFeaturersList(){
		return this.featL;
	}
	
	public Index getIndex(){
		return this.ind;
	}

}
