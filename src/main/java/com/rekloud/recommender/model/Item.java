package com.rekloud.recommender.model;

import java.sql.Timestamp;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "item")
public class Item {
	
	@DatabaseField (uniqueCombo = true,index = true,canBeNull = false,id = true)
    private long itemId;
	
	@DatabaseField(index = true,canBeNull = false)
    private String content;
	
	@DatabaseField
    private Timestamp date;
    
	public Item() {
        // ORMLite needs a no-arg constructor 
    }

	public long getItemId() {
		return itemId;
	}

	public void setItemId(long itemId) {
		this.itemId = itemId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Timestamp getDate() {
		return date;
	}

	public void setDate(Timestamp date) {
		this.date = date;
	}

}
