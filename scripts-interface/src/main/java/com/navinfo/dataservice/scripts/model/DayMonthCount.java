package com.navinfo.dataservice.scripts.model;

public class DayMonthCount {
	private int dayDbId;

	private String tableName;
	private int dayCount;
	private int MonthCount;

	public int getDayDbId() {
		return dayDbId;
	}

	public void setDayDbId(int dayDbId) {
		this.dayDbId = dayDbId;
	}


	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public int getDayCount() {
		return dayCount;
	}

	public void setDayCount(int dayCount) {
		this.dayCount = dayCount;
	}

	public int getMonthCount() {
		return MonthCount;
	}

	public void setMonthCount(int monthCount) {
		MonthCount = monthCount;
	}
}