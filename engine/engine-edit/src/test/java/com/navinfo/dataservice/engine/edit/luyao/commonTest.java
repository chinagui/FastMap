package com.navinfo.dataservice.engine.edit.luyao;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.AngleCalculator;
import com.navinfo.dataservice.commons.util.ResponseUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.search.RdObjectSearch;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchSelector;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.search.SearchProcess;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;

public class commonTest extends InitApplication {

	@Override
	@Before
	public void init() {
		initContext();
	}

	@Test
	public void testGetByPid() {
		Connection conn;
		try {

			String parameter = "{\"dbId\":42,\"type\":\"RDBRANCH\",\"detailId\":0,\"rowId\":\"41937B0F3A6842929633FA164D077DDC\",\"branchType\":5}";
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			int dbId = jsonReq.getInt("dbId");

			conn = DBConnector.getInstance().getConnectionById(dbId);

			if (jsonReq.containsKey("detailId")) {
				int detailId = jsonReq.getInt("detailId");
				int branchType = jsonReq.getInt("branchType");
				String rowId = jsonReq.getString("rowId");
				RdBranchSelector selector = new RdBranchSelector(conn);
				IRow row = selector.loadByDetailId(detailId, branchType, rowId,
						false);

				if (row != null) {

					System.out.println(row.Serialize(ObjLevel.FULL));
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void createZoneFace() throws Exception {
		String parameter = "{\"command\":\"CREATE\",\"type\":\"ZONEFACE\",\"dbId\":42,\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.50073736906052,39.999875527018744],[116.50084733963013,40.00010770716541],[116.50126039981842,40.000105652477295],[116.50119334459303,39.9999412772289],[116.50108337402342,40.00005223056498],[116.50091439485549,39.99993716784262],[116.50090634822845,40.00005633994432],[116.50073736906052,39.999875527018744]]}}}";
		Transaction t = new Transaction(parameter);
		String msg = t.run();
	}

	@Test
	public void createRwNode() throws Exception {
		String parameter = "{\"command\":\"BREAK\",\"dbId\":42,\"objId\":100006675,\"data\":{\"longitude\":116.4776915626939,\"latitude\":40.013441046805035},\"type\":\"RWNODE\"}";
		Transaction t = new Transaction(parameter);
		String msg = t.run();
	}

	@Test
	public void getTitleWithGap_IXPOI() {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getConnectionById(8);

			SearchProcess p = new SearchProcess(conn);

			List<ObjType> objType = new ArrayList<>();

			objType.add(ObjType.IXPOI);

			System.out.println(p.searchDataByTileWithGap(objType, 107937,
					49616, 17, 80));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void createRdNode0801() throws Exception {
		// parameter:{\"command\":\"MOVE\",\"dbId\":42,\"objId\":100025193,\"data\":{\"longitude\":116.62476941943167,\"latitude\":39.999848815977785},\"type\":\"RDNODE\"}
		String parameter = "{\"command\":\"CREATE\",\"dbId\":42,\"data\":{\"eNodePid\":0,\"sNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.46844625473021,40.06658556344194],[116.47164344787598,40.06670872450823]]},\"catchLinks\":[]},\"type\":\"RDLINK\"}";

		Transaction t = new Transaction(parameter);
		String msg = t.run();
	}

	@Test
	public void create_0826_1() throws Exception {
		// parameter:{\"command\":\"MOVE\",\"dbId\":42,\"objId\":100025193,\"data\":{\"longitude\":116.62476941943167,\"latitude\":39.999848815977785},\"type\":\"RDNODE\"}
		String parameter = "{\"command\":\"CREATE\",\"dbId\":42,\"data\":{\"eNodePid\":0,\"sNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.39003992080687,40.03934507769796],[116.39385938644409,40.03917258290088]]},\"catchLinks\":[]},\"type\":\"RDLINK\"}";

		Transaction t = new Transaction(parameter);
		String msg = t.run();
	}

	@Test
	public void create_0826_2() throws Exception {
		// parameter:{\"command\":\"MOVE\",\"dbId\":42,\"objId\":100025193,\"data\":{\"longitude\":116.62476941943167,\"latitude\":39.999848815977785},\"type\":\"RDNODE\"}
		String parameter = "{\"command\":\"CREATE\",\"dbId\":42,\"data\":{\"eNodePid\":0,\"sNodePid\":100026325,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.3981831073761,40.052363068455136],[116.40051126480103,40.05151717991128]]},\"catchLinks\":[{\"nodePid\":100026325,\"lon\":116.3981831073761,\"lat\":40.052363068455136}]},\"type\":\"RDLINK\"}";
		Transaction t = new Transaction(parameter);
		String msg = t.run();
	}

	@Test
	public void create_0826_3() throws Exception {
		// parameter:{\"command\":\"MOVE\",\"dbId\":42,\"objId\":100025193,\"data\":{\"longitude\":116.62476941943167,\"latitude\":39.999848815977785},\"type\":\"RDNODE\"}
		String parameter = "{\"command\":\"CREATE\",\"dbId\":42,\"data\":{\"eNodePid\":0,\"sNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.39896094799042,40.05339372428832],[116.39964222908021,40.054350454598364]]},\"catchLinks\":[{\"linkPid\":100009127,\"lon\":116.39896094799042,\"lat\":40.05339372428832}]},\"type\":\"RDLINK\"}";

		Transaction t = new Transaction(parameter);
		String msg = t.run();
	}

	@Test
	public void create_0826_4() throws Exception {
		// parameter:{\"command\":\"MOVE\",\"dbId\":42,\"objId\":100025193,\"data\":{\"longitude\":116.62476941943167,\"latitude\":39.999848815977785},\"type\":\"RDNODE\"}
		String parameter = "{\"command\":\"CREATE\",\"dbId\":42,\"data\":{\"eNodePid\":0,\"sNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.39898796175908,40.05340591977955],[116.39977633953094,40.05463788168161]]},\"catchLinks\":[{\"linkPid\":100009127,\"lon\":116.3989772170534,\"lat\":40.05339774425346}]},\"type\":\"RDLINK\"}";

		Transaction t = new Transaction(parameter);
		String msg = t.run();
	}

	@Test
	public void updata0805_01() throws Exception {
		// parameter:{\"command\":\"MOVE\",\"dbId\":42,\"objId\":100025193,\"data\":{\"longitude\":116.62476941943167,\"latitude\":39.999848815977785},\"type\":\"RDNODE\"}
		String parameter = "{\"command\":\"UPDATE\",\"dbId\":42,\"type\":\"RDWARNINGINFO\",\"objId\":100000024,\"data\":{\"vehicle\":2415919105,\"rowId\":\"78B7FD0037F64D82BB2364A483F1590C\",\"pid\":100000024,\"objStatus\":\"UPDATE\"}}";
		Transaction t = new Transaction(parameter);
		String msg = t.run();
	}

	@Test
	public void getTitleWithGap_RdDirectroute() {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getConnectionById(42);

			SearchProcess p = new SearchProcess(conn);

			List<ObjType> objType = new ArrayList<>();

			objType.add(ObjType.RDDIRECTROUTE);

			System.out.println(p.searchDataByTileWithGap(objType, 107926,
					49598, 17, 80));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void getTitleWithGap_0818_1() {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getConnectionById(42);

			SearchProcess p = new SearchProcess(conn);

			List<ObjType> objType = new ArrayList<>();

			objType.add(ObjType.ZONELINK);

			System.out.println(p.searchDataByTileWithGap(objType, 431779,
					198455, 19, 80));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void getTitleWithGap_0818_2() {

		Connection conn = null;

		try {
			conn = DBConnector.getInstance().getConnectionById(42);

			SearchProcess p = new SearchProcess(conn);

			List<ObjType> objType = new ArrayList<>();

			objType.add(ObjType.LULINK);

			System.out.println(p.searchDataByTileWithGap(objType, 431779,
					198455, 19, 80));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void getByCondition() {

		Connection conn = null;

		try {

			String parameter = "{\"dbId\":42,\"type\":\"RDLINK\",\"data\":{\"queryType\":\"RDSPEEDLIMIT\",\"linkPid\":732235,\"direct\":3}}";

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String objType = jsonReq.getString("type");

			int dbId = jsonReq.getInt("dbId");

			JSONObject data = jsonReq.getJSONObject("data");

			conn = DBConnector.getInstance().getConnectionById(dbId);

			SearchProcess p = new SearchProcess(conn);

			JSONArray array = p.searchDataByCondition(ObjType.valueOf(objType),
					data);

			System.out.println(array);

		} catch (Exception e) {

			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Test
	public void getTitleWithGap_0819() {

		Connection conn = null;

		try {
			conn = DBConnector.getInstance().getConnectionById(42);

			SearchProcess p = new SearchProcess(conn);

			List<ObjType> objType = new ArrayList<>();

			objType.add(ObjType.RDLINKSPEEDLIMIT);

			System.out.println(p.searchDataByTileWithGap(objType, 107941,
					49613, 17, 80));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void createTest_1() throws Exception {

		String parameter = "{\"command\":\"BATCH\",\"dbId\":42,\"type\":\"RDLINKSPEEDLIMIT\",\"data\":{\"direct\":2,\"linkPids\":[100008842,100008844,100008845],\"linkSpeedLimit\":{\"speedType\":0,\"fromSpeedLimit\":100,\"fromLimitSrc\":2,\"toSpeedLimit\":100,\"toLimitSrc\":3,\"speedClassWork\":1}}}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void createTest_0822() throws Exception {

		String parameter = "{\"command\":\"BATCH\",\"type\":\"RDLINKSPEEDLIMIT\",\"dbId\":42,\"data\":{\"linkPids\":[88026339,732545,735601],\"direct\":2,\"linkSpeedLimit\":{\"speedType\":0,\"fromSpeedLimit\":60,\"toSpeedLimit\":0,\"speedClassWork\":1}}}";
		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_0829() throws Exception {

		String parameter = "{\"command\":\"CREATE\",\"dbId\":1005,\"objId\":87669302,\"data\":{\"longitude\":116.37677623850006,\"latitude\":40.03536495436415},\"type\":\"RDNODE\"}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_0829_1() throws Exception {

		String parameter = "{\"command\":\"CREATE\",\"dbId\":42,\"objId\":100009697,\"data\":{\"longitude\":116.38704563998596,\"latitude\":40.04266076687047},\"type\":\"RDNODE\"}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_0831_1() throws Exception {

		String parameter = "{\"command\":\"CREATE\",\"dbId\":42,\"objId\":100009891,\"data\":{\"longitude\":116.38622093892067,\"latitude\":40.04213625094478},\"type\":\"RDNODE\"}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void test_List_Sub() throws Exception {
		List<Integer> listPid = new ArrayList<Integer>();

		listPid.add(1);
		listPid.add(2);
		listPid.add(3);
		listPid.add(4);

		List<Integer> linkPidTemp = listPid.subList(0, 2);

		listPid.subList(0, 2).clear();

		int count = listPid.size();
	}

	@Test
	public void test_Angle() {
		LineSegment link1 = new LineSegment(new Coordinate(116.16409, 39.87546,
				0), new Coordinate(116.16426, 39.87548, 0));

		LineSegment link2 = new LineSegment(new Coordinate(116.16426, 39.87548,
				0), new Coordinate(116.16409, 39.87546, 0));
		double a1 = AngleCalculator.getAngle(link1, link2);
	}

	@Test
	public void testCrfObjectRender() {
		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(42);

			RdObjectSearch search = new RdObjectSearch(conn);

			List<SearchSnapshot> data = search.searchDataByTileWithGap(107907,
					49609, 17, 80);

			System.out.println("data:"
					+ ResponseUtils.assembleRegularResult(data));

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testJosn() {
		JSONObject repairJson = new JSONObject();

		repairJson.put("id", 1);

		repairJson.element("id", 2);
	}

	@Test
	public void run_0831_2() throws Exception {

		String parameter = "{\"command\":\"DELETE\",\"dbId\":42,\"type\":\"RDLINK\",\"objId\":100009894}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}
	
	@Test
	public void run_0831_3() throws Exception {

		String parameter = "{\"command\":\"DELETE\",\"dbId\":42,\"type\":\"RDLINK\",\"objId\":100009799}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}
	
	
	
	
	
	
	
	

}
