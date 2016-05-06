package com.navinfo.dataservice.engine.edit.rd;

import com.navinfo.dataservice.commons.db.ConfigLoader;
import com.navinfo.dataservice.engine.edit.edit.operation.Transaction;

public class RdLinkTest {
private static final String configPath = "D:/ws_new/DataService/web/edit-web/src/main/resources/config.properties";
	
	static 
	{
		ConfigLoader.initDBConn(configPath);
	}
	public RdLinkTest() throws Exception {
	}
	
	public void testDelete() {
		String parameter = "{\"command\":\"DELETE\",\"type\":\"RDLINK\",\"projectId\":11,\"objId\":100002773}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void testAddRdLink()
	{
		String parameter = "{\"command\":\"CREATE\",\"projectId\":11,\"data\":{\"eNodePid\":0,\"sNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.48393869400024,40.00499358057962],[116.4865028858185,40.00443474312923],[116.4865028858185,40.00443474312923]]},\"catchLinks\":[]},\"type\":\"RDLINK\"}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		try {
			new RdLinkTest().testAddRdLink();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
