package com.navinfo.dataservice.engine.meta.scPointAddrAdmin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;

/**
 * 一体化元数据库和二代合库后，原SC_POINT_ADDR_ADMIN不再增加，改使用SC_POINT_ADMINAREA
 * 
 * @ClassName: ScPointAddrAdmin
 * @author xiaoxiaowen4127
 * @date 2017年3月7日
 * @Description: ScPointAddrAdmin.java
 */
public class ScPointAddrAdmin {
	
	private Map<String, Map<String,String>> addrAdminMap= new HashMap<String, Map<String,String>>();

	private static class SingletonHolder {
		private static final ScPointAddrAdmin INSTANCE = new ScPointAddrAdmin();
	}

	public static final ScPointAddrAdmin getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	public Map<String, Map<String,String>> addrAdminMap() throws Exception{
		if (addrAdminMap==null||addrAdminMap.isEmpty()) {
				synchronized (this) {
					if (addrAdminMap==null||addrAdminMap.isEmpty()) {
						try {
							StringBuilder sb = new StringBuilder();
							sb.append("SELECT ADMINAREACODE as admin_id,DECODE(TYPE,1,PROVINCE,2,CITY,3,DISTRICT) as ADMIN_NAME,TYPE as ADMIN_LEVEL\n");
							sb.append(" FROM (SELECT ADMINAREACODE,\n");
						    sb.append("           PROVINCE,\n");
						    sb.append("           CITY,\n");
						    sb.append("           DISTRICT,\n");
						    sb.append("           CASE\n");
						    sb.append("              WHEN substr(TYPE,0,decode(instr(TYPE,'（'),0,length(TYPE) ,instr(TYPE,'（')-1))  IN ('省', '自治区', '直辖市') THEN 1\n");
						    sb.append("              WHEN substr(TYPE,0,decode(instr(TYPE,'（'),0,length(TYPE) ,instr(TYPE,'（')-1))  IN ('省会', '地级市', '地区', '盟', '自治州') THEN 2\n");
						    sb.append("              WHEN substr(TYPE,0,decode(instr(TYPE,'（'),0,length(TYPE) ,instr(TYPE,'（')-1))  IN ('省直辖市', '省直辖县', '旗', '自治旗', '县级市', '自治县', '区', '县', '林区', '特区') THEN 3\n");
						    sb.append("           END\n");
						    sb.append("              TYPE\n");
						    sb.append("      FROM SC_POINT_ADMINAREA WHERE substr(TYPE,0,decode(instr(TYPE,'（'),0,length(TYPE) ,instr(TYPE,'（')-1))  in('省', '自治区', '直辖市','省会', '地级市', '地区', '盟', '自治州','省直辖市', '省直辖县', '旗', '自治旗', '县级市', '自治县', '区', '县', '林区', '特区'))");
							
//							String sql = "select t.admin_id,t.admin_name,t.admin_level from SC_POINT_ADDR_ADMIN t";
								
							PreparedStatement pstmt = null;
							ResultSet rs = null;
							Connection conn = null;
							try {
								conn = DBConnector.getInstance().getMetaConnection();
								pstmt = conn.prepareStatement(sb.toString());
								rs = pstmt.executeQuery();
								while (rs.next()) {
									Map<String,String> tempMap = new HashMap<String,String>();
									tempMap.put("adminId", rs.getString("admin_id"));
									tempMap.put("adminLevel", rs.getString("admin_level"));
									addrAdminMap.put(rs.getString("admin_name"), tempMap);					
								} 
							} finally {
								DbUtils.closeQuietly(conn, pstmt, rs);
							}
						} catch (Exception e) {
							throw new SQLException("加载SC_ENGSHORT_LIST失败："+ e.getMessage(), e);
						}
					}
				}
			}
			return addrAdminMap;
	}
}
