package com.navinfo.dataservice.engine.edit.xiaolong.ad;

import java.sql.Connection;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.pool.GlmDbPoolManager;
import com.navinfo.dataservice.engine.edit.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.edit.search.SearchProcess;

public class AdAdminGroupTest {
	
	public static void searchAdminGroupLevel() {
		Connection conn;
		try {
			conn = GlmDbPoolManager.getInstance().getConnection(11);

			String parameter = "{\"type\":\"ADADMINGROUP\",\"projectId\":11,\"data\":{\"projectId\":11}}";

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
		searchAdminGroupLevel();
	}
}
