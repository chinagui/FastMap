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
		String parameter = "{\"command\":\"UPDATE\",\"type\":\"RDLANECONNEXITY\",\"dbId\":17,\"data\":{\"pid\":45621,\"objStatus\":\"UPDATE\",\"inLinkPid\":787094,\"nodePid\":595669,\"laneInfo\":\"b,b,[c]\",\"laneNum\":4,\"rightExtend\":1,\"topos\":[{\"pid\":94320,\"objStatus\":\"UPDATE\",\"connexityPid\":45620,\"inLaneInfo\":16384,\"outLinkPid\":807260,\"vias\":[{\"rowId\":\"3AE1F52F55F292F7E050A8C08304EE4C\",\"objStatus\":\"UPDATE\",\"linkPid\":787088,\"topologyId\":0},{\"rowId\":\"3AE1F52F55F392F7E050A8C08304EE4C\",\"objStatus\":\"UPDATE\",\"linkPid\":787064,\"seqNum\":1,\"topologyId\":0}]},{\"pid\":94321,\"objStatus\":\"UPDATE\",\"connexityPid\":45620,\"inLaneInfo\":8192,\"outLinkPid\":787063,\"vias\":[{\"rowId\":\"3AE1F52F55F492F7E050A8C08304EE4C\",\"groupId\":1,\"linkPid\":94274,\"seqNum\":1,\"topologyId\":94274,\"objStatus\":\"INSERT\"}]},{\"pid\":94322,\"objStatus\":\"UPDATE\",\"connexityPid\":45620,\"inLaneInfo\":49152,\"outLinkPid\":787064,\"reachDir\":2,\"vias\":[{\"rowId\":\"3AE1F52F18E492F7E050A8C08304EE4C\",\"groupId\":1,\"linkPid\":94273,\"seqNum\":1,\"topologyId\":94273,\"objStatus\":\"INSERT\"}]}]}}";
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
