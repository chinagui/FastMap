package com.navinfo.dataservice.dao.plus.selector;

import java.lang.reflect.InvocationTargetException;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.plus.glm.GlmColumn;
import com.navinfo.dataservice.dao.plus.glm.GlmFactory;
import com.navinfo.dataservice.dao.plus.glm.GlmObject;
import com.navinfo.dataservice.dao.plus.glm.GlmRef;
import com.navinfo.dataservice.dao.plus.glm.GlmTable;
import com.navinfo.dataservice.dao.plus.glm.GlmTableNotFoundException;
import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.ObjFactory;
import com.navinfo.navicommons.database.QueryRunner;
import com.vividsolutions.jts.geom.Polygon;

/** 
 * 用于加载库中所有数据，不加载逻辑删除的数据
 * @ClassName: ObjAllSelector
 * @author xiaoxiaowen4127
 * @date 2017年8月31日
 * @Description: ObjAllSelector.java
 */
public class ObjAllSelector {
	
	protected static Logger logger = LoggerRepos.getLogger(ObjAllSelector.class);
	
	public static Map<Long,BasicObj> selectAll(Connection conn,String objType,Set<String> tabNames,boolean isMainOnly,boolean isLock,boolean isWait) throws SQLException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException{
		Map<Long,BasicObj> objs = new HashMap<Long,BasicObj>();
		GlmObject glmObj = GlmFactory.getInstance().getObjByType(objType);
		GlmTable mainTable = glmObj.getMainTable();
		String sql = assembleSql(mainTable,mainTable,mainTable.getPkColumn());
		if(isLock){
			sql +=" FOR UPDATE";
			if(!isWait){
				sql +=" NOWAIT";
			}
		}
		logger.info("selectBySpecColumn查询主表："+sql);
		List<BasicRow> mainrowList = new ArrayList<BasicRow>();
		mainrowList = new QueryRunner().query(conn, sql, new SingleBatchSelRsHandler(mainTable));
		
		for(BasicRow mainrow:mainrowList){
			if(mainrow==null){
				continue;
			}
			BasicObj obj = ObjFactory.getInstance().create4Select(mainrow);
			objs.put(mainrow.getObjPid(), obj);
		}
		//加载子表
		if(isMainOnly){
			logger.info("selectByPid不加载子表");
		}else{
			Set<String> subTabs = new HashSet<String>();
			if(tabNames==null||tabNames.isEmpty()){
				//加载所有子表
				for(Map.Entry<String, GlmTable> entry:glmObj.getTables().entrySet()){
					if(entry.getKey().equals(mainTable.getName())){
						continue;
					}
					subTabs.add(entry.getKey());
				}
			}else{
				for(String tn:tabNames){
					if(tn.equals(mainTable.getName())){
						continue;
					}
					subTabs.add(tn);
				}
			}
			logger.info("selectByPid开始加载子表");
			selectChildren(conn,objs.values(),subTabs);
			logger.info("selectByPid加载子表结束");
		}
		return objs;
	}
	


	/**
	 * 
	 * @param conn
	 * @param objList
	 * @param tabNames
	 * @param pids
	 * @throws GlmTableNotFoundException
	 * @throws SQLException
	 */
	private static void selectChildren(Connection conn, Collection<BasicObj> objs, Set<String> tabNames) throws GlmTableNotFoundException, SQLException {
		for(String tabName:tabNames){
			selectChildren(conn,objs,tabName);
		}
	}

	/**
	 * 
	 * @param conn
	 * @param objList
	 * @param tab
	 * @param pids
	 * @throws GlmTableNotFoundException
	 * @throws SQLException
	 */
	private static void selectChildren(Connection conn, Collection<BasicObj> objs, String tab) throws GlmTableNotFoundException, SQLException {
		if(objs.isEmpty()){
			return;
		}
		GlmTable mainTable = GlmFactory.getInstance().getTableByName(objs.iterator().next().getMainrow().tableName());
		selectChildren(conn,objs,GlmFactory.getInstance().getTableByName(tab),mainTable);
		
	}

	/**
	 * 
	 * @param conn
	 * @param objList
	 * @param glmTab
	 * @param pids
	 * @param mainTable
	 * @throws SQLException
	 */
	private static void selectChildren(Connection conn, Collection<BasicObj> objs
			, GlmTable glmTab, GlmTable mainTable) throws SQLException {
		String sql = assembleSql(glmTab,mainTable,mainTable.getPkColumn());
		logger.info("批量查询，selectChildren sql:" + sql);
		Map<Long, List<BasicRow>> childRows = new HashMap<Long, List<BasicRow>>();

		childRows = new QueryRunner().query(conn, sql, new MultipleBatchSelRsHandler(glmTab));

		//更新obj
		for(BasicObj obj:objs){
			obj.setSubrows(glmTab.getName(),(childRows.get(obj.objPid())==null?new ArrayList<BasicRow>():childRows.get(obj.objPid())));
		}

	}
	
	/**
	 * 
	 * 根据参数组装查询sql
	 * @param glmTable
	 * @param mainTable
	 * @param colName
	 * @param colValues
	 * @return
	 * @throws SQLException
	 */
	private static <T> String assembleSql(GlmTable glmTable,GlmTable mainTable, String colName) throws SQLException {
		StringBuilder sb = new StringBuilder();
		
		if(glmTable.getObjRef()==null){			
			sb = new StringBuilder();
			sb.append("SELECT P.*,P." + glmTable.getPkColumn() + " OBJ_PID FROM " + glmTable.getName() + " P WHERE P.U_RECORD <> 2");
		}else{
			GlmTable tempTab = glmTable;
			List<String> tables = new ArrayList<String>();
			List<String> conditions = new ArrayList<String>();
			tables.add(mainTable.getName());
			while(true){
				GlmRef objRef = tempTab.getObjRef();
				tables.add(tempTab.getName());
				conditions.add(tempTab.getName() + "." + objRef.getCol() + "=" + objRef.getRefTable() + "." + objRef.getRefCol());
				if(objRef.isRefMain()){
					break;
				}
				tempTab = GlmFactory.getInstance().getTableByName(objRef.getRefTable());
			}
			
			sb.append("SELECT " + glmTable.getName() + ".*," + mainTable.getName() + "."+ mainTable.getPkColumn() + " AS OBJ_PID FROM "+ StringUtils.join(tables.toArray(),","));
			sb.append(" WHERE "+ StringUtils.join(conditions.toArray()," AND "));
			//排除删除的数据
			for(String table :tables){
				if(table.equals(mainTable.getName())){
					continue;
				}
				sb.append(" AND " + table + ".U_RECORD <> 2");
			}
		}
		//字段类型
		logger.info("table:"+glmTable.getName()+",mainTable:"+mainTable+",condition: all");

		return sb.toString();
	}
}
