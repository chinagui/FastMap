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
import com.navinfo.dataservice.control.row.batch.BatchProcess;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.search.AbstractSearch;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.search.SearchProcess;
import com.navinfo.dataservice.engine.edit.service.EditApiImpl;
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

		String parameter = "{\"command\":\"UPDATE\",\"dbId\":17,\"type\":\"IXPOI\",\"objId\":2574546,\"data\":{\"rowId\":\"3AE1FB52D86492F7E050A8C08304EE4C\",\"pid\":2574546,\"objStatus\":\"UPDATE\"}}";

		Connection conn = null;

		try {

			JSONObject json = JSONObject.fromObject(parameter);

			OperType operType = Enum.valueOf(OperType.class, json.getString("command"));

			ObjType objType = Enum.valueOf(ObjType.class, json.getString("type"));

			int dbId = json.getInt("dbId");

			conn = DBConnector.getInstance().getConnectionById(dbId);

			EditApiImpl editApiImpl = new EditApiImpl(conn);

			editApiImpl.setToken(2);

			JSONObject result = editApiImpl.runPoi(json);

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

			if (operType == OperType.UPDATE) {
				json.put("objId", pid);
				BatchProcess batchProcess = new BatchProcess();
				batchProcess.execute(json, conn, editApiImpl);
			}

			upatePoiStatus(buf.toString(), conn);

			System.out.println(result);

		} catch (DataNotChangeException e) {
			DbUtils.rollback(conn);
		} catch (Exception e) {
			e.printStackTrace();
			DbUtils.rollback(conn);
		} finally {
			DbUtils.commitAndClose(conn);
		}
	}

	@Test
	public void testUpdatePoi() {
		String parameter = "{\"command\":\"UPDATE\",\"dbId\":17,\"type\":\"IXPOI\",\"objId\":2574546,\"data\":{\"rowId\":\"3AE1FB52D86492F7E050A8C08304EE4C\",\"pid\":2574546,\"objStatus\":\"UPDATE\"}}";
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
		String parameter = "{\"dbId\":17,\"subtaskId\":22,\"type\":1,\"pageNum\":1,\"pageSize\":20,\"pidName\":\"\",\"pid\":0}";
		Connection conn = null;
		Connection manConn = null;
		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			int dbId = jsonReq.getInt("dbId");
			// 项目管理（放开）
			// subtaskId
			int subtaskId = jsonReq.getInt("subtaskId");
			int type = jsonReq.getInt("type");
			ManApi apiService = (ManApi) ApplicationContextUtil.getBean("manApi");
			Subtask subtask = apiService.queryBySubtaskId(subtaskId);
			int pageNum = jsonReq.getInt("pageNum");
			int pageSize = jsonReq.getInt("pageSize");
			int pid = 0;
			String pidName = "";
			if (jsonReq.containsKey("pidName")) {
				pidName = jsonReq.getString("pidName");
			}
			if (jsonReq.containsKey("pid")) {
				pid = jsonReq.getInt("pid");
			}
			conn = DBConnector.getInstance().getConnectionById(dbId);
			IxPoiSelector selector = new IxPoiSelector(conn);
			JSONObject jsonObject = selector.loadPids(false, pid, pidName, type, subtask.getGeometry(), pageSize,
					pageNum);
			System.out.println(jsonObject.toString());
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
