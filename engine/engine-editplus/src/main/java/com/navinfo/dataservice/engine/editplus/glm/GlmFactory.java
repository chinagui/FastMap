package com.navinfo.dataservice.engine.editplus.glm;

import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * 表名和字段名全部为大写
 * @ClassName: GlmFactory
 * @author xiaoxiaowen4127
 * @date 2016年11月13日
 * @Description: GlmFactory.java
 */
public class GlmFactory {
	protected Logger log = Logger.getLogger(this.getClass());
	private GlmFactory(){loadGlm();}
	private volatile static GlmFactory instance;
	public static GlmFactory getInstance(){
		if(instance==null){
			synchronized(GlmFactory.class){
				if(instance==null){
					instance=new GlmFactory();
				}
			}
		}
		return instance;
	}
	private Map<String,GlmTable> tables;//key:table_name,value:glmtable
	private Map<String,GlmObject> objs;//key:objType,value:glmobject
	private Set<String> ignoreColumns;//glm不处理的字段

	public Map<String,GlmTable> getTables(){
		return tables;
	}
	public Map<String,GlmObject> getObjs(){
		return objs;
	}
	public Set<String> getIgnoreColumns() {
		return ignoreColumns;
	}
	public GlmTable getTableByName(String tableName)throws GlmTableNotFoundException{
		GlmTable table = null;
		if(tables!=null){
			table=tables.get(tableName);
		}
		
		if(table==null)throw new GlmTableNotFoundException("Glm未初始化该表");
		
		return table;
	}
	public GlmObject getTablesByObjType(String objType)throws GlmTableNotFoundException{
		GlmObject obj=null;
		if(objs!=null){
			obj=objs.get(objType);
		}
		if(obj==null)throw new GlmTableNotFoundException("Glm未初始化该表");
		return obj;
	}
	/**
	 * @param tableName:glm表名大写
	 * @return：模型的className
	 * @throws GlmTableNotFoundException
	 */
	public String getClassByTableName(String tableName)throws GlmTableNotFoundException{
		GlmTable table = getTableByName(tableName);
		return table.getModelClassName();
	}
	
	private void loadGlm(){
		try{
			//todo
		}catch(Exception e){
			log.warn("初始化GLM发生错误，可能是glm.xml配置错误,请检查");
			log.error(e.getMessage(), e);
		}
	}
}
