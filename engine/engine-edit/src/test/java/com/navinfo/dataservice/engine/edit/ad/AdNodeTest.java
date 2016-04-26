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
		String parameter = "{\"command\":\"CREATE\",\"type\":\"ADNODE\",\"projectId\":11,\"objId\":100031211,\"data\":{\"longitude\":116.22590,\"latitude\":39.77897}}";
		Transaction t = new Transaction(parameter);;
		String msg = t.run();
	}
	public void deleteAdNodeTest() throws Exception{
		String parameter = "{\"command\":\"DELETE\",\"type\":\"ADNODE\",\"projectId\":11,\"objId\":100021717}}";
		Transaction t = new Transaction(parameter);;
		String msg = t.run();
	}

	public static void main(String[] args) throws Exception{
		//new AdNodeTest().createAdNodeTest();
		new AdNodeTest().deleteAdNodeTest();

		
	}
}
