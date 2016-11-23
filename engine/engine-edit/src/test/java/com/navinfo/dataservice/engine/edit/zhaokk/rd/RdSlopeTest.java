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

public class RdSlopeTest extends InitApplication{
	
	@Override
	@Before
	public void init() {
		initContext();
	}
	
	private Connection conn;
	public RdSlopeTest() throws Exception {
		//this.conn = DBConnector.getInstance().getConnectionById(11);
		//parameter={"command":"CREATE","dbId":42,"type":"RDSLOPE","data":{"nodePid":100022836,"linkPid":100007426,"linkPids":[100007427]}}
	}
	@Test
	public void TestAdd() {
		//parameter:{"command":"UPDATE","type":"RDSLOPE","dbId":17,"data":{"objStatus":"UPDATE","pid":208000006,"linkPids":[220002832,22
		String parameter = "{\"command\":\"UPDATE\",\"type\":\"RDSLOPE\",\"dbId\":17,\"data\":{\"pid\": 201000004,\"linkPids\":[304002717,202002676]}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
}
