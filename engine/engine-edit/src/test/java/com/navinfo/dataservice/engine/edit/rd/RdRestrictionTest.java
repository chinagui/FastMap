package com.navinfo.dataservice.engine.edit.rd;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.engine.edit.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.xiaolong.InitApplication;

public class RdRestrictionTest extends InitApplication{
	
	@Override
	@Before
	public void init() {
		initContext();
	}
	
	@Test
	public void testAddRestriction() {
		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDRESTRICTION\",\"projectId\":11,\"data\":{\"inLinkPid\":577220,\"nodePid\":462669,\"outLinkPids\":[582725],\"infos\":[1,2]}}";
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
		String parameter = "{\"command\":\"DELETE\",\"type\":\"RDRESTRICTION\",\"projectId\":11,\"objId\": 100000133}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
