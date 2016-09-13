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
		String parameter = "{\"command\":\"UPDATE\",\"type\":\"RDCROSS\",\"dbId\":17,\"data\":{\"names\":[{\"pid\":307000000,\"nameGroupid\":3,\"nameId\":1,\"langCode\":\"CHI\",\"name\":\"路口名3\",\"phonetic\":\"Lu+Kou+Ming+San\",\"srcFlag\":0,\"objStatus\":\"INSERT\"},{\"nameGroupid\":1,\"rowId\":\"8DC951FB53CE420798B14B54833E08F3\",\"pid\":307000000,\"objStatus\":\"UPDATE\",\"nameId\":204000001,\"name\":\"路口名1\",\"phonetic\":\"Lu+Kou+Ming+Yi\"},{\"nameId\":305000001,\"rowId\":\"23128D3064EC4EA9BA98E76D39C86436\",\"pid\":307000000,\"objStatus\":\"UPDATE\",\"name\":\"路口名\",\"phonetic\":\"Lu+Kou+Ming\"},{\"nameGroupid\":2,\"rowId\":\"3E470D305B324E2294E3D419D2B1C1E0\",\"pid\":307000000,\"objStatus\":\"UPDATE\",\"nameId\":305000002,\"name\":\"路口名2\",\"phonetic\":\"Lu+Kou+Ming+Er\"}],\"pid\":307000000}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
