package com.rekloud.recommender.controller;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.jdbc.PostgreSQLJDBCDataModel;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.ItemAverageRecommender;
import org.apache.mahout.cf.taste.impl.similarity.CachingItemSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.GenericItemSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.JDBCDataModel;
import org.apache.mahout.cf.taste.recommender.IDRescorer;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;

public class RecommendationService {

	private static Logger LOG = Logger.getLogger(RecommendationService.class.getCanonicalName());
	
	private static final int SIMILARITY_CACHE_SIZE = 1000000;

	private final DataSource dataSource = DatabaseService.getDataSource();

	private final ItemSimilarity similarity;
	private final Recommender mainRecommender;
	private final Recommender auxilaryRecommender;
	private final JDBCDataModel model;
	private final IDRescorer rescorer;

	public RecommendationService() throws SQLException, TasteException {
		LOG.info("RecommedationService is starting...");
		final FastByIDMap<String> news = DatabaseService.readAllNews();
		model = new PostgreSQLJDBCDataModel(dataSource);
		rescorer = new DateRescorer();
		//similarity = new CachingItemSimilarity(new TFIDFSimilarity(news),SIMILARITY_CACHE_SIZE);
		similarity = new CachingItemSimilarity(new PearsonCorrelationSimilarity(model),SIMILARITY_CACHE_SIZE);
		mainRecommender = new GenericItemBasedRecommender(model, similarity);
		auxilaryRecommender = new ItemAverageRecommender(model);
		LOG.info("RecommedationService is up");
	}

	public List<Long> recommend(final long userID, final int numberOfRecommendation) throws TasteException {
		try{
			return mainRecommender.recommend(userID, numberOfRecommendation,rescorer).stream().map(p->p.getItemID()).collect(Collectors.toList());
		}catch(Exception e) {
			return Collections.<Long>emptyList();
		}
	}
	
	public List<Long> auxilaryRecommend(final long userID, final int numberOfRecommendation) throws TasteException {
		try{
			return auxilaryRecommender.recommend(userID, numberOfRecommendation,rescorer).stream().map(p->p.getItemID()).collect(Collectors.toList());
		}catch(Exception e) {
			return Collections.<Long>emptyList();
		}
	}

	public void refresh() {
		similarity.refresh(null);
	}

	public IDRescorer getRescorer() {
		return rescorer;
	}
}
