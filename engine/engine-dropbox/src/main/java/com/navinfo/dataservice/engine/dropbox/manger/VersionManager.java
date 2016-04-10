package com.navinfo.dataservice.engine.dropbox.manger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.navinfo.dataservice.engine.dropbox.dao.DBConnector;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class VersionManager {

	public String getByType(int type) throws Exception {
		
		String sql = "select type, version from  VERSION where type=:1";

		Connection conn = null;
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			conn = DBConnector.getInstance().getConnection();
			
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
		
		Connection conn = null;

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			conn = DBConnector.getInstance().getConnection();
			
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
