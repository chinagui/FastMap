package com.navinfo.dataservice.integrated;

public class RefConfig {

	private String column;
	private String refTable;
	private String refTablePid;

	private TableConfig tableConfig;

	public RefConfig(String column, String refTable, String refTablePid) {
		super();
		this.column = column;
		this.refTable = refTable;
		this.refTablePid = refTablePid;
	}

	public String toSql() {
		String sql = "select '" +
				tableConfig.getName() + "'," +
				tableConfig.getPid() + ",'" +
				tableConfig.getObjectType() + "','" +
				column + "','" + refTable + "','" +
				refTablePid + "' from " + tableConfig.getName() + " t where t." + column + " not in (select " + refTablePid + " from " + refTable + ")"
                 + " and t." + column + " > 0";
		sql = "INSERT INTO TEMP_NOT_INTEGRATED_DATA(" +
				"TABLE_NAME," +
				"PID," +
				"OBJECT_TYPE," +
				"COLUMN_NAME," +
				"REF_TABLE," +
				"REF_COLUMN)" + sql;
		return sql;
	}

	public String getColumn() {
		return column;
	}

	public void setColumn(String column) {
		this.column = column;
	}

	public String getRefTable() {
		return refTable;
	}

	public void setRefTable(String refTable) {
		this.refTable = refTable;
	}

	public String getRefTablePid() {
		return refTablePid;
	}

	public void setRefTablePid(String refTablePid) {
		this.refTablePid = refTablePid;
	}

	public TableConfig getTableConfig() {
		return tableConfig;
	}

	public void setTableConfig(TableConfig tableConfig) {
		this.tableConfig = tableConfig;
	}

	@Override
	public String toString() {
		return "RefConfig [column=" + column + ", refTable=" + refTable + ", refTablePid=" + refTablePid + "]";
	}
	
	

}
