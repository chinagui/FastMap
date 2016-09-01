package com.navinfo.dataservice.dao.glm.selector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.navicommons.database.sql.DBUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class SelectorUtils {

	private Connection conn;

	public SelectorUtils(Connection conn) {
		this.conn = conn;
	}

	public JSONObject loadByElementCondition(JSONObject object,
			String tableName, int pageSize, int pageNum, boolean isLock)
			throws Exception {

		JSONObject result = new JSONObject();
		JSONArray array = new JSONArray();
		int total = 0;
		int startRow = (pageNum - 1) * pageSize + 1;
		int endRow = pageNum * pageSize;
		StringBuilder buffer = new StringBuilder();
		StringBuilder bufferCondition = new StringBuilder();
		if (tableName.equals(ObjType.RDLINK.toString())) {
			if (object.containsKey("name")) {
				bufferCondition
				.append(" select  /*+ leading(iln,rn) use_hash(iln,rn)*/  COUNT (1) OVER (PARTITION BY 1) total,rln.link_pid pid,rn.name from rd_link_name rln,rd_name rn where rln.name_class=1 and rn.lang_code = 'CHI' and   rn.name_groupid = rln.name_groupId");
				bufferCondition.append(" and rn.name like '%"
						+ object.getString("name") + "%' ");
			} else {
				bufferCondition.append("SELECT  COUNT(1) OVER(PARTITION BY 1) TOTAL, tmp.pid,rn.name FROM( SELECT /*index(tmpLink)*/ tmpLink.LINK_PID PID ,rln.name_groupid  FROM rd_link tmpLink LEFT JOIN RD_LINK_NAME RLN ON tmpLink.Link_Pid = rln.link_pid AND RLN.NAME_CLASS = 1 where tmpLink.LINK_PID = "+object.getString("linkPid")+" GROUP BY tmpLink.LINK_PID,rln.name_groupid )tmp LEFT JOIN RD_NAME rn ON tmp.name_groupid = rn.name_groupid AND  RN.LANG_CODE = 'CHI'");
			}
		}
		if (tableName.equals(ObjType.IXPOI.toString())) {
			if (object.containsKey("name")) {
				bufferCondition
				.append(" select COUNT (1) OVER (PARTITION BY 1) total,ipn.poi_pid pid,ipn.name from ix_poi_name ipn where ipn.name_class=1 and ipn.name_type =2 and ipn.lang_code = 'CHI' ");
				bufferCondition.append(" and ipn.name like '%"
						+ object.getString("name") + "%' ");
			} else {
				bufferCondition.append("SELECT COUNT (1) OVER (PARTITION BY 1) total,tmp.pid,tmp.name from( select ix.pid,ipn.name from ix_poi ix left join ix_poi_name ipn on ix.pid = ipn.poi_pid and ipn.name_class=1 AND ipn.name_type =2 AND ipn.lang_code = 'CHI' where ix.pid = "+object.getString("pid")+" )tmp");
			}
		}

		buffer.append(" SELECT * ");
		buffer.append(" FROM (SELECT c.*, ROWNUM rn ");
		buffer.append(" FROM ( " + bufferCondition.toString() + "");

		buffer.append(" ) c");
		buffer.append(" WHERE ROWNUM <= :1) ");
		buffer.append("  WHERE rn >= :2 ");
		if (isLock) {
			buffer.append(" for update nowait");
		}
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(buffer.toString());
			pstmt.setInt(1, endRow);
			pstmt.setInt(2, startRow);
			resultSet = pstmt.executeQuery();
			System.out.println(buffer.toString());
			while (resultSet.next()) {
				if (total == 0) {
					total = resultSet.getInt("total");
				}
				JSONObject json = new JSONObject();
				json.put("pid", resultSet.getInt("pid"));
				json.put("name", resultSet.getString("name"));
				json.put("type", tableName);

				array.add(json);
			}
			result.put("total", total);

			result.put("rows", array);

			return result;
		} catch (Exception e) {

			throw e;

		} finally {

			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);

		}
	}

	public static void main(String[] args) throws Exception {
		int startRow = 1;
		int endRow = 1 * 5;
		String tableName = "RDLINK";
		JSONObject object = new JSONObject();
		object.put("name", "北京");
		StringBuilder buffer = new StringBuilder();
		StringBuilder bufferCondition = new StringBuilder();
		if (tableName.equals(ObjType.RDLINK.toString())) {
			bufferCondition
					.append(" select  /*+ leading(iln,rn) use_hash(iln,rn)*/  COUNT (1) OVER (PARTITION BY 1) total,rln.link_pid,rn.name from rd_link_name rln,rd_name rn where rln.name_class=1 and rn.lang_code = 'CHI' and   rn.name_groupid = rln.name_groupId");
			if (object.containsKey("name")) {
				bufferCondition.append(" and rn.name like '%"
						+ object.getString("name") + "%' ");
			} else {
				bufferCondition.append(" and  rln.link_pid = "
						+ object.getString("linkPid") + " ");
			}
		}
		if (tableName.equals(ObjType.IXPOI.toString())) {
			bufferCondition
					.append(" select COUNT (1) OVER (PARTITION BY 1) total,ipn.poi_pid,ipn.name from ix_poi_name ipn where ipn.name_class=1 and ipn.name_type =2 and ipn.lang_code = 'CHI' ");
			if (object.containsKey("name")) {
				bufferCondition.append(" and ipn.name like '%"
						+ object.getString("name") + "%' ");
			} else {
				bufferCondition.append(" and ipn.poi_pid = "
						+ object.getString("pid") + " ");
			}
		}

		buffer.append(" SELECT * ");
		buffer.append(" FROM (SELECT c.*, ROWNUM rn ");
		buffer.append(" FROM ( " + bufferCondition.toString() + "");

		buffer.append(" ) c");
		buffer.append(" WHERE ROWNUM <= :1) ");
		buffer.append("  WHERE rn >= :2 ");
		System.out.println(buffer.toString());
	}

}
