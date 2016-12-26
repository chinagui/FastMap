package com.navinfo.dataservice.engine.edit.zhangyuntao.lu;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.util.ResponseUtils;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkName;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.search.SearchProcess;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class LuNodeTest extends InitApplication {

	@Override
	@Before
	public void init() {
		initContext();
	}

	@Test
	public void createLuNodeTest() throws Exception {
		String parameter = "{'command':'CREATE','dbId':43,'objId':100034532,'data':{'longitude':116.48378868782932,'latitude':40.30710911418436},'type':'LUNODE'}";
		Transaction t = new Transaction(parameter);
		;
		String msg = t.run();
	}

	@Test
	public void deleteLuNodeTest() throws Exception {
		String parameter = "{\"command\":\"DELETE\",\"type\":\"LUNODE\",\"dbId\":43,\"objId\":100034553}}";
		Transaction t = new Transaction(parameter);
		;
		String msg = t.run();
	}

	@Test
	public void updateLuNodeTest() throws Exception {
		String parameter = "{\"command\":\"UPDATE\",\"dbId\":43,\"type\":\"LUNODE\",\"projectId\":11,\"data\":{\"form\":\"7\",\"pid\":100034474,\"objStatus\":\"UPDATE\"}}";
		Transaction t = new Transaction(parameter);
		;
		String msg = t.run();
	}

	@Test
	public void moveLuNodeTest() throws Exception {
		// String parameter =
		// "{\"command\":\"MOVE\",\"dbId\":43,\"objId\":100034556,\"data\":{\"longitude\":116.47495463490485,\"latitude\":40.00968804544376},\"type\":\"LUNODE\"}";
		String parameter = "{\"command\":\"MOVE\",\"dbId\":17,\"objId\":305000037,\"data\":{\"longitude\":116.52064472436905,\"latitude\":40.083333333333336},\"type\":\"LUNODE\"}";
		Transaction t = new Transaction(parameter);
		;
		String msg = t.run();
	}

	@Test
	public void testSearchByGap() {
		String parameter = "{\"projectId\":42,\"gap\":80,\"types\":[\"RDLINK\"],\"z\":17,\"x\":107942,\"y\":49586}";
		JSONObject jsonReq = JSONObject.fromObject(parameter);

		JSONArray type = jsonReq.getJSONArray("types");

		int projectId = jsonReq.getInt("projectId");

		int x = jsonReq.getInt("x");

		int y = jsonReq.getInt("y");

		int z = jsonReq.getInt("z");

		int gap = jsonReq.getInt("gap");

		List<ObjType> types = new ArrayList<ObjType>();

		for (int i = 0; i < type.size(); i++) {
			types.add(ObjType.valueOf(type.getString(i)));
		}

		try {
			SearchProcess p = new SearchProcess(DBConnector.getInstance().getConnectionById(projectId));
			JSONObject data = p.searchDataByTileWithGap(types, x, y, z, gap);

			System.out.println(ResponseUtils.assembleRegularResult(data));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testSearchLuNode() {
		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(43);

			SearchProcess p = new SearchProcess(conn);

			System.out.println(p.searchDataByPid(ObjType.LUNODE, 100034532).Serialize(ObjLevel.FULL));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
