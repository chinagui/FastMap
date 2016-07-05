package com.navinfo.dataservice.engine.meta.rdname;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.commons.dbutils.DbUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;

public class RdNameSelector {

	public JSONObject searchByName(String name, int pageSize, int pageNum)
			throws Exception {

		JSONObject result = new JSONObject();

		JSONArray array = new JSONArray();

		int total = 0;

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		Connection conn = null;

		try {

			conn = DBConnector.getInstance().getMetaConnection();

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

			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {

				}
			}
		}

	}

	/**
	 * @Description:判断名称是否存在
	 * @param name
	 * @param adminId
	 * @return 所在行政区划
	 * @throws Exception
	 * @author: y
	 * @time:2016-6-28 下午3:42:20
	 */
	public int isNameExists(String name, int adminId) throws Exception {
		int resultAdmin = 0;

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		Connection conn = null;

		String sql = "SELECT N.ADMIN_ID									\n"
				+ "  FROM RD_NAME N                                      \n"
				+ " WHERE N.NAME = ?                                     \n"
				+ "   AND (N.ADMIN_ID = ? OR N.ADMIN_ID = 214)           \n";

		try {

			conn = DBConnector.getInstance().getMetaConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, name);
			pstmt.setInt(2, adminId);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				resultAdmin = resultSet.getInt("ADMIN_ID");
			}

		} catch (Exception e) {

			throw new Exception(e);
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
			DbUtils.closeQuietly(conn);
		}

		return resultAdmin;
	}

	public static void main(String[] args) throws Exception {

		RdNameSelector selector = new RdNameSelector();

		System.out.println(selector.searchByName("", 10, 1));
	}
}
