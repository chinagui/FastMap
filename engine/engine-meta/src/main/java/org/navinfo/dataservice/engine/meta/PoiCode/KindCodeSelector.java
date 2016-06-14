package org.navinfo.dataservice.engine.meta.PoiCode;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.dbutils.ResultSetHandler;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.navicommons.database.QueryRunner;

public class KindCodeSelector {

	/**
	 * 用SC_POINT_POICODE_NEW的 CLASS_CODE去重，id与code均为CLASS_CODE，name为CLASS_NAME
	 * 参数：无
	 * 
	 * @return id、code、name
	 * @throws Exception
	 */
	public JSONArray queryTopKindInfo() throws Exception {

		Connection conn = null;

		QueryRunner run = new QueryRunner();

		conn = DBConnector.getInstance().getMetaConnection();

		StringBuilder sb = new StringBuilder();

		sb.append(" SELECT DISTINCT t.class_code, t.class_name, t.class_code id ");

		sb.append(" FROM sc_point_poicode_new t ");

		ResultSetHandler<JSONArray> rsHandler = new ResultSetHandler<JSONArray>() {
			public JSONArray handle(ResultSet resultSet) throws SQLException {

				JSONArray array = new JSONArray();

				while (resultSet.next()) {
					String id = resultSet.getString("id");

					String code = resultSet.getString("class_code");

					String name = resultSet.getString("class_name");

					JSONObject json = new JSONObject();

					json.put("id", id);

					json.put("code", code);

					json.put("name", name);

					array.add(json);
				}
				return array;
			}
		};

		return run.query(conn, sb.toString(), rsHandler);
	}

	/**
	 * 用SC_POINT_POICODE_NEW的 CLASS_CODE+SUB_CLASS_CODE去重，
	 * id为拼起来的值，code为SUB_CLASS_CODE，name为SUB_CLASS_NAME 参数：topId 大分类id
	 * 返回：id、code、name
	 * 
	 * @return
	 * @throws Exception
	 */
	public JSONArray queryMediumKindInfo(final String topId) throws Exception {
		Connection conn = null;

		QueryRunner run = new QueryRunner();

		conn = DBConnector.getInstance().getMetaConnection();

		StringBuilder sb = new StringBuilder();

		sb.append(" SELECT DISTINCT ");

		sb.append(" t.sub_class_code,t.sub_class_name, t.class_code||t.sub_class_code id ");

		sb.append(" FROM sc_point_poicode_new t " + " WHERE t.class_code=? ");

		ResultSetHandler<JSONArray> rsHandler = new ResultSetHandler<JSONArray>() {
			public JSONArray handle(ResultSet resultSet) throws SQLException {
				JSONArray array = new JSONArray();

				while (resultSet.next()) {
					String id = resultSet.getString("id");

					String code = resultSet.getString("sub_class_code");

					String name = resultSet.getString("sub_class_name");

					JSONObject json = new JSONObject();

					json.put("topId", topId);

					json.put("id", id);

					json.put("code", code);

					json.put("name", name);

					array.add(json);
				}
				return array;
			}
		};

		return run.query(conn, sb.toString(), rsHandler, topId);
	}

	/**
	 * 通过大分类、中分类获取KindCode信息
	 * 
	 * @param topId
	 *            大分类
	 * @param mediumId
	 *            中分类
	 * @return kindCode、kindName、extend
	 * @throws Exception
	 */
	public JSONArray queryKindInfo(String topId, String mediumId, int region)
			throws Exception {

		Connection conn = null;

		QueryRunner run = new QueryRunner();

		conn = DBConnector.getInstance().getMetaConnection();

		StringBuilder sb = new StringBuilder();

		sb.append(" SELECT t.kind_code,t.kind_name from SC_POINT_POICODE_NEW t");

		sb.append(" WHERE t.class_code=? AND t.sub_class_code=? ");

		if (region == 0) {
			sb.append(" and (t.mhm_des='DHM' or t.mhm_des='D') ");
		}

		if (region == 1) {
			sb.append(" and (t.mhm_des='DHM' or t.mhm_des='HM') ");
		}

		ResultSetHandler<JSONArray> rsHandler = new ResultSetHandler<JSONArray>() {
			public JSONArray handle(ResultSet resultSet) throws SQLException {

				JSONArray array = new JSONArray();

				while (resultSet.next()) {
					String kindCode = resultSet.getString("kind_code");

					String kindName = resultSet.getString("kind_name");

					JSONObject json = new JSONObject();

					json.put("kindCode", kindCode);

					json.put("kindName", kindName);

					json.put("extend", "");

					array.add(json);
				}
				return array;
			}
		};

		return run.query(conn, sb.toString(), rsHandler, topId, mediumId);
	}

}
