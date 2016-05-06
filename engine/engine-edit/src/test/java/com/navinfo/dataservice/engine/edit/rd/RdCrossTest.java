package com.navinfo.dataservice.engine.edit.rd;

import com.navinfo.dataservice.commons.db.ConfigLoader;
import com.navinfo.dataservice.engine.edit.edit.operation.Transaction;

public class RdCrossTest {
	private static final String configPath = "D:/ws_new/DataService/web/edit-web/src/main/resources/config.properties";

	static {
		ConfigLoader.initDBConn(configPath);
	}

	public static void testAddCross() {
		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDCROSS\",\"projectId\":11,\"data\":{\"nodePids\":[\"13644693\"],\"linkPids\":[]}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		testAddCross();
	}
}
