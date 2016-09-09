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
import com.navinfo.dataservice.dao.glm.search.AbstractSearch;
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
		String parameter = "{ \"dbId\": \"17\", \"objId\": 206000000, \"command\": \"CREATE\", \"type\": \"IXPOIUPLOAD\", \"data\": { \"accessFlag\": 0, \"addressFlag\": 0, \"addresses\": [ { \"addons\": \"\", \"addonsPhonetic\": \"\", \"addrname\": \"\", \"addrnamePhonetic\": \"\", \"building\": \"\", \"buildingPhonetic\": \"\", \"city\": \"\", \"cityPhonetic\": \"\", \"county\": \"\", \"countyPhonetic\": \"\", \"estab\": \"\", \"estabPhonetic\": \"\", \"floor\": \"\", \"floorPhonetic\": \"\", \"fullname\": \"我们\", \"fullnamePhonetic\": \"\", \"housenum\": \"\", \"housenumPhonetic\": \"\", \"landmark\": \"\", \"landmarkPhonetic\": \"\", \"langCode\": \"CHI\", \"nameGroupid\": 1, \"pid\": 301000002, \"place\": \"\", \"placePhonetic\": \"\", \"poiPid\": 206000000, \"prefix\": \"\", \"prefixPhonetic\": \"\", \"provPhonetic\": \"\", \"province\": \"\", \"roadname\": \"\", \"roadnamePhonetic\": \"\", \"room\": \"\", \"roomPhonetic\": \"\", \"rowId\": \"2adbfb858d974dbf98544627385c4455\", \"srcFlag\": 0, \"street\": \"\", \"streetPhonetic\": \"\", \"subnum\": \"\", \"subnumPhonetic\": \"\", \"surfix\": \"\", \"surfixPhonetic\": \"\", \"town\": \"\", \"townPhonetic\": \"\", \"type\": \"\", \"typePhonetic\": \"\", \"uDate\": \"\", \"uRecord\": 0, \"unit\": \"\", \"unitPhonetic\": \"\" } ], \"adminReal\": 0, \"advertisements\": [], \"airportCode\": \"\", \"attractions\": [], \"audioes\": [], \"buildings\": [], \"businesstimes\": [], \"carrentals\": [], \"chain\": \"\", \"chargingplotPhs\": [], \"chargingplots\": [], \"chargingstations\": [], \"children\": [], \"collectTime\": \"20160908135603\", \"contacts\": [], \"dataVersion\": \"260+\", \"details\": [], \"difGroupid\": \"\", \"editFlag\": 1, \"editionFlag\": \"\", \"entryImages\": [], \"events\": [], \"exPriority\": \"\", \"fieldState\": \"改种别代码|改酒店星级\", \"fieldTaskId\": 0, \"flags\": [], \"fullAttrFlag\": 9, \"gasstations\": [], \"geoAdjustFlag\": 9, \"geometry\": { \"type\": \"Point\", \"coordinates\": [ 116.35039, 40.06669 ] }, \"hotels\": [ { \"address\": \"\", \"breakfast\": 0, \"checkinTime\": \"14:00\", \"checkoutTime\": \"12:00\", \"city\": \"\", \"creditCard\": \"\", \"longDescripEng\": \"\", \"longDescription\": \"\", \"openHour\": \"\", \"openHourEng\": \"\", \"parking\": 0, \"photoName\": \"\", \"pid\": 301000000, \"poiPid\": 206000000, \"rating\": 0, \"roomCount\": 0, \"roomPrice\": \"\", \"roomType\": \"\", \"rowId\": \"006f7d712f76494caebcf5ca92a8ac4f\", \"service\": \"\", \"telephone\": \"\", \"travelguideFlag\": 0, \"uDate\": \"\", \"uRecord\": 0 } ], \"icons\": [], \"importance\": 0, \"indoor\": 0, \"introductions\": [], \"kindCode\": \"120101\", \"label\": \"\", \"level\": \"B1\", \"linkPid\": 55133674, \"log\": \"改名称|改地址|改分类|改POI_LEVEL|改RELATION\", \"meshId\": 605602, \"meshId5k\": \"\", \"nameGroupid\": 0, \"names\": [ { \"keywords\": \"\", \"langCode\": \"CHI\", \"name\": \"我们\", \"nameClass\": 1, \"nameFlags\": [], \"nameGroupid\": 1, \"namePhonetic\": \"\", \"nameTones\": [], \"nameType\": 2, \"nidbPid\": \"\", \"pid\": 307000000, \"poiPid\": 206000000, \"rowId\": \"623fb8ad8cdf4feeb71b235ee654b1a1\", \"uDate\": \"\", \"uRecord\": 0 } ], \"oldAddress\": \"我们\", \"oldBlockcode\": \"\", \"oldKind\": \"120101\", \"oldName\": \"我们\", \"oldXGuide\": 0, \"oldYGuide\": 0, \"open24h\": 0, \"operateRefs\": [], \"parents\": [], \"parkings\": [], \"photos\": [], \"pid\": 206000000, \"pmeshId\": 0, \"poiMemo\": \"\", \"poiNum\": \"00365520160908135603\", \"postCode\": \"\", \"regionId\": 0, \"reserved\": \"\", \"restaurants\": [], \"roadFlag\": 0, \"rowId\": \"94F79E6EBC0E4EBCBA84B1C624A31BB0\", \"samepoiParts\": [], \"side\": 0, \"sportsVenue\": \"\", \"state\": 0, \"status\": 0, \"taskId\": 0, \"tourroutes\": [], \"type\": 0, \"uDate\": \"\", \"uRecord\": 1, \"verifiedFlag\": 9, \"videoes\": [], \"vipFlag\": \"\", \"xGuide\": 116.35037, \"yGuide\": 40.06671 } }";
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
