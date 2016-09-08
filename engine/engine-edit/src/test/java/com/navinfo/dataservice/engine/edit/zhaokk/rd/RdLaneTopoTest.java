package com.navinfo.dataservice.engine.edit.zhaokk.rd;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLane;
import com.navinfo.dataservice.dao.glm.selector.rd.lane.RdLaneSelector;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.Transaction;

public class RdLaneTopoTest extends InitApplication {

	@Override
	@Before
	public void init() {
		initContext();
	}

	@Test
	public void testAddBatchRdLaneTopo() {
		String parameter1 = "{\"command\":\"BATCH\",\"type\":\"RDLANETOPODETAIL\",\"dbId\":42,"
				+ "\"data\":{\"topoIds\":[100000002],\"inLinkPid\":100010969,\"inNodePid\":100028118,\"laneTopoInfos\":[]}}";
	
		
		
		
		String parameter = "{\"command\":\"BATCH\",\"type\":\"RDLANETOPODETAIL\",\"dbId\":42,"
				+ "\"data\":{\"topoIds\":[{\"topoId\":100000002}],\"inLinkPid\":100010969,\"inNodePid\":100028118,\"laneTopoInfos\":[{\"inLanePid\":100000193,\"outLanePid\":100000196,\"outLinkPid\":100010977,\"laneTopoVias\":[{\"lanePid\":100010977,\"seqNum\":1,\"linkPid\":100010976}]}]}}";
		Transaction t = new Transaction(parameter1);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	@Test
	public void testLoadBatchRdLane() throws Exception {
		Connection conn= DBConnector.getInstance().getConnectionById(17);
		RdLaneSelector selector = new RdLaneSelector(conn);
		List<Integer> linkPids = new ArrayList<Integer>();
		linkPids.add(85586670);
		linkPids.add(85586673);
		
		JSONArray arrays = selector.loadByLinks(linkPids,false);
		System.out.println(arrays);
		conn.close();
	}

}
