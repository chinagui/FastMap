package com.navinfo.dataservice.engine.man.version;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class VersionSelector {

	private Connection conn;

	public VersionSelector(Connection conn) {
		this.conn = conn;
	}

	public String getByType(int type) throws Exception {

		String sql = "select type, version from  VERSION where type=:1";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
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


		}

	}

	public JSONArray getList() throws Exception {

		JSONArray array = new JSONArray();

		String sql = "select type, version from  VERSION";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
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


		}
		return array;
	}
}
