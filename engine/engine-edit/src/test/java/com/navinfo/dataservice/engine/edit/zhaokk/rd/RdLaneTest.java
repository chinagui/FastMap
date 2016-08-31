package com.navinfo.dataservice.engine.edit.zhaokk.rd;

import org.junit.Before;
import org.junit.Test;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.Transaction;

public class RdLaneTest extends InitApplication {

	@Override
	@Before
	public void init() {
		initContext();
	}

	@Test
	public void testAddBatchRdLane() {
		String parameter = "{\"command\":\"BATCH\",\"type\":\"RDLANE\",\"dbId\":42,"
				+ "\"data\":{\"linkPids\":[100009699,100009700],\"laneDir\":2,\"laneInfos\":[{\"pid\":100000159,\"seqNum\":1,\"arrowDir\":\"o\"},{\"pid\":100000161,\"seqNum\":2,\"arrowDir\":\"t\"}]}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void testDelBatchRdLane() {
		String parameter = "{\"command\":\"DELETE\",\"type\":\"RDLANE\",\"dbId\":42,"
				+ "\"data\":{\"linkPid\":100009700,\"laneDir\":1}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
