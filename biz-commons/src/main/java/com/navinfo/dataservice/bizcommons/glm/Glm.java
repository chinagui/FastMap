package com.navinfo.dataservice.bizcommons.glm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 
 * @ClassName: Glm 
 * @author Xiao Xiaowen 
 * @date 2016-1-12 下午2:15:06 
 * @Description: 只支持GDB+模型，没有主键的表使用ROW_ID作为主键
 */
public class Glm {
	private String gdbVersion;//240+,250+...
	private Map<String,GlmTable> editTables;//key:tableName,value:GLMTable object
	private Map<String,GlmTable> extendTables;
	//for cache
	private Map<String,List<String>> editTableNameCache;//key:featureType,value:tableName
	private Map<String,List<String>> extendTableNameCache;//key:featureType,value:tableName
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
			editTableNameCache = new HashMap<String,List<String>>();//空map
			extendTableNameCache = new HashMap<String,List<String>>();//空map
			for(String name:tables.keySet()){
				GlmTable table = tables.get(name);
				if(table.isEditable()){
					editTables.put(name, table);
				}else{
					extendTables.put(name, table);
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
	/**
	 * 获取glm中fm作业的表
	 * @param featureType:GlmTable.FEATURE_TYPE
	 * @return
	 */
	public List<String> getEditTableNames(String featureType){
		List<String> names = editTableNameCache.get(featureType);
		if(names==null){
			synchronized(this){
				names = editTableNameCache.get(featureType);
				if(names==null){
					names = new ArrayList<String>();
					//edit
					if(featureType.equals(GlmTable.FEATURE_TYPE_ALL)){
						names.addAll(editTables.keySet());
					}else{
						for(String name:editTables.keySet()){
							GlmTable table = editTables.get(name);
							if(featureType.equals(table.getFeatureType())){
								names.add(name);
							}
						}
					}
					editTableNameCache.put(featureType, names);
				}
			}
		}
		return names;
	}

	/**
	 * 获取glm中在fm中不作业的其他表
	 * @param featureType:GlmTable.FEATURE_TYPE
	 * @return
	 */
	public List<String> getExtendTableNames(String featureType){
		List<String> names = extendTableNameCache.get(featureType);
		if(names==null){
			synchronized(this){
				names = extendTableNameCache.get(featureType);
				if(names==null){
					names = new ArrayList<String>();
					//extend
					if(featureType.equals(GlmTable.FEATURE_TYPE_ALL)){
						names.addAll(extendTables.keySet());
					}else{
						for(String name:extendTables.keySet()){
							GlmTable table = extendTables.get(name);
							if(featureType.equals(table.getFeatureType())){
								names.add(name);
							}
						}
					}
					extendTableNameCache.put(featureType, names);
				}
			}
		}
		return names;
	}
}
