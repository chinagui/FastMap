package com.navinfo.dataservice.engine.editplus.log;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.navicommons.database.sql.RunnableSQL;

/** 
 * @ClassName: LogDetailGrid
 * @author xiaoxiaowen4127
 * @date 2016年11月9日
 * @Description: LogDetailGrid.java
 */
public class LogDetailGrid {
	protected String logRowId;
	protected long gridId;
	protected int gridType;

	
	public String getLogRowId() {
		return logRowId;
	}
	public void setLogRowId(String logRowId) {
		this.logRowId = logRowId;
	}
	public long getGridId() {
		return gridId;
	}
	public void setGridId(long gridId) {
		this.gridId = gridId;
	}
	public int getGridType() {
		return gridType;
	}
	public void setGridType(int gridType) {
		this.gridType = gridType;
	}
	
	public RunnableSQL toSql(){
		RunnableSQL sql = new RunnableSQL();
		sql.setSql("INSERT INTO LOG_DETAIL_GRID (GRID_ID,GRID_TYPE,LOG_ROW_ID) VALUES (?,?,?)");
		List<Object> columnValues = new ArrayList<Object>();
		columnValues.add(gridId);
		columnValues.add(gridType);
		columnValues.add(logRowId);
		sql.setArgs(columnValues);
		return sql;
	}
	
}
