package com.navinfo.dataservice.engine.meta.rdname;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.util.StringUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

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
	
	
	@SuppressWarnings({ "static-access", "unchecked" })
	public JSONObject searchForWeb(JSONObject params,JSONArray tips) throws Exception {
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		Connection conn = null;
		try {
			JSONObject result = new JSONObject();
			
			conn = DBConnector.getInstance().getMetaConnection();
			
			JSONArray paramList =  params.getJSONArray("params");
			String sortby = params.getString("sortby");
			int pageSize = params.getInt("pageSize");
			int pageNum = params.getInt("pageNum");
			
			StringUtils sUtils = new StringUtils();
			
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT * ");
			sql.append(" FROM (SELECT c.*, rownum rn");
			sql.append(" FROM (select a.NAME_ID,a.NAME_GROUPID,a.LANG_CODE,a.NAME,a.TYPE,a.BASE,a.PREFIX,a.INFIX");
			sql.append(",a.SUFFIX,a.NAME_PHONETIC,a.TYPE_PHONETIC,a.BASE_PHONETIC,a.PREFIX_PHONETIC,a.INFIX_PHONETIC");
			sql.append(",a.SUFFIX_PHONETIC,a.SRC_FLAG,a.ROAD_TYPE,a.ADMIN_ID,a.CODE_TYPE,a.VOICE_FILE,a.SRC_RESUME");
			sql.append(",a.PA_REGION_ID,a.SPLIT_FLAG,a.MEMO,a.ROUTE_ID,a.PROCESS_FLAG,a.CITY");
			sql.append(" from rd_name a where a.name in ()");
			
			// 添加过滤器条件
			for (int i=0;i<paramList.size();i++) {
				JSONObject colum = paramList.getJSONObject(i);
				Iterator<String> keys = colum.keys();
				while (keys.hasNext()) {
					String key = keys.next();
					String columnName = sUtils.toColumnName(key);
					sql.append(" and a.");
					sql.append(columnName);
					sql.append("=");
					sql.append(colum.getString(key));
				}
			}
			
			// 添加排序条件
			int index = sortby.indexOf("-");
			if (index != -1) {
				String sortbyName = sUtils.toColumnName(sortby.substring(1));
				sql.append(" ORDER BY a.");
				sql.append(sortbyName);
				sql.append(" DESC");
			} else {
				String sortbyName = sUtils.toColumnName(sortby);
				sql.append(" ORDER BY a.");
				sql.append(sortbyName);
				sql.append(" ASC");
			}
			
			sql.append(" ) c");
			sql.append(" WHERE rownum <= :1)  WHERE rn >= :2");
			
			int startRow = pageNum * pageSize + 1;

			int endRow = (pageNum + 1) * pageSize;

			pstmt = conn.prepareStatement(sql.toString());

			pstmt.setInt(1, endRow);

			pstmt.setInt(2, startRow);

			resultSet = pstmt.executeQuery();
			
			int total = 0;
			
			List<JSONObject> data = new ArrayList<JSONObject>();
			
			while (resultSet.next()) {
				total++;
				JSONObject rdNameObj = new JSONObject();
				rdNameObj.put("nameId", resultSet.getInt("NAME_ID"));
				rdNameObj.put("nameGroupid", resultSet.getInt("NAME_GROUPID"));
				rdNameObj.put("longCode", resultSet.getString("LANG_CODE"));
				rdNameObj.put("name", resultSet.getString("NAME"));
				rdNameObj.put("type", resultSet.getString("TYPE"));
				rdNameObj.put("base", resultSet.getString("BASE"));
				rdNameObj.put("prefix", resultSet.getString("PREFIX"));
				rdNameObj.put("infix", resultSet.getString("INFIX"));
				rdNameObj.put("suffix", resultSet.getString("SUFFIX"));
				rdNameObj.put("namePhonetic", resultSet.getString("NAME_PHONETIC"));
				rdNameObj.put("typePhonetic", resultSet.getString("TYPE_PHONETIC"));
				rdNameObj.put("basePhonetic", resultSet.getString("BASE_PHONETIC"));
				rdNameObj.put("prefixPhonetic", resultSet.getString("PREFIX_PHONETIC"));
				rdNameObj.put("infixPhonetic", resultSet.getString("INFIX_PHONETIC"));
				rdNameObj.put("suffixPhonetic", resultSet.getString("SUFFIX_PHONETIC"));
				rdNameObj.put("srcFlag", resultSet.getInt("SRC_FLAG"));
				rdNameObj.put("roadType", resultSet.getInt("ROAD_TYPE"));
				rdNameObj.put("adminId", resultSet.getInt("ADMIN_ID"));
				rdNameObj.put("codeType", resultSet.getInt("CODE_TYPE"));
				rdNameObj.put("voiceFile", resultSet.getString("VOICE_FILE"));
				rdNameObj.put("srcResume", resultSet.getString("SRC_RESUME"));
				rdNameObj.put("paRegionId", resultSet.getInt("PA_REGION_ID"));
				rdNameObj.put("splitFlag", resultSet.getInt("SPLIT_FLAG"));
				rdNameObj.put("memo", resultSet.getString("MEMO"));
				rdNameObj.put("routeId", resultSet.getInt("ROUTE_ID"));
				rdNameObj.put("processFlag", resultSet.getInt("PROCESS_FLAG"));
				rdNameObj.put("city", resultSet.getString("CITY"));
				data.add(rdNameObj);
			}
			result.put("total", total);
			result.put("data", data);
			return result;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
			DbUtils.closeQuietly(conn);
		}
	}
}
