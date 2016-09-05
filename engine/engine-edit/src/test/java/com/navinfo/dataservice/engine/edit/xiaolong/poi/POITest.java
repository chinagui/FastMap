package com.navinfo.dataservice.engine.edit.xiaolong.poi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.api.edit.iface.EditApi;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.exception.DataNotChangeException;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.search.AbstractSearch;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.search.SearchProcess;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.DBUtils;

import net.sf.json.JSONObject;

public class POITest extends InitApplication {

	@Override
	@Before
	public void init() {
		initContext();
	}

	@Test
	public void testGetByPid() {
		try {
			Connection conn = DBConnector.getInstance().getConnectionById(42);

			AbstractSearch search = new AbstractSearch();

			IRow jsonObject = search.searchDataByPid(IxPoi.class, 73352736, conn);

			System.out.println(jsonObject.Serialize(ObjLevel.FULL));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void getTitleWithGap() {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getConnectionById(42);

			SearchProcess p = new SearchProcess(conn);

			List<ObjType> objType = new ArrayList<>();

			objType.add(ObjType.IXPOI);

			System.out.println(p.searchDataByTileWithGap(objType, 107937, 49616, 17, 80));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testDeletePoi() {
		String parameter = "{\"command\":\"DELETE\",\"type\":\"IXPOI\",\"dbId\":42,\"objId\":1152117063}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void addPoi() {
		String parameter = "{\"command\":\"CREATE\",\"type\":\"IXPOI\",\"dbId\":42,\"data\":{\"longitude\":116.43301159143448,\"latitude\":40.02784652328596,\"x_guide\":116.43281181786811,\"y_guide\":40.027850841072194,\"linkPid\":15341035}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void createPoiFromRowPro() throws Exception {
		String parameter = "{\"command\":\"CREATE\",\"type\":\"IXPOI\",\"dbId\":42,\"data\":{\"longitude\":116.41263484954834,\"latitude\":40.02841954590441,\"x_guide\":116.41212518018918,\"y_guide\":40.02858491300628,\"linkPid\":100009709}}";
		Connection conn = null;

		try {

			JSONObject json = JSONObject.fromObject(parameter);

			OperType operType = Enum.valueOf(OperType.class, json.getString("command"));

			ObjType objType = Enum.valueOf(ObjType.class, json.getString("type"));

			int dbId = json.getInt("dbId");

			conn = DBConnector.getInstance().getConnectionById(dbId);

			EditApi editApi = (EditApi) ApplicationContextUtil.getBean("editApi");

			JSONObject result = editApi.run(json);

			StringBuffer buf = new StringBuffer();

			int pid = 0;

			if (operType != OperType.CREATE) {
				if (objType == ObjType.IXSAMEPOI) {
					String poiPids = JsonUtils.getStringValueFromJSONArray(json.getJSONArray("poiPids"));
					buf.append(poiPids);
				} else {
					pid = json.getInt("objId");

					buf.append(String.valueOf(pid));
				}
			} else {
				pid = result.getInt("pid");
				buf.append(String.valueOf(pid));
			}

			json.put("objId", pid);
//			BatchProcess batchProcess = new BatchProcess();
//			batchProcess.execute(json, conn);

			upatePoiStatus(buf.toString(), conn);
			
			System.out.println(result.toString());

		} catch (DataNotChangeException e) {
			e.printStackTrace();
			DbUtils.rollbackAndClose(conn);
		} catch (Exception e) {
			e.printStackTrace();
			DbUtils.rollbackAndClose(conn);
		} finally {
			DbUtils.close(conn);
		}
	}

	@Test
	public void testUpdatePoi() {
		String parameter = "{\"command\":\"UPDATE\",\"dbId\":42,\"type\":\"IXPOI\",\"objId\":20928963,\"data\":{\"contacts\":[{\"poiPid\":0,\"rowId\":\"42A096761F65438990B6A0AA4293DE2A\",\"objStatus\":\"UPDATE\",\"contact\":\"010-22222222\"}],\"rowId\":\"3524E590CA526E1AE050A8C08304BA17\",\"pid\":20928963}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testDeleteParent() {
		String parameter = "{\"command\":\"DELETE\",\"type\":\"IXPOIPARENT\",\"dbId\":42,\"objId\":73341675}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testGetPoiList() {
		String parameter = "{\"dbId\":42,\"subtaskId\":117,\"type\":1,\"pageNum\":1,\"pageSize\":20,\"pidName\":\"\",\"pid\":0}";
		Connection conn = null;
		Connection manConn = null;
		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);
			int dbId = jsonReq.getInt("dbId");
			int subtaskId = jsonReq.getInt("subtaskId");
			ManApi apiService = (ManApi) ApplicationContextUtil.getBean("manApi");
			manConn = DBConnector.getInstance().getManConnection();
			Subtask subtaskObj = apiService.queryBySubtaskId(subtaskId);
			String sql = "SELECT E.STATUS, COUNT(1) COUNT_NUM " + "  FROM POI_EDIT_STATUS E, IX_POI P"
					+ " WHERE E.ROW_ID = P.ROW_ID" + "   AND SDO_RELATE(P.GEOMETRY, SDO_GEOMETRY('"
					+ subtaskObj.getGeometry() + "', 8307), 'MASK=ANYINTERACT') =" + "       'TRUE'"
					+ " GROUP BY E.STATUS";
			conn = DBConnector.getInstance().getConnectionById(dbId);
			ResultSetHandler<JSONObject> rsHandler = new ResultSetHandler<JSONObject>() {
				public JSONObject handle(ResultSet rs) throws SQLException {
					JSONObject staticsObj = new JSONObject();
					while (rs.next()) {
						staticsObj.put(rs.getInt("STATUS"), rs.getInt("COUNT_NUM"));
					}
					return staticsObj;
				}
			};
			QueryRunner run = new QueryRunner();
			System.out.println(run.query(conn, sql, rsHandler));
		} catch (Exception e) {
			System.out.println(e.getMessage());
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
	}

	/**
	 * poi操作修改poi状态为已作业，鲜度信息为0 zhaokk sourceFlag 0 web 1 Android
	 * 
	 * @param row
	 * @throws Exception
	 */
	public void upatePoiStatus(String pids, Connection conn) throws Exception {
		StringBuilder sb = new StringBuilder(" MERGE INTO poi_edit_status T1 ");
		sb.append(" USING (SELECT row_id as a , 2 AS b,0 AS C FROM ix_poi where pid in (" + pids + ")) T2 ");
		sb.append(" ON ( T1.row_id=T2.a) ");
		sb.append(" WHEN MATCHED THEN ");
		sb.append(" UPDATE SET T1.status = T2.b,T1.fresh_verified= T2.c ");
		sb.append(" WHEN NOT MATCHED THEN ");
		sb.append(" INSERT (T1.row_id,T1.status,T1.fresh_verified) VALUES(T2.a,T2.b,T2.c)");
		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.executeUpdate();
		} catch (Exception e) {
			throw e;

		} finally {
			DBUtils.closeStatement(pstmt);
		}

	}
}
