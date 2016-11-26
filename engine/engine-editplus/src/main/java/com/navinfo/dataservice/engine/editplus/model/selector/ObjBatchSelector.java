package com.navinfo.dataservice.engine.editplus.model.selector;

import java.lang.reflect.InvocationTargetException;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.engine.editplus.glm.GlmColumn;
import com.navinfo.dataservice.engine.editplus.glm.GlmFactory;
import com.navinfo.dataservice.engine.editplus.glm.GlmObject;
import com.navinfo.dataservice.engine.editplus.glm.GlmRef;
import com.navinfo.dataservice.engine.editplus.glm.GlmTable;
import com.navinfo.dataservice.engine.editplus.glm.GlmTableNotFoundException;
import com.navinfo.dataservice.engine.editplus.model.BasicRow;
import com.navinfo.dataservice.engine.editplus.model.obj.BasicObj;
import com.navinfo.dataservice.engine.editplus.model.obj.ObjFactory;
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
	 * @param selConfig
	 * @param pids
	 * @param isOnlyMain
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
//	public static List<BasicObj> selectByPids(Connection conn,String objType,SelectorConfig selConfig
//			,Collection<Long> pids,boolean isOnlyMain,boolean isLock,boolean isNowait) throws SQLException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException{
//		GlmObject glmObj = GlmFactory.getInstance().getObjByType(objType);
//		GlmTable mainTable = glmObj.getMainTable();
//		StringBuilder sb = new StringBuilder();
//		sb.append("SELECT R.*,R." + mainTable.getPkColumn() + " OBJ_PID FROM "+ mainTable.getName() + " R WHERE R.");
//		Clob clobPids=null;
//		if(pids.size()>1000){
//			clobPids=conn.createClob();
//			clobPids.setString(1, StringUtils.join(pids, ","));
//			sb.append(mainTable.getPkColumn() +" IN (select to_number(column_value) from table(clob_to_table(?)))");
//		}else{
//			sb.append(mainTable.getPkColumn() +" IN (" + StringUtils.join(pids.toArray(),",") + ")");
//		}
//		//排除删除的数据
//		sb.append(" AND R.U_RECORD <> 2");
//
//		if(isLock){
//			sb.append(" FOR UPDATE");
//			if(isNowait){
//				sb.append(" NOWAIT");
//			}
//		}
//		
//		logger.info("批量查询，主表查询 sql:" + sb.toString());
//		List<BasicRow> mainrowList = new ArrayList<BasicRow>();
//		if(clobPids==null){
//			mainrowList = new QueryRunner().query(conn, sb.toString(), new SingleBatchSelRsHandler(mainTable));
//		}else{
//			mainrowList = new QueryRunner().query(conn, sb.toString(), new SingleBatchSelRsHandler(mainTable),clobPids);
//		}
//		
//		List<BasicObj> objList = new ArrayList<BasicObj>();
//		for(BasicRow mainrow:mainrowList){
//			BasicObj obj = ObjFactory.getInstance().create4Select(mainrow);
//			objList.add(obj);
//		}
//		
//		if(!isOnlyMain){
//			selectChildren(conn,objList,selConfig,pids,mainTable);
//		}
//		return objList;
//	}
	public static List<BasicObj> selectByPids(Connection conn,String objType,Set<String> tabNames
			,Collection<Long> pids,boolean isLock,boolean isNowait) throws SQLException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException{
		GlmObject glmObj = GlmFactory.getInstance().getObjByType(objType);
		GlmTable mainTable = glmObj.getMainTable();
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT R.*,R." + mainTable.getPkColumn() + " OBJ_PID FROM "+ mainTable.getName() + " R WHERE R.");
		Clob clobPids=null;
		if(pids.size()>1000){
			clobPids=conn.createClob();
			clobPids.setString(1, StringUtils.join(pids, ","));
			sb.append(mainTable.getPkColumn() +" IN (select to_number(column_value) from table(clob_to_table(?)))");
		}else{
			sb.append(mainTable.getPkColumn() +" IN (" + StringUtils.join(pids.toArray(),",") + ")");
		}
		//排除删除的数据
		sb.append(" AND R.U_RECORD <> 2");

		if(isLock){
			sb.append(" FOR UPDATE");
			if(isNowait){
				sb.append(" NOWAIT");
			}
		}
		
		logger.info("批量查询，主表查询 sql:" + sb.toString());
		List<BasicRow> mainrowList = new ArrayList<BasicRow>();
		if(clobPids==null){
			mainrowList = new QueryRunner().query(conn, sb.toString(), new SingleBatchSelRsHandler(mainTable));
		}else{
			mainrowList = new QueryRunner().query(conn, sb.toString(), new SingleBatchSelRsHandler(mainTable),clobPids);
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
	


	private static void selectChildren(Connection conn, List<BasicObj> objList, Set<String> tabNames,
			Collection<Long> pids) throws GlmTableNotFoundException, SQLException {
		// TODO Auto-generated method stub
		for(String tabName:tabNames){
			selectChildren(conn,objList,tabName,pids);
		}
	}


//	/**
//	 * @param conn
//	 * @param objList
//	 * @param glmObj
//	 * @throws SQLException 
//	 */
//	private static void selectChildren(Connection conn, List<BasicObj> objList, GlmObject glmObj
//			, Collection<Long> pids, GlmTable mainTable) throws SQLException {
//		// TODO Auto-generated method stub
//		Map<String,GlmTable> tables = glmObj.getTables();
//		for(Map.Entry<String, GlmTable> entry:tables.entrySet()){
//			//不查主表
//			if(entry.getKey().equals(mainTable.getName())){
//				continue;
//			}
//			System.out.println(entry.getKey());
//			selectChildren(conn,objList,entry.getValue(),pids,mainTable);
//		}
//		
//	}
//
//	/**
//	 * @param conn
//	 * @param objList
//	 * @param glmObj
//	 * @param filterTables
//	 * @throws SQLException 
//	 */
//	private static void selectChildren(Connection conn, List<BasicObj> objList, GlmObject glmObj,
//			Set<String> filterTables, Collection<Long> pids, GlmTable mainTable) throws SQLException {
//		// TODO Auto-generated method stub
//		Map<String,GlmTable> tables = glmObj.getTables();
//		for(Map.Entry<String, GlmTable> entry:tables.entrySet()){
//			if(filterTables!=null&&filterTables.contains(entry.getKey())){
//				continue;
//			}
//			//不查主表
//			if(entry.getKey().equals(mainTable.getName())){
//				continue;
//			}
//			
//			selectChildren(conn,objList,entry.getValue(), pids,mainTable);
//		}
//		
//	}

	/**
	 * @param conn
	 * @param objList
	 * @param tab
	 * @throws SQLException 
	 * @throws GlmTableNotFoundException 
	 */
	private static void selectChildren(Connection conn, List<BasicObj> objList, String tab
			, Collection<Long> pids) throws GlmTableNotFoundException, SQLException {
		// TODO Auto-generated method stub
		if(objList.isEmpty()){
			return;
		}
		GlmTable mainTable = GlmFactory.getInstance().getTableByName(objList.get(0).getMainrow().tableName());
		selectChildren(conn,objList,GlmFactory.getInstance().getTableByName(tab), pids,mainTable);
		
	}

	/**
	 * @param conn
	 * @param objList
	 * @param glmTab
	 * @throws SQLException 
	 */
	private static void selectChildren(Connection conn, List<BasicObj> objList
			, GlmTable glmTab, Collection<Long> pids, GlmTable mainTable) throws SQLException {
		// TODO Auto-generated method stub
		GlmTable tempTab = glmTab;
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
		
		//查询
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT " + glmTab.getName() + ".*," + mainTable.getName() + "."+ mainTable.getPkColumn() + " AS OBJ_PID FROM "+ StringUtils.join(tables.toArray(),","));
		sb.append(" WHERE "+ StringUtils.join(conditions.toArray()," AND "));
		//排除删除的数据
		for(String table :tables){
			sb.append(" AND " + table + ".U_RECORD <> 2");
		}
		Clob clobPids=null;
		if(pids.size()>1000){
			clobPids=conn.createClob();
			clobPids.setString(1, StringUtils.join(pids, ","));
			sb.append(" AND " + mainTable.getName() + "."+ mainTable.getPkColumn()+" IN (select to_number(column_value) from table(clob_to_table(?)))");
		}else{
			sb.append(" AND " + mainTable.getName() + "."+ mainTable.getPkColumn()+" IN (" + StringUtils.join(pids.toArray(),",") + ")");
		}

		logger.info("批量查询，selectChildren sql:" + sb.toString());
		Map<Long, List<BasicRow>> childRows = new HashMap<Long, List<BasicRow>>();
		if(clobPids==null){
			childRows = new QueryRunner().query(conn, sb.toString(), new MultipleBatchSelRsHandler(glmTab));
		}else{
			childRows = new QueryRunner().query(conn, sb.toString(), new MultipleBatchSelRsHandler(glmTab),clobPids);
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
	 * @param selConfig
	 * @param colName
	 * @param colValues
	 * @param isOnlyMain
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
	public static List<BasicObj> selectBySpecColumn(Connection conn,String objType,Set<String> tabNames,String colName
			,Collection<Object> colValues,boolean isLock,boolean isNowait) throws SQLException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException{
		GlmObject glmObj = GlmFactory.getInstance().getObjByType(objType);
		GlmTable mainTable = glmObj.getMainTable();
		
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT R.*,R." + mainTable.getPkColumn() + " OBJ_PID FROM "+ mainTable.getName() + " R WHERE R.U_RECORD <> 2 AND R.");
		//字段类型
		String colType = mainTable.getColumByName(colName).getType();
		//根据字段类型拼接查询条件
		if(colType.equals(GlmColumn.TYPE_VARCHAR)){
			List<String> colValues2 = new ArrayList<String>();
			for(Object colValue:colValues){
				colValues2.add("'" + colValue + "'");
			}
			colValues.clear();
			colValues.addAll(colValues2);
		}else{
			if(!colType.equals(GlmColumn.TYPE_NUMBER)){
				logger.info("selectBySpecColumn查询字段非字符型/数字型");
				return null;
			}
		}
		
		Clob clobPids=null;
		if(colValues.size()>1000){
			clobPids=conn.createClob();
			clobPids.setString(1, StringUtils.join(colValues, ","));
			sb.append(colName +" IN (select to_number(column_value) from table(clob_to_table(?)))");
		}else{
			sb.append(colName +" IN (" + StringUtils.join(colValues.toArray(),",") + ")");
		}

		if(isLock){
			sb.append(" FOR UPDATE");
			if(isNowait){
				sb.append(" NOWAIT");
			}
		}
		logger.info("selectBySpecColumn查询主表："+sb.toString());
		List<BasicRow> mainrowList = new ArrayList<BasicRow>();
		if(clobPids==null){
			mainrowList = new QueryRunner().query(conn, sb.toString(), new SingleBatchSelRsHandler(mainTable));
		}else{
			mainrowList = new QueryRunner().query(conn, sb.toString(), new SingleBatchSelRsHandler(mainTable),clobPids);
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
	
	public static List<BasicObj> selectByPolygon(String objType,SelectorConfig selConfig,Polygon polygon,boolean isOnlyMain,boolean isLock){
		return null;
	}
	public static List<BasicObj> selectByMeshIds(String objType,SelectorConfig selConfig,Collection<String> meshIds,boolean isOnlyMain,boolean isLock){
		return null;
	}
}
