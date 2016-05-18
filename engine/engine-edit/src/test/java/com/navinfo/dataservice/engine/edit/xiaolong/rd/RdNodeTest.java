package com.navinfo.dataservice.engine.edit.xiaolong.rd;

import com.navinfo.dataservice.commons.db.ConfigLoader;
import com.navinfo.dataservice.engine.edit.edit.operation.Transaction;

public class RdNodeTest {
	private static final String configPath = "D:/ws_new/DataService/web/edit-web/src/main/resources/config.properties";

	static {
		ConfigLoader.initDBConn(configPath);
	}

	public static void testMove() {
		String parameter = "{\"command\":\"MOVE\",\"projectId\":11,\"objId\":47039001,\"data\":{\"longitude\":116.47554874420166,\"latitude\":40.018470039395126},\"type\":\"RDNODE\"}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		testMove();
	}
}
