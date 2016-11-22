package com.navinfo.dataservice.engine.editplus.model.selector;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
	protected Logger log = LoggerRepos.getLogger(this.getClass());
	
	
	/**
	 * 根据groupId查询IxPoiParent数据
	 * @author Han Shaoming
	 * @param conn
	 * @param groupId
	 * @return
	 * @throws ServiceException
	 */
	public List<IxPoiParent> getIxPoiParentByGroupId(Connection conn,long groupId) throws ServiceException{
		List<IxPoiParent> msgs = null;
		try{
			QueryRunner queryRunner = new QueryRunner();
			String sql = "SELECT * FROM IX_POI_PARENT WHERE GROUP_ID=?";
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
	public List<IxPoiChildren> getIxPoiChildrenByGroupId(Connection conn,long groupId) throws ServiceException{
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
	 * 
	 * @ClassName IxPoiParentHandler
	 * @author Han Shaoming
	 * @date 2016年11月21日 下午8:25:04
	 * @Description TODO
	 */
	class IxPoiParentHandler implements ResultSetHandler<List<IxPoiParent>>{
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
	
	class IxPoiChildrenHandler implements ResultSetHandler<List<IxPoiChildren>>{
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
	
	
}
