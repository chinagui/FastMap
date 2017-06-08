package com.navinfo.dataservice.engine.edit.luyao;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.glm.geolive.GeoliveHelper;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.search.specialMap.SpecialMapUtils;
import com.navinfo.dataservice.dao.glm.selector.SearchAllObject;
import com.navinfo.dataservice.dao.glm.selector.SelectorUtils;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.search.SearchProcess;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class serchConditionTest extends InitApplication {

	@Override
	@Before
	public void init() {
		initContext();
	}

	@Test
	public void getByCondition() {

		Connection conn = null;

		try {

			String parameter = "{\"dbId\":17,\"type\":\"RDLINK\",\"data\":{\"queryType\":\"RDSPEEDLIMIT\",\"linkPid\":309000108,\"direct\":3}}";

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String objType = jsonReq.getString("type");

			int dbId = jsonReq.getInt("dbId");

			JSONObject data = jsonReq.getJSONObject("data");

			conn = DBConnector.getInstance().getConnectionById(dbId);

			SearchProcess p = new SearchProcess(conn);

			JSONArray array = p.searchDataByCondition(ObjType.valueOf(objType),
					data);

			System.out.println(array);

		} catch (Exception e) {

			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@Test
	public void getByCondition4() {

		Connection conn = null;

		try {

			String parameter = "{\"dbId\":13,\"type\":\"RDLANEVIA\",\"data\":{\"inLinkPid\":407000013,\"nodePid\":420000011,\"outLinkPid\":520000007,\"type\":\"RDLANECONNEXITY\"}}";

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String objType = jsonReq.getString("type");

			int dbId = jsonReq.getInt("dbId");

			JSONObject data = jsonReq.getJSONObject("data");

			conn = DBConnector.getInstance().getConnectionById(dbId);

			SearchProcess p = new SearchProcess(conn);

			JSONArray array = p.searchDataByCondition(ObjType.valueOf(objType),
					data);

			System.out.println(array);

		} catch (Exception e) {

			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Test
	public void getBySpecialMap() {

		Connection conn = null;

		try {

			String parameter = "{\"dbId\":17,\"gap\":10,\"types\":[\"rdLinkLimitTruck\",\"rdLinkLimit\",\"rdlinkSpeedlimitSpeedClass\",\"rdlinkSpeedlimitSpeedClassWork\",\"rdlinkSpeedlimitSpeedLimitSrc\",\"rdLinkLaneClass\",\"rdLinkFunctionClass\",\"rdLinkLaneNum\",\"rdLinkDevelopState\",\"rdLinkMultiDigitized\",\"rdLinkPaveStatus\",\"rdLinkTollInfo\",\"rdLinkSpecialTraffic\",\"rdLinkIsViaduct\",\"rdLinkAppInfo\",\"rdLinkForm50\",\"rdLinkNameContent\",\"rdLinkNameGroupid\",\"rdLinkNameType\",\"rdlinkSpeedlimitConditionCount\",\"rdLinkLimitType3\",\"rdLinkFormOfWay10\",\"rdLinkFormOfWay11\",\"rdLinkFormOfWay12\",\"rdLinkFormOfWay13\",\"rdLinkFormOfWay14\",\"rdLinkFormOfWay15\",\"rdLinkFormOfWay16\",\"rdLinkFormOfWay17\",\"rdLinkFormOfWay20\",\"rdLinkFormOfWay31\",\"rdLinkFormOfWay33\",\"rdLinkFormOfWay34\",\"rdLinkFormOfWay35\",\"rdLinkFormOfWay36\",\"rdLinkFormOfWay37\",\"rdLinkFormOfWay38\",\"rdLinkFormOfWay39\",\"rdLinkLimitType0\",\"rdLinkLimitType8\",\"rdLinkLimitType9\",\"rdLinkLimitType10\",\"rdLinkLimitType2\",\"rdLinkLimitType5\",\"rdLinkLimitType6\",\"rdLinkLimitType7\",\"rdLinkLimitType1\",\"rdLinkRticRank\",\"rdLinkIntRticRank\",\"rdLinkZoneTpye\",\"rdLinkZoneCount\",\"rdLinkZoneSide\"],\"z\":20,\"x\":861639,\"y\":396652}";

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			List<String> types = new ArrayList<String>();

			if (jsonReq.containsKey("type")) {
				types.add(jsonReq.getString("type"));
			}
			if (jsonReq.containsKey("types")) {
				JSONArray typeArray = jsonReq.getJSONArray("types");

				for (int i = 0; i < typeArray.size(); i++) {
					types.add(typeArray.getString(i));
				}
			}

			int dbId = jsonReq.getInt("dbId");

			int x = jsonReq.getInt("x");

			int y = jsonReq.getInt("y");

			int z = jsonReq.getInt("z");

			int gap = 0;

			if (jsonReq.containsKey("gap")) {
				gap = jsonReq.getInt("gap");
			}

			JSONObject data = null;

			if (z > 9) {

				conn = DBConnector.getInstance().getConnectionById(dbId);

				SpecialMapUtils specialMap = new SpecialMapUtils(conn);

				data = specialMap.searchDataByTileWithGap(types, x, y, z, gap);
			}

			System.out.println(data);

		} catch (Exception e) {

			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	

	@Test
	public void getBySpecialMap1() {

		Connection conn = null;

		try {

			String parameter = "{\"dbId\":17,\"gap\":20,\"types\":[\"rdLinkProperty\"],\"z\":18,\"x\":215672,\"y\":98705}";

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			List<String> types = new ArrayList<String>();

			if (jsonReq.containsKey("type")) {
				types.add(jsonReq.getString("type"));
			}
			if (jsonReq.containsKey("types")) {
				JSONArray typeArray = jsonReq.getJSONArray("types");

				for (int i = 0; i < typeArray.size(); i++) {
					types.add(typeArray.getString(i));
				}
			}

			int dbId = jsonReq.getInt("dbId");

			int x = jsonReq.getInt("x");

			int y = jsonReq.getInt("y");

			int z = jsonReq.getInt("z");

			int gap = 0;

			if (jsonReq.containsKey("gap")) {
				gap = jsonReq.getInt("gap");
			}

			JSONObject data = null;

			if (z > 9) {

				conn = DBConnector.getInstance().getConnectionById(dbId);

				SpecialMapUtils specialMap = new SpecialMapUtils(conn);

				data = specialMap.searchDataByTileWithGap(types, x, y, z, gap);
			}

			System.out.println(data);

		} catch (Exception e) {

			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	
	
	@Test
	public void getBySpecialMap2() {

		Connection conn = null;

		try {

			String parameter = "{\"dbId\":13,\"gap\":10,\"types\":[\"rdLinkImiCode\",\"rdLinkNameType\"],\"z\":19,\"x\":431657,\"y\":198398}";

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			List<String> types = new ArrayList<String>();

			if (jsonReq.containsKey("type")) {
				types.add(jsonReq.getString("type"));
			}
			if (jsonReq.containsKey("types")) {
				JSONArray typeArray = jsonReq.getJSONArray("types");

				for (int i = 0; i < typeArray.size(); i++) {
					types.add(typeArray.getString(i));
				}
			}

			int dbId = jsonReq.getInt("dbId");

			int x = jsonReq.getInt("x");

			int y = jsonReq.getInt("y");

			int z = jsonReq.getInt("z");

			int gap = 0;

			if (jsonReq.containsKey("gap")) {
				gap = jsonReq.getInt("gap");
			}

			JSONObject data = null;

			if (z > 9) {

				conn = DBConnector.getInstance().getConnectionById(dbId);

				SpecialMapUtils specialMap = new SpecialMapUtils(conn);

				data = specialMap.searchDataByTileWithGap(types, x, y, z, gap);
			}

			System.out.println(data);

		} catch (Exception e) {

			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	
	@Test
	public void getBySpecialMap3() {

		Connection conn = null;

		try {

			String parameter = "{\"dbId\":19,\"gap\":10,\"types\":[\"rdLinkProperty\"],\"z\":17,\"x\":107844,\"y\":49360}";

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			List<String> types = new ArrayList<String>();

			if (jsonReq.containsKey("type")) {
				types.add(jsonReq.getString("type"));
			}
			if (jsonReq.containsKey("types")) {
				JSONArray typeArray = jsonReq.getJSONArray("types");

				for (int i = 0; i < typeArray.size(); i++) {
					types.add(typeArray.getString(i));
				}
			}

			int dbId = jsonReq.getInt("dbId");

			int x = jsonReq.getInt("x");

			int y = jsonReq.getInt("y");

			int z = jsonReq.getInt("z");

			int gap = 0;

			if (jsonReq.containsKey("gap")) {
				gap = jsonReq.getInt("gap");
			}

			JSONObject data = null;

			if (z > 9) {

				conn = DBConnector.getInstance().getConnectionById(dbId);

				SpecialMapUtils specialMap = new SpecialMapUtils(conn);

				data = specialMap.searchDataByTileWithGap(types, x, y, z, gap);
			}

			System.out.println(data);

		} catch (Exception e) {

			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	
	@Test
	public void getTitleWithGap1() {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getConnectionById(13);

			SearchProcess p = new SearchProcess(conn);

			List<ObjType> objType = new ArrayList<>();

			objType.add(ObjType.RDOBJECT);

			System.out.println(p.searchDataByTileWithGap(objType, 215865,
					99247, 18, 10));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	@Test
	public void getByCondition_variableSpeed() {

		Connection conn = null;

		try {

			String parameter = "{\"dbId\":84,\"type\":\"RDLINK\",\"data\":{\"queryType\":\"RDVARIABLESPEED\",\"linkPid\":502000057,\"nodePid\":403000050}}";

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String objType = jsonReq.getString("type");

			int dbId = jsonReq.getInt("dbId");

			JSONObject data = jsonReq.getJSONObject("data");

			conn = DBConnector.getInstance().getConnectionById(dbId);

			SearchProcess p = new SearchProcess(conn);

			JSONArray array = p.searchDataByCondition(ObjType.valueOf(objType),
					data);

			System.out.println(array);

		} catch (Exception e) {

			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@Test
	public void getByCondition2() {

		Connection conn = null;

		try {

			String parameter = "{\"dbId\":17,\"type\":\"RDLINK\",\"data\":{\"queryType\":\"RDSPEEDLIMIT_DEPENDENT\",\"linkPid\":310001938,\"direct\":2}}";

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String objType = jsonReq.getString("type");

			int dbId = jsonReq.getInt("dbId");

			JSONObject data = jsonReq.getJSONObject("data");

			conn = DBConnector.getInstance().getConnectionById(dbId);

			SearchProcess p = new SearchProcess(conn);

			JSONArray array = p.searchDataByCondition(ObjType.valueOf(objType),
					data);

			System.out.println(array);

		} catch (Exception e) {

			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	
	@Test
	public void getByCondition3() {

		Connection conn = null;

		try {

			String parameter = "{\"dbId\":17,\"type\":\"RDLINK\",\"data\":{\"queryType\":\"RDSPEEDLIMIT\",\"linkPid\":310001938,\"direct\":2}}";

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String objType = jsonReq.getString("type");

			int dbId = jsonReq.getInt("dbId");

			JSONObject data = jsonReq.getJSONObject("data");

			conn = DBConnector.getInstance().getConnectionById(dbId);

			SearchProcess p = new SearchProcess(conn);

			JSONArray array = p.searchDataByCondition(ObjType.valueOf(objType),
					data);

			System.out.println(array);

		} catch (Exception e) {

			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	@Test
	public void getTitleWithGap2() {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getConnectionById(247);

			SearchProcess p = new SearchProcess(conn);

			List<ObjType> objType = new ArrayList<>();

			objType.add(ObjType.RDOBJECT);

			System.out.println(p.searchDataByTileWithGap(objType, 107932,
					49627, 17, 10));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void searchDataBySpatial1() {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getConnectionById(13);

			SearchProcess p = new SearchProcess(conn);

			List<ObjType> objType = new ArrayList<>();


			objType.add(ObjType.RWNODE);
			
			String box="{\"type\": \"Polygon\", \"coordinates\": [[[116.4638690650463,40.01772233000533],[116.46422848105429,40.01772233000533],[116.46422848105429,40.01803045299908],[116.4638690650463,40.01803045299908],[116.4638690650463,40.01772233000533]]]}";

			System.out.println(p.searchDataBySpatial(objType, box));
			

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void getTitleWithGap3() {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getConnectionById(84);

			SearchProcess p = new SearchProcess(conn);

			List<ObjType> objType = new ArrayList<>();

			objType.add(ObjType.RDINTER);
			
			System.out.println(p.searchDataByTileWithGap(objType, 862301,
					397168, 20, 40));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void getTitleWithGap4() {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getConnectionById(13);

			SearchProcess p = new SearchProcess(conn);

			List<ObjType> objType = new ArrayList<>();

			objType.add(ObjType.RDOBJECT);

			System.out.println(p.searchDataByTileWithGap(objType, 431721,
					198496, 19, 40));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testGetByPids() {
		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(13);

			SearchProcess p = new SearchProcess(conn);
			
			List<Integer> pidList = new ArrayList<>();
			
			pidList.add(238269);
			
			pidList.add(235801);
			
			JSONArray pids = new JSONArray();
			
			pids.add(238269);
			
			pids.add(235801);
			
			List<? extends IRow> objs = p.searchDataByPids(ObjType.RDNODE, pids);
			
			for(IRow obj : objs)
			{
				System.out.println(obj.Serialize(ObjLevel.FULL));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void searchLinkByNode() {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getConnectionById(13);

			SearchProcess p = new SearchProcess(conn);
			
			String parameter = "{\"dbId\":13,\"type\":\"RDNODE\",\"pid\":13064669}";

			JSONObject jsonReq = JSONObject.fromObject(parameter);			
			
			System.out.println(p.searchLinkByNode(jsonReq));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void searchDataByObject() {
		
		Connection conn = null;
		
		try {
			conn = DBConnector.getInstance().getConnectionById(13);

			SearchProcess p = new SearchProcess(conn);
			
			String parameter = "{\"dbId\":13,\"type\":\"CMGBUILDNODE\",\"pids\":[504000026,509000042]}";

			JSONObject jsonReq = JSONObject.fromObject(parameter);			
			
			System.out.println(p.searchDataByObject(jsonReq));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void searchDataByObject2() {
		
		Connection conn = null;
		
		try {
			conn = DBConnector.getInstance().getConnectionById(13);

			SearchProcess p = new SearchProcess(conn);
			
			String parameter = "{\"dbId\":13,\"type\":\"CMGBUILDLINK\",\"pids\":[500000015]}";

			JSONObject jsonReq = JSONObject.fromObject(parameter);			
			
			System.out.println(p.searchDataByObject(jsonReq));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	@Test
	public void testGetByPid() {
		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(13);

			SearchProcess p = new SearchProcess(conn);


			int pid = 2842164;

			IObj obj = p.searchDataByPid(ObjType.CMGBUILDING, pid);


			System.out.println(obj.Serialize(ObjLevel.FULL));




		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testGetByPid1() {
		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(13);

			SearchProcess p = new SearchProcess(conn);

			int pid = 401000040;

			String objType = "CMGBUILDING";

			IObj obj = p.searchDataByPid(ObjType.CMGBUILDING, pid);

			if (obj != null) {
				JSONObject json = obj.Serialize(ObjLevel.FULL);
				if (!json.containsKey("geometry")) {
					int pageNum = 1;
					int pageSize = 1;
					JSONObject data = new JSONObject();
					data.put(obj.primaryKey().toLowerCase(), pid);
					SelectorUtils selectorUtils = new SelectorUtils(conn);
					JSONObject jsonObject = selectorUtils
							.loadByElementCondition(data,
									ObjType.valueOf(objType), pageSize,
									pageNum, false);
					json.put("geometry", jsonObject.getJSONArray("rows")
							.getJSONObject(0).getString("geometry"));
				}
				json.put("geoLiveType", objType);
//				return new ModelAndView("jsonView", success(json));

			} else {
//				return new ModelAndView("jsonView", success());
			}


			System.out.println(obj.Serialize(ObjLevel.FULL));




		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void SearchAllObject1() {
		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(13);

			SearchAllObject p = new SearchAllObject(conn);

			JSONObject json= new JSONObject();

			json.put("uuid","123");
			json.put("pageNum",2);
			json.put("pageSize",10);

			json.put("mainTableName","RD_LINK");
			json.put("searchTableName","RD_LINK_FORM");
			json.put("primaryKey","LINK_PID");
			json.put("foreignKey","LINK_PID");

			JSONObject jsonCondition1 = new JSONObject();

			jsonCondition1.put("fieldType","Integer");
			jsonCondition1.put("operator",">");
			jsonCondition1.put("fieldName","FORM_OF_WAY");
			jsonCondition1.put("value",1);

			JSONObject jsonCondition2 = new JSONObject();

			jsonCondition2.put("fieldType","Integer");
			jsonCondition2.put("operator","<");
			jsonCondition2.put("fieldName","FORM_OF_WAY");
			jsonCondition2.put("value",51);

			JSONArray jsonConditions= new JSONArray();//json.getJSONArray("conditions");

			jsonConditions.add(jsonCondition1);
			jsonConditions.add(jsonCondition2);

			json.put("conditions",jsonConditions);

//			meshIds

//			JSONArray jsonMeshId= new JSONArray();
//			jsonMeshId.add();
//			jsonMeshId.add();

			JSONArray jsonGridId= new JSONArray();
			jsonGridId.add(59564401);
			jsonGridId.add(59564402);
			json.put("gridIds",jsonGridId);

			JSONObject  pids = p.loadByElementCondition(json);


		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	@Test
	public void SearchAllObject2() {
		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(13);

			SearchAllObject p = new SearchAllObject(conn);

			JSONObject json= new JSONObject();

			json.put("uuid","123");
			json.put("pageNum",2);
			json.put("pageSize",10);

			json.put("mainTableName","RD_LINK");
			json.put("searchTableName","RD_LINK");
			json.put("primaryKey","LINK_PID");
			json.put("foreignKey","LINK_PID");

			JSONObject jsonCondition1 = new JSONObject();

			jsonCondition1.put("fieldType","Integer");
			jsonCondition1.put("operator",">");
			jsonCondition1.put("fieldName","kind");
			jsonCondition1.put("value",0);

			JSONObject jsonCondition2 = new JSONObject();

			jsonCondition2.put("fieldType","Integer");
			jsonCondition2.put("operator","<");
			jsonCondition2.put("fieldName","kind");
			jsonCondition2.put("value",7);

			JSONArray jsonConditions= new JSONArray();//json.getJSONArray("conditions");

			jsonConditions.add(jsonCondition1);
			jsonConditions.add(jsonCondition2);

			json.put("conditions",jsonConditions);

			JSONArray jsonMeshId= new JSONArray();
			jsonMeshId.add(595644);
			json.put("meshIds",jsonMeshId);

//			JSONArray jsonGridId= new JSONArray();
//			jsonGridId.add(59564401);
//			jsonGridId.add(59564402);
//			json.put("gridIds",jsonGridId);

			JSONObject  pids = p.loadByElementCondition(json);


		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void SearchAllObject3() {
		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(13);

			SearchAllObject p = new SearchAllObject(conn);

			JSONObject json= new JSONObject();

			json.put("uuid","123");
			json.put("pageNum",2);
			json.put("pageSize",10);

			json.put("mainTableName","RD_LINK");
			json.put("searchTableName","RD_LINK");
			json.put("primaryKey","LINK_PID");
			json.put("foreignKey","LINK_PID");

			JSONArray jsonConditions= new JSONArray();//json.getJSONArray("conditions");

			json.put("conditions",jsonConditions);


//
//			JSONArray jsonMeshId= new JSONArray();
//			jsonMeshId.add(595644);
//			json.put("meshIds",jsonMeshId);

			JSONArray jsonGridId= new JSONArray();
			jsonGridId.add(59564401);
			json.put("gridIds",jsonGridId);

			JSONObject  pids = p.loadByElementCondition(json);


		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void SearchAllObject4() {
		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(13);

			SearchAllObject p = new SearchAllObject(conn);

			JSONObject json= new JSONObject();

			json.put("uuid","123");
			json.put("pageNum",2);
			json.put("pageSize",10);

			json.put("mainTableName","RD_LINK");
			json.put("searchTableName","RD_LINK");
			json.put("primaryKey","LINK_PID");
			json.put("foreignKey","LINK_PID");

			JSONObject jsonCondition1 = new JSONObject();

			jsonCondition1.put("fieldType","Integer");
			jsonCondition1.put("operator","IN");
			jsonCondition1.put("fieldName","kind");

			JSONArray jsonValues= new JSONArray();
			jsonValues.add(1);
			jsonValues.add(2);
			jsonValues.add(3);
			jsonValues.add(4);
			jsonValues.add(5);
			jsonValues.add(6);
			jsonCondition1.put("values",jsonValues);

			JSONObject jsonCondition2 = new JSONObject();

			jsonCondition2.put("fieldType","Integer");
			jsonCondition2.put("operator","=");
			jsonCondition2.put("fieldName","DIRECT");
			jsonCondition2.put("value",1);

			JSONArray jsonConditions= new JSONArray();//json.getJSONArray("conditions");

			jsonConditions.add(jsonCondition1);
			jsonConditions.add(jsonCondition2);

			json.put("conditions",jsonConditions);

			JSONArray jsonMeshId= new JSONArray();
			jsonMeshId.add(595644);
			json.put("meshIds",jsonMeshId);

//			JSONArray jsonGridId= new JSONArray();
//			jsonGridId.add(59564401);
//			jsonGridId.add(59564402);
//			json.put("gridIds",jsonGridId);

			JSONObject  pids = p.loadByElementCondition(json);


		} catch (Exception e) {
			e.printStackTrace();
		}


	}

	@Test
	public void SearchAllObject5() {
		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(13);

			SearchAllObject p = new SearchAllObject(conn);

			JSONObject json= new JSONObject();
			json.put("uuid","123");
			json.put("mainTableName","RD_LINK");
			json.put("searchTableName","RD_LINK_FORM");


			json.put("pageNum",2);
			json.put("pageSize",10);

			JSONObject jsonCondition1 = new JSONObject();

			jsonCondition1.put("fieldType","Integer");
			jsonCondition1.put("operator","IN");
			jsonCondition1.put("fieldName","FORM_OF_WAY");

			JSONArray jsonValues= new JSONArray();
			jsonValues.add(1);
			jsonValues.add(2);
			jsonValues.add(10);
			jsonValues.add(11);
			jsonValues.add(12);
			jsonValues.add(50);

			jsonCondition1.put("values",jsonValues);

			JSONObject jsonCondition2 = new JSONObject();

			jsonCondition2.put("fieldType","Integer");
			jsonCondition2.put("operator","=");
			jsonCondition2.put("fieldName","AUXI_FLAG");
			jsonCondition2.put("value",0);

			JSONArray jsonConditions= new JSONArray();//json.getJSONArray("conditions");

			jsonConditions.add(jsonCondition1);
			jsonConditions.add(jsonCondition2);

			json.put("conditions",jsonConditions);

//			meshIds

//			JSONArray jsonMeshId= new JSONArray();
//			jsonMeshId.add();
//			jsonMeshId.add();

			JSONArray jsonGridId= new JSONArray();
			jsonGridId.add(59564401);
			jsonGridId.add(59564402);
			json.put("gridIds",jsonGridId);

			JSONObject  pids = p.loadByElementCondition(json);


		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void SearchGeolive() throws Exception {

		try {
			String str="123";

			str +="456"+null;

			GeoliveHelper geoliveHelper = GeoliveHelper.getIstance();

			String foreignKey = geoliveHelper.getForeignKey("RD_LINK_FORM", "RD_LINK");

			String primaryKey = geoliveHelper.getPrimaryKey("RD_LINK");

			SearchAllObject p = new SearchAllObject();

			JSONObject result;

			JSONObject condition1 =new JSONObject();

			condition1.put("searchType","PARENT_TABLE_LABLE");

			result = p.getGeoLiveInfo(condition1);

			JSONObject condition2 =new JSONObject();

			condition2.put("searchType","TABLE_LABLE");

			condition2.put("tableName","RD_LINK");

			result = p.getGeoLiveInfo(condition2);

			JSONObject condition3 =new JSONObject();

			condition3.put("searchType","TABLE_INFO");

			condition3.put("tableName","RD_LINK_FORM");

			result = p.getGeoLiveInfo(condition3);



			System.out.println(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void SearchAllObject6() {
		Connection conn;
		try {

			String parameter = "{\"dbId\":13,\"uuid\":\"123\",\"mainTableName\":\"RD_LANE_CONNEXITY\",\"searchTableName\":\"RD_LANE_TOPOLOGY\",\"pageNum\":4,\"pageSize\":10,\"gridIds\":[59564401,59564402],\"meshIds\":[595644],\"conditions\":[{\"fieldType\":\"Integer\",\"operator\":\"=\",\"fieldName\":\"RELATIONSHIP_TYPE\",\"value\":1}]}";

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			conn = DBConnector.getInstance().getConnectionById(13);

			SearchAllObject p = new SearchAllObject(conn);

			JSONObject  pids = p.loadByElementCondition(jsonReq);


		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void SearchAllObject7() {
		Connection conn;
		try {

			String parameter = "{\"dbId\":13,\"uuid\":\"123\",\"mainTableName\":\"RD_BRANCH\",\"searchTableName\":\"RD_BRANCH\",\"pageNum\":1,\"pageSize\":10,\"gridIds\":[59564401,59564402],\"meshIds\":[595644],\"conditions\":[{\"fieldType\":\"Integer\",\"operator\":\"=\",\"fieldName\":\"RELATIONSHIP_TYPE\",\"value\":1}]}";


			JSONObject jsonReq = JSONObject.fromObject(parameter);

			conn = DBConnector.getInstance().getConnectionById(13);

			SearchAllObject p = new SearchAllObject(conn);

			JSONObject  pids = p.loadByElementCondition(jsonReq);


		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void SearchAllObject8() {
		Connection conn;
		try {

			String parameter = "{\"dbId\":13,\"uuid\":\"123\",\"mainTableName\":\"RD_BRANCH\",\"searchTableName\":\"RD_BRANCH_DETAIL\",\"pageNum\":1,\"pageSize\":10,\"gridIds\":[59564401,59564402],\"meshIds\":[595644],\"conditions\":[{\"fieldType\":\"Integer\",\"operator\":\"=\",\"fieldName\":\"BRANCH_TYPE\",\"value\":1}]}";



			JSONObject jsonReq = JSONObject.fromObject(parameter);

			conn = DBConnector.getInstance().getConnectionById(13);

			SearchAllObject p = new SearchAllObject(conn);

			JSONObject  result = p.loadByElementCondition(jsonReq);


			System.out.println(result);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void SearchAllObject9() {
		Connection conn;
		try {

			String parameter = "{\"dbId\":13,\"uuid\":\"123\",\"mainTableName\":\"RD_SAMELINK\",\"searchTableName\":\"RD_SAMELINK_PART\",\"pageNum\":1,\"pageSize\":10,\"gridIds\":[59564401,59564402],\"meshIds\":[595644],\"conditions\":[{\"fieldType\":\"String\",\"operator\":\"=\",\"fieldName\":\"TABLE_NAME\",\"value\":\"RD_LINK\"}]}";


			JSONObject jsonReq = JSONObject.fromObject(parameter);

			conn = DBConnector.getInstance().getConnectionById(13);

			SearchAllObject p = new SearchAllObject(conn);

			JSONObject  result = p.loadByElementCondition(jsonReq);


			System.out.println(result);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void SearchAllObject10() {
		Connection conn;
		try {

			String parameter = "{\"dbId\":13,\"uuid\":\"123\",\"mainTableName\":\"RD_SAMENODE\",\"searchTableName\":\"RD_SAMENODE_PART\",\"pageNum\":1,\"pageSize\":10,\"gridIds\":[59564401,59564402],\"meshIds\":[595644],\"conditions\":[{\"fieldType\":\"String\",\"operator\":\"=\",\"fieldName\":\"TABLE_NAME\",\"value\":\"RD_NODE\"}]}";


			JSONObject jsonReq = JSONObject.fromObject(parameter);

			conn = DBConnector.getInstance().getConnectionById(13);

			SearchAllObject p = new SearchAllObject(conn);

			JSONObject  result = p.loadByElementCondition(jsonReq);


			System.out.println(result);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void SearchAllObject11() {
		Connection conn;
		try {

			String parameter = "{\"dbId\":13,\"uuid\":\"123\",\"mainTableName\":\"RD_CROSS\",\"searchTableName\":\"RD_CROSS\",\"pageNum\":1,\"pageSize\":10,\"gridIds\":[59564401,59564402],\"meshIds\":[595644],\"conditions\":[{\"fieldType\":\"String\",\"operator\":\"=\",\"fieldName\":\"TYPE\",\"value\":1}]}";

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			conn = DBConnector.getInstance().getConnectionById(13);

			SearchAllObject p = new SearchAllObject(conn);

			JSONObject  result = p.loadByElementCondition(jsonReq);


			System.out.println(result);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
