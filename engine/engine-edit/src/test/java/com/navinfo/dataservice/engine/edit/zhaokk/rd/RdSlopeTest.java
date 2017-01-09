package com.navinfo.dataservice.engine.edit.zhaokk.rd;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.search.rd.utils.RdLinkSearchUtils;

public class RdSlopeTest extends InitApplication {

	@Override
	@Before
	public void init() {
		initContext();
	}

	private Connection conn;

	public RdSlopeTest() throws Exception {
		// this.conn = DBConnector.getInstance().getConnectionById(11);
		// parameter={"command":"CREATE","dbId":42,"type":"RDSLOPE","data":{"nodePid":100022836,"linkPid":100007426,"linkPids":[100007427]}}
	}

	@Test
	public void TestAdd() {
		//{"command":"CREATE","dbId":19,"type":"RDSLOPE","data":{"nodePid":"202002401","linkPid":"308003137","linkPids":[],"length":164.556}}
		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDSLOPE\",\"dbId\":19,\"data\":{\"nodePid\":\"220002362\",\"linkPid\":\"303003129\",\"linkPids\":[306003124,309003108],\"length\":80}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
