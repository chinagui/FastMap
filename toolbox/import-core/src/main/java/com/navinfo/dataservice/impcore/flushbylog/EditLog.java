package com.navinfo.dataservice.impcore.flushbylog;
/*
 * @author MaYunFei
 * 2016年6月16日
 * 描述：import-coreEditLog.java
 */
public class EditLog {
	int opType;//1 新增；2 删除 ；3 修改
	String rowId;
	String opId;
	String newValue ;
	String tableName;
	String tableRowId;
	
	public EditLog(int opType, String rowId, String opId, String logRowId,
			String newValue, String tableName, String tableRowId) {
		super();
		this.opType = opType;
		this.rowId = rowId;
		this.opId = opId;
		this.newValue = newValue;
		this.tableName = tableName;
		this.tableRowId = tableRowId;
	}
	public int getOpType() {
		return opType;
	}
	public void setOpType(int opType) {
		this.opType = opType;
	}
	public String getRowId() {
		return rowId;
	}
	public void setRowId(String rowId) {
		this.rowId = rowId;
	}
	public String getOpId() {
		return opId;
	}
	public void setOpId(String opId) {
		this.opId = opId;
	}
	
	public String getNewValue() {
		return newValue;
	}
	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getTableRowId() {
		return tableRowId;
	}
	public void setTableRowId(String tableRowId) {
		this.tableRowId = tableRowId;
	}
	
	
	
}

