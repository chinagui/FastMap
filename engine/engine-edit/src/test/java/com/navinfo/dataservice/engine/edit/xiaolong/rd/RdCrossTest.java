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
		String parameter = "{\"command\":\"UPDATE\",\"type\":\"RDCROSS\",\"dbId\":42,\"data\":{\"names\":[{\"nameGroupid\":1,\"nameId\":1,\"langCode\":\"CHI\",\"name\":\"ces\",\"phonetic\":\"\",\"srcFlag\":0,\"objStatus\":\"INSERT\"}],\"pid\":46991877}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
