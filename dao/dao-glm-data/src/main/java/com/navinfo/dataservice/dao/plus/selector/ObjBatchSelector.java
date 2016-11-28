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

import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.ObjFactory;
import com.navinfo.dataservice.dao.plus.glm.GlmColumn;
import com.navinfo.dataservice.dao.plus.glm.GlmFactory;
import com.navinfo.dataservice.dao.plus.glm.GlmObject;
import com.navinfo.dataservice.dao.plus.glm.GlmRef;
import com.navinfo.dataservice.dao.plus.glm.GlmTable;
import com.navinfo.dataservice.dao.plus.glm.GlmTableNotFoundException;
import com.navinfo.navicommons.database.QueryRunner;
import com.vividsolutions.jts.geom.Polygon;

/** 
 * @ClassName: ObjBatchSelector
 * @author xiaoxiaowen4127
 * @date 2016年11月14日
 * @Description: ObjBatchSelector.java
 */
public class ObjBatchSelector {

	private static final Logger logger = Logger.getLogger(ObjSelector.class);
	/**
	 * 
	 * @param conn
	 * @param objType
	 * @param tabNames
	 * @param pids
	 * @param isLock
	 * @param isNowait
	 * @return
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public static List<BasicObj> selectByPids(Connection conn,String objType,Set<String> tabNames
			,Collection<Long> pids,boolean isLock,boolean isNowait) throws SQLException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException{
		GlmObject glmObj = GlmFactory.getInstance().getObjByType(objType);
		GlmTable mainTable = glmObj.getMainTable();
		String sql = assembleSql(mainTable,mainTable,mainTable.getPkColumn(),pids);
		if(isLock){
			sql +=" FOR UPDATE";
			if(isNowait){
				sql +=" NOWAIT";
			}
		}
		logger.info("selectBySpecColumn查询主表："+sql);
		List<BasicRow> mainrowList = new ArrayList<BasicRow>();
		if(pids.size()>1000){
			Clob clobPids=conn.createClob();
			clobPids.setString(1, StringUtils.join(pids, ","));
			mainrowList = new QueryRunner().query(conn, sql, new SingleBatchSelRsHandler(mainTable),clobPids);
		}else{
			mainrowList = new QueryRunner().query(conn, sql, new SingleBatchSelRsHandler(mainTable));
		}
		
		List<BasicObj> objList = new ArrayList<BasicObj>();
		for(BasicRow mainrow:mainrowList){
			BasicObj obj = ObjFactory.getInstance().create4Select(mainrow);
			objList.add(obj);
		}
		
		if(tabNames!=null&&!tabNames.isEmpty()){
			selectChildren(conn,objList,tabNames,pids);
		}
		return objList;
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
	private static void selectChildren(Connection conn, List<BasicObj> objList, Set<String> tabNames,
			Collection<Long> pids) throws GlmTableNotFoundException, SQLException {
		for(String tabName:tabNames){
			selectChildren(conn,objList,tabName,pids);
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
	public static void selectChildren(Connection conn, List<BasicObj> objList, String tab
			, Collection<Long> pids) throws GlmTableNotFoundException, SQLException {
		if(objList.isEmpty()){
			return;
		}
		GlmTable mainTable = GlmFactory.getInstance().getTableByName(objList.get(0).getMainrow().tableName());
		selectChildren(conn,objList,GlmFactory.getInstance().getTableByName(tab), pids,mainTable);
		
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
	private static void selectChildren(Connection conn, List<BasicObj> objList
			, GlmTable glmTab, Collection<Long> pids, GlmTable mainTable) throws SQLException {
		String sql = assembleSql(glmTab,mainTable,mainTable.getPkColumn(),pids);
		logger.info("批量查询，selectChildren sql:" + sql);
		Map<Long, List<BasicRow>> childRows = new HashMap<Long, List<BasicRow>>();
		
		Clob clobPids=null;
		if(pids.size()>1000){
			clobPids=conn.createClob();
			clobPids.setString(1, StringUtils.join(pids, ","));
			childRows = new QueryRunner().query(conn, sql, new MultipleBatchSelRsHandler(glmTab),clobPids);
		}else{
			childRows = new QueryRunner().query(conn, sql, new MultipleBatchSelRsHandler(glmTab));
		}

		//更新obj
		for(BasicObj obj:objList){
			obj.setSubrows(glmTab.getName(),childRows.get(obj.objPid()));
		}

	}

	public static List<BasicObj> selectByRowids(String objType,SelectorConfig selConfig,Collection<String> rowids,boolean isOnlyMain,boolean isLock){
		return null;
	}

	/**
	 * 如果多条只返回第一条,仅支持主表数值或字符类型字段
	 * @param conn
	 * @param objType
	 * @param tabNames
	 * @param colName
	 * @param colValues
	 * @param isLock
	 * @param isNowait
	 * @return
	 * @throws SQLException 
	 * @throws InstantiationException 
	 * @throws IllegalAccessException 
	 * @throws InvocationTargetException 
	 * @throws NoSuchMethodException 
	 * @throws ClassNotFoundException 
	 */
	public static <T> List<BasicObj> selectBySpecColumn(Connection conn,String objType,Set<String> tabNames,String colName
			,Collection<T> colValues,boolean isLock,boolean isNowait) throws SQLException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException{
		GlmObject glmObj = GlmFactory.getInstance().getObjByType(objType);
		GlmTable mainTable = glmObj.getMainTable();
		//字段类型
		String colType = mainTable.getColumByName(colName).getType();
		if(!colType.equals(GlmColumn.TYPE_NUMBER)&&!colType.equals(GlmColumn.TYPE_VARCHAR)){
			logger.info("selectBySpecColumn不支持查询字段非字符型/数字型");
			return null;
		}
		//参数为空
		if(colValues.isEmpty()){
			logger.info("selectBySpecColumn查询字段为空");
			return null;
		}
		String sql = assembleSql(mainTable,mainTable,colName,colValues);
		if(isLock){
			sql +=" FOR UPDATE";
			if(isNowait){
				sql +=" NOWAIT";
			}
		}
		logger.info("selectBySpecColumn查询主表："+sql);
		List<BasicRow> mainrowList = new ArrayList<BasicRow>();
		
		if(colValues.size()>1000){
			Clob clobPids=conn.createClob();
			clobPids.setString(1, StringUtils.join(colValues, ","));
			mainrowList = new QueryRunner().query(conn, sql, new SingleBatchSelRsHandler(mainTable),clobPids);
		}else{
			mainrowList = new QueryRunner().query(conn, sql, new SingleBatchSelRsHandler(mainTable));
		}

		
		List<BasicObj> objList = new ArrayList<BasicObj>();
		List<Long> pids = new ArrayList<Long>();
		for(BasicRow mainrow:mainrowList){
			BasicObj obj = ObjFactory.getInstance().create4Select(mainrow);
			objList.add(obj);
			pids.add(mainrow.getObjPid());
		}
		
		if(tabNames!=null&&!tabNames.isEmpty()){
			logger.info("selectBySpecColumn开始加载子表");
			selectChildren(conn,objList,tabNames,pids);
			logger.info("selectBySpecColumn开始加载子表");
		}
		return objList;
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
	private static <T> String assembleSql(GlmTable glmTable,GlmTable mainTable, String colName,Collection<T> colValues) throws SQLException {
		StringBuilder sb = new StringBuilder();
		
		if(glmTable.getObjRef()==null){			
			sb = new StringBuilder();
			sb.append("SELECT P.*,P." + glmTable.getPkColumn() + " OBJ_PID FROM " + glmTable.getName() + " P WHERE P." + colName);
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
			sb.append(" AND " + mainTable.getName() + "."+ mainTable.getPkColumn());	
		}
		//字段类型
		String colType = mainTable.getColumByName(colName).getType();
		Collection<String> colValues2 = new HashSet<String>();
		if(colType.equals(GlmColumn.TYPE_VARCHAR)){
			for(T colValue:colValues){
				colValues2.add("'" + colValue.toString() + "'");
			}
			colValues.clear();
			colValues.addAll((Collection<? extends T>) colValues2);
		}
		
				
		if(colValues.size()<=1000){
			sb.append(" IN (" + StringUtils.join(colValues.toArray(),",") + ")");
		}else{
			sb.append(" IN (select to_number(column_value) from table(clob_to_table(?)))");
		}
		
		return sb.toString();
	}
	
	public static List<BasicObj> selectByPolygon(String objType,SelectorConfig selConfig,Polygon polygon,boolean isOnlyMain,boolean isLock){
		return null;
	}
	public static List<BasicObj> selectByMeshIds(String objType,SelectorConfig selConfig,Collection<String> meshIds,boolean isOnlyMain,boolean isLock){
		return null;
	}
}
