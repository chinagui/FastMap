package com.navinfo.dataservice.engine.edit.zhangyuntao.lu;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.edit.operation.Transaction;

public class LuFaceTest extends InitApplication {

	@Override
	@Before
	public void init() {
		initContext();
	}

	protected Logger log = Logger.getLogger(this.getClass());

	@Test
	public void createFaceTest() {
		String parameter = "{\"command\":\"CREATE\",\"type\":\"LUFACE\",\"dbId\":43,\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.12778425216673,40.66462393693607],[116.12915754318236,40.665608653941],[116.12951159477232,40.66479483927713],[116.12778425216673,40.66462393693607]]}}}";
		log.info(parameter);
		Transaction t = new Transaction(parameter);
		;
		try {
			String msg = t.run();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void createFaceByAdLInkTest() {
		String parameter = "{\"command\":\"CREATE\",\"type\":\"LUFACE\",\"dbId\":43,\"linkType\":\"LULINK\",\"projectId\":11,"
				+ "\"data\":{\"linkPids\":[100034455,100034454]}}";
		log.info(parameter);
		Transaction t = new Transaction(parameter);
		;
		try {
			String msg = t.run();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
