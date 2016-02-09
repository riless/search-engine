package indexation;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Index {
	private String INDEX_PATH = "index/";
	protected String src;
	protected String filename;
	protected String dest;
	protected Parser parser;
	protected TextRepresenter textrepresenter;
	
	protected RandomAccessFile index; // flux rw vers name+"_index";
	protected RandomAccessFile inverted; // flux rw vers name+"_inverted";
	
	protected HashMap<String, Integer[]> docs; // <docId, <pos, lon>> in index
	protected HashMap<String, Integer[]> stems; // <stem, <pos, lon>> in inverted
	protected HashMap<String, String[]> docFrom; // <docId, <src, <pos, lon>>>
	
	public Set<String> getDocs(){
		return this.docs.keySet();
	}
	
	public void serialize(java.lang.Object object, String filename) throws IOException{
		FileOutputStream fout = new FileOutputStream(this.src+INDEX_PATH+filename);
		ObjectOutputStream oos = new ObjectOutputStream(fout);
		oos.writeObject(object);
		oos.close();
	}
	
	public Object unserialize(String filename) throws IOException, ClassNotFoundException{
		FileInputStream fout = new FileInputStream(this.src+INDEX_PATH+filename);
		ObjectInputStream oos = new ObjectInputStream(fout);
		Object obj = oos.readObject();
		oos.close();
		return obj;
	}
	
	// Construire l'index
	public Index(String src, String filename, Parser parser, TextRepresenter textrepresenter) throws IOException{
		this.src = src;
		this.filename = filename;
		this.parser = parser;
		this.textrepresenter = textrepresenter;
		
		try {
			String index_file = src+INDEX_PATH+filename+"_index";
			String inverted_file = src+INDEX_PATH+filename+"_inverted";
			
			new FileOutputStream(index_file, false).close();
			new FileOutputStream(inverted_file, false).close();

			this.index = new RandomAccessFile(index_file,"rw");
			this.inverted = new RandomAccessFile(inverted_file,"rw");
		} catch (FileNotFoundException e) {
			System.out.println("Index files not found: "  + src + filename);
		}
		
		this.docs = new HashMap<String, Integer[]>();
		this.stems = new HashMap<String, Integer[]>();
		this.docFrom = new HashMap<String, String[]>();
	}
	
	// Charger l'index
	public Index(String src, String filename) throws ClassNotFoundException, IOException{
		this.src = src;
		this.filename = filename;
		
		try {
			this.index = new RandomAccessFile(this.src+INDEX_PATH+this.filename+"_index","rw");
			this.inverted = new RandomAccessFile(this.src+INDEX_PATH+this.filename+"_inverted","rw");
		} catch (FileNotFoundException e) {
			System.out.println("Index files not found");
		}
		
		this.docs = (HashMap<String, Integer[]>)this.unserialize("docs");
		this.stems = (HashMap<String, Integer[]>)this.unserialize("stems");
		this.docFrom = (HashMap<String, String[]>)this.unserialize("docFrom");
		
		// String id = "4";
		// System.out.println( getTfsForDoc(id) );
		// System.out.println( getStrDoc(id) );

	}
	
	// construire les index ( index, inverted_index )
	public void indexation() throws IOException{
		parser.init(this.src+this.filename);
		Document doc;
		
		Integer i= 0;
		Integer _pos = 0;
		Integer _len = 0;
		HashMap<String, HashMap<String, Integer>> invertedIndexLines = new HashMap<String, HashMap<String, Integer>>();
		
		while ( (doc = parser.nextDocument()) != null){
				// System.out.println( doc.getId() + "=" + doc.get("links") );
				
				if (++i % 10 == 0) // debug
					System.out.println("read doc " + i);
				
				HashMap<String, Integer> _bow = this.textrepresenter.getTextRepresentation(doc.getText());
				String docId = doc.getId();
				StringBuilder indexLine = new StringBuilder(docId + "=");
				
				for ( Map.Entry<String,Integer> stem: _bow.entrySet() ){ // pour chaque stem du document DocId
					indexLine.append( stem.getKey() + ":" + stem.getValue()+";" );
					

					if ( invertedIndexLines.get(stem.getKey()) == null ){ // si le stem n'existe pas
						HashMap<String, Integer> docIdOcc = new HashMap<String, Integer>();
						docIdOcc.put(docId, 1);
						invertedIndexLines.put( stem.getKey(), docIdOcc );
					} else {
						if ( invertedIndexLines.get(stem.getKey()).get(docId) == null ){ // si le docId dans le stem trouvé n'xiste pas
							invertedIndexLines.get(stem.getKey()).put( docId, 1 );
						} else { // si le docId existe déja dans le stem
							invertedIndexLines.get(stem.getKey()).put( docId, invertedIndexLines.get(stem.getKey()).get(docId)+1 );
						}
					}
				} 
				indexLine.setCharAt(indexLine.length()-1, '\n');
				_len=indexLine.length();
				Integer[] poslen = {_pos, _len};
				_pos +=_len;
				
				// Write
				this.index.writeBytes(indexLine.toString());
				this.docs.put( docId, poslen );
				this.docFrom.put(doc.getId(), doc.get("from").split(";"));
				
		}

		_pos = 0; // bug corrected
		for ( Entry<String, HashMap<String, Integer>> stemLine: invertedIndexLines.entrySet() ){
			String stem = stemLine.getKey();
			StringBuilder docIdOccStr = new StringBuilder(stem + "=");
			for ( Entry<String, Integer> docIdOcc:stemLine.getValue().entrySet() ){
				docIdOccStr.append( docIdOcc.getKey()+":"+docIdOcc.getValue().toString()+";" );
			}
			docIdOccStr.setCharAt(docIdOccStr.length()-1, '\n');
			_len=docIdOccStr.length();
			Integer[] poslen = {_pos, _len};
			_pos +=_len;
			
			inverted.writeBytes(docIdOccStr.toString());
			this.stems.put( stem, poslen );
		}
		
		// serialize
		this.serialize(docs, "docs");
		this.serialize(docFrom, "docFrom");
		this.serialize(stems, "stems");
		System.out.println("Indexation terminée");
	}
	
	
	static String _readFile(String path, Charset encoding) throws IOException 
			  {
			    byte[] encoded = Files.readAllBytes(Paths.get(path));
			    return new String(encoded, encoding);
			  }
	
	public HashMap<String, Integer> getTfsForDoc(String docId) throws IOException{ // retourne la représentation stem-tf d'un document a partir de l'index
		byte[] b = new byte[docs.get(docId)[1]];
		this.index.seek(docs.get(docId)[0]);
		this.index.read(b); // pos
		String stemTf = new String(b);
		
		String[] keyValuePairs = stemTf.split("=")[1].split(";");
		HashMap<String,Integer> map = new HashMap<>();               
		
		for(String pair : keyValuePairs) {
		    String[] entry = pair.split(":");                    
		    map.put(entry[0].trim(), Integer.parseInt( entry[1].trim()) );
		}
		return map;
	}
	
	public Integer getCorpusLength() throws IOException{
		Integer cl = 0;
		for ( String docId: this.getDocs()){
			cl += this.getTfsForDoc(docId).size();
		}
		return cl;
	}
	
	public Integer getDocLength(String docId) throws IOException{
		return this.getTfsForDoc(docId).size();
	}
	
	public Integer getTfForStemInCorpus(String stem) throws IOException{
		Integer tf = 0;
		for (Map.Entry<String, Integer> tfDoc: this.getTfsForStem(stem).entrySet()){
			tf += tfDoc.getValue();
		}
		return tf;
	}
	
	public Integer getDF(String stem, String docId) throws IOException{
		return this.getTfsForStem(stem).get(docId);
	}
	
	public HashMap<String, Integer> getTfsForStem(String stem) throws IOException{ // from inveted
		// stem = "cancel"; 4270573-3211517
		Integer[] stem_entry = stems.get(stem);
		if ( stem_entry == null){
			return new HashMap<String, Integer>();
		}
		byte[] b = new byte[stems.get(stem)[1]]; 
		this.inverted.seek(stems.get(stem)[0]);
		
		// System.out.println("ok " + stems.get(stem)[0].toString() );
		
		this.inverted.read(b); // pos
		String stemTf = new String(b);
		
		
		String[] keyValuePairs = stemTf.split("=")[1].split(";");
		HashMap<String,Integer> map = new HashMap<>();               
		
		for(String pair : keyValuePairs) {
		    String[] entry = pair.split(":");                    
		    map.put(entry[0].trim(), Integer.parseInt( entry[1].trim()) );
		}
		return map;
	}
	
	public String getStrDoc(String docId) throws NumberFormatException, IOException{
		RandomAccessFile file = new RandomAccessFile(this.docFrom.get(docId)[0], "r");
		byte[] b = new byte[Integer.parseInt(this.docFrom.get(docId)[2])];
		file.seek(Integer.parseInt(this.docFrom.get(docId)[1]));
		file.read(b);
		file.close();
		return new String(b);
	}


	public static void main(String[] args) throws IOException, ClassNotFoundException {
		
		Integer mode = 0; // 0: indexation; 1: load index; 2: test
		
//		String pathSource = "cisi/";
//		String fileSource = "cisi.txt";
		
		
//		String pathSource = "BENCHMARKS/easy235/";
//		String fileSource = "easy235_text.txt";
		
		String pathSource = "BENCHMARKS/easyCLEF08/";
		String fileSource = "easyCLEF08_text.txt";

		if (mode==0){
			// System.out.println("0");
			Index index = new Index(pathSource, fileSource, new ParserCISI_CACM(), new Stemmer());
			index.indexation();
		} else if (mode == 1) {
			Index index = new Index(pathSource, fileSource);
		} else {
			// test code here
		}
		
		
	}



}
