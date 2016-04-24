package com.navinfo.dataservice.engine.edit.ad;


import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.db.ConfigLoader;
import com.navinfo.dataservice.engine.edit.edit.operation.Transaction;

public class AdNodeTest {
	
    public AdNodeTest(){
    	ConfigLoader
		.initDBConn("H:/GitHub/DataService/web/edit-web/src/main/resources/config.properties");
    }
	protected Logger log = Logger.getLogger(this.getClass());
	public void createAdNodeTest() throws Exception{
		//116.27701 39.76824
		//[116.22590 ,39.77897
		String parameter = "{\"command\":\"CREATE\",\"type\":\"ADNODE\",\"projectId\":11,\"objId\":100031211,\"data\":{\"longitude\":116.22590,\"latitude\":39.77897}}";
		Transaction t = new Transaction(parameter);;
		String msg = t.run();
	}
	public  void createAdLinkTest() {
		//fm 1151854524
		//100031210
		//[3] LINESTRING (116.22633 39.79070, 116.22590 39.77897, 116.22275 39.76482)
		String parameter = "{\"command\":\"CREATE\",\"type\":\"ADLINK\",\"projectId\":11," +
				"\"data\":{\"eNodePid\":0,\"sNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.22633 ,39.79070],[116.22590 ,39.77897],[116.22275 ,39.76482]]},\"catchLinks\":[]}}";
		log.info(parameter);
		Transaction t = new Transaction(parameter);;
		try {
			String msg = t.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	public  void deleteAdLinkTest() {
		//fm 1151854524
		//100031210
		//[3] LINESTRING (116.22633 39.79070, 116.22590 39.77897, 116.22275 39.76482)
		String parameter = "{\"command\":\"DELETE\",\"type\":\"ADLINK\",\"projectId\":11,\"objId\":4107915 }";
		log.info(parameter);
		Transaction t = new Transaction(parameter);;
		try {
			String msg = t.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	public  void createFaceTest() {
		String parameter = "{\"command\":\"CREATE\",\"type\":\"ADFACE\",\"projectId\":11," +
				"\"data\":{\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[116.30551,39.96853],[ 116.29007,39.95719],[116.30732,39.95042],[116.30551,39.96853]]]}}}";
		log.info(parameter);
		Transaction t = new Transaction(parameter);;
		try {
			String msg = t.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	public static void main(String[] args) throws Exception{
		//new AdNodeTest().createAdLinkTest();
		//new AdNodeTest().createAdNodeTest();
		new AdNodeTest().createAdLinkTest();
		
	}
}
