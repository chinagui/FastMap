package com.navinfo.dataservice.integrated;

public class SqlConfig {
	private String sql;

	private TableConfig tableConfig;

	public SqlConfig(String sql, TableConfig tableConfig) {
		super();
		this.sql = sql;
		this.tableConfig = tableConfig;
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public TableConfig getTableConfig() {
		return tableConfig;
	}

	public void setTableConfig(TableConfig tableConfig) {
		this.tableConfig = tableConfig;
	}

	public String toSql() {
		String sql2 = "select '" +
				tableConfig.getName() + "'," +
				tableConfig.getPid() + ",'" +
				tableConfig.getObjectType() + "','" +
				"" + "','" + "" + "','" +
				"" + "' from (" + sql + ")";
		return "INSERT INTO TEMP_NOT_INTEGRATED_DATA(" +
				"TABLE_NAME," +
				"PID," +
				"OBJECT_TYPE, " +
				"COLUMN_NAME," +
				"REF_TABLE," +
				"REF_COLUMN)" + sql2;
	}

}
