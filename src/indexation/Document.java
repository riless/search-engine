package indexation;

import java.util.HashMap;
public class Document {
	private String id;
	private String text="";
	private HashMap<String,String> other;
	
	public Document(String id,String text,HashMap<String,String> other){
		this.id=id;
		this.text=text;
		this.other=other;
	}
	public Document(String id,String text){
		this(id,text,new HashMap<String,String>());
	}
	public Document(String id){
		this(id,"");
	}
	public String getId() {
		return id;
	}
	public String getText() {
		return text;
	}
	/*public HashMap<String, String> getOther() {
		return other;
	}*/
	public String get(String key){
		return other.get(key);
	}
	
	public void set(String key,String val){
		other.put(key, val);
	}
}
