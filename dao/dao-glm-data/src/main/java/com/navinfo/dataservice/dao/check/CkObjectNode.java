package com.navinfo.dataservice.dao.check;
/**
 * 存储要素定位用geometry，mesh等的查询语句。
 * 例如：rd_branch:234,这个要素可以用进入link的geometry，meshid
 * @author zhangxiaoyi
 *
 */
public class CkObjectNode {
	private String objectName;
	private String meshTable;
	private String meshSql;
	
	public CkObjectNode(String objectName,String meshTable,String meshSql) {
		this.objectName=objectName;
		this.meshTable=meshTable;
		this.meshSql=meshSql;
	}
	
	public String getObjectName() {
		return objectName;
	}
	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}
	public String getMeshTable() {
		return meshTable;
	}
	public void setMeshTable(String meshTable) {
		this.meshTable = meshTable;
	}
	public String getMeshSql() {
		return meshSql;
	}
	public void setMeshSql(String meshSql) {
		this.meshSql = meshSql;
	}
}
