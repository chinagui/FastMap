package com.navinfo.dataservice.engine.edit.luyao;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.search.rd.utils.RdLinkSearchUtils;

public class departNodeTest extends InitApplication {

	@Override
	@Before
	public void init() {
		initContext();
	}

	private Connection conn;

	public departNodeTest() throws Exception {
		// this.conn = DBConnector.getInstance().getConnectionById(11);
	}



	@Test
	public void departNode() throws Exception {

		String parameter = "{\"command\":\"DEPART\",\"type\":\"RDLINK\",\"dbId\":17,\"objId\":202000088,"
				+ "\"data\":{\"linkPid\":320000125,\"catchNodePid\":310000099,\"longitude\":116.26585,\"latitude\":40.54694}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	@Test
	public void departNode_1() throws Exception {

		String parameter = "{\"command\":\"DEPART\",\"type\":\"RDLINK\",\"dbId\":17,\"objId\":303000092,"
				+ "\"data\":{\"linkPid\":309000132,\"catchNodePid\":320000089,\"longitude\":116.12935,\"latitude\":40.55974}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void departNode_2() throws Exception {

		String parameter = "{\"command\":\"DEPART\",\"type\":\"RDLINK\",\"dbId\":17,\"objId\":208000111S,"
				+ "\"data\":{\"linkPid\":309000132,\"catchNodePid\":320000089,\"longitude\":116.12935,\"latitude\":40.55974}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void departNode_3() throws Exception {

		String parameter = "{\"command\":\"DEPART\",\"type\":\"RDLINK\",\"dbId\":17,\"objId\":210000099,"
				+ "\"data\":{\"linkPid\":300000091,\"catchNodePid\":320000089,\"longitude\":116.12935,\"latitude\":40.55974}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
