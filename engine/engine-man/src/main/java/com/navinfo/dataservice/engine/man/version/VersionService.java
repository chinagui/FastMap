package com.navinfo.dataservice.engine.man.version;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.springframework.stereotype.Service;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;

@Service
public class VersionService {

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
}
