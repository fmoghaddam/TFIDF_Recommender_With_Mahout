package com.rekloud.recommender.controller;
import java.sql.SQLException;

import org.apache.mahout.cf.taste.recommender.IDRescorer;

import com.rekloud.recommender.model.Item;
import com.rekloud.recommender.util.Config;
import com.rekloud.recommender.util.DateService;

public class DateRescorer implements IDRescorer {

	private static final int DATE_BACK = Config.getInt("DATE_BACK", 1);
	
	@Override
	public double rescore(long id, double originalScore) {
		return originalScore;
	}

	@Override
	public boolean isFiltered(long id) {
		try {
			Item item = DatabaseService.getItemDao().queryForId(String.valueOf(id));
			if(item.getDate().after(DateService.getDateBack(DATE_BACK))) {
				return false;
			}else {
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		
	}

}
