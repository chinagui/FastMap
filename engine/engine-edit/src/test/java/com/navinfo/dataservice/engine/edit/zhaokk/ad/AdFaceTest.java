package com.navinfo.dataservice.engine.edit.zhaokk.ad;


import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.Transaction;

public class AdFaceTest extends InitApplication {
	
	@Override
	@Before
	public void init() {
		initContext();
	}
	
	protected Logger log = Logger.getLogger(this.getClass());
	
	@Test
	public  void createFaceTest() {
		String parameter = "{\"command\":\"CREATE\",\"type\":\"ADFACE\",\"dbId\":43," +
				"\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[117.01995000243186,39.250035216523884],[117.0199567079544,39.24992720818254],[117.020246386528,39.25004560193254],[117.01995000243186,39.250035216523884]]}}}";
		log.info(parameter);
		Transaction t = new Transaction(parameter);;
		try {
			String msg = t.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	@Test
	public  void createFaceByAdLInkTest() {
		String parameter = "{\"command\":\"CREATE\",\"type\":\"ADFACE\",\"linkType\":\"ADLINK\",\"projectId\":11," +
				"\"data\":{\"linkPids\":[100032563,100032561,100032562]}}";
		log.info(parameter);
		Transaction t = new Transaction(parameter);;
		try {
			String msg = t.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
