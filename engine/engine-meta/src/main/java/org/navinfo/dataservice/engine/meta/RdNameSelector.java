package org.navinfo.dataservice.engine.meta;

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

	public JSONObject searchByName(String name, int pageSize, int pageNum)
			throws Exception {

		JSONObject result = new JSONObject();

		JSONArray array = new JSONArray();

		int total = 0;

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {

			if (name.length() == 0) {
				result.put("total", total);

				result.put("data", array);

				return result;
			}

			String sql = "SELECT *   FROM (SELECT c.*, rownum rn           FROM (select  count(1) over(partition by 1) total,        a.name_groupid,        a.name,        b.province   from rd_name a, cp_provincelist b  where a.name like :1    and a.admin_id = b.admincode) c          WHERE rownum <= :2)  WHERE rn >= :3";

			int startRow = pageNum * pageSize + 1;

			int endRow = (pageNum + 1) * pageSize;

			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, name + "%");

			pstmt.setInt(2, endRow);

			pstmt.setInt(3, startRow);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				if (total == 0) {
					total = resultSet.getInt("total");
				}

				int nameId = resultSet.getInt("name_groupid");

				String nameStr = resultSet.getString("name");

				String province = resultSet.getString("province");

				JSONObject json = new JSONObject();

				json.put("nameId", nameId);

				json.put("name", nameStr);

				json.put("province", province);

				array.add(json);
			}

			result.put("total", total);

			result.put("data", array);

			return result;
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

	public static void main(String[] args) throws Exception {
		String username1 = "mymeta3";

		String password1 = "mymeta3";

		int port1 = 1521;

		String ip1 = "192.168.4.131";

		String serviceName1 = "orcl";

		OracleAddress oa1 = new OracleAddress(username1, password1, port1, ip1,
				serviceName1);

		RdNameSelector selector = new RdNameSelector(oa1.getConn());

		System.out.println(selector.searchByName("", 10, 1));
	}
}
