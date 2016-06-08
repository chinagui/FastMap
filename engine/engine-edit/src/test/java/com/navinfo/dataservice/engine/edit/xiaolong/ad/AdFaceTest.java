package com.navinfo.dataservice.engine.edit.xiaolong.ad;


import org.apache.log4j.Logger;

import com.navinfo.dataservice.engine.edit.edit.operation.Transaction;

public class AdFaceTest {
	protected Logger log = Logger.getLogger(this.getClass());
	public static void createFaceTest() {
		String parameter = "{\"command\":\"CREATE\",\"type\":\"ADFACE\",\"linkType\":\"ADLINK\",\"projectId\":11,\"data\":{\"linkPids\":[\"100031871\",\"100031872\",\"100031873\",\"100031874\"]}}";
		Transaction t = new Transaction(parameter);;
		try {
			String msg = t.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static void testDeleteFaceLink()
	{
		String parameter = "{\"command\":\"DELETE\",\"type\":\"ADLINK\",\"projectId\":11,\"objId\":  100031906}";
		Transaction t = new Transaction(parameter);;
		try {
			String msg = t.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void main(String[] args) throws Exception{
		testDeleteFaceLink();
	}
}
