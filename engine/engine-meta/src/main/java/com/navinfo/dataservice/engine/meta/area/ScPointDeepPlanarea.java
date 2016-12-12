package com.navinfo.dataservice.engine.meta.area;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;

public class ScPointDeepPlanarea {
	private Logger log = LoggerRepos.getLogger(this.getClass());
	
	private Connection conn;
	
	public ScPointDeepPlanarea() {
		
	}
	
	public ScPointDeepPlanarea(Connection conn) {
		this.conn = conn;
	}
	
	/**
	 * 深度信息元数据ScPointDeepPlanarea获取admincodelist
	 * @return
	 * @throws Exception
	 */
	public List<String> getDeepAdminCodeList() throws Exception{
		
		String sql = "select t.ADMIN_CODE from SC_POINT_DEEP_PLANAREA t where t.PLAN_FLAG=1";
		
		ResultSet resultSet = null;
		
		PreparedStatement pstmt = null;
		
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getMetaConnection();
			
			pstmt = conn.prepareStatement(sql);
			
			resultSet = pstmt.executeQuery();
			
			List<String> adminCodeList = new ArrayList<String>();
			
			while (resultSet.next()){
				adminCodeList.add(resultSet.getString("ADMIN_CODE"));
			}
			
			return adminCodeList;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
			DbUtils.closeQuietly(conn);
		}

	}
}


