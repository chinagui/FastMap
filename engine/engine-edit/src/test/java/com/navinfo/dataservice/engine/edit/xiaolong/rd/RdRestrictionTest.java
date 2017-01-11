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
		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDRESTRICTION\",\"dbId\":17,\"data\":{\"nodePid\":15193779,\"inLinkPid\":19953004,\"infos\":[{\"arrow\":[4],\"outLinkPid\":1780407},{\"arrow\":[2]}],\"restricType\":0}}";
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
		String parameter = "{ \"command\": \"UPDATE\", \"type\": \"RDRESTRICTION\", \"dbId\": 19, \"data\": { \"restricInfo\": \"3,[4]\", \"pid\": 302000049, \"objStatus\": \"UPDATE\", \"details\": [{ \"pid\": 0, \"restricPid\": 0, \"outLinkPid\": 0, \"flag\": 1, \"restricInfo\": 3, \"type\": 1, \"relationshipType\": 1, \"conditions\": [], \"vias\": [], \"objStatus\": \"INSERT\" }] } }";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
