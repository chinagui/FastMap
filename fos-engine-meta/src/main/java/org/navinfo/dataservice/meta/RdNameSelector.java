package org.navinfo.dataservice.meta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.navinfo.dataservice.commons.db.OracleAddress;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class RdNameSelector {

	private Connection conn;

	public RdNameSelector(Connection conn) {
		this.conn = conn;
	}

	public JSONArray searchByName(String name, int pageSize, int pageNum)
			throws Exception {
		JSONArray array = new JSONArray();

		String sql = "SELECT *   FROM (SELECT c.*, rownum rn           FROM (select a.name_id, a.name, b.province   from rd_name a, cp_meshlist b  where a.name like :1 and a.admin_id = b.admincode    ) c          WHERE rownum <= :2)  WHERE rn >= :3";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		int startRow = pageNum * pageSize + 1;

		int endRow = (pageNum + 1) * pageSize;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, name + "%");

			pstmt.setInt(2, endRow);

			pstmt.setInt(3, startRow);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				int nameId = resultSet.getInt("name_id");

				String nameStr = resultSet.getString("name");

				String province = resultSet.getString("province");

				JSONObject json = new JSONObject();

				json.put("nameId", nameId);

				json.put("name", nameStr);

				json.put("province", province);

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

	public static void main(String[] args) throws Exception {
		String username1 = "mymeta3";

		String password1 = "mymeta3";

		int port1 = 1521;

		String ip1 = "192.168.4.131";

		String serviceName1 = "orcl";

		OracleAddress oa1 = new OracleAddress(username1, password1, port1, ip1,
				serviceName1);

		RdNameSelector selector = new RdNameSelector(oa1.getConn());

		System.out.println(selector.searchByName("望", 10, 1));
	}
}
