package com.navinfo.dataservice.integrated;

public class RemoveConfig {

	private String tableName;
	private String refPid;

	private TableConfig tableConfig;

	public RemoveConfig(String tableName, String refPid) {
		super();
		this.tableName = tableName;
		this.refPid = refPid;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getRefPid() {
		return refPid;
	}

	public void setRefPid(String refPid) {
		this.refPid = refPid;
	}

	public TableConfig getTableConfig() {
		return tableConfig;
	}

	public void setTableConfig(TableConfig tableConfig) {
		this.tableConfig = tableConfig;
	}

	public String toSql() {
		String sql = "DELETE FROM " + tableName + " T WHERE T." + refPid + " IN (SELECT PID FROM TEMP_NOT_INTEGRATED_DATA where OBJECT_TYPE='" + tableConfig.getObjectType() + "')";
		return sql;
	}

}
