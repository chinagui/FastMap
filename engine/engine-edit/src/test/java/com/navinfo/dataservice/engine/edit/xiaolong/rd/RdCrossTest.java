package com.navinfo.dataservice.engine.edit.xiaolong.rd;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.Transaction;

public class RdCrossTest extends InitApplication{
	
	@Override
	@Before
	public void init() {
		initContext();
	}
	
	@Test
	public void testAddCross() {
		String parameter = "{\"command\":\"UPDATE\",\"type\":\"RDRESTRICTION\",\"dbId\":17,\"data\":{\"restricInfo\":\"[1],1\",\"pid\":303000022,\"objStatus\":\"UPDATE\",\"details\":[{\"pid\":0,\"restricPid\":0,\"outLinkPid\":200002496,\"flag\":2,\"restricInfo\":1,\"type\":1,\"relationshipType\":1,\"conditions\":[],\"objStatus\":\"INSERT\"}]}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
