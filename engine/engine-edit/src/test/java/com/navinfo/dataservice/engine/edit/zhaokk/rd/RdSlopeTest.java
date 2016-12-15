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
		//2016-12-09 18:08:42 INFO - parameter===={"command":"CREATE","dbId":17,"type":"RDSLOPE","data":{"nodePid":"209002321","linkPid":"304002985","linkPids":[202002910],"length":377.698}}
		//{"dbId":17,"command":"UPDATE","type":"RDSLOPE","objId":308000005,"linkPids":[304002729],"length":49.101,"data":{}}"
		// {"command":"CREATE","dbId":17,"type":"RDSLOPE","data":{"nodePid":"308002126","linkPid":"320002692","linkPids":[303002756],"length":261.724}}
		// parameter:{"command":"UPDATE","type":"RDSLOPE","dbId":17,"data":{"objStatus":"UPDATE","pid":208000006,"linkPids":[220002832,22
		//{"dbId":17,"command":"UPDATE","type":"RDSLOPE","objId":320000005,"linkPids":[201002944],"length":403.003,"data":{"objStatus":"UPDATE","linkPid":309002929}}
		//SysTem ID 
		
		String parameter = "{\"command\":\"UPDATE\",\"objId\":301000013,\"type\":\"RDSLOPE\",\"dbId\":17,\"data\":{\"objStatus\":\"UPDATE\",\"linkPid\":309002958},\"linkPids\":[],\"length\":14.291}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
