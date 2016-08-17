package com.navinfo.dataservice.engine.edit.luyao.rdDirectroute;

import java.sql.Connection;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.search.SearchProcess;

public class RdDirectrouteTest extends InitApplication{

	protected Logger log = Logger.getLogger(this.getClass());
	
	@Override
	public void init() {
		initContext();
	}
	

	@Test
	public void testGetByPid() {
		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(42);

			SearchProcess p = new SearchProcess(conn);

			System.out.println(p.searchDataByPid(ObjType.RDDIRECTROUTE, 100000007).Serialize(ObjLevel.BRIEF));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void createTest_1() throws Exception {
		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDDIRECTROUTE\",\"dbId\":42,\"data\":{\"inLinkPid\":100006668,\"nodePid\": 100023804,\"outLinkPid\":100006671}}";
		log.info(parameter);
		Transaction t = new Transaction(parameter);
		String msg = t.run();
	}
	
	@Test
	public void createTest_2() throws Exception {
		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDDIRECTROUTE\",\"dbId\":42,\"data\":{\"inLinkPid\":607312,\"nodePid\": 742411,\"outLinkPid\":607304}}";
		log.info(parameter);
		Transaction t = new Transaction(parameter);
		String msg = t.run();
	}
	@Test
	public void updateTest_1() throws Exception {
		String parameter = "{\"command\":\"UPDATE\",\"type\":\"RDDIRECTROUTE\",\"dbId\":42,\"data\":{\"flag\":2,\"processFlag\": 1,\"objStatus\":\"UPDATE\",\"pid\":100000000}}";
		log.info(parameter);
		Transaction t = new Transaction(parameter);	
		String msg = t.run();
	}
	
	@Test
	public void deleteTest_1() throws Exception {
		String parameter = "{\"command\":\"DELETE\",\"type\":\"RDDIRECTROUTE\",\"dbId\":42,\"objId\":100000000}";
		Transaction t = new Transaction(parameter);
		String msg = t.run();
	}

}
