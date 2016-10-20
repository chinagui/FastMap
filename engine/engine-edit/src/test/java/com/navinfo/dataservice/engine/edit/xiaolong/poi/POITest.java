package com.navinfo.dataservice.engine.edit.xiaolong.poi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.model.rd.gate.RdGate;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.warninginfo.RdWarninginfo;
import com.navinfo.dataservice.dao.glm.search.AbstractSearch;
import com.navinfo.dataservice.dao.glm.search.RdRestrictionSearch;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.search.SearchProcess;
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
			Connection conn = DBConnector.getInstance().getConnectionById(17);

			RdRestrictionSearch search = new RdRestrictionSearch(conn);

			IRow jsonObject = search.searchDataByPid(208000002);

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
		String parameter = "{\"command\":\"DELETE\",\"dbId\":17,\"type\":\"IXPOIPARENT\",\"objId\":78813399}";
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
		String parameter = "{\"command\":\"UPDATE\",\"dbId\":2005,\"type\":\"IXPOI\",\"objId\":207000010,\"data\":{\"kindCode\":\"110101\",\"rowId\":\"BEC2B67CA9D04E178C26BE142D4F3235\",\"pid\":207000010,\"objStatus\":\"UPDATE\",\"chain\":\"3032\",\"postCode\":\"123456\",\"level\":\"B2\",\"names\":[{\"pid\":0,\"poiPid\":0,\"nameGroupid\":1,\"langCode\":\"CHI\",\"nameClass\":1,\"nameType\":2,\"name\":\"测线新增\",\"namePhonetic\":\"\",\"keywords\":\"\",\"nidbPid\":\"\",\"rowId\":\"\",\"objStatus\":\"INSERT\"}],\"addresses\":[{\"nameGroupid\":1,\"poiPid\":0,\"langCode\":\"CHI\",\"fullname\":\"测线新增\",\"objStatus\":\"INSERT\"}],\"contacts\":[{\"contactType\":1,\"contact\":\"010-89098765\",\"contactDepart\":0,\"priority\":1,\"objStatus\":\"INSERT\"}],\"photos\":[{\"pid\":\"054f687df3ac44a5a6fb432c320c29dc\",\"tag\":1,\"photoId\":0,\"status\":\"\",\"memo\":\"\",\"objStatus\":\"INSERT\"}],\"restaurants\":[{\"pid\":0,\"poiPid\":0,\"foodType\":\"3000|2007\",\"creditCard\":\"1|2|3|4|5|6\",\"avgCost\":80,\"parking\":2,\"openHour\":\"24小时\",\"travelguideFlag\":0,\"objStatus\":\"INSERT\"}]}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

//	@Test
//	public void createPoiFromRowPro() throws Exception {
//
//		String parameter = "{\"command\":\"UPDATE\",\"dbId\":17,\"type\":\"IXPOI\",\"objId\":2574546,\"data\":{\"rowId\":\"3AE1FB52D86492F7E050A8C08304EE4C\",\"pid\":2574546,\"objStatus\":\"UPDATE\"}}";
//
//		Connection conn = null;
//
//		try {
//
//			JSONObject json = JSONObject.fromObject(parameter);
//
//			OperType operType = Enum.valueOf(OperType.class, json.getString("command"));
//
//			ObjType objType = Enum.valueOf(ObjType.class, json.getString("type"));
//
//			int dbId = json.getInt("dbId");
//
//			conn = DBConnector.getInstance().getConnectionById(dbId);
//
//			EditApiImpl editApiImpl = new EditApiImpl(conn);
//
//			editApiImpl.setToken(2);
//
//			JSONObject result = editApiImpl.runPoi(json);
//
//			StringBuffer buf = new StringBuffer();
//
//			int pid = 0;
//
//			if (operType != OperType.CREATE) {
//				if (objType == ObjType.IXSAMEPOI) {
//					String poiPids = JsonUtils.getStringValueFromJSONArray(json.getJSONArray("poiPids"));
//					buf.append(poiPids);
//				} else {
//					pid = json.getInt("objId");
//
//					buf.append(String.valueOf(pid));
//				}
//			} else {
//				pid = result.getInt("pid");
//				buf.append(String.valueOf(pid));
//			}
//
//			if (operType == OperType.UPDATE) {
//				json.put("objId", pid);
//				BatchProcess batchProcess = new BatchProcess();
//				batchProcess.execute(json, conn, editApiImpl);
//			}
//
//			upatePoiStatus(buf.toString(), conn);
//
//			System.out.println(result);
//
//		} catch (DataNotChangeException e) {
//			DbUtils.rollback(conn);
//		} catch (Exception e) {
//			e.printStackTrace();
//			DbUtils.rollback(conn);
//		} finally {
//			DbUtils.commitAndClose(conn);
//		}
//	}

	@Test
	public void testUpdatePoi() {
		String parameter = "{\"command\":\"UPDATE\",\"dbId\":17,\"type\":\"IXPOI\",\"objId\":3452060,\"data\":{\"photos\":[{\"fccPid\":\"4c3e0a43742e4f908d1e102343a110e4\",\"tag\":1,\"photoId\":0,\"status\":\"\",\"memo\":\"\",\"objStatus\":\"INSERT\"},{\"fccPid\":\"9e4e27bb912047ca923d7b628299d618\",\"rowId\":\"46BEC9A37C594562BDA8260B532B20FE\",\"objStatus\":\"UPDATE\"},{\"rowId\":\"2ADB648758DC426D92BBDA08B6623DC0\",\"objStatus\":\"UPDATE\",\"fccPid\":\"c4c16f1e25e643bcaec9e34ad80b93b3\"}],\"rowId\":\"3AE1FB55484A92F7E050A8C08304EE4C\",\"pid\":3452060}}";
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
