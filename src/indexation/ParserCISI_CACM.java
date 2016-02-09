package indexation;

import java.util.HashMap;

/**
 * 
 * Format of input files :
 * .I <id>
 * .T 
 * <Title>
 * .A <Author>
 * .K
 * <Keywords>
 * .W
 * <Text>
 * .X
 * <Links> 
 *
 */
public class ParserCISI_CACM extends Parser{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;



	public ParserCISI_CACM(){
		super(".I");
	}
	
	
	
	Document getDocument(String str){
		//System.out.println(str);
		HashMap<String,String> other=new HashMap<String,String>();
		String st[]=str.split("\n");
		boolean modeT=false;
		boolean modeA=false;
		boolean modeK=false;
		boolean modeW=false;
		boolean modeX=false;
		String info="";
		String id="";
		String text="";
		String author="";
		String keyWords="";
		String links="";
		String title="";
		for(String s:st){
			if(s.startsWith(".I")){
				id=s.substring(3);
				continue;
			}
			if(s.startsWith(".")){
				if(modeW){
					text=info;
					info="";
					modeW=false;
				}
				if(modeA){
					author=info;
					info="";
					modeA=false;
				}
				if(modeK){
					keyWords=info;
					info="";
					modeK=false;
				}
				if(modeT){
					title=info;
					info="";
					modeT=false;
				}
				if(modeX){
					other.put("links", links);
					info="";
					modeX=false;
				}
			}
			
			if(s.startsWith(".W")){
				modeW=true;
				info=s.substring(2);
				continue;
			}
			if(s.startsWith(".A")){
				modeA=true;
				info=s.substring(2);
				continue;
			}
			if(s.startsWith(".K")){
				modeK=true;
				info=s.substring(2);
				continue;
			}
			if(s.startsWith(".T")){
				modeT=true;
				info=s.substring(2);
				continue;
			}
			if(s.startsWith(".X")){
				modeX=true;
				continue;
			}
			if(modeX){
				String l[]=s.split("\t");
				if(!l[0].equals(id)){
					links+=l[0]+";";
				}
				continue;
			}
			if((modeK) || (modeW) || (modeA) || (modeT)){
				info+=" "+s;
			}
		}
	
		if(modeW){
			text=info;
			info="";
			modeW=false;
		}
		if(modeA){
			author=info;
			info="";
			modeA=false;
		}
		if(modeK){
			keyWords=info;
			info="";
			modeK=false;
		}
		if(modeX){
			other.put("links", links);
			info="";
			modeX=false;
		}
		if(modeT){
			title=info;
			info="";
			modeT=false;
		}
		other.put("title", title);
		other.put("text", text);
		other.put("author", author);
		other.put("keywords", keyWords);
		
		Document doc=new Document(id,title+" \n "+author+" \n "+keyWords+" \n "+text,other);
		//System.out.println(doc.getId()+" => "+doc.getText());
		return doc;
	}
	
	
	

	
}
