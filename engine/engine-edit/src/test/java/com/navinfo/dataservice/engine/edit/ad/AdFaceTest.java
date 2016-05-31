package com.navinfo.dataservice.engine.edit.ad;


import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.db.ConfigLoader;
import com.navinfo.dataservice.engine.edit.edit.operation.Transaction;

public class AdFaceTest {
	private static final String configPath = "H:/GitHub/zhaokk/DataService/web/edit-web/src/main/resources/config.properties";

    public AdFaceTest(){
    	ConfigLoader
		.initDBConn(configPath);
    }
	protected Logger log = Logger.getLogger(this.getClass());
	public  void createFaceTest() {
		String parameter = "{\"command\":\"CREATE\",\"type\":\"ADFACE\",\"projectId\":11," +
				"\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.49993538856506,39.89978075765668],[116.50024786591528,39.8997653248759],[116.4998871088028,39.89965832416705],[116.49993538856506,39.89978075765668]]}}}";
		log.info(parameter);
		Transaction t = new Transaction(parameter);;
		try {
			String msg = t.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	public static void main(String[] args) throws Exception{
		new AdFaceTest().createFaceTest();
		
	}
}
