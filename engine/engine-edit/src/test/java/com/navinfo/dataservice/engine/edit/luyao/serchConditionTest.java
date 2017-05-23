package com.navinfo.dataservice.engine.edit.luyao;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.search.specialMap.SpecialMapUtils;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.search.SearchProcess;

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

			String parameter = "{\"dbId\":17,\"gap\":10,\"types\":[\"rdLinkFormOfWay10\",\"rdLinkNameType\"],\"z\":19,\"x\":431657,\"y\":198398}";

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
}
