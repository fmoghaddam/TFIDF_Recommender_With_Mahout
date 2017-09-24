package com.rekloud.recommender.controller;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;

import com.rekloud.recommender.tfidf.TFIDFCalc;

public class TFIDFSimilarity implements ItemSimilarity {

	private FastByIDMap<String> items = new FastByIDMap<>();

	public TFIDFSimilarity(FastByIDMap<String> items) {
		this.items = items;
	}

	@Override
	public void refresh(Collection<Refreshable> alreadyRefreshed) {
		try {
			this.items=DatabaseService.readAllNews();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public double itemSimilarity(long itemID1, long itemID2) throws TasteException {
		final String item1Content = items.get(itemID1);
		final String item2Content = items.get(itemID2);
		if(item1Content==null||item2Content==null) {
			return 0.;
		}
		final List<String> list = new ArrayList<>(items.values());
		return TFIDFCalc.calc(item1Content, item2Content, list);
	}

	@Override
	public double[] itemSimilarities(long itemID1, long[] itemID2s) throws TasteException {
		final double[] result = new double[itemID2s.length];
		for (int i = 0; i < itemID2s.length; i++) {
			result[i] = itemSimilarity(itemID1, itemID2s[i]);
		}
		return result;
	}

	@Override
	public long[] allSimilarItemIDs(long itemID) throws TasteException {
		FastIDSet allSimilarItemIDs = new FastIDSet();
		LongPrimitiveIterator allItemIDs = items.keySetIterator();
		while (allItemIDs.hasNext()) {
			long possiblySimilarItemID = allItemIDs.nextLong();
			if (!Double.isNaN(itemSimilarity(itemID, possiblySimilarItemID))) {
				allSimilarItemIDs.add(possiblySimilarItemID);
			}
		}
		return allSimilarItemIDs.toArray();
	}

}
