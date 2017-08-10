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
	public JSONObject getNameType(int pageNum,int pageSize,String name,String sortby) throws Exception {
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		Connection conn = null;
		
		JSONObject result = new JSONObject();
		
		String sql = "SELECT * FROM (SELECT c.*, rownum rn FROM (SELECT COUNT (1) OVER (PARTITION BY 1) total,s.* from SC_ROADNAME_TYPENAME s ";
		if (!name.isEmpty()) {
			sql +=  " where s.name like '%"+name+"%'";
		}
		if (!sortby.isEmpty()) {
			sql += " ORDER BY s."+sortby;
		}
		sql +=	")c WHERE rownum<= :1) WHERE rn>= :2";
		
		try {
			conn = DBConnector.getInstance().getMetaConnection();
			
			int startRow = (pageNum-1) * pageSize + 1;

			int endRow = pageNum * pageSize;
			
			pstmt = conn.prepareStatement(sql);
			
			pstmt.setInt(1, endRow);

			pstmt.setInt(2, startRow);
			
			resultSet = pstmt.executeQuery();
			
			JSONArray dataList = new JSONArray();
			
			int total = 0;
			
			while (resultSet.next()) {
				if (total == 0) {
					total = resultSet.getInt("total");
				}
				JSONObject data = new JSONObject();
				data.put("id", resultSet.getInt("id"));
				data.put("name", resultSet.getString("name"));
				data.put("py", resultSet.getString("py"));
				data.put("englishname", resultSet.getString("englishname"));
				data.put("regionFlag", resultSet.getString("region_flag"));
				data.put("langCode", resultSet.getString("lang_code"));
				dataList.add(data);
			}
			
			result.put("total", total);
			result.put("data", dataList);
			
			return result;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(conn, pstmt, resultSet);
		}
	}

}
