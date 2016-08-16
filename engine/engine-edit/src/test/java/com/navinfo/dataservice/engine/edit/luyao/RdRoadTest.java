package com.navinfo.dataservice.engine.edit.luyao;

import java.sql.Connection;
import java.util.List;

import net.sf.json.JSONObject;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.util.ResponseUtils;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.search.RdRoadSearch;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.search.SearchProcess;

public class RdRoadTest extends InitApplication {

	@Override
	@Before
	public void init() {
		initContext();
	}

	@Test
	public void testRender() {
		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(42);

			RdRoadSearch search = new RdRoadSearch(conn);

			List<SearchSnapshot> data = search.searchDataByTileWithGap(107943,
					49613, 17, 80);

			System.out.println("data:"
					+ ResponseUtils.assembleRegularResult(data));

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testGetByPid() {
		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(42);

			SearchProcess p = new SearchProcess(conn);

			System.out.println(p.searchDataByPid(ObjType.RDROAD, 262960)
					.Serialize(ObjLevel.BRIEF));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testCreate1() {
		String parameter = "{\"command\":\"CREATE\",\"dbId\":42,\"type\":\"RDROAD\",\"data\":{\"linkPids\":[50113354,712683]}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();

			String log = t.getLogs();

			JSONObject json = new JSONObject();

			json.put("result", msg);

			json.put("log", log);

			json.put("check", t.getCheckLog());

			json.put("pid", t.getPid());

			System.out.println(json.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testUpdate1() {
		String parameter = "{\"command\":\"UPDATE\",\"dbId\":42,\"type\":\"RDROAD\",\"data\":{\"linkPids\":[50113354],\"pid\":100000007}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();

			String log = t.getLogs();

			JSONObject json = new JSONObject();

			json.put("result", msg);

			json.put("log", log);

			json.put("check", t.getCheckLog());

			json.put("pid", t.getPid());

			System.out.println(json.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testDel1() {
		String parameter = "{\"command\":\"DELETE\",\"type\":\"RDROAD\",\"dbId\":42,\"objId\":100000007}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();

			String log = t.getLogs();

			JSONObject json = new JSONObject();

			json.put("result", msg);

			json.put("log", log);

			json.put("check", t.getCheckLog());

			json.put("pid", t.getPid());

			System.out.println(json.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
