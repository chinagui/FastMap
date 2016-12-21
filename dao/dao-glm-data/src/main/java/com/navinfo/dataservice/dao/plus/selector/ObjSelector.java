package com.navinfo.dataservice.dao.plus.selector;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;
import com.navinfo.dataservice.dao.plus.glm.GlmColumn;
import com.navinfo.dataservice.dao.plus.glm.GlmFactory;
import com.navinfo.dataservice.dao.plus.glm.GlmObject;
import com.navinfo.dataservice.dao.plus.glm.GlmRef;
import com.navinfo.dataservice.dao.plus.glm.GlmTable;
import com.navinfo.dataservice.dao.plus.glm.GlmTableNotFoundException;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.ObjFactory;
import com.navinfo.navicommons.database.QueryRunner;

/** 
 * selector出来的row为UPDATE状态
 * @ClassName: ObjSelector
 * @author xiaoxiaowen4127
 * @date 2016年11月10日
 * @Description: ObjSelector.java
 */
public class ObjSelector {
	
	private static final Logger logger = Logger.getLogger(ObjSelector.class);

	/**
	 * 
	 * @param conn
	 * @param objType
	 * @param tabNames：所需要加载子表列表；null或空则加载所有子表
	 * @param isMainOnly:是否只加载主表，true则只加载主表，判断优先级高于tabNames
	 * @param pid
	 * @param isLock
	 * @return
	 * @throws Exception
	 */
	public static BasicObj selectByPid(Connection conn,String objType,Set<String> tabNames,boolean isMainOnly,long pid,boolean isLock)throws Exception{
		//若参数为空，返回null
		if(pid==0){
			logger.info("selectByPid查询主表，PID为空");
			return null;
		}
		//根据对象类型构造glmObj
		GlmObject glmObj = GlmFactory.getInstance().getObjByType(objType);
		GlmTable mainTable = glmObj.getMainTable();
		String sql = assembleSql(mainTable,"PID");
		if(isLock){
			sql += " FOR UPDATE NOWAIT";
		}
		logger.info("selectByPid查询主表："+sql);
		BasicRow mainrow = new QueryRunner().query(conn, sql, new SingleSelRsHandler(mainTable,pid),pid);
		BasicObj obj = ObjFactory.getInstance().create4Select(mainrow);
		
		//加载子表
		if(isMainOnly){
			logger.info("selectByPid不加载子表");
		}else{
			if(tabNames==null||tabNames.isEmpty()){
				//加载所有子表
				tabNames = glmObj.getTables().keySet();
			}
			logger.info("selectByPid开始加载子表");
			selectChildren(conn,obj,tabNames);
			logger.info("selectByPid加载子表结束");
		}
//		if(tabNames!=null&&!tabNames.isEmpty()){
//			logger.info("selectByPid开始加载子表");
//			selectChildren(conn,obj,tabNames);
//			logger.info("selectByPid加载子表结束");
//		}
		return obj;
	}

	/**
	 * 拼装查询sql
	 * @param mainTable
	 * @param colName
	 * @return
	 */
	private static String assembleSql(GlmTable glmTable, String colName) {
		// TODO Auto-generated method stub
		if(glmTable.getObjRef()==null){			
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT P.*,P." + glmTable.getPkColumn() + " OBJ_PID  FROM " + glmTable.getName() + " P WHERE P." + colName + "=?");

			return sb.toString();
		}else{
			GlmRef objRef = glmTable.getObjRef();
			//根据参考配置组装sql
			int i=0;
			StringBuilder sb = new StringBuilder();
			StringBuilder whereSb = new StringBuilder();
			sb.append("SELECT R0.* FROM "+glmTable.getName()+" R0");
			whereSb.append(" WHERE 1=1");
			while(objRef!=null&&(!objRef.isRefMain())){
				sb.append(","+objRef.getRefTable()+" R"+(i+1));
				whereSb.append(" AND R"+i+"."+objRef.getCol()+"=R"+(i+1)+"."+objRef.getRefCol());
				whereSb.append(" AND R"+(i+1)+".U_RECORD <> 2");
				objRef=GlmFactory.getInstance().getTableByName(objRef.getRefTable()).getObjRef();
				i++;
			}
			whereSb.append(" AND R"+i+"."+objRef.getCol()+"=?");
			whereSb.append(" AND R"+i+".U_RECORD <> 2");
			sb.append(whereSb.toString());
			return sb.toString();
		}
	}

	/**
	 * @param conn
	 * @param obj
	 * @param tabNames子表名称
	 * @throws SQLException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InvocationTargetException 
	 * @throws NoSuchMethodException 
	 * @throws GlmTableNotFoundException 
	 */
	private static void selectChildren(Connection conn, BasicObj obj, Set<String> tabNames) throws GlmTableNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, IllegalArgumentException, SQLException {
		// TODO Auto-generated method stub
		for(String tabName:tabNames){
			selectChildren(conn, obj, tabName);
		}
		
	}

	public static BasicObj selectByRowid(String objType,SelectorConfig selConfig,String rowid,boolean isOnlyMain,boolean isLock){
		//根据rowId查询主表，获得主表pid,继续根据pid获取子表
		return null;
	}
	
	
	/**
	 * 如果多条只返回第一条,仅支持主表数值或字符类型字段 
	 * @param conn
	 * @param objType
	 * @param tabNames：所需要加载子表列表；null或空则加载所有子表
	 * @param isMainOnly:是否只加载主表，true则只加载主表
	 * @param colName
	 * @param colValue
	 * @param isLock
	 * @return
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public static BasicObj selectBySpecColumn(Connection conn,String objType,Set<String> tabNames,boolean isMainOnly,String colName
			,Object colValue,boolean isLock) throws SQLException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException{
		GlmObject glmObj = GlmFactory.getInstance().getObjByType(objType);
		GlmTable mainTable = glmObj.getMainTable();
		//字段类型
		String colType = mainTable.getColumByName(colName).getType();
		if(!colType.equals(GlmColumn.TYPE_NUMBER)&&!colType.equals(GlmColumn.TYPE_VARCHAR)){
			logger.info("selectBySpecColumn不支持查询字段非字符型/数字型");
			return null;
		}
		
		//根据对象类型构造glmObj
		String sql = assembleSql(mainTable,colName);
		if(isLock){
			sql += " FOR UPDATE NOWAIT";
		}
		logger.info("selectBySpecColumn查询主表："+sql);
		
		BasicRow mainrow = new QueryRunner().query(conn, sql, new SingleSpecColumnSelRsHandler(mainTable),colValue);
		BasicObj obj = ObjFactory.getInstance().create4Select(mainrow);
		
		if(isMainOnly){
			logger.info("selectByPid不加载子表");
		}else{
			if(tabNames==null||tabNames.isEmpty()){
				//加载所有子表
				tabNames = glmObj.getTables().keySet();
			}
			logger.info("selectByPid开始加载子表");
			selectChildren(conn,obj,tabNames);
			logger.info("selectByPid加载子表结束");
		}
//		if(tabNames!=null&&!tabNames.isEmpty()){
//			logger.info("selectBySpecColumn开始加载子表");
//			selectChildren(conn,obj,tabNames);
//			logger.info("selectBySpecColumn开始加载子表");
//		}
		return obj;	
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
		String sql = assembleSql(glmTab,"PID");
		logger.info("查询，selectChildren sql:"+sql);
		List<BasicRow> childRows = new QueryRunner().query(conn, sql, new MultipleSelRsHandler(glmTab,objPid),objPid);
		//更新obj
		obj.setSubrows(glmTab.getName(),childRows);
	}
	
	
}