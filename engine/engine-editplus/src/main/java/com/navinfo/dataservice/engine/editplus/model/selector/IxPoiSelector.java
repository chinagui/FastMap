package com.navinfo.dataservice.engine.editplus.model.selector;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.engine.editplus.model.ixpoi.IxPoiChildren;
import com.navinfo.dataservice.engine.editplus.model.ixpoi.IxPoiParent;
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
	public static List<IxPoiParent> getIxPoiParentByGroupId(Connection conn,long groupId) throws ServiceException{
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
	}
	
	/**
	 * 根据groupId查询IxPoiChildren数据
	 * @author Han Shaoming
	 * @param conn
	 * @param groupId
	 * @return
	 * @throws ServiceException
	 */
	public static List<IxPoiChildren> getIxPoiChildrenByGroupId(Connection conn,long groupId) throws ServiceException{
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
	}
	
	/**
	 * 根据regionId查询IxPoiChildren数据
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
	
	
}
