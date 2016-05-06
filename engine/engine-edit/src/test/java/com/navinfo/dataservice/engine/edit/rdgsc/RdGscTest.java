package com.navinfo.dataservice.engine.edit.rdgsc;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.db.ConfigLoader;
import com.navinfo.dataservice.commons.util.ResponseUtils;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.pool.GlmDbPoolManager;
import com.navinfo.dataservice.engine.edit.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.edit.search.SearchProcess;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class RdGscTest {

	private static final String configPath = "D:/ws_new/DataService/web/edit-web/src/main/resources/config.properties";

	static {
		ConfigLoader.initDBConn(configPath);
	}

	public RdGscTest() throws Exception {
	}

	public static void testCreate() {
		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDGSC\",\"projectId\":11,\"data\":{\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[116.50103,39.99649],[116.50031,39.99654],[116.50133,39.99572],[116.50053,39.9958],[116.50103,39.99649]]]},\"linkObjs\":[{\"pid\":\"100002820\",\"level_index\":0},{\"pid\":\"100002819\",\"level_index\":1}]}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void testDelete()
	{
		String parameter = "{\"command\":\"DELETE\",\"type\":\"RDGSC\",\"projectId\":11,\"objId\":100002503}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void testSearch() throws Exception {
		String parameter = "{\"projectId\":11,\"type\":\"RDGSC\",\"pid\":100002452}";
		JSONObject jsonReq = JSONObject.fromObject(parameter);

		String objType = jsonReq.getString("type");

		int projectId = jsonReq.getInt("projectId");

		int pid = jsonReq.getInt("pid");

		SearchProcess p = new SearchProcess(
				GlmDbPoolManager.getInstance().getConnection(projectId));

		IObj obj = p.searchDataByPid(ObjType.valueOf(objType), pid);

		System.out.println(ResponseUtils.assembleRegularResult(obj.Serialize(ObjLevel.FULL)));
	}

	public static void main(String[] args) {
		try {
			testCreate();
			// testSearch();
			// testDelete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
