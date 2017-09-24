package com.rekloud.recommender.util;

import java.util.Date;

public class DateService {
	public static final long DAY = 24 * 60 * 60 * 1000;

	public static Date getDateBack(int day) {
		return new Date(System.currentTimeMillis() - day * DAY);
	}
}
