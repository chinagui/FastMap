package com.navinfo.dataservice.engine.man.version;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.QueryRunner;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service
public class VersionService {
	Logger log=LoggerRepos.getLogger(this.getClass());

	public String query(int type) throws Exception {

		String sql = "select type, version from app_data_version where type=:1";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		Connection conn = null;

		try {

			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();

			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, type);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {

				String version = resultSet.getString("version");

				return version;
			}

			return null;
		} catch (Exception e) {

			throw new Exception(e);

		} finally {
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (Exception e) {

				}
			}

			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (Exception e) {

				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {

				}
			}
		}

	}

	public JSONArray getList() throws Exception {

		JSONArray array = new JSONArray();

		String sql = "select type, version from  VERSION";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		Connection conn = null;

		try {

//			conn = DBConnector.getInstance().getManConnection();
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			
			pstmt = conn.prepareStatement(sql);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				String version = resultSet.getString("version");

				int type = resultSet.getInt("type");

				JSONObject json = new JSONObject();

				json.put("type", type);

				json.put("specVersion", version);

				array.add(json);
			}

		} catch (Exception e) {

			throw new Exception(e);

		} finally {
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (Exception e) {

				}
			}

			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (Exception e) {

				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {

				}
			}
		}
		return array;
	}

	public Map<String, Object> getAppVersion(String appPlatform, int appType) throws Exception {
		Connection conn=null;
		try{
			conn=DBConnector.getInstance().getManConnection();
			String sql="SELECT APP_NAME,"
					+ "       APP_PLATFORM,"
					+ "       APP_VERSION,"
					+ "       DOWN_URL,"
					+ "       RELEASE_DATE,"
					+ "       APP_SIZE,"
					+ "       UPGRADE_LEVEL,"
					+ "       RELEASE_NOTE,"
					+ "       APP_TYPE"
					+ "  FROM APP_VERSION A"
					+ " WHERE A.APP_PLATFORM = '"+appPlatform+"'"
					+ "   AND A.APP_TYPE = "+appType
					+ " ORDER BY A.RELEASE_DATE DESC";
			QueryRunner run=new QueryRunner();			
			Map<String, Object> appVersion=run.query(conn, sql, new ResultSetHandler<Map<String, Object>>(){

				@Override
				public Map<String, Object> handle(ResultSet rs)
						throws SQLException {
					if(rs.next()){
						Map<String, Object> appVersion=new HashMap<String, Object>();
						appVersion.put("appName", rs.getString("APP_NAME"));
						appVersion.put("appPlatform", rs.getString("APP_PLATFORM"));
						appVersion.put("appVersion", rs.getString("APP_VERSION"));
						appVersion.put("downUrl", rs.getString("DOWN_URL"));
						appVersion.put("appSize", rs.getString("APP_SIZE"));
						appVersion.put("releaseNote", rs.getString("RELEASE_NOTE"));
						appVersion.put("upgradeLevel", rs.getInt("UPGRADE_LEVEL"));
						appVersion.put("appType", rs.getInt("APP_TYPE"));
						return appVersion;
					}
					return null;
				}
				
			});
			return appVersion;
		}catch(Exception e){
			log.error("", e);
			DbUtils.rollbackAndCloseQuietly(conn);
			throw e;
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
}
