package com.navinfo.dataservice.engine.edit.xiaolong.rd;

import com.navinfo.dataservice.engine.edit.edit.operation.Transaction;

public class RdRestrictionTest {

	public static void testAddRestriction() {
		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDRESTRICTION\",\"projectId\":11,\"data\":{\"inLinkPid\":100004072,\"nodePid\":100020258,\"infos\":\"1\"}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void testDeleteRestriction() {
		String parameter = "{\"command\":\"DELETE\",\"type\":\"RDRESTRICTION\",\"projectId\":11,\"objId\": 100000186}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		testAddRestriction();
		//testDeleteRestriction();
	}
}
