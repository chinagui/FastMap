package com.navinfo.dataservice.dao.log;

public class LogDetailGrid {

	private String rowId;
	
	private int gridId;
	
	private int gridType;

	public String getRowId() {
		return rowId;
	}

	public void setRowId(String rowId) {
		this.rowId = rowId;
	}

	public int getGridId() {
		return gridId;
	}

	public void setGridId(int gridId) {
		this.gridId = gridId;
	}

	public int getGridType() {
		return gridType;
	}

	public void setGridType(int gridType) {
		this.gridType = gridType;
	}
	
	public String tableName() {
		return "log_detail_grid";
	}
}
