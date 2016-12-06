package com.navinfo.dataservice.engine.edit.zhaokk.rd;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLane;
import com.navinfo.dataservice.dao.glm.selector.rd.lane.RdLaneSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.lane.RdLaneTopoDetailSelector;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.Transaction;

public class RdLaneTest extends InitApplication {

	@Override
	@Before
	public void init() {
		initContext();
	}

	// {"command":"BATCH","type":"RDLANE","dbId":17,"data":{"linkPids":["200000021"],"laneDir":2,"laneInfos":[{"pid":205000006,"conditions":[{"direction":"2","directionTime":"[[(t5t6t7)]]","vehicle":120,"vehicleTime":null,"geoLiveType":"RDLANECONDITION"}],"seqNum":1}],"geoLiveType":"RDLANE"}}
	@Test
	public void testAddBatchRdLane() {
		String parameter = "{\"command\":\"BATCH\",\"type\":\"RDLANE\",\"dbId\":17,"
				+ "\"data\":{\"linkPids\":[309000008],\"laneDir\":2,\"laneInfos\":[{\"pid\":0,\"seqNum\":1,\"arrowDir\":\"a\",\"conditions\":[{\"direction\":\"2\",\"directionTime\":\"[[(t5t6t7)]]\",\"vehicle\":120,\"vehicleTime\":null}]},{\"pid\":0,\"seqNum\":2,\"arrowDir\":\"c\"}]}}";
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
		Connection conn = DBConnector.getInstance().getConnectionById(17);
		RdLaneTopoDetailSelector selector = new RdLaneTopoDetailSelector(conn);
		List<Integer> linkPids = new ArrayList<Integer>();
		linkPids.add(280391);
		linkPids.add(280389);
		linkPids.add(292334);
		List<IRow> rows = selector.loadByLinkPids(linkPids, 228645, false);
		for (IRow row : rows) {
			System.out.println(row.toString());
		}
	}
}
