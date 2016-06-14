package org.navinfo.dataservice.engine.meta.mesh;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.navicommons.geo.computation.MeshUtils;

public class MeshSelector {

	public JSONObject getProvinceByLocation(double lon, double lat)
			throws Exception {

		String meshId = MeshUtils.point2Meshes(lon, lat)[0];

		String sql = "select admincode,province from cp_meshlist where mesh = :1";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		Connection conn = null;

		try {

			conn = DBConnector.getInstance().getMetaConnection();
			
			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, meshId);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {

				int admincode = resultSet.getInt("admincode");
				
				String province = resultSet.getString("province");

				JSONObject json = new JSONObject();
				
				json.put("id", admincode/10000);
				
				json.put("name", province);

				return json;
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

		return null;
	}

	public static void main(String[] args) throws Exception {

		MeshSelector selector = new MeshSelector();

		System.out.println(selector.getProvinceByLocation(115.57763, 39.92789));
	}
}
