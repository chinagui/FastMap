package com.navinfo.dataservice.engine.edit.xiaolong.rd;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.Transaction;

public class RdRestrictionTest extends InitApplication{
	
	@Override
	@Before
	public void init() {
		initContext();
	}
	
	@Test
	public void testAddRestriction() {
		String parameter = "{\"command\":\"UPDATE\",\"type\":\"RDLANECONNEXITY\",\"dbId\":17,\"data\":{\"topos\":[{\"vias\":[{\"rowId\":\"\",\"groupId\":1,\"linkPid\":28017067,\"seqNum\":1,\"topologyId\":97102,\"objStatus\":\"INSERT\"},{\"rowId\":\"\",\"groupId\":1,\"linkPid\":28017066,\"seqNum\":2,\"topologyId\":97102,\"objStatus\":\"INSERT\"},{\"rowId\":\"\",\"groupId\":1,\"linkPid\":28017061,\"seqNum\":3,\"topologyId\":97102,\"objStatus\":\"INSERT\"},{\"rowId\":\"\",\"groupId\":1,\"linkPid\":28017056,\"seqNum\":4,\"topologyId\":97102,\"objStatus\":\"INSERT\"},{\"rowId\":\"\",\"groupId\":1,\"linkPid\":28016955,\"seqNum\":5,\"topologyId\":97102,\"objStatus\":\"INSERT\"},{\"rowId\":\"\",\"groupId\":1,\"linkPid\":28017069,\"seqNum\":6,\"topologyId\":97102,\"objStatus\":\"INSERT\"},{\"rowId\":\"\",\"groupId\":1,\"linkPid\":28017071,\"seqNum\":7,\"topologyId\":97102,\"objStatus\":\"INSERT\"},{\"rowId\":\"\",\"groupId\":1,\"linkPid\":28017073,\"seqNum\":8,\"topologyId\":97102,\"objStatus\":\"INSERT\"},{\"rowId\":\"3AE1F52ED52792F7E050A8C08304EE4C\",\"objStatus\":\"UPDATE\",\"linkPid\":794607,\"seqNum\":9}],\"pid\":97102}],\"pid\":45519}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testDeleteRestriction() {
		String parameter = "{\"command\":\"DELETE\",\"type\":\"RDRESTRICTION\",\"dbId\":42,\"objId\": 100000480}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
