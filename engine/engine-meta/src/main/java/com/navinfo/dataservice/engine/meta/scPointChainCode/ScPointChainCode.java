package com.navinfo.dataservice.engine.meta.scPointChainCode;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;

public class ScPointChainCode {
	
	private List<String> chainList= new ArrayList<String>();
	
	private Map<String,String> chainMap = new HashMap<String,String>();

	private static class SingletonHolder {
		private static final ScPointChainCode INSTANCE = new ScPointChainCode();
	}

	public static final ScPointChainCode getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	/**
	 * SELECT CHAIN_CODE FROM SC_POINT_CHAIN_CODE WHERE TYPE = 1
	 * @return
	 * @throws Exception
	 */
	public List<String> scPointChainCodeList() throws Exception{
		if (chainList==null||chainList.isEmpty()) {
				synchronized (this) {
					if (chainList==null||chainList.isEmpty()) {
						try {
							String sql = "SELECT CHAIN_CODE FROM SC_POINT_CHAIN_CODE WHERE TYPE = 1";								
							PreparedStatement pstmt = null;
							ResultSet rs = null;
							Connection conn = null;
							try {
								conn = DBConnector.getInstance().getMetaConnection();
								pstmt = conn.prepareStatement(sql);
								rs = pstmt.executeQuery();
								while (rs.next()) {
									chainList.add(rs.getString("CHAIN_CODE"));					
								} 
							} catch (Exception e) {
								throw new Exception(e);
							} finally {
								DbUtils.close(conn);
							}
						} catch (Exception e) {
							throw new SQLException("加载SC_POINT_CHAIN_CODE失败："+ e.getMessage(), e);
						}
					}
				}
			}
			return chainList;
	}
	
	/**
	 * SELECT DISTINCT CHAIN_CODE,CHAIN_NAME FROM SC_POINT_CHAIN_CODE
	 * @return	Map<String,String> key:CHAIN_CODE,value:CHAIN_NAME
	 * @throws Exception
	 */
	public Map<String,String> getChainNameMap() throws Exception{
		if (chainMap==null||chainMap.isEmpty()) {
			synchronized (this) {
				if (chainMap==null||chainMap.isEmpty()) {
					try {
						String sql = "SELECT DISTINCT CHAIN_CODE,CHAIN_NAME FROM SC_POINT_CHAIN_CODE";								
						PreparedStatement pstmt = null;
						ResultSet rs = null;
						Connection conn = null;
						try {
							conn = DBConnector.getInstance().getMetaConnection();
							pstmt = conn.prepareStatement(sql);
							rs = pstmt.executeQuery();
							while (rs.next()) {
								chainMap.put(rs.getString("CHAIN_CODE"),rs.getString("CHAIN_NAME"));					
							} 
						} catch (Exception e) {
							throw new Exception(e);
						} finally {
							DbUtils.close(conn);
						}
					} catch (Exception e) {
						throw new SQLException("加载SC_POINT_CHAIN_CODE失败："+ e.getMessage(), e);
					}
				}
			}
		}
		return chainMap;
	}
	
}
