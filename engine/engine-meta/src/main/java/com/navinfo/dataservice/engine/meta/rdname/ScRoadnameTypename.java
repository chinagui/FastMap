package com.navinfo.dataservice.engine.meta.rdname;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ScRoadnameTypename {
	
	private Logger log = LoggerRepos.getLogger(this.getClass());
	
	/**
	 * 获取类型名称
	 * @return
	 * @throws Exception
	 */
	public JSONArray getNameType() throws Exception {
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		Connection conn = null;
		
		String sql = "SELECT * from SC_ROADNAME_TYPENAME";
		
		try {
			conn = DBConnector.getInstance().getMetaConnection();
			
			pstmt = conn.prepareStatement(sql);
			
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
