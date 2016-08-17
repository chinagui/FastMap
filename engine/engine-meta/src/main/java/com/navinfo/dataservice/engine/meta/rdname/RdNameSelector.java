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
	
	/**
	 * web端查询rdName
	 * @author wangdongbin
	 * @param params
	 * @param tips
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "static-access", "unchecked" })
	public JSONObject searchForWeb(JSONObject params,JSONArray tips) throws Exception {
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		Connection conn = null;
		try {
			JSONObject result = new JSONObject();
			
			conn = DBConnector.getInstance().getMetaConnection();
			
			JSONObject param =  params.getJSONObject("params");
			String sortby = params.getString("sortby");
			int pageSize = params.getInt("pageSize");
			int pageNum = params.getInt("pageNum");
			
			StringUtils sUtils = new StringUtils();
			
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT * ");
			sql.append(" FROM (SELECT c.*, rownum rn");
			sql.append(" FROM (select * ");
			sql.append(" from rd_name a where 1=1");
			
			String tmep = "";
			if (tips.size()>0) {
				sql.append(" a.SRC_RESUME in (");
				for (int i=0;i<tips.size();i++) {
					JSONObject tipsObj = tips.getJSONObject(i);
					sql.append(tmep);
					tmep = ",";
					sql.append("'");
					sql.append(tipsObj.getString("id"));
					sql.append("'");
				}
				sql.append(")");
			}
			
			
			// 添加过滤器条件
			Iterator<String> keys = param.keys();
			while (keys.hasNext()) {
				String key = keys.next();
				String columnName = sUtils.toColumnName(key);
				if (!param.getString(key).isEmpty()) {
					sql.append(" and a.");
					sql.append(columnName);
					sql.append("='");
					sql.append(param.getString(key));
					sql.append("'");
				}
			}
			
			sql.append(" ORDER BY a.NAME_GROUPID,a.NAME_ID");
			
			
			// 添加排序条件
			if (sortby.length()>0) {
				int index = sortby.indexOf("-");
				if (index != -1) {
					String sortbyName = sUtils.toColumnName(sortby.substring(1));
					sql.append(" , a.");
					sql.append(sortbyName);
					sql.append(" DESC");
				} else {
					String sortbyName = sUtils.toColumnName(sortby.substring(1));
					sql.append(" , a.");
					sql.append(sortbyName);
					sql.append(" ASC");
				}
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
				data.add(result2Json(resultSet));
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
	
	/**
	 * 判断是否存在重复的名称
	 * @author wangdongbin
	 * @param rdName
	 * @return
	 * @throws Exception
	 */
	public JSONObject checkRdNameExists(RdName rdName) throws Exception {
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		Connection conn = null;
		
		// 检查是否存在同一行政区划、同一道路类型（默认是未区分）的相同的道路名数据
		//（对于“行政区划”为“全国”时，不判断是否重名，即允许重复名称记录存在，但NAME_GROUPID不同；）
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM rd_name");
		sb.append(" WHERE name=:1");
		sb.append(" AND road_type=:2");
		sb.append(" AND admin_id=:3");
		// “行政区划”为“全国”
		if (rdName.getAdminId() == 214 && rdName.getNameGroupId() != 0) {
			sb.append(" AND name_groupid=:4");
		}
		try {
			
			conn = DBConnector.getInstance().getMetaConnection();
			
			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setString(1, rdName.getName());

			pstmt.setInt(2, rdName.getRoadType());
			
			pstmt.setInt(3, rdName.getAdminId());
			
			if (rdName.getAdminId() == 214) {
				pstmt.setInt(4, rdName.getNameGroupId());
			}

			resultSet = pstmt.executeQuery();
			
			JSONObject resultObj = new JSONObject();
			
			if (resultSet.next()) {
				resultObj = result2Json(resultSet);
			}
			
			return resultObj;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
			DbUtils.closeQuietly(conn);
		}
	}
	
	
	/**
	 * 将查询结果转为json型
	 * @param resultSet
	 * @return
	 * @throws Exception
	 */
	private JSONObject result2Json(ResultSet resultSet) throws Exception{
		JSONObject rdNameObj = new JSONObject();
		try {
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
//			rdNameObj.put("city", resultSet.getString("CITY"));
			return rdNameObj;
		} catch (Exception e) {
			throw e;
		}
	}
}
