package evaluation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Map.Entry;

import indexation.Document;
import indexation.Parser;
import indexation.ParserCISI_CACM;

public class QueryParserCISI_CACM {
	
	protected ParserCISI_CACM parser;
	private String gtSplit;
	private String gtFile;
	
	public QueryParserCISI_CACM(){
		this.parser = new indexation.ParserCISI_CACM();
	}

	@SuppressWarnings("resource")
	public DocMetaData getMetaData(String csv_file, String cvsSplitBy, String docQueryId) throws IOException{

		/**** READ FILE ***/
		HashMap<String, Double> relevants = new HashMap<String, Double>();
		HashMap<String, String> subtopics = new HashMap<String, String>();
		Scanner scanner = new Scanner(new File(csv_file));
        String queryId = "";
        String docId = "";
        String subTopicId = "";
        
    	BufferedReader br = null;
    	String line = "";

        br = new BufferedReader(new FileReader(csv_file));
		while ((line = br.readLine()) != null) {
			String[] line_array = line.split(cvsSplitBy);
			queryId = line_array[0];
			docId = line_array[1];
			subTopicId = line_array[3];
			
			if ( queryId.equals(docQueryId) ){ // on ne prend que les donnée de la requete docQueryId
					relevants.put(docId, 1.0);
					subtopics.put(docId, subTopicId);
			}
		}
        scanner.close();
		
        DocMetaData metaData = new DocMetaData(relevants, subtopics) ;
        return metaData;
	}
	
	
	
	public void init(String queryFilename){
		// System.out.println("init query parser...");
		this.parser.init(queryFilename);
	}
	
	public void init(String queryFilename, String gtFile, String gtSplit){
		// System.out.println("init query parser...");
		this.gtFile = gtFile; 
		this.gtSplit = gtSplit;
		this.parser.init(queryFilename);
	}
	
	public Query nextQuery() throws IOException {
//		String relQuery = "cisi/cisi.rel"; String csvSplitBy = " \t ";
		
		// String relQuery = "BENCHMARKS/easyCLEF08/easyCLEF08_gt_clean.txt"; String csvSplitBy = " ";
		Document doc; 
		Query query = null;
		if ( (doc = this.parser.nextDocument()) != null ){
			DocMetaData metaData = this.getMetaData(this.gtFile, this.gtSplit, doc.getId());
			query = new Query(doc.getId(), doc.getText(), metaData.relevants, metaData.subtopics );
//			System.out.println("Query:");
//			System.out.println("ID: " + doc.getId());
//		    System.out.println("Text: " + doc.getText());
//		    System.out.println("Relevants: " + metaData.relevants);
//		    System.out.println("subTopics: " + metaData.subtopics);
//		    System.out.println("End Query");
			
		}
		return query;
	}

}
