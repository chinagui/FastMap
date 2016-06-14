package com.navinfo.dataservice.engine.edit.ad;


import org.apache.log4j.Logger;

import com.navinfo.dataservice.engine.edit.edit.operation.Transaction;

public class AdFaceTest {
	protected Logger log = Logger.getLogger(this.getClass());
	public  void createFaceTest() {
		String parameter = "{\"command\":\"CREATE\",\"type\":\"ADFACE\",\"projectId\":11," +
				"\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[117.01995000243186,39.250035216523884],[117.0199567079544,39.24992720818254],[117.020246386528,39.25004560193254],[117.01995000243186,39.250035216523884]]}}}";
		log.info(parameter);
		Transaction t = new Transaction(parameter);;
		try {
			String msg = t.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
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
	public static void main(String[] args) throws Exception{
		new AdFaceTest().createFaceByAdLInkTest();
		
	}
}
