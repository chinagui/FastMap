package com.navinfo.dataservice.engine.editplus.model.selector;

import java.sql.Connection;
import java.util.Collection;
import java.util.Map;

import com.navinfo.dataservice.engine.editplus.glm.GlmFactory;
import com.navinfo.dataservice.engine.editplus.glm.GlmObject;
import com.navinfo.dataservice.engine.editplus.glm.GlmTable;
import com.navinfo.dataservice.engine.editplus.model.BasicRow;
import com.navinfo.dataservice.engine.editplus.model.obj.BasicObj;
import com.navinfo.dataservice.engine.editplus.model.obj.ObjFactory;
import com.navinfo.navicommons.database.QueryRunner;

/** 
 * 
 * @ClassName: ObjSelector
 * @author xiaoxiaowen4127
 * @date 2016年11月10日
 * @Description: ObjSelector.java
 */
public class ObjSelector {

	public static BasicObj selectByPid(Connection conn,String objType,SelectorConfig selConfig,long pid,boolean isOnlyMain,boolean isLock)throws Exception{
		GlmObject glmObj = GlmFactory.getInstance().getObjByType(objType);
		GlmTable mainTable = glmObj.getMainTable();
		Class<?> clazz = Class.forName(mainTable.getModelClassName());
		String sql = "SELECT * FROM "+mainTable.getName()+" WHERE "+mainTable.getPkColumn()+"=?";
		BasicRow mainrow = new QueryRunner().query(conn, sql, new SelRsHandler(mainTable,pid),pid);
		BasicObj obj = ObjFactory.getInstance().create4Select(mainrow);
		selectChildren(conn,obj,selConfig);
		return obj;
	}

	public static BasicObj selectByRowid(String objType,SelectorConfig selConfig,String rowid,boolean isOnlyMain,boolean isLock){
		return null;
	}
	
	/**
	 * 如果多条只返回第一条
	 * @param objType
	 * @param selConfig
	 * @param colName
	 * @param colValue
	 * @param isOnlyMain
	 * @param isLock
	 * @return
	 */
	public static BasicObj selectBySpecColumn(Connection conn,String objType,SelectorConfig selConfig,String colName,Object colValue,boolean isOnlyMain,boolean isLock){
		return null;
	}
	
	public static void selectChildren(Connection conn,BasicObj obj,GlmObject glmObj){
		Map<String,GlmTable> tables = glmObj.getTables();
		for(Map.Entry<String, GlmTable> entry:tables.entrySet()){
			
		}
	}

	public static void selectChildren(Connection conn,BasicObj obj,GlmObject glmObj,Collection<String> filterTables){
		Map<String,GlmTable> tables = glmObj.getTables();
		for(Map.Entry<String, GlmTable> entry:tables.entrySet()){
			if(filterTables!=null&&filterTables.contains(entry.getKey())){
				continue;
			}
			selectChildren(conn,obj,entry.getValue());
		}
	}
	
	public static void selectChildren(Connection conn,BasicObj obj,SelectorConfig selConfig){
		if(selConfig!=null){
			if(selConfig.getSpecTables()!=null){
				for(String tab:selConfig.getSpecTables()){
					//
				}
			}else if(selConfig.getFilterTables()!=null){
				
			}
		}else{
			selectChildren(conn,obj,GlmFactory.getInstance().getObjByType(obj.objType()));
		}
	}

	public static void selectChildren(Connection conn,BasicObj obj,String specTable){
		selectChildren(conn,obj,GlmFactory.getInstance().getTableByName(specTable));
	}
	
	private static void selectChildren(Connection conn,BasicObj obj,GlmTable glmTab){
		//todo
	}
	
}
