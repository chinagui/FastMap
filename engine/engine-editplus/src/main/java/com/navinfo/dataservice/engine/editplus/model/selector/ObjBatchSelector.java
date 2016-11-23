package com.navinfo.dataservice.engine.editplus.model.selector;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

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
	public static List<BasicObj> selectByPids(Connection conn,String objType,SelectorConfig selConfig
			,Collection<Long> pids,boolean isOnlyMain,boolean isLock,boolean isNowait) throws SQLException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException{
		GlmObject glmObj = GlmFactory.getInstance().getObjByType(objType);
		GlmTable mainTable = glmObj.getMainTable();
		String sql = "SELECT R.*,R." + mainTable.getPkColumn() + " OBJ_PID FROM "+ mainTable.getName() + " R WHERE "+mainTable.getPkColumn()
				+ " IN (" + StringUtils.join(pids.toArray(),",") + ")";
		if(isLock){
			sql += " FOR UPDATE";
			if(isNowait){
				sql += " NOWAIT";
			}
		}
		List<BasicRow> mainrowList = new QueryRunner().query(conn, sql, new SingleBatchSelRsHandler(mainTable));
		
		List<BasicObj> objList = new ArrayList<BasicObj>();
		for(BasicRow mainrow:mainrowList){
			BasicObj obj = ObjFactory.getInstance().create4Select(mainrow);
			objList.add(obj);
		}
		
		if(!isOnlyMain){
			selectChildren(conn,objList,selConfig,pids,mainTable);
		}
		return objList;
	}

	/**
	 * @param conn
	 * @param objList
	 * @param selConfig
	 * @param pids 
	 * @param mainTable 
	 * @throws SQLException 
	 */
	private static void selectChildren(Connection conn, List<BasicObj> objList, SelectorConfig selConfig, Collection<Long> pids, GlmTable mainTable) throws SQLException {
		// TODO Auto-generated method stub
		if(selConfig!=null){
			//存在配置
			if(selConfig.getSpecTables()!=null){
				for(String tab:selConfig.getSpecTables()){
					//不查主表
					if(tab.equals(mainTable.getName())){
						continue;
					}
					//获取单个子表
					selectChildren(conn,objList,tab,pids,mainTable);
				}
			}else if(selConfig.getFilterTables()!=null){
				GlmObject glmObj = GlmFactory.getInstance().getObjByType(objList.get(0).objType());
				selectChildren(conn,objList,glmObj,selConfig.getFilterTables(),pids,mainTable);
			}
		}else{
			//全部子表
			GlmObject glmObj = GlmFactory.getInstance().getObjByType(objList.get(0).objType());
			selectChildren(conn,objList,glmObj,pids,mainTable);
		}
		
	}

	/**
	 * @param conn
	 * @param objList
	 * @param glmObj
	 * @throws SQLException 
	 */
	private static void selectChildren(Connection conn, List<BasicObj> objList, GlmObject glmObj
			, Collection<Long> pids, GlmTable mainTable) throws SQLException {
		// TODO Auto-generated method stub
		Map<String,GlmTable> tables = glmObj.getTables();
		for(Map.Entry<String, GlmTable> entry:tables.entrySet()){
			//不查主表
			if(entry.getKey().equals(mainTable.getName())){
				continue;
			}
			System.out.println(entry.getKey());
			selectChildren(conn,objList,entry.getValue(),pids,mainTable);
		}
		
	}

	/**
	 * @param conn
	 * @param objList
	 * @param glmObj
	 * @param filterTables
	 * @throws SQLException 
	 */
	private static void selectChildren(Connection conn, List<BasicObj> objList, GlmObject glmObj,
			Set<String> filterTables, Collection<Long> pids, GlmTable mainTable) throws SQLException {
		// TODO Auto-generated method stub
		Map<String,GlmTable> tables = glmObj.getTables();
		for(Map.Entry<String, GlmTable> entry:tables.entrySet()){
			if(filterTables!=null&&filterTables.contains(entry.getKey())){
				continue;
			}
			//不查主表
			if(entry.getKey().equals(mainTable.getName())){
				continue;
			}
			
			selectChildren(conn,objList,entry.getValue(), pids,mainTable);
		}
		
	}

	/**
	 * @param conn
	 * @param objList
	 * @param tab
	 * @throws SQLException 
	 * @throws GlmTableNotFoundException 
	 */
	private static void selectChildren(Connection conn, List<BasicObj> objList, String tab
			, Collection<Long> pids, GlmTable mainTable) throws GlmTableNotFoundException, SQLException {
		// TODO Auto-generated method stub
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
		String sql = "SELECT " + glmTab.getName() + ".*," + mainTable.getName() + "."+ mainTable.getPkColumn() + " AS OBJ_PID FROM "+ StringUtils.join(tables.toArray(),",") +" WHERE "
				+ StringUtils.join(conditions.toArray()," AND ")
				+ " AND " + mainTable.getName() + "."+ mainTable.getPkColumn()+" IN (" + StringUtils.join(pids.toArray(),",") + ")";
		Map<Long, List<BasicRow>> childRows = new QueryRunner().query(conn, sql, new MultipleBatchSelRsHandler(glmTab));
		//更新obj
		for(BasicObj obj:objList){
			obj.insertSubrows(glmTab.getName(),childRows.get(obj.objPid()));
		}

	}

	public static List<BasicObj> selectByRowids(String objType,SelectorConfig selConfig,Collection<String> rowids,boolean isOnlyMain,boolean isLock){
		return null;
	}

	public static List<BasicObj> selectBySpecColumn(String objType,SelectorConfig selConfig,String colName,Collection<Object> colValues,boolean isOnlyMain,boolean isLock){
		return null;
	}
	
	public static List<BasicObj> selectByPolygon(String objType,SelectorConfig selConfig,Polygon polygon,boolean isOnlyMain,boolean isLock){
		return null;
	}
	public static List<BasicObj> selectByMeshIds(String objType,SelectorConfig selConfig,Collection<String> meshIds,boolean isOnlyMain,boolean isLock){
		return null;
	}
}
