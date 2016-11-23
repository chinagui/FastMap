package com.navinfo.dataservice.engine.editplus.model.selector;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

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
import com.vividsolutions.jts.geom.Geometry;

import java.util.List;

/** 
 * selector出来的row为UPDATE状态
 * @ClassName: ObjSelector
 * @author xiaoxiaowen4127
 * @date 2016年11月10日
 * @Description: ObjSelector.java
 */
public class ObjSelector {

	public static BasicObj selectByPid(Connection conn,String objType,SelectorConfig selConfig,long pid,boolean isOnlyMain,boolean isLock)throws Exception{
		GlmObject glmObj = GlmFactory.getInstance().getObjByType(objType);
		GlmTable mainTable = glmObj.getMainTable();
		String sql = selectByPidSql(mainTable);
		if(isLock){
			sql += " FOR UPDATE NOWAIT";
		}
		BasicRow mainrow = new QueryRunner().query(conn, sql, new SingleSelRsHandler(mainTable,pid),pid);
		BasicObj obj = ObjFactory.getInstance().create4Select(mainrow);
		if(!isOnlyMain){
			selectChildren(conn,obj,selConfig);
		}
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
	 * @throws SQLException 
	 * @throws InstantiationException 
	 * @throws IllegalAccessException 
	 * @throws InvocationTargetException 
	 * @throws NoSuchMethodException 
	 * @throws ClassNotFoundException 
	 */
	public static BasicObj selectBySpecColumn(Connection conn,String objType,SelectorConfig selConfig,String colName,Object colValue,boolean isOnlyMain,boolean isLock) throws SQLException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException{
		GlmObject glmObj = GlmFactory.getInstance().getObjByType(objType);
		GlmTable mainTable = glmObj.getMainTable();
		
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT P.*,P." + mainTable.getPkColumn() + " OBJ_PID FROM " + mainTable.getName() + " P WHERE P.");
		//根据字段类型拼接查询条件
		if(colValue instanceof Long){
			sb.append(colName + "=" + colValue);
		}else if(colValue instanceof Integer){
			sb.append(colName + "=" + colValue);
		}else if(colValue instanceof Geometry){
			
		}else if(colValue instanceof String){
			sb.append(colName + "=" + colValue);
		}else if(colValue instanceof Date){
			
		}
		
		if(isLock){
			sb.append(" FOR UPDATE NOWAIT");
		}
		BasicRow mainrow = new QueryRunner().query(conn, sb.toString(), new SingleSpecColumnSelRsHandler(mainTable));
		BasicObj obj = ObjFactory.getInstance().create4Select(mainrow);
		if(!isOnlyMain){
			selectChildren(conn,obj,selConfig);
		}
		return obj;
	}
	
	/**
	 * 获取全部子表
	 * @param conn
	 * @param obj
	 * @param glmObj
	 * @throws SQLException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InvocationTargetException 
	 * @throws NoSuchMethodException 
	 */
	public static void selectChildren(Connection conn,BasicObj obj,GlmObject glmObj) throws SQLException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, IllegalArgumentException{
		Map<String,GlmTable> tables = glmObj.getTables();
		for(Map.Entry<String, GlmTable> entry:tables.entrySet()){
			//不查主表
			if(entry.getKey().equals(obj.getMainrow().tableName())){
				continue;
			}
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
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InvocationTargetException 
	 * @throws NoSuchMethodException 
	 */
	public static void selectChildren(Connection conn,BasicObj obj,GlmObject glmObj,Collection<String> filterTables) throws SQLException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, IllegalArgumentException{
		Map<String,GlmTable> tables = glmObj.getTables();
		for(Map.Entry<String, GlmTable> entry:tables.entrySet()){
			if(filterTables!=null&&filterTables.contains(entry.getKey())){
				continue;
			}
			//不查主表
			if(entry.getKey().equals(obj.getMainrow().tableName())){
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
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InvocationTargetException 
	 * @throws NoSuchMethodException 
	 */
	public static void selectChildren(Connection conn,BasicObj obj,SelectorConfig selConfig) throws GlmTableNotFoundException, SQLException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, IllegalArgumentException{
		if(selConfig!=null){
			//存在配置
			if(selConfig.getSpecTables()!=null){
				for(String tab:selConfig.getSpecTables()){
					//不查主表
					if(tab.equals(obj.getMainrow().tableName())){
						continue;
					}
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
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InvocationTargetException 
	 * @throws NoSuchMethodException 
	 */
	public static void selectChildren(Connection conn,BasicObj obj,String specTable) throws GlmTableNotFoundException, SQLException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, IllegalArgumentException{
		selectChildren(conn,obj,GlmFactory.getInstance().getTableByName(specTable));
	}

	/**
	 * 获取子表最终实现
	 * @param conn
	 * @param obj
	 * @param glmTab 子表
	 * @throws SQLException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InvocationTargetException 
	 * @throws NoSuchMethodException 
	 */
	private static void selectChildren(Connection conn,BasicObj obj,GlmTable glmTab) throws SQLException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, IllegalArgumentException{
		long objPid = obj.objPid();
		String sql = selectByPidSql(glmTab);
		System.out.println(sql);
		List<BasicRow> childRows = new QueryRunner().query(conn, sql, new MultipleSelRsHandler(glmTab,objPid),objPid);
		//更新obj
		obj.insertSubrows(glmTab.getName(),childRows);
	}
	/**
	 * 返回带主表pid参数的sql语句
	 * @param glmTable
	 * @return
	 */
	public static String selectByPidSql(GlmTable glmTable){
		if(glmTable.getObjRef()==null){			
			StringBuilder sb = new StringBuilder();
//			List<String> columnList = getSelectColumns("P",glmTable);
//			sb.append("SELECT ");
//			sb.append(StringUtils.join(columnList,","));
//			sb.append(" FROM " + glmTable.getName() + " P WHERE P." + glmTable.getPkColumn() + "=?");
			sb.append("SELECT P.* FROM " + glmTable.getName() + " P WHERE P." + glmTable.getPkColumn() + "=?");
			return sb.toString();
		}else{
			GlmRef objRef = glmTable.getObjRef();
			//根据参考配置组装sql
			int i=0;
			StringBuilder sb = new StringBuilder();
			StringBuilder whereSb = new StringBuilder();
//			List<String> columnList = getSelectColumns("R0",glmTable);	
//			sb.append("SELECT ");
//			sb.append(StringUtils.join(columnList,","));
//			sb.append(" FROM "+glmTable.getName()+" R0");
			sb.append("SELECT R0.* FROM "+glmTable.getName()+" R0");
			whereSb.append(" WHERE 1=1");
			while(objRef!=null&&(!objRef.isRefMain())){
				sb.append(","+objRef.getRefTable()+" R"+(i+1));
				whereSb.append(" AND R"+i+"."+objRef.getCol()+"=R"+(i+1)+"."+objRef.getRefCol());
				objRef=GlmFactory.getInstance().getTableByName(objRef.getRefTable()).getObjRef();
				i++;
			}
			whereSb.append(" AND R"+i+"."+objRef.getCol()+"=?");
			sb.append(whereSb.toString());
			return sb.toString();
		}
	}

	/**
	 * @param string
	 * @param glmTable
	 * @return
	 */
	private static List<String> getSelectColumns(String tableAlias, GlmTable glmTable) {
		// TODO Auto-generated method stub
		Map<String,GlmColumn> columns = glmTable.getColumns();
		List<String> columnList = new ArrayList<String>();
		for(Map.Entry<String,GlmColumn> entry:columns.entrySet()){
			if(entry.getKey().equals("U_RECORD")||entry.getKey().equals("U_FIELDS")||entry.getKey().equals("U_DATE")){
				continue;
			}
			if(entry.getKey().equals("LEVEL")){
				if(tableAlias!=null){
					columnList.add(tableAlias + ".\"" +entry.getKey() + "\"");
				}else{
					columnList.add("\"" +entry.getKey() + "\"");
				}
				continue;
			}
			if(tableAlias!=null){
				columnList.add(tableAlias + "." +entry.getKey());
			}else{
				columnList.add(entry.getKey());
			}
		}
		return columnList;
	}
}
