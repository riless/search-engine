package modeles;

import indexation.Index;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import evaluations.IRList;


public abstract class RandomWalk {
	protected List<String> docs;
	protected Map<String, List<String>> predec;
	protected Map<String, List<String>> succ;

	public RandomWalk(List<String> docsId,Map<String, List<String>> predec,Map<String, List<String>> succ){
		this.docs=docsId;
		this.succ=succ;
		this.predec=predec;
	}


	public abstract TreeMap<String, Double> marcheAleatoire();

}
