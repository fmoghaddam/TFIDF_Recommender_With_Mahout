package com.rekloud.recommender.controller;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.apache.mahout.cf.taste.common.TasteException;

import com.rekloud.recommender.util.Config;

public class Runner {

	private static Logger LOG = Logger.getLogger(Runner.class.getCanonicalName());
	
	private static final int ITEM_UPDATE_INTERVAL_IN_SECOND = Config.getInt("ITEM_UPDATE_INTERVAL_IN_SECOND", 5)*1000;

	public void execute() throws SQLException, TasteException {
		final RecommendationService rs = new RecommendationService();
		new ApiHandler(rs);
		refreshItems(rs);
	}

	private void refreshItems(final RecommendationService rs) {
		final Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {					
					try {
						Thread.sleep(ITEM_UPDATE_INTERVAL_IN_SECOND);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					LOG.info("Refreshing items by querying database...");
					rs.refresh();
					LOG.info("Items refreshed");
				}
			}
		});
		t.setDaemon(true);
		t.start();
	}
}
