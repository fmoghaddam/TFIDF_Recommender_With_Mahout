package com.rekloud.recommender.model;

import java.sql.Timestamp;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "taste_preferences")
public class Rating {
    
	@DatabaseField (uniqueCombo = true,index = true,canBeNull = false)
    private long user_id;
	@DatabaseField (uniqueCombo = true,index = true,canBeNull = false)
    private long item_id;
	@DatabaseField
    private double preference;
	@DatabaseField
    private Timestamp date;
	
    public Rating() {
    }

	public long getUser_id() {
		return user_id;
	}

	public void setUser_id(long user_id) {
		this.user_id = user_id;
	}

	public long getItem_id() {
		return item_id;
	}

	public void setItem_id(long item_id) {
		this.item_id = item_id;
	}

	public double getPreference() {
		return preference;
	}

	public void setPreference(double preference) {
		this.preference = preference;
	}

	public Timestamp getDate() {
		return date;
	}

	public void setDate(Timestamp date) {
		this.date = date;
	}

	@Override
	public String toString() {
		return "Rating [user_id=" + user_id + ", item_id=" + item_id + ", preference=" + preference + ", date=" + date
				+ "]";
	}

}
