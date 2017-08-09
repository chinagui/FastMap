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
	
	private Map<String, List<String>> dataMap= new HashMap<String, List<String>>();
	
	private Map<String, Map<String,String>> adminIdDataMap= new HashMap<String, Map<String,String>>();

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
	 * 查询省市区名称
	 * @return Map<String, List<String>> :key,省市区;value,对应的名称列表
	 * @throws Exception
	 */
	public Map<String, List<String>> scPointAdminareaDataMap() throws Exception{
		if (dataMap==null||dataMap.isEmpty()) {
				synchronized (this) {
					if (dataMap==null||dataMap.isEmpty()) {
						try {
							String sql = "SELECT distinct sp.province,sp.province_short,sp.city,sp.city_short,sp.district,sp.district_short,sp.remark FROM SC_POINT_ADMINAREA sp";
								
							PreparedStatement pstmt = null;
							ResultSet rs = null;
							Connection conn = null;
							try {
								conn = DBConnector.getInstance().getMetaConnection();
								pstmt = conn.prepareStatement(sql);
								rs = pstmt.executeQuery();
								dataMap.put("province", new ArrayList<String>());
								dataMap.put("province_short", new ArrayList<String>());
								dataMap.put("city", new ArrayList<String>());
								dataMap.put("city_short", new ArrayList<String>());
								dataMap.put("district", new ArrayList<String>());
								dataMap.put("district_short", new ArrayList<String>());
								dataMap.put("district_remark1", new ArrayList<String>());
								dataMap.put("district_short_remark1", new ArrayList<String>());
								while (rs.next()) {
									String province=rs.getString("province");
									String province_short=rs.getString("province_short");
									String city=rs.getString("city");
									String city_short=rs.getString("city_short");
									String district=rs.getString("district");
									String district_short=rs.getString("district_short");
									String remark=rs.getString("remark");
									if(province!=null&&!province.isEmpty()){
										if(!dataMap.get("province").contains(province)){
											dataMap.get("province").add(province);
										}
									}
									if(province_short!=null&&!province_short.isEmpty()){
										if(!dataMap.get("province_short").contains(province_short)){
											dataMap.get("province_short").add(province_short);
										}
									}
									if(city!=null&&!city.isEmpty()){
										if(!dataMap.get("city").contains(city)){
											dataMap.get("city").add(city);
										}
									}
									if(city_short!=null&&!city_short.isEmpty()){
										if(!dataMap.get("city_short").contains(city_short)){
											dataMap.get("city_short").add(city_short);
										}
									}
									if(district!=null&&!district.isEmpty()){
										if(!dataMap.get("district").contains(district)){
											dataMap.get("district").add(district);
										}
									}
									if(district_short!=null&&!district_short.isEmpty()){
										if(!dataMap.get("district_short").contains(district_short)){
											dataMap.get("district_short").add(district_short);
										}
									}
									if(remark!=null&&remark.equals("1")&&district!=null&&!district.isEmpty()){
										if(!dataMap.get("district_remark1").contains(district)){
											dataMap.get("district_remark1").add(district);
										}
									}
									if(remark!=null&&remark.equals("1")&&district_short!=null&&!district_short.isEmpty()){
										if(!dataMap.get("district_short_remark1").contains(district_short)){
											dataMap.get("district_short_remark1").add(district_short);
										}
									}
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
			return dataMap;
	}
	
	/**
	 * 根据错别字获取记录
	 * @param name
	 * @return List<Map<String, Object>>
	 * @throws Exception
	 */
	public List<String> searchByErrorName(String name)
			throws Exception {
		Connection conn = null;
		
		try{
			conn = DBConnector.getInstance().getMetaConnection();
			QueryRunner queryRunner = new QueryRunner();
			 
			StringBuilder builder = new StringBuilder();
			builder.append("SELECT DISTINCT A.PROVINCE_SHORT SHORT FROM SC_POINT_ADMINAREA A");
			builder.append(" WHERE A.PROVINCE_SHORT LIKE '%"+name+"%'");
			builder.append(" UNION");
			builder.append(" SELECT DISTINCT A.CITY_SHORT SHORT FROM SC_POINT_ADMINAREA A");
			builder.append(" WHERE A.CITY_SHORT LIKE '%"+name+"%'");
			builder.append(" UNION");
			builder.append(" SELECT DISTINCT A.DISTRICT_SHORT SHORT FROM SC_POINT_ADMINAREA A");
			builder.append(" WHERE A.DISTRICT_SHORT LIKE '%"+name+"%'");
			Object[] params = {};
			
			ResultSetHandler<List<String>> rsh = new ResultSetHandler<List<String>>() {
				@Override
				public List<String> handle(ResultSet rs) throws SQLException {
					// TODO Auto-generated method stub
					List<String> msgs = new ArrayList<String>();
					while(rs.next()){
						msgs.add(rs.getString("SHORT"));
					}
					return msgs;
				}
			};
			List<String> query = queryRunner.query(conn, builder.toString(), rsh, params);
			return query;
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 查询省市区名称
	 * @return Map<String, Map<String,String>> :key,AdminId;value,对应的名称列表
	 * @throws Exception
	 */
	public Map<String, Map<String,String>> scPointAdminareaByAdminId() throws Exception{
		if (adminIdDataMap==null||adminIdDataMap.isEmpty()) {
				synchronized (this) {
					if (adminIdDataMap==null||adminIdDataMap.isEmpty()) {
						try {
							String sql = "SELECT distinct sp.adminareacode,sp.province,sp.province_short,sp.city,sp.city_short,sp.district,sp.district_short,sp.remark FROM SC_POINT_ADMINAREA sp";
								
							PreparedStatement pstmt = null;
							ResultSet rs = null;
							Connection conn = null;
							try {
								conn = DBConnector.getInstance().getMetaConnection();
								pstmt = conn.prepareStatement(sql);
								rs = pstmt.executeQuery();
								while (rs.next()) {
									Map<String,String> map = new HashMap<String,String>();
									String adminId = rs.getString("adminareacode");
									String province=rs.getString("province");
									String city=rs.getString("city");
									String district=rs.getString("district");
									map.put("adminId", adminId);
									map.put("province", province);
									map.put("city", city);
									map.put("district", district);
									adminIdDataMap.put(adminId, map);
								} 
							} catch (Exception e) {
								DbUtils.rollbackAndCloseQuietly(conn);
								throw new Exception(e);
							} finally {
								DbUtils.commitAndCloseQuietly(conn);
							}
						} catch (Exception e) {
							throw new SQLException("加载SC_POINT_ADMINAREA失败："+ e.getMessage(), e);
						}
					}
				}
			}
			return adminIdDataMap;
	}
	
}
