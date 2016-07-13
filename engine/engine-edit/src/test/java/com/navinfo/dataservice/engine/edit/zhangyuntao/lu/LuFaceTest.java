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
		String parameter = "{\"command\":\"CREATE\",\"type\":\"LUFACE\",\"projectId\":11,\"dbId\":43,"
				+ "\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[117.01995000243186,39.250035216523884],[117.0199567079544,39.24992720818254],[117.020246386528,39.25004560193254],[117.01995000243186,39.250035216523884]]}}}";
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
