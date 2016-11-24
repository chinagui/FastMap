package com.navinfo.dataservice.engine.editplus.glm;

import java.util.Map;

/** 
 * @ClassName: GlmObject
 * @author xiaoxiaowen4127
 * @date 2016年11月13日
 * @Description: GlmObject.java
 */
public class GlmObject {
	protected String name;
	protected String type;//FEATURE|RELATION
	protected GlmTable mainTable;//主表
	protected String modelClassName;//对象对应的模型类名
	protected Map<String,GlmTable> tables;//key:表名，value:glmtable,所有表
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public GlmTable getMainTable() {
		return mainTable;
	}
	public void setMainTable(GlmTable mainTable) {
		this.mainTable = mainTable;
	}
	public String getModelClassName() {
		return modelClassName;
	}
	public void setModelClassName(String modelClassName) {
		this.modelClassName = modelClassName;
	}
	public Map<String, GlmTable> getTables() {
		return tables;
	}
	public void setTables(Map<String, GlmTable> tables) {
		this.tables = tables;
	}
	
	/**
	 * 根据表名获取GlmTable对象，
	 * @param tableName：大写glm模型表名
	 * @return
	 * @throws GlmTableNotFoundException
	 */
	public GlmTable getTableByName(String tableName)throws GlmTableNotFoundException{
		GlmTable table = null;
		if(tables!=null){
			table=tables.get(tableName);
		}
		
		if(table==null)throw new GlmTableNotFoundException("Glm未初始化该表");
		
		return table;
	}
}
