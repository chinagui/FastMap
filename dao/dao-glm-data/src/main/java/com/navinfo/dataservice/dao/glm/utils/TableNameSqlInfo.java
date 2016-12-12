/**
 * 
 */
package com.navinfo.dataservice.dao.glm.utils;

/** 
* @ClassName: TableNameMap 
* @author Zhang Xiaolong
* @date 2016年12月12日 下午2:33:45 
* @Description: TODO
*/
public class TableNameSqlInfo {
	private String tableName;
	
	private String selectColumn;
	
	private String leftJoinSql;
	
	private String outSelectCol;
	
	private String outLeftJoinSql;
	
	private int sqlLevel = 0; //0代表最内层，依次类推

	public String getTableName() {
		return tableName;
	}

	public int getSqlLevel() {
		return sqlLevel;
	}

	public void setSqlLevel(int sqlLevel) {
		this.sqlLevel = sqlLevel;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getSelectColumn() {
		return selectColumn;
	}

	public void setSelectColumn(String selectColumn) {
		this.selectColumn = selectColumn;
	}

	public String getLeftJoinSql() {
		return leftJoinSql;
	}

	public void setLeftJoinSql(String leftJoinSql) {
		this.leftJoinSql = leftJoinSql;
	}

	public String getOutSelectCol() {
		return outSelectCol;
	}

	public void setOutSelectCol(String outSelectCol) {
		this.outSelectCol = outSelectCol;
	}

	public String getOutLeftJoinSql() {
		return outLeftJoinSql;
	}

	public void setOutLeftJoinSql(String outLeftJoinSql) {
		this.outLeftJoinSql = outLeftJoinSql;
	}
	
}
