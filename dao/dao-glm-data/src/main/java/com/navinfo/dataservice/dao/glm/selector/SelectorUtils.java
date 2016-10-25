package com.navinfo.dataservice.dao.glm.selector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.navicommons.database.sql.DBUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class SelectorUtils {

	private Connection conn;

	public SelectorUtils(Connection conn) {
		this.conn = conn;
	}

	public JSONObject loadByElementCondition(JSONObject object, String tableName, int pageSize, int pageNum,
			boolean isLock) throws Exception {

		JSONObject result = new JSONObject();
		JSONArray array = new JSONArray();
		int total = 0;
		int startRow = (pageNum - 1) * pageSize + 1;
		int endRow = pageNum * pageSize;
		String sql = "";
		StringBuilder bufferCondition = new StringBuilder();
		if (tableName.equals(ObjType.RDLINK.toString())) {
			if (object.containsKey("name")) {
				bufferCondition
						.append("with tmp1 as ( select lang_code,name_groupid,name from rd_name where name like '%"
								+ object.getString("name")
								+ "%' and u_record !=2) ,tmp2 AS (	SELECT /*+ leading(iln,tmp1) use_hash(iln,tmp1)*/ rln.link_pid pid,tmp1.name FROM rd_link_name rln,tmp1 WHERE rln.name_class=1 AND tmp1.name_groupid = rln.name_groupId and rln.u_record !=2 ) ,tmp3 AS (	SELECT count(*) over () total,tmp2.*, ROWNUM rn FROM tmp2 ) ,tmp4 as ( select * from tmp3 where ROWNUM <=:1 ) SELECT * FROM tmp4 WHERE rn >=:2");
				sql = bufferCondition.toString();
			} else {
				bufferCondition
						.append("SELECT  COUNT(1) OVER(PARTITION BY 1) TOTAL, tmp.pid,rn.name FROM( SELECT /*index(tmpLink)*/ tmpLink.LINK_PID PID ,rln.name_groupid  FROM rd_link tmpLink LEFT JOIN RD_LINK_NAME RLN ON tmpLink.Link_Pid = rln.link_pid AND RLN.NAME_CLASS = 1 and RLN.u_record !=2 where tmpLink.LINK_PID = "
								+ object.getString("linkPid")
								+ " and tmpLink.u_record !=2 GROUP BY tmpLink.LINK_PID,rln.name_groupid )tmp LEFT JOIN RD_NAME rn ON tmp.name_groupid = rn.name_groupid AND  RN.LANG_CODE = 'CHI' and rn.u_record !=2");

				sql = getSqlFromBufferCondition(bufferCondition, isLock);
			}
		}
		if (tableName.equals(ObjType.IXPOI.toString())) {
			if (object.containsKey("name")) {
				bufferCondition.append(
						" select COUNT (1) OVER (PARTITION BY 1) total,ipn.poi_pid pid,ipn.name from ix_poi_name ipn where ipn.name_class=1 and ipn.name_type =2 and ipn.lang_code = 'CHI' ");
				bufferCondition.append(" and ipn.name like '%" + object.getString("name") + "%' ");
				sql = getSqlFromBufferCondition(bufferCondition, isLock);
			} else {
				bufferCondition
						.append("SELECT COUNT (1) OVER (PARTITION BY 1) total,tmp.pid,tmp.name FROM(select poi.pid,ipn.name from( SELECT ix.pid FROM ix_poi ix where ix.pid ="+object.getInt("pid")+" and ix.U_RECORD !=2 union all select ix.pid from ix_poi ix,poi_edit_status ps where ix.PID = "+object.getInt("pid")+" and ix.U_RECORD = 2 and ix.ROW_ID = ps.ROW_ID and ps.STATUS <3)poi LEFT JOIN ix_poi_name ipn ON poi.pid = ipn.poi_pid AND ipn.name_class=1 AND ipn.name_type =2 AND ipn.lang_code = 'CHI' "+")tmp");
				sql = getSqlFromBufferCondition(bufferCondition, isLock);

			}
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(sql);
			System.out.println(sql);
			
			pstmt.setInt(1, endRow);
			pstmt.setInt(2, startRow);
			resultSet = pstmt.executeQuery();
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

	private String getSqlFromBufferCondition(StringBuilder bufferCondition, boolean isLock) {
		StringBuilder buffer = new StringBuilder();
		buffer.append(" SELECT * ");
		buffer.append(" FROM (SELECT c.*, ROWNUM rn ");
		buffer.append(" FROM ( " + bufferCondition.toString() + "");

		buffer.append(" ) c");
		buffer.append(" WHERE ROWNUM <= :1) ");
		buffer.append("  WHERE rn >= :2 ");
		if (isLock) {
			buffer.append(" for update nowait");
		}

		return buffer.toString();
	}

	/**
	 * 获取顶级主表表名（履历中的obNm），针对对象冲突的特殊处理
	 * 
	 * @param row
	 * @return
	 */
	public static String getObjTableName(IRow row) {

		String rowTableName = row.tableName().toUpperCase();

		String rowParentTableName = row.parentTableName().toUpperCase();

		switch (rowTableName) {
		// 交限
		case "RD_ RESTRICTION_VIA":
			return "RD_RESTRICTION";
		case "RD_RESTRICTION_CONDITION":
			return "RD_RESTRICTION";
		// 分歧
		case "RD_BRANCH_NAME":
			return "RD_BRANCH";
		case "RD_SIGNBOARD_NAME":
			return "RD_BRANCH";
		// 车信
		case "RD_LANE_VIA":
			return "RD_LANE_CONNEXITY";
		// 语音引导
		case "RD_VOICEGUIDE_VIA":
			return "RD_VOICEGUIDE";
		// POI
		case "IX_POI_NAME_TONE":
			return "IX_POI";
		case "IX_SAMEPOI":
			return "IX_POI";
		case "IX_SAMEPOI_PART":
			return "IX_POI";
		case "IX_POI_CHILDREN":
			return "IX_POI";
		// 铁路
		case "RW_LINK":
			return "RW_LINK";
		// 土地覆盖
		case "LC_FACE":
			return "LC_FACE";
		// 土地利用
		case "LU_FACE":
			return "LU_FACE";
		default:
			return rowParentTableName;
		}
	}
}
