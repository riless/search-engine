package features;

import indexation.Index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import modeles.Okapi;
import modeles.Weighter;
import modeles.Weighter2;
import evaluations.EvalIRModel;
import evaluations.EvalMeasure;
import evaluations.EvalPrecMoyenne;
import evaluations.IRList;
import evaluations.Query;

public class ThreadOptimize extends Thread
{
	private List<String> docId;
    private Index idx;
    private List<Query> trainQuery;
    private FeaturerModel fm;

    public ThreadOptimize(String name, List<String> docId, Index idx, List<Query> trainQuery,FeaturerModel fm)
    {
        super(name);
        this.docId=docId;
        this.idx = idx;
        this.fm=fm;
        this.trainQuery = trainQuery;
    }

    public void run()
    {
    	
		for(String doc:this.docId){
			for(Query q:this.trainQuery){
				try {
					fm.getFeatures(doc, q.getId());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
    }
    
} 
