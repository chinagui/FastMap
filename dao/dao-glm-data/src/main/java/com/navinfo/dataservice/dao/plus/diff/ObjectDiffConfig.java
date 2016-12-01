package com.navinfo.dataservice.dao.plus.diff;

import java.util.Collection;
import java.util.Map;

import com.navinfo.dataservice.dao.plus.obj.ObjectName;

/** 
 * @ClassName: ObjectDiffConfig
 * @author xiaoxiaowen4127
 * @date 2016年11月13日
 * @Description: ObjectDiffConfig.java
 */
public abstract class ObjectDiffConfig {
	protected String objType;
	protected Map<String,Collection<String>> specTables;//key:tablename,value:columns
	protected Collection<String> filterTables;//ignore tables
	public String getObjType() {
		return objType;
	}
	public void setObjType(String objType) {
		this.objType = objType;
	}
	public Map<String, Collection<String>> getSpecTables() {
		return specTables;
	}
	public void setSpecTables(Map<String, Collection<String>> specTables) {
		this.specTables = specTables;
	}
	public Collection<String> getFilterTables() {
		return filterTables;
	}
	public void setFilterTables(Collection<String> filterTables) {
		this.filterTables = filterTables;
	}
	
	/**
	 * 初始化objType, specTables, filterTables
	 */
	public abstract void parse();
}
