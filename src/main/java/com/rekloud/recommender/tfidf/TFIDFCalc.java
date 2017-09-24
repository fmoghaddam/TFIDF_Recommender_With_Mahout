package com.rekloud.recommender.tfidf;

import java.util.ArrayList;
import java.util.List;

public class TFIDFCalc {

	private static Corpus corpus;
	private static VectorSpaceModel vectorSpace ;
	private static ArrayList<Document> documents ;
	public static double calc(String n1,String n2,List<String> all) {
		if(n1==null || n2==null) {
			return 0.;
		}

		if(documents==null || documents.isEmpty() || documents.size()!=all.size()) {
			documents = new ArrayList<Document>();
			for(String n:all) {
				documents.add(new Document(n));
			}
			corpus = new Corpus(documents);
			vectorSpace = new VectorSpaceModel(corpus);
		}
		final Document query = new Document(n1);
		final Document d = new Document(n2);

		return vectorSpace.cosineSimilarity(query, d);
	}
}