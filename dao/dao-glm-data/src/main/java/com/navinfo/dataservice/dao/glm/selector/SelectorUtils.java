package com.navinfo.dataservice.dao.glm.selector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.bizcommons.glm.GlmGridCalculator;
import com.navinfo.dataservice.bizcommons.glm.GlmGridCalculatorFactory;
import com.navinfo.dataservice.bizcommons.glm.GlmGridRefInfo;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.utils.TableNameFactory;
import com.navinfo.dataservice.dao.glm.utils.TableNameSqlInfo;
import com.navinfo.navicommons.database.sql.DBUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

public class SelectorUtils {

	private Connection conn;

	private GlmGridCalculator gridCalculator;

	public SelectorUtils(Connection conn) {
		this.conn = conn;
		String gdbVersion = SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion);

		this.gridCalculator = GlmGridCalculatorFactory.getInstance().create(gdbVersion);
	}

	public JSONObject loadByElementCondition(JSONObject object, ObjType objType, int pageSize, int pageNum,
			boolean isLock) throws Exception {

		TableNameFactory nameFactory = TableNameFactory.getInstance();

		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		JSONObject result = new JSONObject();
		JSONArray array = new JSONArray();
		String key = object.keys().next().toString();

		String sql = "";

		if (objType == ObjType.RDLINK || objType == ObjType.IXPOI) {
			sql = getSearchSqlFromLinkPOI(objType, object, isLock);
		} else {

			int pid = object.getInt(key);

			String columnName = StringUtils.toColumnName(key);

			String tableName = ReflectionAttrUtils.getTableNameByObjType(objType);

			StringBuilder fromSql = new StringBuilder(" FROM ");

			StringBuilder innerLeftJoinSql = new StringBuilder();

			StringBuilder selectSql = new StringBuilder("SELECT COUNT (1) OVER (PARTITION BY 1) total,tmp.pid,");

			// 获取关联名称的sql
			TableNameSqlInfo sqlInfo = nameFactory.getSqlInfoByTableName(tableName);

			if (sqlInfo != null && StringUtils.isNotEmpty(sqlInfo.getOutSelectCol())) {
				selectSql.append(sqlInfo.getOutSelectCol());
			} else {
				selectSql.append("'' as name");
			}

			String tmpSql = ",tmp.geometry from(" + "SELECT P." + columnName + " AS PID,";

			selectSql.append(tmpSql);

			if (sqlInfo != null && StringUtils.isNotEmpty(sqlInfo.getSelectColumn())) {
				selectSql.append(sqlInfo.getSelectColumn() + " as name,");

				if (StringUtils.isNotEmpty(sqlInfo.getLeftJoinSql())) {
					innerLeftJoinSql.append(" LEFT JOIN " + sqlInfo.getLeftJoinSql());
				}
			}

			StringBuilder whereSql = new StringBuilder();

			GlmGridRefInfo glmGridRefInfo = gridCalculator.getGlmGridRefInfo(tableName);

			if (CollectionUtils.isNotEmpty(glmGridRefInfo.getRefInfo())
					|| glmGridRefInfo.getTableName().equals("RD_CROSS")) {
				selectSql.append("R1.GEOMETRY ");
			} else {
				selectSql.append("P.GEOMETRY ");
			}

			// 解析glm grid map,获取计算geometry的sql
			String editQuerySql = glmGridRefInfo.getEditQuerySql();

			int fromIndex = editQuerySql.indexOf("FROM");

			int whereIndex = editQuerySql.indexOf("WHERE");

			String fromTableSql = editQuerySql.substring(fromIndex + 4, whereIndex);

			fromSql.append(fromTableSql);

			String whereCondition = editQuerySql.substring(whereIndex);

			whereSql.append(
					" " + whereCondition + " AND P." + ReflectionAttrUtils.getBranchPrimaryKey(objType, columnName)
							+ "= " + pid + " AND P.U_RECORD !=2 ");

			StringBuilder outerLeftJoinSql = new StringBuilder();

			if (sqlInfo != null && StringUtils.isNotEmpty(sqlInfo.getOutLeftJoinSql())) {
				outerLeftJoinSql.append(" LEFT JOIN " + sqlInfo.getOutLeftJoinSql());
			}

			selectSql.append(fromSql).append(innerLeftJoinSql).append(whereSql).append(")tmp").append(outerLeftJoinSql);

			sql = getSqlFromBufferCondition(selectSql, false);
		}

		try {
			pstmt = conn.prepareStatement(sql);

			int total = 0;
			int startRow = (pageNum - 1) * pageSize + 1;
			int endRow = pageNum * pageSize;

			pstmt.setInt(1, endRow);
			pstmt.setInt(2, startRow);
			resultSet = pstmt.executeQuery();
			List<String> dataStr = new ArrayList<>();
			while (resultSet.next()) {
				if (total == 0) {
					total = resultSet.getInt("total");
				}
				JSONObject json = new JSONObject();
				//连续分歧返回rowId
				String rowId = null;
				int pid = 0;
				if (key.equals("rowId")) {
					rowId = resultSet.getString("pid");
				} else {
					pid = resultSet.getInt("pid");
				}
				json.put("pid", rowId==null?pid:rowId);
				STRUCT struct = (STRUCT) resultSet.getObject("geometry");
				json.put("geometry", GeoTranslator.jts2Geojson(GeoTranslator.struct2Jts(struct)));
				String name = resultSet.getString("name");
				if (name == null) {
					name = "";
				}
				json.put("name", name);
				json.put("type", objType);
				String jsonStr = (rowId==null?pid:rowId) + "," + name + "," + objType;
				if (dataStr.contains(jsonStr)) {
					total = total - 1;
				} else {
					array.add(json);
					dataStr.add(jsonStr);
				}
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

	private String getSearchSqlFromLinkPOI(ObjType objType, JSONObject object, boolean isLock) {
		String sql = "";
		StringBuilder bufferCondition = new StringBuilder();
		if (objType == ObjType.RDLINK) {
			if (object.containsKey("name")) {
				bufferCondition
						.append("with tmp1 as ( select lang_code,name_groupid,name from rd_name where name like '%"
								+ object.getString("name")
								+ "%' and u_record !=2) ,tmp2 AS (	SELECT /*+ index(r1)*/ rln.link_pid pid,rl.geometry AS geometry,tmp1.name FROM rd_link_name rln,tmp1,rd_link rl WHERE rln.name_class=1 AND rln.link_pid = rl.link_pid and tmp1.name_groupid = rln.name_groupId AND rln.u_record !=2 ) ,tmp3 AS (	SELECT count(*) over () total,tmp2.*, ROWNUM rn FROM tmp2 ) ,tmp4 as ( select * from tmp3 where ROWNUM <=:1 ) SELECT * FROM tmp4 WHERE rn >=:2");
				sql = bufferCondition.toString();
			} else {
				bufferCondition
						.append("SELECT COUNT(1) OVER(PARTITION BY 1) TOTAL, TMP.PID, RN.NAME,TMP.geometry FROM (SELECT /*index(tmpLink)*/ TMPLINK.LINK_PID PID, RLN.NAME_GROUPID,TMPLINK.geometry FROM RD_LINK TMPLINK LEFT JOIN RD_LINK_NAME RLN ON TMPLINK.LINK_PID = RLN.LINK_PID AND RLN.NAME_CLASS = 1 AND RLN.U_RECORD != 2 and RLN.SEQ_NUM  =1 WHERE TMPLINK.LINK_PID = "
								+ object.getString("linkPid")
								+ " AND TMPLINK.U_RECORD != 2 ) TMP LEFT JOIN RD_NAME RN ON TMP.NAME_GROUPID = RN.NAME_GROUPID AND RN.LANG_CODE = 'CHI' AND RN.U_RECORD != 2");

				sql = getSqlFromBufferCondition(bufferCondition, isLock);
			}
		}
		if (objType == ObjType.IXPOI) {
			if (object.containsKey("name")) {
				bufferCondition.append(
						" select COUNT (1) OVER (PARTITION BY 1) total,ipn.poi_pid pid,ipn.name,poi.geometry from ix_poi_name ipn,ix_poi poi WHERE ipn.poi_pid = poi.pid and ipn.name_class=1 and ipn.name_type =2 and ipn.lang_code = 'CHI' ");
				bufferCondition.append(" and ipn.name like '%" + object.getString("name") + "%' ");
				sql = getSqlFromBufferCondition(bufferCondition, isLock);
			} else {
				bufferCondition.append("SELECT COUNT (1) OVER (PARTITION BY 1) total,tmp.pid,tmp.name,tmp.geometry "
						+ "FROM(select poi.pid,ipn.name,poi.geometry from( SELECT ix.pid,ix.geometry FROM ix_poi ix "
						+ "where ix.pid =" + object.getInt("pid") + " and ix.U_RECORD !=2 " + "union all "
						+ "select ix.pid,ix.geometry from ix_poi ix,poi_edit_status ps " + "where ix.PID = "
						+ object.getInt("pid") + " and ix.U_RECORD = 2 " + "and ix.PID = ps.PID and ps.STATUS <3)poi "
						+ "LEFT JOIN ix_poi_name ipn ON poi.pid = ipn.poi_pid "
						+ "AND ipn.name_class=1 AND ipn.name_type =2 AND ipn.lang_code = 'CHI' " + ")tmp");
				sql = getSqlFromBufferCondition(bufferCondition, isLock);

			}
		}
		return sql;
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
		System.out.println(buffer.toString());
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
