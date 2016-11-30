package com.navinfo.dataservice.dao.plus.selector.custom;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChildren;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiParent;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectType;
import com.navinfo.dataservice.dao.plus.selector.ObjBatchSelector;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;

/**
 * 
 * @ClassName IxPoiSelector
 * @author Han Shaoming
 * @date 2016年11月21日 下午5:36:44
 * @Description TODO
 */
public class IxPoiSelector {
	protected static Logger log = LoggerRepos.getLogger(IxPoiSelector.class);

	public static Map<Long,Long> getAdminIdByPids(Connection conn,Collection<Long> pids)throws Exception{
		if(pids!=null&&pids.size()>0){
			if(pids.size()>1000){
				String sql= "SELECT T.PID,P.ADMIN_ID FROM IX_POI T,AD_ADMIN  WHERE T.REGION_ID=P.REGION_ID AND T.PID IN (SELECT TO_NUMBER(COLUMN_VALUE) FROM TABLE(CLOB_TO_TABLE(?)))";
				Clob clob = ConnectionUtil.createClob(conn);
				clob.setString(1, StringUtils.join(pids, ","));
				return new QueryRunner().query(conn, sql, new PoiAdminIdSelHandler(),clob);
			}else{
				String sql= "SELECT T.PID,P.ADMIN_ID FROM IX_POI T,AD_ADMIN  WHERE T.REGION_ID=P.REGION_ID AND T.PID IN ("+StringUtils.join(pids, ",")+")";
				return new QueryRunner().query(conn,sql,new PoiAdminIdSelHandler());
			}
		}
		return null;
	}
	
	/**
	 * 查询父POI的Fid
	 * @author Han Shaoming
	 * @param conn
	 * @param pids
	 * @return
	 * @throws Exception
	 */
	public static Map<Long,String> getParentFidByPids(Connection conn,Collection<Long> pids)throws Exception{
		if(pids!=null&&pids.size()>0){
			if(pids.size()>1000){
				String sql= "SELECT C.GROUP_ID,C.CHILD_POI_PID,P.PARENT_POI_PID,I.PID,I.POI_NUM "
						+ "FROM IX_POI_CHILDREN C,IX_POI_PARENT P,IX_POI I "
						+ "WHERE C.GROUP_ID=P.GROUP_ID AND P.PARENT_POI_PID=I.PID AND C.CHILD_POI_PID IN  "
						+ "(SELECT TO_NUMBER(COLUMN_VALUE) FROM TABLE(CLOB_TO_TABLE(?)))";
				Clob clob = ConnectionUtil.createClob(conn);
				clob.setString(1, StringUtils.join(pids, ","));
				return new QueryRunner().query(conn, sql, new PoiParentFidSelHandler(),clob);
			}else{
				String sql= "SELECT C.GROUP_ID,C.CHILD_POI_PID,P.PARENT_POI_PID,I.PID,I.POI_NUM "
						+ "FROM IX_POI_CHILDREN C,IX_POI_PARENT P,IX_POI I WHERE C.GROUP_ID=P.GROUP_ID "
						+ "AND P.PARENT_POI_PID=I.PID AND C.CHILD_POI_PID IN ("+StringUtils.join(pids, ",")+")";
				return new QueryRunner().query(conn,sql,new PoiParentFidSelHandler());
			}
		}
		return null;
	}
	
	
	public static Map<Long,BasicObj> getIxPoiParentMapByChildrenPidList(Connection conn,Set<Long> pidList) throws ServiceException{
		Map<Long,BasicObj> objMap = new HashMap<Long,BasicObj>();
		try{
			Map<Long,Long> childPidParentPid = new HashMap<Long,Long>();
			String sql = "SELECT DISTINCT IPP.PARENT_POI_PID,IPC.CHILD_POI_PID"
					+ " FROM IX_POI_PARENT IPP,IX_POI_CHILDREN IPC"
					+ " WHERE IPC.GROUP_ID = IPP.GROUP_ID"
					+ " AND IPC.CHILD_POI_PID IN (" + StringUtils.join(pidList.toArray(),",") + ")";
			
			ResultSetHandler<Map<Long,Long>> rsHandler = new ResultSetHandler<Map<Long,Long>>() {
				public Map<Long,Long> handle(ResultSet rs) throws SQLException {
					Map<Long,Long> result = new HashMap<Long,Long>();
					while (rs.next()) {
						long parentPid = rs.getLong("PARENT_POI_PID");
						long childPid = rs.getLong("CHILD_POI_PID");
						result.put(childPid, parentPid);
					}
					return result;
				}
			};
			
			log.info("getIxPoiParentMapByChildrenPidList查询主表："+sql);
			childPidParentPid = new QueryRunner().query(conn,sql, rsHandler);

			//获取父对象
			if(childPidParentPid!=null&&!childPidParentPid.isEmpty()){
				Set<String> tabNames = new HashSet<String>();
				tabNames.add("IX_POI_PARENT");
				tabNames.add("IX_POI_CHILDREN");
				Map<Long,BasicObj> objs = ObjBatchSelector.selectByPids(conn, ObjectType.IX_POI, tabNames, childPidParentPid.values(), true, true);
				for(BasicObj obj:objs.values()){
					long pid = obj.objPid();
					for(Map.Entry<Long, Long> entry:childPidParentPid.entrySet()){
						if(pid==entry.getValue()){
							objMap.put(entry.getKey(), obj);
						}
					}
				}
			}
			return objMap;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败，原因为:"+e.getMessage(),e);
		}
	}
	
	/**
	 * PARENT_POI_PID查询IX_POI表(非删除)
	 * @author Han Shaoming
	 * @param conn
	 * @param parentPoiId
	 * @return
	 * @throws ServiceException
	 */
	public static List<Map<String,Object>> getIxPoiByPid(Connection conn,long parentPoiId) throws ServiceException{
		List<Map<String,Object>> msgs = null;
		try{
			QueryRunner queryRunner = new QueryRunner();
			String sql = "SELECT * FROM IX_POI WHERE PID=? AND U_RECORD IN(0,1,3)";
			Object[] params = {parentPoiId};
			msgs = queryRunner.query(conn, sql, new IxPoiHandler(),params);
			return msgs;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败，原因为:"+e.getMessage(),e);
		}
	}
	
	
	/**
	 * 查询子POI的fid
	 * @author Han Shaoming
	 * @param conn
	 * @param pids
	 * @return
	 * @throws ServiceException
	 */
	public static Map<Long,String> getChildFid(Connection conn,List<Long> pids) throws ServiceException{
		try {
			Map<Long,String> msg = new HashMap<Long,String>();
			for (Long pid : pids) {
				List<Map<String, Object>> ixPoiList = getIxPoiByPid(conn, pid);
				if(ixPoiList != null && ixPoiList.size()>0){
					for (Map<String, Object> map : ixPoiList) {
						String poiNum = StringUtils.trimToEmpty((String) map.get("poiNum"));
						msg.put(pid, poiNum);
					}
				}else{
					//未查询到记录
					msg.put(pid, "");
				}
			}
			return msg;
		} catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败，原因为:"+e.getMessage(),e);
		}
	}
	
	/**
	 * 
	 * @ClassName IxPoiParentHandler
	 * @author Han Shaoming
	 * @date 2016年11月21日 下午8:25:04
	 * @Description TODO
	 */
	static class IxPoiParentHandler implements ResultSetHandler<List<IxPoiParent>>{
		public List<IxPoiParent> handle(ResultSet rs) throws SQLException {
			List<IxPoiParent> msgs = new ArrayList<IxPoiParent>();
			while(rs.next()){
				IxPoiParent msg = new IxPoiParent();
				msg.setGroupId(rs.getLong("GROUP_ID"));
				msg.setParentPoiPid(rs.getLong("PARENT_POI_PID"));
				msg.setTenantFlag(rs.getInt("TENANT_FLAG"));
				msg.setMemo(rs.getString("MEMO"));
				msgs.add(msg);
			}
			return msgs;
		}
	}
	
	
	static class IxPoiChildrenHandler implements ResultSetHandler<List<IxPoiChildren>>{
		public List<IxPoiChildren> handle(ResultSet rs) throws SQLException {
			List<IxPoiChildren> msgs = new ArrayList<IxPoiChildren>();
			while(rs.next()){
				IxPoiChildren msg = new IxPoiChildren();
				msg.setGroupId(rs.getLong("GROUP_ID"));
				msg.setChildPoiPid(rs.getLong("CHILD_POI_PID"));
				msg.setRelationType(rs.getInt("RELATION_TYPE"));
				msgs.add(msg);
			}
			return msgs;
		}
	}
	
	static class IxPoiHandler implements ResultSetHandler<List<Map<String,Object>>>{
		public List<Map<String,Object>> handle(ResultSet rs) throws SQLException {
			List<Map<String,Object>> msgs = new ArrayList<Map<String,Object>>();
			while(rs.next()){
				Map<String,Object> msg = new HashMap<String,Object>();
				msg.put("pid",rs.getLong("PID"));
				msg.put("kindCode",rs.getLong("KIND_CODE"));
				msg.put("poiNum",rs.getString("POI_NUM"));
				msgs.add(msg);
			}
			return msgs;
		}
	}
	
	static class Children2ParentHandler implements ResultSetHandler<List<Map<String,Object>>>{
		public List<Map<String,Object>> handle(ResultSet rs) throws SQLException {
			List<Map<String,Object>> msgs = new ArrayList<Map<String,Object>>();
			while(rs.next()){
				Map<String,Object> msg = new HashMap<String,Object>();
				msg.put("groupId",rs.getLong("GROUP_ID"));
				msg.put("childPoiPid",rs.getLong("CHILD_POI_PID"));
				msg.put("relationType",rs.getLong("RELATION_TYPE"));
				msg.put("rowId",rs.getString("ROW_ID"));
				msg.put("parentPoiPid",rs.getLong("PARENT_POI_PID"));
				msg.put("TenantFlag",rs.getLong("TENANT_FLAG"));
				msg.put("Memo",rs.getString("MEMO"));
				msg.put("pRowId",rs.getString("P_ROW_ID"));
				msgs.add(msg);
			}
			return msgs;
		}
	}
	
	
}
