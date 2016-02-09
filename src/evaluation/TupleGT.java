package evaluation;

public class TupleGT {
	String idQuery;
	String idDoc;
	String rell;
	
	public TupleGT(String lineGT){
		
	}
	
	protected TupleGT(String[] tab){
		
	}
	
	public boolean isRelevant(){
		return true;
	}
}
