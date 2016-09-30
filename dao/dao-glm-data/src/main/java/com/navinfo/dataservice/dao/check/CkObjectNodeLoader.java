package com.navinfo.dataservice.dao.check;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;

public class CkObjectNodeLoader {
	
	private static class SingletonHolder {
		private static final CkObjectNodeLoader INSTANCE = new CkObjectNodeLoader();
	}

	public static final CkObjectNodeLoader getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	/**
	 * 存放各个ObjectName对应的CK_OBJECT_NODE
	 */
	private Map<String, CkObjectNode> map = new HashMap<String, CkObjectNode>();
	
	
	public CkObjectNode getObjectNode(String tableName) throws Exception {
		
		if (!map.containsKey(tableName)) {
			synchronized(this) {
				if (!map.containsKey(tableName)) {					
					String sql = "SELECT OBJECT_NAME, MESH_TABLE, MESH_SQL FROM CK_OBJECT_NODE WHERE OBJECT_NAME=?";
					PreparedStatement pstmt = null;
					ResultSet resultSet = null;
					Connection conn = null;
					try {
						conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,tableName);
						resultSet = pstmt.executeQuery();
						if (resultSet.next()) {
							String objectName=resultSet.getString("OBJECT_NAME");
							String meshTable=resultSet.getString("MESH_TABLE");
							String meshSql=resultSet.getString("MESH_SQL");
							CkObjectNode myObject = new CkObjectNode(objectName,meshTable,meshSql);							
							map.put(tableName,myObject);					
						} 
					} catch (Exception e) {
						throw new Exception(e);
					} finally {
						if (resultSet != null) {
							try {
								resultSet.close();
							} catch (Exception e) {}
						}
						if (pstmt != null) {
							try {
								pstmt.close();
							} catch (Exception e) {}
						}
						if (conn != null) {
							try {
								conn.close();
							} catch (Exception e) {}
						}
					}
				}
			}
		}
		return map.get(tableName);
	}

}
