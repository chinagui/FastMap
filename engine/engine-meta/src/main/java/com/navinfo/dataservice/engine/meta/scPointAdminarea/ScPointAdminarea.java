package com.navinfo.dataservice.engine.meta.scPointAdminarea;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;

public class ScPointAdminarea {
	
	private Logger log = LoggerRepos.getLogger(this.getClass());
	
	private Map<String, List<String>> contactMap= new HashMap<String, List<String>>();

	private static class SingletonHolder {
		private static final ScPointAdminarea INSTANCE = new ScPointAdminarea();
	}

	public static final ScPointAdminarea getInstance() {
		return SingletonHolder.INSTANCE;
	}
	/**
	 * SELECT ADMINAREACODE, AREACODE FROM SC_POINT_ADMINAREA
	 * @return Map<String, List<String>> :key,AREACODE电话区号;value,ADMINAREACODE列表，对应的行政区划号列表
	 * @throws Exception
	 */
	public Map<String, List<String>> scPointAdminareaContactMap() throws Exception{
		if (contactMap==null||contactMap.isEmpty()) {
				synchronized (this) {
					if (contactMap==null||contactMap.isEmpty()) {
						try {
							String sql = "SELECT ADMINAREACODE, AREACODE FROM SC_POINT_ADMINAREA";
								
							PreparedStatement pstmt = null;
							ResultSet rs = null;
							Connection conn = null;
							try {
								conn = DBConnector.getInstance().getMetaConnection();
								pstmt = conn.prepareStatement(sql);
								rs = pstmt.executeQuery();
								while (rs.next()) {
									String admin=rs.getString("ADMINAREACODE");
									String contact=rs.getString("AREACODE");
									if(!contactMap.containsKey(contact)){
										contactMap.put(contact, new ArrayList<String>());}
									contactMap.get(contact).add(admin);					
								} 
							} catch (Exception e) {
								throw new Exception(e);
							} finally {
								DbUtils.close(conn);
							}
						} catch (Exception e) {
							throw new SQLException("加载SC_ENGSHORT_LIST失败："+ e.getMessage(), e);
						}
					}
				}
			}
			return contactMap;
	}
	
	/**
	 * 根据错别字获取记录
	 * @param name
	 * @return List<Map<String, Object>>
	 * @throws Exception
	 */
	public List<Map<String, Object>> searchByErrorName(String name)
			throws Exception {
		Connection conn = null;
		
		try{
			conn = DBConnector.getInstance().getMetaConnection();
			QueryRunner queryRunner = new QueryRunner();
			
			StringBuilder builder = new StringBuilder();
			builder.append("SELECT DISTINCT A.ADMINAREACODE,A.WHOLE FROM SC_POINT_ADMINAREA A WHERE");
			builder.append(" A.PROVINCE_SHORT LIKE '%"+name+"%'");
			builder.append(" OR A.CITY_SHORT LIKE '%"+name+"%'");
			builder.append(" OR A.DISTRICT_SHORT LIKE '%"+name+"%'");
			Object[] params = {};
			
			ResultSetHandler<List<Map<String,Object>>> rsh = new ResultSetHandler<List<Map<String,Object>>>() {
				@Override
				public List<Map<String, Object>> handle(ResultSet rs) throws SQLException {
					// TODO Auto-generated method stub
					List<Map<String,Object>> msgs = new ArrayList<Map<String,Object>>();
					while(rs.next()){
						Map<String,Object> msg = new HashMap<String, Object>();
						msg.put("adminAreaCode",rs.getString("ADMINAREACODE"));
						msg.put("whole",rs.getString("WHOLE"));
						msgs.add(msg);
					}
					return msgs;
				}
			};
			List<Map<String, Object>> query = queryRunner.query(conn, builder.toString(), rsh, params);
			return query;
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
}
