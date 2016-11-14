package com.navinfo.dataservice.engine.editplus.diff;

import java.util.Collection;
import java.util.Map;

import com.navinfo.dataservice.engine.editplus.model.obj.ObjectType;

/** 
 * @ClassName: ObjectDiffConfig
 * @author xiaoxiaowen4127
 * @date 2016年11月13日
 * @Description: ObjectDiffConfig.java
 */
public abstract class ObjectDiffConfig {
	protected ObjectType objType;
	protected Map<String,Collection<String>> specTables;//key:tablename,value:columns
	protected Collection<String> filterTables;//ignore tables
	public ObjectType getObjType() {
		return objType;
	}
	public void setObjType(ObjectType objType) {
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
