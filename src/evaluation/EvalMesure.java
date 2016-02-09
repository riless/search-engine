package evaluation;

import java.util.ArrayList;

public abstract class EvalMesure {

	protected String mesureName;
	
	public String getName(){
		return this.mesureName;
	}
	public abstract Double  eval(IRList l);
	
}
