package com.navinfo.dataservice.engine.edit.xiaolong.rd;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.Transaction;

public class RdRestrictionTest extends InitApplication{
	
	@Override
	@Before
	public void init() {
		initContext();
	}
	
	@Test
	public void testAddRestriction() {
		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDRESTRICTION\",\"dbId\":17,\"data\":{\"inLinkPid\":300000015,\"nodePid\":302000028,\"infos\":\"[3]\"}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testDeleteRestriction() {
		String parameter = "{\"command\":\"DELETE\",\"type\":\"RDRESTRICTION\",\"dbId\":42,\"objId\": 100000480}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
