package com.navinfo.dataservice.engine.edit.rdgsc;

import java.sql.Connection;

import com.navinfo.dataservice.commons.db.ConfigLoader;
import com.navinfo.dataservice.commons.util.ResponseUtils;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.pool.GlmDbPoolManager;
import com.navinfo.dataservice.engine.edit.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.edit.search.SearchProcess;

import net.sf.json.JSONObject;

public class RdGscTest {

	private static final String configPath = "D:/ws_new/DataService/web/edit-web/src/main/resources/config.properties";

	static {
		ConfigLoader.initDBConn(configPath);
	}

	public RdGscTest() throws Exception {
	}

	public static void testCreate() {
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

	public static void searchAdminGroupLevel() {
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

	public static void deleteAdminGroupLevel() {
		Connection conn;
		try {
			conn = GlmDbPoolManager.getInstance().getConnection(11);

			String parameter = "{\"command\": \"DELETE\",\"type\": \"ADADMINGROUP\",\"projectId\": 11,\"data\": {\"groupTree\": [{\"regionId\": 1273,\"name\": \"中国大陆\",\"group\": {\"groupId\": 248,\"regionIdUp\": 1273,\"rowId\": \"2D71EFCB1966DCE7E050A8C083040693\"},\"children\": [{\"regionId\": 163,\"name\": \"北京市\",\"group\": {\"groupId\": 40,\"regionIdUp\": 163,\"rowId\": \"2D71EFCB16D7DCE7E050A8C083040693\"},\"part\": {\"groupId\": 248,\"regionIdDown\": 163,\"rowId\": \"2D71EFCB56BEDCE7E050A8C083040693\"},\"children\": [{\"regionId\": 580,\"name\": \"北京市\",\"group\": {\"groupId\": 114,\"regionIdUp\": 580,\"rowId\": \"2D71EFCB1711DCE7E050A8C083040693\"},\"part\": {\"groupId\": 40,\"regionIdDown\": 580,\"rowId\": \"2D71EFCB642CDCE7E050A8C083040693\"},\"children\": [{\"regionId\": 1421,\"name\": \"北京市区\",\"objType\": \"delete\",\"group\": {\"groupId\": 286,\"regionIdUp\": 1421,\"rowId\": \"2D71EFCB179FDCE7E050A8C083040693\"},\"part\": {\"groupId\": 114,\"regionIdDown\": 1421,\"objType\": \"delete\",\"rowId\": \"2D71EFCB679CDCE7E050A8C083040693\"}}]}]}]}]}}";

			Transaction t = new Transaction(parameter);
			try {
				String msg = t.run();
				System.out.println(msg);
			} catch (Exception e) {
				e.printStackTrace();
			}

			String parameter2 = "{\"projectId\":11,\"type\":\"ADADMINGROUP\"}";

			JSONObject jsonReq2 = JSONObject.fromObject(parameter2);

			SearchProcess p = new SearchProcess(conn);

			System.out.println(p.searchDataByCondition(ObjType.ADADMINGROUP, jsonReq2));

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		try {
			testCreate();
			//testSearch();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
