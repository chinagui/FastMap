package com.navinfo.dataservice.control.row.query;

import java.sql.Connection;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;

import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;

import com.navinfo.navicommons.database.QueryRunner;

import net.sf.json.JSONObject;

public class PoiQuery {
	private static final Logger logger = Logger.getLogger(PoiQuery.class);

	/**
	 * @zhaokk POI count 統計
	 * @param classNames
	 * @param poi
	 * @return
	 * @throws Exception
	 */
	public JSONObject getPoiCount(String parameter) throws Exception {

		Connection conn = null;
		Connection manConn = null;
		JSONObject resultJson = null;
		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);
			int subtaskId = jsonReq.getInt("subtaskId");
			ManApi apiService = (ManApi) ApplicationContextUtil
					.getBean("manApi");
			manConn = DBConnector.getInstance().getManConnection();
			Subtask subtaskObj = apiService.queryBySubtaskId(subtaskId);
			String sql = "SELECT E.STATUS, COUNT(1) COUNT_NUM "
					+ "  FROM POI_EDIT_STATUS E, IX_POI P"
					+ " WHERE E.PID = P.PID" + "   AND E.STATUS IN (1,2)"
					+ "   AND SDO_RELATE(P.GEOMETRY, SDO_GEOMETRY('"
					+ subtaskObj.getGeometry()
					+ "', 8307), 'MASK=ANYINTERACT') =" + "       'TRUE'"
					+ " GROUP BY E.STATUS";
			int dbId = subtaskObj.getDbId();
			conn = DBConnector.getInstance().getConnectionById(dbId);
			ResultSetHandler<JSONObject> rsHandler = new ResultSetHandler<JSONObject>() {
				public JSONObject handle(ResultSet rs) throws SQLException {
					JSONObject staticsObj = new JSONObject();
					int total = 0;
					while (rs.next()) {
						staticsObj.put(rs.getInt("STATUS"),
								rs.getInt("COUNT_NUM"));
						total += rs.getInt("COUNT_NUM");
					}
					staticsObj.put(4, total);
					return staticsObj;
				}
			};
			QueryRunner run = new QueryRunner();
			resultJson = run.query(conn, sql, rsHandler);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);

		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (manConn != null) {
				try {
					manConn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return resultJson;
	}

	/**
	 * @zhaokk POI count 統計
	 * @param classNames
	 * @param poi
	 * @return
	 * @throws Exception
	 */
	public JSONObject getPoiList(String parameter) throws Exception {

		Connection conn = null;
		JSONObject resultJson = null;

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			int dbId = jsonReq.getInt("dbId");
			// 项目管理（放开）
			// subtaskId
			int subtaskId = jsonReq.getInt("subtaskId");
			int type = jsonReq.getInt("type");
			ManApi apiService = (ManApi) ApplicationContextUtil
					.getBean("manApi");
			Subtask subtask = apiService.queryBySubtaskId(subtaskId);
			int pageNum = jsonReq.getInt("pageNum");
			int pageSize = jsonReq.getInt("pageSize");
			//int pid = 0;
			//20170106_gpr:pid和name搜索合成一个字段；
			String pidName = "";
			if (jsonReq.containsKey("pidName")) {
				pidName = jsonReq.getString("pidName");
			}
//			if (jsonReq.containsKey("pid")) {
//				pid = jsonReq.getInt("pid");
//			}
			conn = DBConnector.getInstance().getConnectionById(dbId);
			IxPoiSelector selector = new IxPoiSelector(conn);
			resultJson = selector.loadPids(false, pidName, type,
					subtask.getGeometry(), pageSize, pageNum);
			logger.debug(resultJson);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);

		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return resultJson;
	}
}
