/* ===============================================
	CLASSE DE FEATURER GENERIQUE
================================================== */

package features;

import indexation.Index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import modeles.PageRank;

public abstract class Featurer {

	protected boolean init=false;

	protected Index idx;
	
	public Featurer(Index idx){
		this.idx=idx;
	}

	public abstract List<Double> getFeatures (String docId, String query) throws IOException;

}
