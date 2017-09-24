package com.rekloud.recommender.controller;

import static spark.Spark.get;
import static spark.Spark.post;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.rekloud.recommender.model.Item;
import com.rekloud.recommender.model.Rating;
import com.rekloud.recommender.util.Config;

public class ApiHandler {

	private static Logger LOG = Logger.getLogger("debugLogger");

	private static final int NUMBER_OF_RECOMMENDATION = Config.getInt("NUMBER_OF_RECOMMENDATION", 5);

	public ApiHandler(RecommendationService rs) {
		startApis(rs);
	}

	public void startApis(final RecommendationService rs) {
		get("/recom/:userId", (req, res) -> {
			final long now = System.currentTimeMillis();
			final long userId = Long.parseLong(req.params(":userId"));
			List<Long> recommend = rs.recommend(userId, NUMBER_OF_RECOMMENDATION);
			if (recommend.isEmpty()) {
				recommend = rs.auxilaryRecommend(userId, NUMBER_OF_RECOMMENDATION);
			}
			System.err.println(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - now));
			LOG.info("Recommned item for user " + userId);
			return recommend;

		});

		post("/rating", (req, res) -> {
			final JSONParser parser = new JSONParser();
			try {
				final Object obj = parser.parse(req.body());
				final JSONObject jsonObject = (JSONObject) obj;
				final long userId = Long.parseLong((String) jsonObject.get("userId"));
				final long itemId = Long.parseLong((String) jsonObject.get("itemId"));
				final double ratingNumber = Double.parseDouble((String) jsonObject.get("rating"));
				final Date date = new Date();
				
				final Rating rating = new Rating();
				rating.setUser_id(userId);
				rating.setItem_id(itemId);
				rating.setPreference(ratingNumber);
				rating.setDate(new Timestamp(date.getTime()));

				try {
					DatabaseService.addRating(rating);
				} catch (Exception e) {
					e.printStackTrace();
				}
				res.status(201);
				LOG.info("Rating added " + rating);
				return "Rating added";
			} catch (ParseException e) {
				LOG.error(e.getMessage());
			}
			return "error";
		});

		post("/item", (req, res) -> {

			final JSONParser parser = new JSONParser();
			try {
				final Object obj = parser.parse(req.body());
				final JSONObject jsonObject = (JSONObject) obj;
				final String id = (String) jsonObject.get("id");
				final String body = (String) jsonObject.get("body");
				final String date = (String) jsonObject.get("date");
				final SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
				final Item item = new Item();
				item.setContent(body);
				item.setDate(new Timestamp(formatter.parse(date).getTime()));
				item.setItemId(Integer.parseInt(id));
				DatabaseService.addItem(item);
				res.status(201);
				LOG.info("Item added " + item);
				return "Item added";
			} catch (ParseException e) {
				LOG.error(e.getMessage());
			}
			return "error";
		});

	}

}
