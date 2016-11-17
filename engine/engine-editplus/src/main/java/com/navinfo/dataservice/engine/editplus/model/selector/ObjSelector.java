package com.navinfo.dataservice.engine.editplus.model.selector;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import com.navinfo.dataservice.engine.editplus.glm.GlmFactory;
import com.navinfo.dataservice.engine.editplus.glm.GlmObject;
import com.navinfo.dataservice.engine.editplus.glm.GlmRef;
import com.navinfo.dataservice.engine.editplus.glm.GlmTable;
import com.navinfo.dataservice.engine.editplus.glm.GlmTableNotFoundException;
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
//		Class<?> clazz = Class.forName(mainTable.getModelClassName());
		String sql = "SELECT * FROM "+mainTable.getName()+" WHERE "+mainTable.getPkColumn()+"=?";
		BasicRow mainrow = new QueryRunner().query(conn, sql, new SelRsHandler(mainTable,pid),pid);
		BasicObj obj = ObjFactory.getInstance().create4Select(mainrow);
		selectChildren(conn,obj,selConfig);
		return obj;
	}

	public static BasicObj selectByRowid(String objType,SelectorConfig selConfig,String rowid,boolean isOnlyMain,boolean isLock){
		//根据rowId查询主表，获得主表pid,继续根据pid获取子表
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
	
	/**
	 * 获取全部子表
	 * @param conn
	 * @param obj
	 * @param glmObj
	 * @throws SQLException 
	 */
	public static void selectChildren(Connection conn,BasicObj obj,GlmObject glmObj) throws SQLException{
		Map<String,GlmTable> tables = glmObj.getTables();
		for(Map.Entry<String, GlmTable> entry:tables.entrySet()){
			selectChildren(conn,obj,entry.getValue());
		}
	}

	/**
	 * 获取配置之外的子表
	 * @param conn
	 * @param obj
	 * @param glmObj
	 * @param filterTables
	 * @throws SQLException 
	 */
	public static void selectChildren(Connection conn,BasicObj obj,GlmObject glmObj,Collection<String> filterTables) throws SQLException{
		Map<String,GlmTable> tables = glmObj.getTables();
		for(Map.Entry<String, GlmTable> entry:tables.entrySet()){
			if(filterTables!=null&&filterTables.contains(entry.getKey())){
				continue;
			}
			selectChildren(conn,obj,entry.getValue());
		}
	}
	
	/**
	 * 根据配置获取子表
	 * @param conn
	 * @param obj
	 * @param selConfig
	 * @throws SQLException 
	 * @throws GlmTableNotFoundException 
	 */
	public static void selectChildren(Connection conn,BasicObj obj,SelectorConfig selConfig) throws GlmTableNotFoundException, SQLException{
		if(selConfig!=null){
			//存在配置
			if(selConfig.getSpecTables()!=null){
				for(String tab:selConfig.getSpecTables()){
					//获取单个子表
					selectChildren(conn,obj,tab);
				}
			}else if(selConfig.getFilterTables()!=null){
				selectChildren(conn,obj,GlmFactory.getInstance().getObjByType(obj.objType()),selConfig.getFilterTables());
			}
		}else{
			//全部子表
			selectChildren(conn,obj,GlmFactory.getInstance().getObjByType(obj.objType()));
		}
	}

	/**
	 * 根据子表名获取子表
	 * @param conn
	 * @param obj
	 * @param specTable 子表名
	 * @throws SQLException 
	 * @throws GlmTableNotFoundException 
	 */
	public static void selectChildren(Connection conn,BasicObj obj,String specTable) throws GlmTableNotFoundException, SQLException{
		selectChildren(conn,obj,GlmFactory.getInstance().getTableByName(specTable));
	}

	/**
	 * 获取子表最终实现
	 * @param conn
	 * @param obj
	 * @param glmTab 子表
	 * @throws SQLException 
	 */
	private static void selectChildren(Connection conn,BasicObj obj,GlmTable glmTab) throws SQLException{
		long objPid = obj.objPid();
		GlmRef objRef = glmTab.getObjRef();
		//直接关联
		if(objRef.isRefMain()){
			String sql = "SELECT T.* FROM "+glmTab.getName()+"T,"+objRef.getRefTable()+"R WHERE T."
					+ objRef.getCol() + "=" + objRef.getRefCol();
			BasicRow childRow = new QueryRunner().query(conn, sql, new SelRsHandler(glmTab,objPid),objPid);
			//更新obj
			
		}
		//间接关联
		else{
			//获取关联表。此处的递归放到BasicObj中实现
			
			//查询
			//更新obj
			
		}
		
	}
	
}
