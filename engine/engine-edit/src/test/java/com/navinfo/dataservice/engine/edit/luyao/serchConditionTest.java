package com.navinfo.dataservice.engine.edit.luyao;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.util.ResponseUtils;
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

			String parameter = "{\"dbId\":17,\"type\":\"RDLINK\",\"data\":{\"queryType\":\"RDLINKSPEEDLIMIT\",\"linkPid\":220000997,\"direct\":2}}";

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

			String parameter = "{\"dbId\":17,\"gap\":10,\"types\":[\"rdLinkLimitTruck\",\"rdLinkLimit\"],\"z\":20,\"x\":861639,\"y\":396652}";

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

				// response.getWriter().println(
				// ResponseUtils.assembleRegularResult(data));
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

			String parameter = "{\"dbId\":17,\"gap\":10,\"types\":[\"rdLinkNameContent\",\"rdLinkNameType\"],\"z\":19,\"x\":431657,\"y\":198398}";

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

				// response.getWriter().println(
				// ResponseUtils.assembleRegularResult(data));
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
			conn = DBConnector.getInstance().getConnectionById(17);

			SearchProcess p = new SearchProcess(conn);

			List<ObjType> objType = new ArrayList<>();

			objType.add(ObjType.RDLINK);

			System.out.println(p.searchDataByTileWithGap(objType, 6744,
					3102, 13, 10));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	@Test
	public void getByCondition_variableSpeed() {

		Connection conn = null;

		try {

			String parameter = "{\"dbId\":17,\"type\":\"RDLINK\",\"data\":{\"queryType\":\"RDVARIABLESPEED\",\"linkPid\":306002745,\"nodePid\":204002212}}";

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

}
