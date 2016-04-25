package com.navinfo.dataservice.engine.edit.rdgsc;

import java.sql.Connection;

import com.navinfo.dataservice.commons.db.ConfigLoader;
import com.navinfo.dataservice.commons.service.PidService;
import com.navinfo.dataservice.commons.util.ResponseUtils;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.search.RdGscSearch;
import com.navinfo.dataservice.dao.pool.GlmDbPoolManager;
import com.navinfo.dataservice.engine.edit.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.edit.search.SearchProcess;

import net.sf.json.JSONObject;

public class RdGscTest {

	private static final String configPath = "D:/ws_new/DataService/web/edit-web/src/main/resources/config.properties";
	
	static 
	{
		ConfigLoader.initDBConn(configPath);
	}
	public RdGscTest() throws Exception {
	}

	public void testCreate() {
		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDGSC\",\"projectId\":11,\"data\":{\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[116.3964195549488,39.90916838843326],[116.3964195549488,39.90920542201831],[116.3964933156967,39.90920542201831],[116.3964933156967,39.90916838843326],[116.3964195549488,39.90916838843326]]]},\"linkObjs\":[{\"pid\":\"100002361\",\"level_index\":0},{\"pid\":\"100002362\",\"level_index\":1}]}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void testUpdateAdadmin() {
		String parameter = "{\"command\":\"UPDATE\",\"type\":\"ADADMIN\",\"projectId\":11,\"data\":{\"population\":2,\"pid\":3538,\"objStatus\":\"UPDATE\"}}";
		try {
			Transaction t = new Transaction(parameter);

			String msg = t.run();

			String log = t.getLogs();

			JSONObject json = new JSONObject();

			json.put("result", msg);

			json.put("log", log);

			json.put("check", t.getCheckLog());

			json.put("pid", t.getPid());

			System.out.println(ResponseUtils.assembleRegularResult(json));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void testSearch() {
		int pid = 100002452;
		String parameter = "{\"projectId\":11,\"type\":\"RDGSC\",\"pid\":100002452}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void searchAdminGroupLevel()
	{
		Connection conn;
		try {
			conn = GlmDbPoolManager.getInstance().getConnection(11);
			
			String parameter = "{\"projectId\":11,\"type\":\"ADADMINGROUP\"}";
			
			JSONObject jsonReq = JSONObject.fromObject(parameter);
			
			SearchProcess p = new SearchProcess(conn);
			
			System.out.println(p.searchDataByCondition(ObjType.ADADMINGROUP, jsonReq));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	public static void main(String[] args) {
		try {
			searchAdminGroupLevel();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
