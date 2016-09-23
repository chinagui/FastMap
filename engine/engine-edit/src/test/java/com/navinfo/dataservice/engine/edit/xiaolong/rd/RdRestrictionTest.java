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
		String parameter = "{\"command\":\"UPDATE\",\"type\":\"RDLANECONNEXITY\",\"dbId\":17,\"data\":{\"pid\":31986,\"objStatus\":\"UPDATE\",\"inLinkPid\":574126,\"nodePid\":470125,\"laneInfo\":\"[l],a,a,[c]\",\"leftExtend\":1,\"rightExtend\":1,\"topos\":[{\"options\":{},\"pid\":77620,\"objStatus\":\"UPDATE\",\"connexityPid\":31990,\"inLaneInfo\":4096,\"outLinkPid\":582698,\"reachDir\":3,\"relationshipType\":2},{\"options\":{},\"pid\":77621,\"objStatus\":\"UPDATE\",\"connexityPid\":31990,\"outLinkPid\":15443269,\"reachDir\":2,\"relationshipType\":2},{\"options\":{},\"pid\":8766549,\"objStatus\":\"UPDATE\",\"connexityPid\":31990,\"inLaneInfo\":32768,\"outLinkPid\":730636,\"reachDir\":4,\"relationshipType\":1},{\"options\":{},\"pid\":8766550,\"objStatus\":\"UPDATE\",\"connexityPid\":31990,\"inLaneInfo\":24576,\"outLinkPid\":574125,\"reachDir\":1,\"relationshipType\":1}]}}";
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
