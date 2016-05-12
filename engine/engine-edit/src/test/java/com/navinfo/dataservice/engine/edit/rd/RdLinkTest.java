package com.navinfo.dataservice.engine.edit.rd;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.navinfo.dataservice.commons.db.ConfigLoader;
import com.navinfo.dataservice.engine.edit.edit.operation.Transaction;

public class RdLinkTest {
private static final String configPath = "H:/GitHub/DataService/web/edit-web/src/main/resources/config.properties";
	
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
		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDLINK\"\"projectId\":11,\"data\":{\"eNodePid\":0,\"sNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.48393869400024,40.00499358057962],[116.4865028858185,40.00443474312923],[116.4865028858185,40.00443474312923]]},\"catchLinks\":[]},\"type\":\"RDLINK\"}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void departRdLink()
	{
		String parameter =  "{\"command\":\"UPDOWNDEPART\",\"type\":\"RDLINK\",\"distance\":50,\"projectId\":11,\"data\":{\"linkPids\":[100002627,100002629]}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void testSet(){
		List<Boolean> booleans = new ArrayList<Boolean>();
		booleans.add(false);
		booleans.add(false);
		booleans.add(false);
		booleans.add(true);
		if(!booleans.contains(true)){
			System.out.println("kkv5");
		}
		
	}
	
	public static void main(String[] args) {
		try {
			new RdLinkTest().departRdLink();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
