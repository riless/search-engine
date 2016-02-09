package indexation;

import java.util.HashMap;
import java.io.Serializable;

public abstract class TextRepresenter implements Serializable{
	
	
	private static final long serialVersionUID = 1L;

	public abstract HashMap<String,Integer> getTextRepresentation(String text);
}
