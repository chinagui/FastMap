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

public class RdLinkTest extends InitApplication {

	@Override
	@Before
	public void init() {
		initContext();
	}

	private Connection conn;

	public RdLinkTest() throws Exception {
		// this.conn = DBConnector.getInstance().getConnectionById(11);
	}

	public void testDelete() {
		String parameter = "{\"command\":\"DELETE\",\"type\":\"RDLINK\",\"projectId\":11,\"objId\":100002773}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testAddRdLink() {
		//{"command":"CREATE","dbId":17,"data":{"eNodePid":0,"sNodePid":0,"geometry":{"type":"LineString","coordinates":[[116.3859374821186,40.00522677058955],[116.3860179677389,40.005098137296194],[116.38613194227219,40.005007962365426],[116.38617365799722,40.00510596742316],[116.3862982392311,40.00520725251356],[116.38636246104478,40.00511552385705],[116.38639479875565,40.00502645322839]]},"catchLinks":[{"linkPid":205002674,"lon":116.3860179677389,"lat":40.005098137296194},{"linkPid":205002674,"lon":116.38617365799722,"lat":40.00510596742316},{"linkPid":205002674,"lon":116.38636246104478,"lat":40.00511552385705}]},"type":"RDLINK"}
		String parameter ="{\"command\":\"CREATE\",\"dbId\":17,\"data\":{\"eNodePid\":0,\"sNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.49890139698982,40.07775023545874],[116.49942710995674,40.07774818310886]]},\"catchInfos\":[{\"nodePid\":203001965,\"longitude\":116.49890139698982,\"latitude\":40.07775023545874},{\"nodePid\":307001986,\"longitude\":116.49942710995674,\"latitude\":40.07774818310886}]},\"type\":\"RDLINK\"}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void testRepairLink() {
		//{"command":"REPAIR","dbId":19,"objId":206002732,"data":{"geometry":{"type":"LineString","coordinates":[[116.49890139698982,40.07775023545874],[116.49942710995674,40.07774818310886]]},"catchInfos":[{"nodePid":203001965,"longitude":116.49890139698982,"latitude":40.07775023545874},{"nodePid":307001986,"longitude":116.49942710995674,"latitude":40.07774818310886}]},"type":"RDLINK"}
		String parameter = "{\"command\":\"REPAIR\",\"type\":\"RDLINK\",\"dbId\":19,\"objId\":206002732,\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.49890139698982,40.07775023545874],[116.49942710995674,40.07774818310886]]},\"catchInfos\":[{\"nodePid\":203001965,\"longitude\":116.49890139698982,\"latitude\":40.07775023545874},{\"nodePid\":307001986,\"longitude\":116.49942710995674,\"latitude\":40.07774818310886}]}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void TrackRdLink() throws Exception {
		// 创建起始link LINESTRING (116.20091 39.84598, 116.20095 39.84568,
		// 116.20111 39.84551)
		// PID 100002627 s 100018779 e 100018780
		// 1.LINESTRING (116.20111 39.84551, 116.20122 39.84585) pid 100002628 s
		// 100018780 e 100018781
		// 2.LINESTRING (116.20111 39.84551, 116.20133 39.84551, 116.20156
		// 39.84551) pid 100002629 s 100018780 e 100018782
		// 3.LINESTRING (116.20111 39.84551, 116.20133 39.84536, 116.20166
		// 39.84544) pid 100002637 s 100018780 e 100018787
		// 4. LINESTRING (116.20111 39.84551, 116.20081 39.84565, 116.20083
		// 39.84554) pid 100002641 s 100018780 e 100018791
		int cuurentLinkPid = 100003385;
		int cruuentNodePidDir = 100019726;
		List<RdLink> links = new RdLinkSearchUtils(conn).getNextTrackLinks(
				cuurentLinkPid, cruuentNodePidDir);
		for (RdLink rdLink : links) {
			System.out.println(rdLink.getPid());
		}
	}

	@Test
	public void departRdLink() {
		
		//parameter:{"command":"UPDOWNDEPART","type":"RDLINK","dbId":17,"distance":"13.4","data":{"linkPids":[302000223,205000223]}}
		//String line = "[689377,689376,19689131,19689130,710604,710603,699493,704371,697069,648751,15827796,15827797,693579,693580,701090,19689128,19689129,705810,692581,87767844,87767843,690536,49047568,49047570,49047569,702861,689037,711053,19361357,689048,695032,691035,691034,691049,650228,650227,697023,697022,704239,701032,15480410,15480409,701279,701278,701277,689427,689426,697918,697916,697917,691038,17168486,17168487,17168488,54013231,54013235,54013236,54013238,695397,695396]";
		//String line = "[693580,701090,19689128,19689129]";
        //String line = "[690786,692316,692317]";
        String line = "[678037,700374]";
		
		String parameter = "{\"command\":\"UPDOWNDEPART\",\"type\":\"RDLINK\",\"distance\":13.4,\"dbId\":17,\"data\":{\"linkPids\":"
				+ line + "}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testSet() {
		List<Boolean> booleans = new ArrayList<Boolean>();
		booleans.add(false);
		booleans.add(false);
		booleans.add(false);
		booleans.add(true);
		if (!booleans.contains(true)) {
			System.out.println("kkv5");
		}

	}

	@Test
	public void breankRdLink() {
		// 05d420113061454b8b45083f2ff15dd8
    //{"command":"CREATE","dbId":17,"objId":202000173,"data":{"longitude":119.51203372122866,"latitude":39.80374531851753},"type":"RDNODE"}
		// AFABA38D96AC41E09E0A385F653C8FA0
		// 72ade565c57c4747986b6402382908d6
		// parameter:{"command":"BREAK","dbId":42,"objId":100007804,"data":{"longitude":116.64105676404851,"latitude":39.827078818848676},"type":"RDLINK"}
		// access_token:00000002IRBME52A8A7DE96FAC8D03B1AB9ECA00A2977965
		// parameter:{"command":"CREATE","dbId":42,"objId":100007804,"data":{"longitude":116.64105541766425,"latitude":39.827078822935626},"type":"RDNODE"}
		String parameter = "{\"command\":\"CREATE\",\"dbId\":17,\"objId\":202000173,\"data\":{\"longitude\":119.51203372122866,\"latitude\":39.80374531851753},\"type\":\"RDNODE\"}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testLoadTractLink() throws Exception {
		List<Integer> pids = new ArrayList<Integer>();
		pids.add(20465744);
		pids.add(20465745);
		pids.add(14226884);
		System.out.println(pids);
		RdLinkSelector linkSelector = new RdLinkSelector(conn);
		List<RdLink> links = linkSelector.loadByPids(pids, true);
		for (RdLink r : links) {
			System.out.println(r.getPid());
		}
	}

	@Test
	public void departNode() throws Exception {

		String parameter = "{\"command\":\"DEPART\",\"type\":\"RDLINK\",\"dbId\":17,"
				+ "\"data\":{\"objId\":220000040,\"linkPid\":307000039,\"catchNodePid\":0,\"longitude\":116.28849,\"latitude\":40.65765}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
