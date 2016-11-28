package com.navinfo.dataservice.dao.plus.selector;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChildren;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiParent;
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
	
	
	/**
	 * 根据groupId查询IxPoiParent数据
	 * @author Han Shaoming
	 * @param conn
	 * @param groupId
	 * @return
	 * @throws ServiceException
	 */
	/*public static List<IxPoiParent> getIxPoiParentByGroupId(Connection conn,long groupId) throws ServiceException{
		List<IxPoiParent> msgs = null;
		try{
			QueryRunner queryRunner = new QueryRunner();
			String sql = "SELECT * FROM IX_POI_PARENT WHERE GROUP_ID=? ORDER BY PARENT_POI_PID ASC";
			Object[] params = {groupId};
			msgs = queryRunner.query(conn, sql, new IxPoiParentHandler(),params);
			return msgs;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败，原因为:"+e.getMessage(),e);
		}
	}*/
	
	/**
	 * 根据pid查询IxPoiParent数据
	 * @author Han Shaoming
	 * @param conn
	 * @param groupId
	 * @return
	 * @throws ServiceException
	 */
	/*public static List<IxPoiParent> getIxPoiParentByPid(Connection conn,long pid) throws ServiceException{
		List<IxPoiParent> msgs = null;
		try{
			QueryRunner queryRunner = new QueryRunner();
			String sql = "SELECT * FROM IX_POI_PARENT WHERE PARENT_POI_PID=?";
			Object[] params = {pid};
			msgs = queryRunner.query(conn, sql, new IxPoiParentHandler(),params);
			return msgs;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败，原因为:"+e.getMessage(),e);
		}
	}*/
	
	/**
	 * 根据groupId查询IxPoiChildren数据
	 * @author Han Shaoming
	 * @param conn
	 * @param groupId
	 * @return
	 * @throws ServiceException
	 */
	/*public static List<IxPoiChildren> getIxPoiChildrenByGroupId(Connection conn,long groupId) throws ServiceException{
		List<IxPoiChildren> msgs = null;
		try{
			QueryRunner queryRunner = new QueryRunner();
			String sql = "SELECT * FROM IX_POI_CHILDREN WHERE GROUP_ID=?";
			Object[] params = {groupId};
			msgs = queryRunner.query(conn, sql, new IxPoiChildrenHandler(),params);
			return msgs;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败，原因为:"+e.getMessage(),e);
		}
	}*/
	
	/**
	 * 根据pid查询父子表数据
	 * @author Han Shaoming
	 * @param conn
	 * @param groupId
	 * @return
	 * @throws ServiceException
	 */
	public static List<Map<String,Object>> getParentByPid(Connection conn,long pid) throws ServiceException{
		List<Map<String,Object>> msgs = null;
		try{
			QueryRunner queryRunner = new QueryRunner();
			String sql = "SELECT C.*,P.PARENT_POI_PID,P.TENANT_FLAG,P.MEMO,P.ROW_ID P_ROW_ID "
					+ " FROM IX_POI_CHILDREN C,IX_POI_PARENT P WHERE C.GROUP_ID=P.GROUP_ID AND C.CHILD_POI_PID="+pid
					+ " ORDER BY P.PARENT_POI_PID ASC";
			log.info("根据pid查询父子表数据"+sql);
			Object[] params = {};
			msgs = queryRunner.query(conn, sql, new Children2ParentHandler(),params);
			return msgs;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败，原因为:"+e.getMessage(),e);
		}
	}
	
	/**
	 * 根据regionId查询adAdmin数据
	 * @author Han Shaoming
	 * @param conn
	 * @param regionId
	 * @return
	 * @throws ServiceException
	 */
	public static List<Map<String,Object>> getAdminByRegionId(Connection conn,long regionId) throws ServiceException{
		List<Map<String,Object>> msgs = null;
		try{
			QueryRunner queryRunner = new QueryRunner();
			String sql = "SELECT * FROM AD_ADMIN WHERE REGION_ID=?";
			Object[] params = {regionId};
			msgs = queryRunner.query(conn, sql, new AdminHandler(),params);
			return msgs;
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
	 * 查询父POI的Fid
	 * @author Han Shaoming
	 * @param conn
	 * @param pids
	 * @return
	 * @throws ServiceException
	 */
	public static Map<Long,String> getParentFid(Connection conn,List<Long> pids) throws ServiceException{
		Long parentPoiPid = null;
		try {
			Map<Long,String> msgs = new HashMap<Long,String>();
			for (Long pid : pids) {
				List<Map<String, Object>> msg = getParentByPid(conn, pid);
				if(msg != null && msg.size()>0){
					if(msg.size()>1){
						List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
						for (Map<String, Object> map : msg) {
							if((long)map.get("relationType") == 2){
								list.add(map);
							}
						}
						//若得到多个符合条件的PARENT_POI_PID，
						//则取IX_POI_CHILDREN.RELATION_TYPE=2（物理关系）的那条
						if(list.size()==1){
							parentPoiPid = (long) list.get(0).get("parentPoiPid");
							
						}else {
							//如果没有符合该条件的或有多个符合，则取PARENT_POI_PID最小的那个
							parentPoiPid = (long) msg.get(0).get("parentPoiPid");
						}
						
					}else if(msg.size() == 1){
						//获取parentPoiPid
						parentPoiPid = (long) msg.get(0).get("parentPoiPid");
					}
					if(parentPoiPid != null){
						//查询POI主表
						List<Map<String, Object>> ixPoiList = getIxPoiByPid(conn, parentPoiPid);
						if(ixPoiList != null && ixPoiList.size()>0){
							for (Map<String, Object> map : ixPoiList) {
								String poiNum = StringUtils.trimToEmpty((String) map.get("poiNum"));
								msgs.put(pid, poiNum);
							}
						}else{
							//未查询到记录
							msgs.put(pid, "");
						}
					}
				}else{
					//未找到符合条件的父时转出空字符串
					msgs.put(pid, "");
				}
			}
			return msgs;
		} catch(Exception e){
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
			Map<Long,String> msgs = new HashMap<Long,String>();
			for (Long pid : pids) {
				List<Map<String, Object>> ixPoiList = getIxPoiByPid(conn, pid);
				if(ixPoiList != null && ixPoiList.size()>0){
					for (Map<String, Object> map : ixPoiList) {
						String poiNum = StringUtils.trimToEmpty((String) map.get("poiNum"));
						msgs.put(pid, poiNum);
					}
				}else{
					//未查询到记录
					msgs.put(pid, "");
				}
			}
			return msgs;
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
	
	static class AdminHandler implements ResultSetHandler<List<Map<String,Object>>>{
		public List<Map<String,Object>> handle(ResultSet rs) throws SQLException {
			List<Map<String,Object>> msgs = new ArrayList<Map<String,Object>>();
			while(rs.next()){
				Map<String,Object> msg = new HashMap<String,Object>();
				msg.put("regionId",rs.getLong("REGION_ID"));
				msg.put("adminId",rs.getLong("ADMIN_ID"));
				msg.put("extendId",rs.getLong("EXTEND_ID"));
				msg.put("adminType",rs.getLong("ADMIN_TYPE"));
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
