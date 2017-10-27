package com.navinfo.dataservice.dao.plus.selector.custom;

import java.lang.reflect.InvocationTargetException;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.plus.glm.GlmFactory;
import com.navinfo.dataservice.dao.plus.glm.GlmObject;
import com.navinfo.dataservice.dao.plus.glm.GlmTable;
import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;
import com.navinfo.dataservice.dao.plus.model.ixpointaddress.IxPointaddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.ObjFactory;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.ObjBatchSelector;
import com.navinfo.dataservice.dao.plus.selector.SingleBatchSelRsHandler;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;

/**
 * 
 * @ClassName IxPoiSelector
 * @author Han Shaoming
 * @date 2016年11月21日 下午5:36:44
 * @Description TODO
 */
public class IxPointaddressSelector {
	protected static Logger log = LoggerRepos.getLogger(IxPointaddressSelector.class);

	public static Map<String,Long> getPidByFids(Connection conn,Collection<String> fids)throws Exception{
		if(fids==null|fids.size()==0)return new HashMap<String,Long>();

		if(fids.size()>1000){
			String sql= "SELECT PID,idcode FROM IX_POINTADDRESS WHERE idcode IN (SELECT COLUMN_VALUE FROM TABLE(CLOB_TO_TABLE(?))) AND U_RECORD <>2";
			Clob clob = ConnectionUtil.createClob(conn);
			clob.setString(1, StringUtils.join(fids, ","));
			return new QueryRunner().query(conn, sql, new FidPidSelHandler(),clob);
		}else{
			String sql= "SELECT PID,idcode FROM IX_POINTADDRESS WHERE idcode IN ('"+StringUtils.join(fids, "','")+"') AND U_RECORD <>2";
			return new QueryRunner().query(conn,sql,new FidPidSelHandler());
		}
	}
	
	/**
	 * 如果多条只返回第一条,仅支持主表数值或字符类型字段
	 * @param conn
	 * @param objType
	 * @param tabNames
	 * @param colName
	 * @param colValues
	 * @param isLock
	 * @param isWait//是否等待，true:等待；false：不等待
	 * @return
	 * @throws SQLException 
	 * @throws InstantiationException 
	 * @throws IllegalAccessException 
	 * @throws InvocationTargetException 
	 * @throws NoSuchMethodException 
	 * @throws ClassNotFoundException 
	 */
	public static Map<String,BasicObj> selectByFids(Connection conn,Set<String> tabNames
			,Collection<String> fids,boolean isLock,boolean isWait) throws SQLException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException{
		if(fids==null||fids.isEmpty()){
			log.info("fids为空");
			return null;
		}
		GlmObject glmObj = GlmFactory.getInstance().getObjByType(ObjectName.IX_POINTADDRESS);
		GlmTable mainTable = glmObj.getMainTable();

		String sql = ObjBatchSelector.assembleSql(mainTable,mainTable,IxPointaddress.IDCODE,fids);
		if(isLock){
			sql +=" FOR UPDATE";
			if(!isWait){
				sql +=" NOWAIT";
			}
		}
		log.info("selectByFids查询主表："+sql);
		List<BasicRow> mainrowList = new ArrayList<BasicRow>();
		
		if(fids.size()>1000){
			Clob clobPids=ConnectionUtil.createClob(conn);
			clobPids.setString(1, StringUtils.join(fids, ","));
			mainrowList = new QueryRunner().query(conn, sql, new SingleBatchSelRsHandler(mainTable),clobPids);
		}else{
			mainrowList = new QueryRunner().query(conn, sql, new SingleBatchSelRsHandler(mainTable));
		}

		
		Map<String,BasicObj> objs = new HashMap<String,BasicObj>();
		List<Long> pids = new ArrayList<Long>();
		for(BasicRow mainrow:mainrowList){
			BasicObj obj = ObjFactory.getInstance().create4Select(mainrow);
			String idcode = ((IxPointaddress)mainrow).getIdcode();
			objs.put(idcode, obj);
			pids.add(obj.objPid());
		}
		
		if(tabNames!=null&&!tabNames.isEmpty()){
			log.info("selectByFids开始加载子表");
			//ObjBatchSelector.selectChildren(conn,objs.values(),tabNames,pids);
			log.info("selectByFids开始加载子表");
		}
		return objs;
	}
	
	public static Map<Long,Long> getParentPidsByChildrenPids(Connection conn,Set<Long> pidList) throws ServiceException{
		Map<Long,Long> childPidParentPid = new HashMap<Long,Long>();
		try{
			Clob clob = ConnectionUtil.createClob(conn);
			clob.setString(1, StringUtils.join(pidList, ","));
			
			String sql = "SELECT DISTINCT IPP.PARENT_PA_PID,IPC.CHILD_PA_PID"
					+ " FROM IX_POINTADDRESS_PARENT IPP,IX_POINTADDRESS_CHILDREN IPC"
					+ " WHERE IPC.GROUP_ID = IPP.GROUP_ID"
					+ " AND IPP.U_RECORD != 2"
					+ " AND IPC.U_RECORD != 2"
					+ " AND IPC.CHILD_PA_PID IN (SELECT TO_NUMBER(COLUMN_VALUE) FROM TABLE(CLOB_TO_TABLE(?)))";
			
			ResultSetHandler<Map<Long,Long>> rsHandler = new ResultSetHandler<Map<Long,Long>>() {
				public Map<Long,Long> handle(ResultSet rs) throws SQLException {
					Map<Long,Long> result = new HashMap<Long,Long>();
					while (rs.next()) {
						long parentPid = rs.getLong("PARENT_PA_PID");
						long childPid = rs.getLong("CHILD_PA_PID");
						result.put(childPid, parentPid);
					}
					return result;
				}
			};
			
			childPidParentPid = new QueryRunner().query(conn,sql, rsHandler,clob);
			return childPidParentPid;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new ServiceException("查询失败，原因为:"+e.getMessage(),e);
		}

	}
	
	/**
	 * 查询多个父的子poiPid
	 * @param conn
	 * @param pidList
	 * @return
	 * @throws ServiceException
	 */
	public static Map<Long,List<Long>> getChildrenPidsByParentPidList(Connection conn,Set<Long> pidList) throws ServiceException{
		Map<Long,List<Long>> childPids = new HashMap<Long,List<Long>>();
		if(pidList.isEmpty()){
			return childPids;
		}
		try{
			ResultSetHandler<Map<Long,List<Long>>> rsHandler = new ResultSetHandler<Map<Long,List<Long>>>() {
				public Map<Long,List<Long>> handle(ResultSet rs) throws SQLException {
					Map<Long,List<Long>> result = new HashMap<Long,List<Long>>();
					while (rs.next()) {
						List<Long> childList = new ArrayList<Long>();
						if (result.containsKey(rs.getLong("PARENT_PA_PID"))) {
							childList = result.get(rs.getLong("PARENT_PA_PID"));
						} 
						childList.add(rs.getLong("CHILD_PA_PID"));
						result.put(rs.getLong("PARENT_PA_PID"), childList);
					}
					return result;
				}
			};
			String sql = "SELECT DISTINCT IPC.CHILD_PA_PID,IPP.PARENT_PA_PID"
					+ " FROM IX_POINTADDRESS_PARENT IPP,IX_POINTADDRESS_CHILDREN IPC"
					+ " WHERE IPC.GROUP_ID = IPP.GROUP_ID"
					+ " AND IPC.U_RECORD <>2 AND IPP.U_RECORD <>2 "
					+ " AND IPP.PARENT_PA_PID IN ";
			if (pidList.size()>100) {
				sql +=  "(SELECT TO_NUMBER(COLUMN_VALUE) FROM TABLE(CLOB_TO_TABLE(?))) ";
				Clob clob = ConnectionUtil.createClob(conn);
				clob.setString(1, StringUtils.join(pidList, ","));
				return new QueryRunner().query(conn, sql, rsHandler,clob);
			} else {
				sql += " (" + StringUtils.join(pidList.toArray(),",") + ")";
				return new QueryRunner().query(conn,sql, rsHandler);
			}
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new ServiceException("查询失败，原因为:"+e.getMessage(),e);
		}
	}
	
}
