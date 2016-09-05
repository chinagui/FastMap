package com.navinfo.dataservice.engine.edit.zhaokk.rd;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLane;
import com.navinfo.dataservice.dao.glm.selector.rd.lane.RdLaneSelector;
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
				+ "\"data\":{\"linkPids\":[100010969,100010976,100010977],\"laneDir\":2,\"laneInfos\":[{\"pid\":0,\"seqNum\":1,\"arrowDir\":\"a\"},{\"pid\":0,\"seqNum\":2,\"arrowDir\":\"b\"},{\"pid\":0,\"seqNum\":3,\"arrowDir\":\"c\"}]}}";
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
	@Test
	public void testLoadBatchRdLane() throws Exception {
		Connection conn= DBConnector.getInstance().getConnectionById(42);
		RdLaneSelector selector = new RdLaneSelector(conn);
		List<Integer> linkPids = new ArrayList<Integer>();
		linkPids.add(100009699);
		linkPids.add(100009700);
		
		List<RdLane> lanes = selector.loadByLinks(linkPids, 0, false);
		for(RdLane lane :lanes){
			System.out.println(lane.getPid() +"------" +lane.getLinkPid());
		}
	}
}
