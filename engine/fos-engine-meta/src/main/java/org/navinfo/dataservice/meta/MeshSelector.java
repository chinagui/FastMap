package org.navinfo.dataservice.meta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.db.OracleAddress;
import com.navinfo.dataservice.commons.util.MeshUtils;

public class MeshSelector {

	private Connection conn;

	public MeshSelector(Connection conn) {
		this.conn = conn;
	}

	public JSONObject getProvinceByLocation(double lon, double lat)
			throws Exception {

		String meshId = MeshUtils.lonlat2Mesh(lon, lat);

		String sql = "select admincode,province from cp_meshlist where mesh = :1";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
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


		}

		return null;
	}

	public static void main(String[] args) throws Exception {

		String username1 = "mymeta3";

		String password1 = "mymeta3";

		int port1 = 1521;

		String ip1 = "192.168.4.131";

		String serviceName1 = "orcl";

		OracleAddress oa1 = new OracleAddress(username1, password1, port1, ip1,
				serviceName1);

		MeshSelector selector = new MeshSelector(oa1.getConn());

		System.out.println(selector.getProvinceByLocation(115.57763, 39.92789));
	}
}
