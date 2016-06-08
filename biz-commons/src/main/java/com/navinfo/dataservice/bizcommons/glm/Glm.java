package com.navinfo.dataservice.bizcommons.glm;

import java.util.HashMap;
import java.util.Map;

/** 
 * @ClassName: Glm 
 * @author Xiao Xiaowen 
 * @date 2016-1-12 下午2:15:06 
 * @Description: 只支持GDB+模型，没有主键的表使用ROW_ID作为主键
 */
public class Glm {
	private String gdbVersion;//240+,...
	private Map<String,GlmTable> editTables;
	private Map<String,GlmTable> extendTables;
	public Glm(String gdbVersion){
		this.gdbVersion=gdbVersion;
	}
	public String getGdbVersion() {
		return gdbVersion;
	}
	public void setGdbVersion(String gdbVersion) {
		this.gdbVersion = gdbVersion;
	}

	public Map<String, GlmTable> getExtendTables() {
		return extendTables;
	}
	public void setExtendTables(Map<String, GlmTable> extendTables) {
		this.extendTables = extendTables;
	}
	public Map<String, GlmTable> getEditTables() {
		return editTables;
	}
	public void setAllTables(Map<String, GlmTable> tables) {
		if(tables!=null){
			editTables = new HashMap<String,GlmTable>();
			extendTables = new HashMap<String,GlmTable>();
			for(String key:tables.keySet()){
				GlmTable table = tables.get(key);
				if(table.isEditable()){
					editTables.put(key, table);
				}else{
					extendTables.put(key, table);
				}
			}
		}
	}
	public String getTablePidColName(String tableName){
		GlmTable glmTable = editTables.get(tableName);
		if(glmTable==null) return null;
		for(GlmColumn c:glmTable.getPks()){
			if(c.getName().contains("ID")&&c.getDataType().equals(GlmColumn.TYPE_NUMBER)){
				return c.getName();
			}
		}
		return null;
	}
}
