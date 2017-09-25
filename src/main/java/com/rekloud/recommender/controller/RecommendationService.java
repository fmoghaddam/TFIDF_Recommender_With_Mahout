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
import org.apache.mahout.cf.taste.impl.recommender.CachingRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.SamplingCandidateItemsStrategy;
import org.apache.mahout.cf.taste.model.JDBCDataModel;
import org.apache.mahout.cf.taste.recommender.CandidateItemsStrategy;
import org.apache.mahout.cf.taste.recommender.IDRescorer;
import org.apache.mahout.cf.taste.recommender.MostSimilarItemsCandidateItemsStrategy;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;

import com.rekloud.recommender.util.CachingItemSimilarity;
import com.rekloud.recommender.util.Config;

public class RecommendationService {

	private static Logger LOG = Logger.getLogger(RecommendationService.class.getCanonicalName());
	private static final int SIMILARITY_CACHE_SIZE = Config.getInt("SIMILARITY_CACHE_SIZE", 10);
	private final DataSource dataSource = DatabaseService.getDataSource();

	private final ItemSimilarity similarity;
	private final Recommender mainRecommender;
	private final JDBCDataModel model;
	private final IDRescorer rescorer;

	public RecommendationService() throws SQLException, TasteException {
		LOG.info("RecommedationService is starting...");
		final FastByIDMap<String> news = DatabaseService.readAllNews();
		model = new PostgreSQLJDBCDataModel(dataSource);
		rescorer = new DateRescorer();
		similarity = new CachingItemSimilarity(new TFIDFSimilarity(news), SIMILARITY_CACHE_SIZE);
		
		final CandidateItemsStrategy candidateStrategy = new SamplingCandidateItemsStrategy(5,5);
		final MostSimilarItemsCandidateItemsStrategy mostSimilarStrategy = new SamplingCandidateItemsStrategy(5,5);
		
		mainRecommender = new CachingRecommender(new GenericItemBasedRecommender(model, similarity,candidateStrategy,mostSimilarStrategy));
		LOG.info("RecommedationService is up");
	}

	public List<Long> recommend(final long userID, final int numberOfRecommendation) throws TasteException {
		try{
			return mainRecommender.recommend(userID, numberOfRecommendation,rescorer).stream().map(p->p.getItemID()).collect(Collectors.toList());
		}catch(Exception e) {
			LOG.error(e.getMessage());
			return Collections.<Long>emptyList();
		}
	}
	
	public List<Long> auxilaryRecommend(final long userID, final int numberOfRecommendation) throws TasteException {
		try{
			return DatabaseService.getPopularItems(userID);
		}catch(Exception e) {
			LOG.error(e.getMessage());
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
