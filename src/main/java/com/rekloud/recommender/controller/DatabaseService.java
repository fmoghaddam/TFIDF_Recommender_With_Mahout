package com.rekloud.recommender.controller;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.postgresql.ds.PGSimpleDataSource;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.rekloud.recommender.model.Item;
import com.rekloud.recommender.model.Rating;
import com.rekloud.recommender.util.Config;
import com.rekloud.recommender.util.DateService;

public class DatabaseService {

	private static Logger LOG = Logger.getLogger(DatabaseService.class.getCanonicalName());
	private static final int NUMBER_OF_RECOMMENDATION = Config.getInt("NUMBER_OF_RECOMMENDATION", 5);

	private static final PGSimpleDataSource dataSource;
	private static final String DATABASE_HOST = Config.getString("DATABASE_HOST", "127.0.0.1");
	private static final String DATABASE_USERNAME = Config.getString("DATABASE_USERNAME","postgres");
	private static final String DATABASE_PASSWORD = Config.getString("DATABASE_PASSWORD","postgres");
	private static final String DATABASE_NAME = Config.getString("DATABASE_NAME","postgres");
	private static final int DATABASE_PORT = Config.getInt("DATABASE_PORT", 5432);

	private static final int DATE_BACK = Config.getInt("DATE_BACK", 1);

	private static final String DATABAE_URL = "jdbc:postgresql://"+DATABASE_HOST+":"+DATABASE_PORT+"/"+DATABASE_NAME;

	private static Dao<Item, String> itemDao;
	private static Dao<Rating, String> ratingDao;

	static {
		LOG.info("Dataservice is starting...");
		dataSource = new PGSimpleDataSource();
		dataSource.setServerName(DATABASE_HOST);
		dataSource.setUser(DATABASE_USERNAME);
		dataSource.setPassword(DATABASE_PASSWORD);
		dataSource.setDatabaseName(DATABASE_NAME);
		dataSource.setPortNumber(DATABASE_PORT);

		//TODO: Duplicate; should be fixed

		try {
			final ConnectionSource cs = new JdbcConnectionSource(DATABAE_URL);
			((JdbcConnectionSource) cs).setUsername(DATABASE_USERNAME);
			((JdbcConnectionSource) cs).setPassword(DATABASE_PASSWORD);
			itemDao = DaoManager.createDao(cs, Item.class);
			ratingDao = DaoManager.createDao(cs, Rating.class);

			initTables(cs);
		} catch (SQLException e) {
			LOG.error(e.getMessage());
		}
		LOG.info("Dataservice is up");
	}

	public static PGSimpleDataSource getDataSource() {
		return dataSource;
	}

	private static void initTables(ConnectionSource cs) throws SQLException {
		try {
			itemDao.countOf();
		} catch (SQLException ex) {
			TableUtils.createTableIfNotExists(cs, Item.class);
			itemDao.executeRaw("ALTER TABLE item ALTER COLUMN content TYPE TEXT;");
		}
		try {
			ratingDao.countOf();
		} catch (SQLException ex) {
			TableUtils.createTableIfNotExists(cs, Rating.class);
		}
	}

	public static FastByIDMap<String> readAllNews() throws SQLException {
		final FastByIDMap<String> result = new FastByIDMap<>();
		final QueryBuilder<Item, String> queryBuilder = itemDao.queryBuilder();
		final Where<Item, String> where = queryBuilder.where();
		final SelectArg selectArg = new SelectArg();
		where.ge("date", selectArg);
		final PreparedQuery<Item> preparedQuery = queryBuilder.prepare();
		selectArg.setValue(DateService.getDateBack(DATE_BACK));

		final List<Item> allItemUserRate = itemDao.query(preparedQuery);
		for(final Item i:allItemUserRate) {
			result.put(i.getItemId(),i.getContent());
		}
		return result;
	}

	public static void addItem(Item item) {
		try {
			itemDao.create(item);
		} catch (SQLException e) {
			LOG.error(e.getMessage());
		}
	}

	public static void addRating(Rating rating) {
		try {
			ratingDao.create(rating);
		} catch (SQLException e) {
			LOG.error(e.getMessage());
		}
	}

	public static Dao<Item, String> getItemDao() {
		return itemDao;
	}

	public static Dao<Rating, String> getRatingDao() {
		return ratingDao;
	}

	public static List<Long> getPopularItems(long userID) {
		final List<Long> result = new ArrayList<>();
		
		try {
			List<Long> itemsUserRated = ratingDao.queryForEq("user_id",userID).stream().map(p->p.getItem_id()).collect(Collectors.toList());
			final QueryBuilder<Rating, String> queryBuilder = ratingDao.queryBuilder();
			queryBuilder.selectRaw("avg(preference) AS average,item_id");
			queryBuilder.groupBy("item_id");
			queryBuilder.orderByRaw("average DESC");

			final GenericRawResults<String[]> allItemUserRate = ratingDao.queryRaw(queryBuilder.prepareStatementString());
			final List<String[]> firstResult = allItemUserRate.getResults();

			for(final String[] s:firstResult) {				
				String split = s[1].split(", ")[0];
				long itemId = Long.parseLong(split);
				if(result.size()<NUMBER_OF_RECOMMENDATION && !itemsUserRated.contains(itemId)) {
					result.add(itemId);
				}else {
					return result;
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}

}
