package com.navinfo.dataservice.engine.meta.rdname;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ScRoadnameTypename {
	
	/**
	 * 获取类型名称
	 * @return
	 * @throws Exception
	 */
	public JSONArray getNameType(int pageNum,int pageSize) throws Exception {
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		Connection conn = null;
		
		String sql = "SELECT * FROM (SELECT c.*, rownum rn FROM (SELECT * from SC_ROADNAME_TYPENAME)c WHERE rownum<= :1) WHERE rn>= :2";
		
		try {
			conn = DBConnector.getInstance().getMetaConnection();
			
			int startRow = pageNum * pageSize + 1;

			int endRow = (pageNum + 1) * pageSize;
			
			pstmt = conn.prepareStatement(sql);
			
			pstmt.setInt(1, endRow);

			pstmt.setInt(2, startRow);
			
			resultSet = pstmt.executeQuery();
			
			JSONArray result = new JSONArray();
			
			while (resultSet.next()) {
				JSONObject data = new JSONObject();
				data.put("id", resultSet.getInt("id"));
				data.put("name", resultSet.getString("name"));
				data.put("py", resultSet.getString("py"));
				data.put("englishname", resultSet.getString("englishname"));
				data.put("regionFlag", resultSet.getString("region_flag"));
				data.put("langCode", resultSet.getString("lang_code"));
				result.add(data);
			}
			
			return result;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
			DbUtils.closeQuietly(conn);
		}
	}

}
