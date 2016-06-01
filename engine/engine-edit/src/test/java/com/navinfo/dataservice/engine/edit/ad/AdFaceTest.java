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
				"\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[115.25151327252388,39.416733182082986],[115.25143951177597,39.4165850242258],[115.25185123085974,39.416572591384295],[115.25151327252388,39.416733182082986]]}}}";
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
