package com.navinfo.dataservice.integrated;

import java.util.ArrayList;
import java.util.List;

public class TableConfig {

	private String name;
	private String pid;
	private String objectType;
	private List<RefConfig> refConfigs;
	private List<RemoveConfig> removeConfigs;
	private List<SqlConfig> sqlList;

	public TableConfig(String name, String pid, String objectType) {
		super();
		this.name = name;
		this.pid = pid;
		this.objectType = objectType;
	}

	public List<String> getRefSql() {
		List<String> sqls = new ArrayList<String>();
		for (RefConfig refConfig : refConfigs) {
			sqls.add(refConfig.toSql());
		}
		for (SqlConfig sqlConfig :sqlList) {
			sqls.add(sqlConfig.toSql());
		}
		return sqls;
	}
	
	public List<String> getRemoveSql() {
		List<String> sqls = new ArrayList<String>();
		for (RemoveConfig removeConfig : removeConfigs) {
			sqls.add(removeConfig.toSql());
		}
		return sqls;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	

	public String getObjectType() {
		return objectType;
	}

	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}

	public List<RefConfig> getRefConfigs() {
		return refConfigs;
	}

	public void setRefConfigs(List<RefConfig> refConfigs) {
		this.refConfigs = refConfigs;
	}

	public List<RemoveConfig> getRemoveConfigs() {
		return removeConfigs;
	}

	public void setRemoveConfigs(List<RemoveConfig> removeConfigs) {
		this.removeConfigs = removeConfigs;
	}

	public List<SqlConfig> getSqlList() {
		return sqlList;
	}

	public void setSqlList(List<SqlConfig> sqlList) {
		this.sqlList = sqlList;
	}

}
