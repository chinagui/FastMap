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
import com.navinfo.dataservice.dao.glm.iface.IObj;
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

public class runTest extends InitApplication {

	@Override
	@Before
	public void init() {
		initContext();
	}

	@Test
	public void testGetByPid_0() {
		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(17);

			SearchProcess p = new SearchProcess(conn);

			IObj obj = p.searchDataByPid(ObjType.IXPOI, 303000023);
			System.out.println(obj.Serialize(ObjLevel.BRIEF));
			System.out.println(obj.Serialize(ObjLevel.FULL));
			System.out.println(obj.Serialize(ObjLevel.HISTORY));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testGetByPid_1() {
		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(17);

			SearchProcess p = new SearchProcess(conn);

			IObj obj = p.searchDataByPid(ObjType.RDBRANCH, 208000024);
			System.out.println(obj.Serialize(ObjLevel.BRIEF));
			System.out.println(obj.Serialize(ObjLevel.FULL));
			System.out.println(obj.Serialize(ObjLevel.HISTORY));

		} catch (Exception e) {
			e.printStackTrace();
		}
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
	public void getTitleWithGap() {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getConnectionById(17);

			SearchProcess p = new SearchProcess(conn);

			List<ObjType> objType = new ArrayList<>();

			objType.add(ObjType.RDLANECONNEXITY);

			System.out.println(p.searchDataByTileWithGap(objType, 862722,
					394896, 20, 80));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void getTitleWithGap2() {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getConnectionById(17);

			SearchProcess p = new SearchProcess(conn);

			List<ObjType> objType = new ArrayList<>();

			objType.add(ObjType.RDRESTRICTION);

			System.out.println(p.searchDataByTileWithGap(objType, 862722,
					394896, 20, 80));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void getTitleWithGap1() {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getConnectionById(17);

			SearchProcess p = new SearchProcess(conn);

			List<ObjType> objType = new ArrayList<>();

			objType.add(ObjType.RDLANECONNEXITY);

			System.out.println(p.searchDataByTileWithGap(objType, 431360,
					197448, 19, 80));

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

		String parameter = "{\"command\":\"BATCH\",\"dbId\":17,\"type\":\"RDLINKSPEEDLIMIT\",\"data\":{\"direct\":3,\"linkPids\":[201001108,201001109,309001117],\"linkSpeedLimit\":{\"speedType\":0,\"fromSpeedLimit\":500,\"fromLimitSrc\":2,\"toSpeedLimit\":800,\"toLimitSrc\":3,\"speedClassWork\":1}}}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void createTest_0822() throws Exception {

		String parameter = "{\"command\":\"BATCH\",\"type\":\"RDLINKSPEEDLIMIT\",\"dbId\":17,\"data\":{\"linkPids\":[201001108,201001109,201001110],\"direct\":2,\"linkSpeedLimit\":{\"speedType\":0,\"fromSpeedLimit\":60,\"toSpeedLimit\":0,\"speedClassWork\":1}}}";
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

	@Test
	public void run_00908_3() throws Exception {

		String parameter = "{\"command\":\"ONLINEBATCH\",\"type\":\"FACE\",\"dbId\":17,\"pid\":2399,\"ruleId\":\"BATCHREGIONIDRDLINK\"}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_00912_1() throws Exception {

		String parameter = "{\"type\":\"RDBRANCH\",\"command\":\"UPDATE\",\"dbId\":17,\"data\":{\"details\":[{\"branchType\":1,\"pid\":220000001,\"objStatus\":\"UPDATE\",\"patternCode\":\"8\"}],\"pid\":205000003}}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_00921_1() throws Exception {

		String parameter = "{\"type\":\"RDBRANCH\",\"command\":\"UPDATE\",\"dbId\":17,\"data\":{\"details\":[{\"names\":[{\"pid\":308000001,\"objStatus\":\"UPDATE\",\"name\":\"11111\",\"phonetic\":\"Yi+Yi+Yi+Yi+Yi\",\"voiceFile\":\"YiYiYiYiYi\",\"nameGroupid\":2},{\"pid\":304000003,\"objStatus\":\"UPDATE\",\"name\":\"2222\",\"phonetic\":\"Er+Er+Er+Er\",\"voiceFile\":\"ErErErEr\",\"nameGroupid\":1}],\"pid\":40254881}],\"pid\":40291389}}";
		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_00921_2() throws Exception {

		String parameter = "{\"command\":\"REPAIR\",\"dbId\":17,\"objId\":309000092,\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.27721,40.55273],[116.27744216546803,40.552798113177765],[116.27771973609924,40.552882305427765],[116.278,40.55296]]},\"interLinks\":[],\"interNodes\":[]},\"type\":\"RDLINK\"}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_00926_1() throws Exception {

		String parameter = "{\"command\":\"CREATE\",\"dbId\":17,\"type\":\"IXSAMEPOI\",\"poiPids\":[307000011,308000014]}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_00926_2() throws Exception {

		String parameter = "{\"command\":\"BREAK\",\"dbId\":17,\"objId\":309000144,\"data\":{\"longitude\":116.26802,\"latitude\":40.54648},\"type\":\"RDLINK\"}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_00928_1() throws Exception {

		String parameter = "{\"command\":\"BREAK\",\"dbId\":17,\"objId\":205000148,\"data\":{\"longitude\":116.261,\"latitude\":40.54496},\"type\":\"RDLINK\"}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_00928_2() throws Exception {

		String parameter = "{\"type\":\"RDBRANCH\",\"command\":\"UPDATE\",\"dbId\":17,\"data\":{\"details\":[{\"branchType\":2,\"rowId\":\"61E533B51B6C40E4B916403BCD9CEDE8\",\"pid\":202000013,\"objStatus\":\"UPDATE\",\"estabType\":0,\"nameKind\":0}],\"pid\":304000020}}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_00928_3() throws Exception {

		String parameter = "{\"command\":\"REPAIR\",\"dbId\":17,\"objId\":204000154,\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.26463,40.54527],[116.26564,40.54528]]},\"interLinks\":[],\"interNodes\":[]},\"type\":\"RDLINK\"}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_00928_4() throws Exception {

		String parameter = "{\"type\":\"RDBRANCH\",\"command\":\"UPDATE\",\"dbId\":17,\"data\":{\"signboards\":[{\"names\":[{\"phonetic\":\"ａ\",\"pid\":209000001,\"objStatus\":\"UPDATE\",\"nameGroupid\":1}],\"pid\":220000003}],\"pid\":304000021}}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_00929_1() throws Exception {

		String parameter = "{\"command\":\"REPAIR\",\"dbId\":17,\"objId\":209000152,\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.28325,40.56169],[116.28362850249809,40.561585569738845],[116.28385110928193,40.5615244049076],[116.28419,40.56143],[116.28452681622852,40.56133650839071],[116.28464803308924,40.561302935344315],[116.28492113608729,40.561226541258385],[116.28516,40.56116]]},\"interLinks\":[],\"interNodes\":[]},\"type\":\"RDLINK\"}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_00929_2() throws Exception {

		String parameter = "{\"command\":\"REPAIR\",\"dbId\":17,\"objId\":204000167,\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.28252,40.55834],[116.28275,40.55826],[116.2828685512793,40.55821583513286],[116.2831,40.55813],[116.28333,40.55805],[116.28363,40.55793],[116.28385,40.55785]]},\"interLinks\":[],\"interNodes\":[]},\"type\":\"RDLINK\"}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_00929_3() throws Exception {

		String parameter = "{\"command\":\"MOVE\",\"dbId\":17,\"objId\":209000166,\"data\":{\"longitude\":116.37475669384003,\"latitude\":40.479879546842724},\"type\":\"RDNODE\"}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_00930_1() throws Exception {

		String parameter = "{\"command\":\"MOVE\",\"dbId\":17,\"objId\":209000166,\"data\":{\"longitude\":116.37585,\"latitude\":40.48021},\"type\":\"RDNODE\"}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_1008_1() throws Exception {

		String parameter = "{\"command\":\"MOVE\",\"dbId\":17,\"objId\":309000179,\"data\":{\"longitude\":116.2514340877533,\"latitude\":40.51831775506456},\"type\":\"RDNODE\"}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_1010_1() throws Exception {

		String parameter = "{\"command\":\"UPDOWNDEPART\",\"type\":\"RDLINK\",\"dbId\":17,\"distance\":\"6.6\",\"data\":{\"linkPids\":[671762,687278,687277]}}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_1010_11() throws Exception {

		String parameter = "{\"command\":\"UPDOWNDEPART\",\"type\":\"RDLINK\",\"dbId\":17,\"distance\":\"6.6\",\"data\":{\"linkPids\":[300001407,309001379,320001335]}}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_1010_2() throws Exception {

		String parameter = "{\"command\":\"UPDOWNDEPART\",\"type\":\"RDLINK\",\"dbId\":17,\"distance\":\"6.6\",\"data\":{\"linkPids\":[307001293]}}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_1013_1() throws Exception {

		String parameter = "{\"command\":\"CREATE\",\"type\":\"ADFACE\",\"dbId\":17,\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.1938738822937,40.5403843741422],[116.19474291801451,40.54014384965778],[116.19376659393309,40.53978102295414],[116.19336962699889,40.54031099388258],[116.1938738822937,40.5403843741422]]}}}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_1013_2() throws Exception {

		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDLANECONNEXITY\",\"dbId\":17,\"data\":{\"inLinkPid\":308000970,\"nodePid\":200000659,\"outLinkPids\":[308000969],\"laneInfo\":\"b,[a]\"}}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_1018_1() throws Exception {

		String parameter = "{\"command\":\"DELETE\",\"dbId\":17,\"type\":\"IXPOIPARENT\",\"objId\":303000023}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_1018_2() throws Exception {

		String parameter = "{\"command\":\"UPDATE\",\"dbId\":17,\"type\":\"IXPOIPARENT\",\"objId\":220000037,\"parentPid\":320000035}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_1020_2() throws Exception {

		String parameter = "{\"command\":\"DELETE\",\"dbId\":17,\"type\":\"RDLINK\",\"objId\":306001082}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_1025_1() throws Exception {

		String parameter = "{\"command\":\"CREATE\",\"dbId\":17,\"data\":{\"eNodePid\":0,\"sNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.40500664710999,39.979117898234875],[116.40540361404423,39.978484856884535]]},\"catchLinks\":[{\"linkPid\":220001464,\"lon\":116.40540361404423,\"lat\":39.978484856884535}]},\"type\":\"RDLINK\"}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_1026_1() throws Exception {

		String parameter = "{\"command\":\"UPDOWNDEPART\",\"type\":\"RDLINK\",\"dbId\":17,\"distance\":\"8.3\",\"data\":{\"linkPids\":[88652904,88652903,85159920,276577,276575,276238]}}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_1101_1() throws Exception {

		String parameter = "{\"command\":\"CREATE\",\"dbId\":17,\"data\":{\"eNodePid\":0,\"sNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.51475191116333,40.08406399698187],[116.51481628417967,40.08259463490513]]},\"catchLinks\":[]},\"type\":\"RDLINK\"}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_1101_2() throws Exception {

		String parameter = "{\"command\":\"CREATE\",\"dbId\":17,\"data\":{\"eNodePid\":0,\"sNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.51569068431854,40.08268493181319],[116.51568531990051,40.08390803267927]]},\"catchLinks\":[]},\"type\":\"RDLINK\"}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_1102_1() throws Exception {

		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDLANECONNEXITY\",\"dbId\":17,\"data\":{\"inLinkPid\":305002573,\"nodePid\":303001877,\"outLinkPids\":[210002470],\"laneInfo\":\"c,c\"}}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_1103_1() throws Exception {

		String parameter = "{\"command\":\"UPDOWNDEPART\",\"type\":\"RDLINK\",\"dbId\":17,\"distance\":\"15.1\",\"data\":{\"linkPids\":[301002606,204002490]}}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_1103_2() throws Exception {

		String parameter = "{\"command\":\"UPDATE\",\"dbId\":17,\"type\":\"RDLINK\",\"objId\":206002438,\"data\":{\"kind\":7,\"pid\":206002438,\"objStatus\":\"UPDATE\",\"routeAdopt\":4}}";
		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_1110_1() throws Exception {

		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDLANECONNEXITY\",\"dbId\":17,\"data\":{\"inLinkPid\":209002563,\"nodePid\":303001955,\"outLinkPids\":[301002648],\"laneInfo\":\"[a]\"}}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_1111_1() throws Exception {

		String parameter = "{\"command\":\"CREATE\",\"dbId\":17,\"objId\":200002625,\"data\":{\"longitude\":116.45005,\"latitude\":40.04905},\"type\":\"RDNODE\"}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_1115_1() throws Exception {

		String parameter = "{\"command\":\"CREATESIDEROAD\",\"type\":\"RDLINK\",\"dbId\":17,\"distance\":\"5\",\"sideType\":1,\"data\":{\"linkPids\":[305002750,200002676,202002616,310002675]}}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_1115_2() throws Exception {

		String parameter = "{\"command\":\"CREATESIDEROAD\",\"type\":\"RDLINK\",\"dbId\":17,\"distance\":\"5\",\"sideType\":1,\"data\":{\"linkPids\":[310002711,209002685]}}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_1122_1() throws Exception {

		String parameter = "{\"command\":\"CREATESIDEROAD\",\"type\":\"RDLINK\",\"dbId\":17,\"distance\":\"5\",\"sideType\":1,\"data\":{\"linkPids\":[208002745,302002728]}}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_1124_1() throws Exception {

		String parameter = "{\"command\":\"CREATE\",\"dbId\":17,\"objId\":310002772,\"data\":{\"longitude\":116.45041146329042,\"latitude\":40.05730591966208},\"type\":\"RDNODE\"}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_1124_2() throws Exception {

		String parameter = "{\"command\":\"DELETE\",\"dbId\":17,\"type\":\"RDNODE\",\"objId\":310002144}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_1125_1() throws Exception {

		String parameter = "{\"command\":\"REPAIR\",\"dbId\":17,\"objId\":208002784,\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.47862,40.07587],[116.47898733615875,40.07585075919639],[116.47937,40.0759]]},\"catchInfos\":[]},\"type\":\"RDLINK\"}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_1126_1() throws Exception {

		String parameter = "{\"command\":\"DEPART\",\"dbId\":17,\"objId\":306002106,\"data\":{\"catchNodePid\":0,\"catchLinkPid\":\"308002763\",\"linkPid\":\"306002741\",\"longitude\":116.38298679474569,\"latitude\":40.05133614230701},\"type\":\"RDLINK\"}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_1130_1() throws Exception {

		String parameter = "{\"command\":\"CREATESIDEROAD\",\"type\":\"RDLINK\",\"dbId\":17,\"distance\":\"5.5\",\"sideType\":1,\"data\":{\"linkPids\":[300002869,300002870,17256975,17256976]}}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_1201_1() throws Exception {

		String parameter = "{\"type\":\"RDBRANCH\",\"command\":\"UPDATE\",\"dbId\":17,\"data\":{\"signboards\":[{\"arrowCode\":\"100200DJ001\",\"pid\":300000004,\"objStatus\":\"UPDATE\"}],\"pid\":209000031}}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_1202_1() throws Exception {

		String parameter = "{\"command\":\"MOVE\",\"dbId\":17,\"objId\":307000050,\"data\":{\"longitude\":116.65068492293356,\"latitude\":40.30207164564197},\"type\":\"ZONENODE\"}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_1202_2() throws Exception {

		String parameter = "{\"command\":\"BATCH\",\"type\":\"RDLINKSPEEDLIMIT\",\"dbId\":17,\"data\":{\"linkPids\":[300002533,301002537],\"linkSpeedLimit\":{\"speedType\":0,\"fromSpeedLimit\":500,\"fromLimitSrc\":0,\"toSpeedLimit\":0,\"toLimitSrc\":0,\"speedClassWork\":1}}}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_1215_1() throws Exception {

		String parameter = "{\"command\":\"REPAIR\",\"dbId\":17,\"objId\":306002996,\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.75,40.3009],[116.75055,40.30088],[116.75110280513765,40.301165450662424]]},\"catchInfos\":[{\"nodePid\":310002364,\"longitude\":116.75110280513765,\"latitude\":40.301165450662424}]},\"type\":\"RDLINK\"}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_1215_2() throws Exception {

		String parameter = "{\"command\":\"REPAIR\",\"dbId\":17,\"objId\":208003072,\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.75053,40.3123],[116.75046775131834,40.31248246083938],[116.74963295459747,40.31254416826017],[116.75039201974869,40.312707788542156],[116.75109,40.31234]]},\"catchInfos\":[]},\"type\":\"RDLINK\"}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_1216_1() throws Exception {

		String parameter = "{\"command\":\"REPAIR\",\"dbId\":17,\"objId\":305003101,\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.75024986267088,40.31194286031829],[116.75154,40.31177]]},\"catchInfos\":[{\"nodePid\":200002388,\"longitude\":116.75024986267088,\"latitude\":40.31194286031829}]},\"type\":\"RDLINK\"}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_1216_2() throws Exception {

		String parameter = "{\"command\":\"REPAIR\",\"dbId\":17,\"objId\":309002993,\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.75131,40.31441],[116.74915015697479,40.3144175967717]]},\"catchInfos\":[{\"nodePid\":207002276,\"longitude\":116.74915015697479,\"latitude\":40.3144175967717}]},\"type\":\"RDLINK\"}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_1216_3() throws Exception {

		String parameter = "{\"command\":\"REPAIR\",\"dbId\":17,\"objId\":200003042,\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.74949884414673,40.3158819435657],[116.75127,40.31589]]},\"catchInfos\":[{\"nodePid\":205002389,\"longitude\":116.74949884414673,\"latitude\":40.3158819435657}]},\"type\":\"RDLINK\"}";
		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_1216_4() throws Exception {

		String parameter = "{\"command\":\"CREATE\",\"dbId\":17,\"type\":\"RDSAMELINK\",\"data\":{\"links\":[{\"linkPid\":202002965,\"type\":\"RDLINK\",\"isMain\":1},{\"linkPid\":201000062,\"type\":\"ADLINK\",\"isMain\":0}]}}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_1219_1() throws Exception {

		String parameter = "{\"command\":\"REPAIR\",\"dbId\":17,\"objId\":301003059,\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.7502,40.31965],[116.75041,40.31951]]},\"interLinks\":[],\"interNodes\":[]},\"type\":\"RDLINK\"}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_1220_1() throws Exception {

		String parameter = "{\"command\":\"REPAIR\",\"dbId\":17,\"objId\":303003035,\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.66208565235138,40.30279168355063],[116.66261,40.30248]]},\"catchInfos\":[{\"nodePid\":210002390,\"catchNodePid\":\"309002361\"}]},\"type\":\"RDLINK\"}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_1221_1() throws Exception {

		String parameter = "{\"command\":\"REPAIR\",\"dbId\":17,\"objId\":207003013,\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.62555, 40.31876],[116.62461, 40.31901]]},\"catchInfos\":[{\"nodePid\":307002289,\"longitude\":116.62461,\"latitude\":40.31901}]},\"type\":\"RDLINK\"}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_1221_2() throws Exception {

		String parameter = "{\"command\":\"REPAIR\",\"dbId\":17,\"objId\":301003078,\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.62586,40.31674],[116.62632912397385,40.31672045187265]]},\"catchInfos\":[{\"nodePid\":207002303,\"catchNodePid\":\"304002404\"}]},\"type\":\"RDLINK\"}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_1226_2() throws Exception {

		String parameter = "{\"command\":\"REPAIR\",\"dbId\":17,\"objId\":690342,\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.39619,39.93577],[116.39584153890608,39.93558984701896],[116.3962,39.93541]]},\"catchInfos\":[]},\"type\":\"RDLINK\"}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_1227_1() throws Exception {

		String parameter = "{\"command\":\"CREATE\",\"dbId\":17,\"data\":{\"eNodePid\":290417,\"sNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.637524664402,40.08026944827676],[116.63706868886948,40.08041105502878]]},\"catchLinks\":[{\"nodePid\":290417,\"seqNum\":1}]},\"type\":\"RDLINK\"}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_1227_2() throws Exception {

		String parameter = "{\"command\":\"UPDATE\",\"dbId\":19,\"type\":\"RDNODE\",\"objId\":200002455,\"data\":{\"kind\":2,\"pid\":200002455,\"objStatus\":\"UPDATE\"}}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_1229_1() throws Exception {

		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDTOLLGATE\",\"dbId\":17,\"data\":{\"inLinkPid\":308003107,\"outLinkPid\":320003013,\"nodePid\":302002408}}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_1229_2() throws Exception {

		String parameter = "{\"command\":\"UPDATE\",\"type\":\"RDTOLLGATE\",\"dbId\":17,\"data\":{\"type\":0,\"rowId\":\"4EACEF96A92342EF992EBF2AE76D9C18\",\"pid\":304000018,\"objStatus\":\"UPDATE\",\"passages\":[{\"cardType\":0,\"rowId\":\"26A8A884DB6E4B36959072F0B591DBD2\",\"pid\":304000018,\"objStatus\":\"UPDATE\"}]}}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_1229_3() throws Exception {

		String parameter = "{\"command\":\"UPDATE\",\"type\":\"RDTOLLGATE\",\"dbId\":17,\"data\":{\"names\":[{\"pid\":0,\"nameGroupid\":1,\"langCode\":\"CHI\",\"name\":\"收费符收费符收费符收费符收费符收费符收费符收费符收费符收费符收费符收费符收费符收费符收费\",\"phonetic\":\"Shou Fu Shou Fei Fu Shou Fei Fu Shou Fei Fu Shou Fei Fu Shou Fei Fu Shou Fei Fu Shou Fei Fu Shou Fei Fu Shou Fei Fu Shou Fei Fu Shou Fei Fu Shou Fei Fu Shou Fei Fu Shou Fei Fu Shou Fei\",\"rowId\":\"\",\"objStatus\":\"INSERT\"}],\"rowId\":\"4EACEF96A92342EF992EBF2AE76D9C18\",\"pid\":304000018}}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_1229_4() throws Exception {

		String parameter = "{\"command\":\"UPDATE\",\"type\":\"RDTOLLGATE\",\"dbId\":17,\"data\":{\"passageNum\":0,\"rowId\":\"9ED3B4049CAD4722A1F831E6B52303B5\",\"pid\":310000012,\"objStatus\":\"UPDATE\",\"etcFigureCode\":\"\",\"passages\":[{\"pid\":310000012,\"seqNum\":1,\"tollForm\":0,\"cardType\":0,\"vehicle\":0,\"rowId\":\"6C89A279EB5F43219231A5F3F2E567EB\",\"objStatus\":\"DELETE\"}]}}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_1229_5() throws Exception {

		String parameter = "";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_0104_1() throws Exception {

		String parameter = "{\"command\":\"CREATE\",\"dbId\":19,\"type\":\"RDBRANCH\",\"data\":{\"branchType\":3,\"inLinkPid\":302003134,\"nodePid\":206002438,\"outLinkPid\":308003160}}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_0106_1() throws Exception {

		String parameter = "{\"command\":\"UPDATE\",\"dbId\":19,\"type\":\"RDLINK\",\"objId\":303003140,\"data\":{\"kind\":2,\"pid\":303003140,\"objStatus\":\"UPDATE\",\"routeAdopt\":4}}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_0106_2() throws Exception {

		String parameter = "{\"command\":\"UPDATE\",\"dbId\":17,\"type\":\"RDLINK\",\"objId\":201003164,\"data\":{\"kind\":9,\"pid\":201003164,\"objStatus\":\"UPDATE\"}}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_0111_1() throws Exception {

		String parameter = "{\"command\":\"UPDATE\",\"dbId\":17,\"type\":\"RDLINK\",\"objId\":220003059,\"data\":{\"kind\":9,\"routeAdopt\":0,\"laneNum\":1,\"limits\":[{\"linkPid\":220003059,\"type\":3,\"limitDir\":0,\"timeDomain\":\"\",\"vehicle\":0,\"tollType\":9,\"weather\":9,\"inputTime\":\"\",\"processFlag\":2,\"objStatus\":\"INSERT\"}],\"pid\":220003059,\"objStatus\":\"UPDATE\"}}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_0111_2() throws Exception {

		String parameter = "{\"command\":\"UPDATE\",\"dbId\":17,\"type\":\"RDLINK\",\"objId\":306003172,\"data\":{\"kind\":9,\"pid\":306003172,\"objStatus\":\"UPDATE\"}}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_0111_3() throws Exception {

		String parameter = "{\"command\":\"UPDATE\",\"dbId\":17,\"type\":\"RDLINK\",\"objId\":205003184,\"data\":{\"kind\":9,\"pid\":205003184,\"objStatus\":\"UPDATE\"}}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_0111_4() throws Exception {

		String parameter = "{\"command\":\"CREATE\",\"dbId\":17,\"type\":\"RDBRANCH\",\"data\":{\"branchType\":3,\"inLinkPid\":208003262,\"nodePid\":308002492,\"outLinkPid\":307003133 }}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_0112_1() throws Exception {

		String parameter = "{\"command\":\"CREATESIDEROAD\",\"type\":\"RDLINK\",\"dbId\":19,\"distance\":15.1,\"sideType\":1,\"sNodePid\":204002584,\"data\":{\"linkPids\":[203003244]}}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_0112_2() throws Exception {

		String parameter = "{\"command\":\"UPDATE\",\"dbId\":17,\"type\":\"RDBRANCH\",\"objId\":306000058,\"data\":{\"details\":[{\"names\":[{\"name\":\"1\",\"phonetic\":\"Yi\",\"voiceFile\":\"Yi\",\"rowId\":\"04BA9E1BF53D4CFF936743E3247BEA9E\",\"pid\":304000035,\"objStatus\":\"UPDATE\"}],\"rowId\":\"F932431D960D4157A74ED7BA4485AA37\",\"pid\":220000035,\"objStatus\":\"UPDATE\"}],\"rowId\":\"0C3242D1EC254F41B964CDE4579615BC\",\"pid\":306000058,\"objStatus\":\"UPDATE\"}}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_0112_3() throws Exception {

		String parameter = "{\"command\":\"CREATE\",\"dbId\":17,\"type\":\"RDBRANCH\",\"data\":{\"branchType\":2,\"inLinkPid\":206000525,\"nodePid\":320000356,\"outLinkPid\":203002059}}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_0112_4() throws Exception {

		String parameter = "{\"command\":\"CREATE\",\"dbId\":17,\"type\":\"RDBRANCH\",\"data\":{\"branchType\":8,\"inLinkPid\":220003253,\"nodePid\":306002383,\"outLinkPid\":309003076}}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_0112_5() throws Exception {

		String parameter = "{\"command\":\"CREATE\",\"dbId\":17,\"type\":\"RDBRANCH\",\"data\":{\"branchType\":9,\"inLinkPid\":309003211,\"nodePid\":203002376,\"outLinkPid\":207003213}}";
		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_0113_1() throws Exception {

		String parameter = "{\"command\":\"CREATESIDEROAD\",\"type\":\"RDLINK\",\"dbId\":17,\"distance\":\"9.0\",\"sideType\":1,\"sNodePid\":\"215837\",\"data\":{\"linkPids\":[201000200]}}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_0113_2() throws Exception {

		String parameter = "{\"command\":\"UPDATE\",\"dbId\":17,\"type\":\"RDBRANCH\",\"objId\":306000059,\"data\":{\"details\":[{\"names\":[{\"pid\":0,\"seqNum\":1,\"nameGroupid\":2,\"nameClass\":0,\"langCode\":\"CHI\",\"codeType\":0,\"name\":\"\",\"phonetic\":\"\",\"srcFlag\":0,\"voiceFile\":\"\",\"objStatus\":\"INSERT\"},{\"pid\":0,\"seqNum\":1,\"nameGroupid\":1,\"nameClass\":0,\"langCode\":\"CHI\",\"codeType\":0,\"name\":\"\",\"phonetic\":\"\",\"srcFlag\":0,\"voiceFile\":\"\",\"objStatus\":\"INSERT\"}],\"rowId\":\"04430BCD2B1B4446AD9BF36913F861C3\",\"pid\":307000039,\"objStatus\":\"UPDATE\"}],\"rowId\":\"9EFE30E65F6B432C9231D105B49311DA\",\"pid\":306000059,\"objStatus\":\"UPDATE\"}}";
		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_0114_1() throws Exception {

		String parameter = "{\"command\":\"UPDATE\",\"dbId\":17,\"type\":\"RDBRANCH\",\"objId\":208000041,\"data\":{\"details\":[{\"names\":[{\"srcFlag\":1,\"rowId\":\"0F601476B86F471FAB6344FB193E7D4A\",\"pid\":302000028,\"objStatus\":\"UPDATE\"}],\"rowId\":\"15653B71F444491A908C83906BCE974A\",\"pid\":310000045,\"objStatus\":\"UPDATE\"}],\"rowId\":\"4131F4CB9D9A4C69BE266D10B2042A6D\",\"pid\":208000041,\"objStatus\":\"UPDATE\"}}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_0118_1() throws Exception {

		String parameter = "{\"command\":\"UPDATE\",\"dbId\":17,\"type\":\"RDLINK\",\"objId\":203001135,\"data\":{\"kind\":9,\"pid\":203001135,\"objStatus\":\"UPDATE\"}}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_0118_2() throws Exception {

		String parameter = "{\"command\":\"DELETE\",\"dbId\":249,\"type\":\"RDBRANCH\",\"detailId\":0,\"rowId\":\"0824AFB032BB444CBA2856268C12B54D\",\"branchType\":5}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_0118_3() throws Exception {

		String parameter = "{\"command\":\"UPDATE\",\"dbId\":17,\"type\":\"RDBRANCH\",\"objId\":304000056,\"data\":{\"details\":[{\"names\":[{\"voiceFile\":\"1234\",\"rowId\":\"DC7AEF26A57840D2A0BB0D2FFFAF48B4\",\"pid\":200000021,\"objStatus\":\"UPDATE\"}],\"rowId\":\"030CF7B0F2754127ADFAFDE861CF6D59\",\"pid\":207000038,\"objStatus\":\"UPDATE\"}],\"rowId\":\"8CD6143170A547F4A0B116E9A71873C0\",\"pid\":304000056,\"objStatus\":\"UPDATE\"}}";
		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_0207_1() throws Exception {

		String parameter = "{\"command\":\"BATCH\",\"type\":\"RDLINKSPEEDLIMIT\",\"dbId\":17,\"data\":{\"linkPids\":[300001979,310001938,301001969,310001939,208001974],\"direct\":2,\"linkSpeedLimit\":{\"speedType\":3,\"fromSpeedLimit\":300,\"fromLimitSrc\":2,\"toSpeedLimit\":0,\"toLimitSrc\":0,\"speedDependent\":6}}}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_0207_2() throws Exception {

		String parameter = "{\"command\":\"BATCH\",\"type\":\"RDLINKSPEEDLIMIT\",\"dbId\":17,\"data\":{\"linkPids\":[300001979,310001938,301001969,310001939,208001974],\"direct\":2,\"linkSpeedLimit\":{\"speedType\":0,\"fromSpeedLimit\":400,\"fromLimitSrc\":0,\"toSpeedLimit\":0,\"toLimitSrc\":0,\"speedClassWork\":1}}}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_0209_1() throws Exception {

		String parameter = "{\"command\":\"UPDOWNDEPART\",\"type\":\"RDLINK\",\"dbId\":17,\"distance\":\"6.6\",\"data\":{\"linkPids\":[303003307,309003330,308003366]}}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_0210_1() throws Exception {

		String parameter = "{\"command\":\"UPDOWNDEPART\",\"type\":\"RDLINK\",\"dbId\":17,\"distance\":\"6.6\",\"data\":{\"linkPids\":[308003401,201003357,205003359]}}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_0213_2() throws Exception {

		String parameter = "{\"command\":\"UPDOWNDEPART\",\"type\":\"RDLINK\",\"dbId\":17,\"distance\":\"6.6\",\"data\":{\"linkPids\":[301003406,300003409,205003370]}}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_0214_1() throws Exception {

		String parameter = "{\"command\":\"UPDOWNDEPART\",\"type\":\"RDLINK\",\"dbId\":17,\"distance\":\"6.6\",\"data\":{\"linkPids\":[220003525,205003379,208003454]}}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_0214_2() throws Exception {

		String parameter = "{\"command\":\"UPDOWNDEPART\",\"type\":\"RDLINK\",\"dbId\":17,\"distance\":\"6.6\",\"data\":{\"linkPids\":[304003413,210003401]}}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_0216_1() throws Exception {

		String parameter = "{\"command\":\"CREATE\",\"dbId\":17,\"objId\":310003405,\"data\":{\"longitude\":116.184167837713,\"latitude\":40.55273596910603},\"type\":\"RDNODE\"}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_0216_2() throws Exception {

		String parameter = "{\"command\":\"REPAIR\",\"dbId\":17,\"objId\":210003436,\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.20255,40.55834],[116.20235100388527,40.55852415747297]]},\"catchInfos\":[{\"nodePid\":204002750,\"longitude\":116.20235100388527,\"latitude\":40.55852415747297}]},\"type\":\"RDLINK\"}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_0216_3() throws Exception {

		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDLINK\",\"data\":{\"eNodePid\":0,\"sNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.19138615259806,40.560664562764494],[116.19161278009415,40.560811534718106],[116.19173014375765,40.56065583014521],[116.19190514087677,40.560766704912744],[116.1921570815714,40.56064632981665]]},\"catchLinks\":[{\"linkPid\":302003429,\"lon\":116.19138615259806,\"lat\":40.560664562764494},{\"linkPid\":201003429,\"lon\":116.19173014375765,\"lat\":40.56065583014521},{\"linkPid\":310003435,\"lon\":116.1921570815714,\"lat\":40.56064632981665}]},\"dbId\":17}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_0217_2() throws Exception {

		String parameter = "{\"command\":\"UPDOWNDEPART\",\"type\":\"RDLINK\",\"dbId\":19,\"distance\":\"6.6\",\"data\":{\"linkPids\":[209003431,304003471,200003498]}}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_0218_1() throws Exception {

		String parameter = "{\"command\":\"DEPART\",\"dbId\":17,\"objId\":74872405,\"data\":{\"catchNodePid\":0,\"catchLinkPid\":0,\"linkPid\":85626567,\"longitude\":116.24983549118042,\"latitude\":40.21274386373456},\"type\":\"RDLINK\"}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_0218_2() throws Exception {

		String parameter = "{\"command\":\"UPDATE\",\"dbId\":17,\"type\":\"RDBRANCH\",\"objId\":200000046,\"data\":{\"details\":[{\"names\":[{\"phonetic\":\"Aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\",\"rowId\":\"07576561D94B408D93DA9F9016EE035D\",\"pid\":308000029,\"objStatus\":\"UPDATE\"}],\"rowId\":\"BB9A60444AAC447DA5A02912A9078738\",\"pid\":301000043,\"objStatus\":\"UPDATE\"}],\"rowId\":\"8F2578FCD2324E4AAC9BB27EC4D4DA2E\",\"pid\":200000046,\"objStatus\":\"UPDATE\"}}";
		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_0220_1() throws Exception {

		String parameter = "{\"command\":\"CREATESIDEROAD\",\"type\":\"RDLINK\",\"dbId\":17,\"distance\":9,\"sideType\":1,\"sNodePid\":\"354020\",\"data\":{\"linkPids\":[487559]}}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_0220_2() throws Exception {

		String parameter = "{\"command\":\"REPAIR\",\"dbId\":17,\"objId\":204003417,\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.3280701637268,40.13417569137543],[116.32845,40.13381]]},\"catchInfos\":[{\"nodePid\":306002723,\"longitude\":116.3280701637268,\"latitude\":40.13417569137543}]},\"type\":\"RDLINK\"}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_0222_2() throws Exception {

		String parameter = "{\"command\":\"REPAIR\",\"dbId\":17,\"objId\":210003533,\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.33617,40.13427],[116.3355964422226,40.134577617232516]]},\"catchInfos\":[{\"nodePid\":306002729,\"longitude\":116.3355964422226,\"latitude\":40.134577617232516}]},\"type\":\"RDLINK\"}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_0223_2() throws Exception {

		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDRESTRICTION\",\"dbId\":17,\"data\":{\"nodePid\":303002762,\"inLinkPid\":207003527,\"restricType\":0,\"infos\":[{\"arrow\":\"3\",\"outLinkPid\":300003549}]}}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}
	
	
	@Test
	public void run_0223_1() throws Exception {

		String parameter = "{\"command\":\"REPAIR\",\"dbId\":17,\"objId\":320003460,\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.32571,40.13285],[116.32612824440002,40.132684853787616],[116.32668,40.13277]]},\"catchInfos\":[]},\"type\":\"RDLINK\"}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}
	
	@Test
	public void run_0224_1() throws Exception {

		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDLINK\",\"data\":{\"eNodePid\":0,\"sNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.4366191625595,39.95853180087665],[116.43663548992161,39.9586567080045]]},\"catchLinks\":[{\"linkPid\":501000053,\"lon\":116.43663548992161,\"lat\":39.9586567080045}]},\"dbId\":84}";
		
		
		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}
	
	@Test
	public void run_00306_1() throws Exception {

		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDLINK\",\"data\":{\"eNodePid\":509000060,\"sNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.43648639321329,39.95854413650709],[116.43645957112312,39.95865001724366]]},\"catchLinks\":[{\"nodePid\":509000060,\"seqNum\":1}]},\"dbId\":84}";
		
		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}
	
	@Test
	public void run_00306_2() throws Exception {

		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDLINK\",\"data\":{\"eNodePid\":0,\"sNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.43657222390175,39.95855338822849],[116.43657353334267,39.958656507844964]]},\"catchLinks\":[{\"linkPid\":401000075,\"lon\":116.43657353334267,\"lat\":39.958656507844964}]},\"dbId\":84}";
		
		
		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}
	
	@Test
	public void run_00308_2() throws Exception {

		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDLINK\",\"data\":{\"eNodePid\":0,\"sNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.43621951341628,39.966109570623715],[116.43618461460889,39.96623225672083],[116.43618732690811,39.96629047293043]]},\"catchLinks\":[{\"linkPid\":510000101,\"lon\":116.43618461460889,\"lat\":39.96623225672083}]},\"dbId\":84}";
		
		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}
	
	@Test
	public void run_00309_1() throws Exception {

		String parameter = "{\"command\":\"UPDATE\",\"dbId\":84,\"type\":\"RDBRANCH\",\"objId\":408000003,\"data\":{\"details\":[{\"patternCode\":\"8e000003\",\"voiceDir\":\"\",\"arrowCode\":\"1e000003\",\"names\":[{\"pid\":0,\"seqNum\":1,\"nameGroupid\":1,\"nameClass\":0,\"langCode\":\"CHI\",\"codeType\":0,\"name\":\"\",\"phonetic\":\"\",\"srcFlag\":0,\"voiceFile\":\"\",\"objStatus\":\"INSERT\"}],\"rowId\":\"F134A36415BA42F8BCBE6302D036AE62\",\"pid\":500000001,\"objStatus\":\"UPDATE\"}],\"rowId\":\"55CCC61FC34544E2BD1D3B61DCA740ED\",\"pid\":408000003,\"objStatus\":\"UPDATE\"}}";
		
		
		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}
	
	@Test
	public void run_00310_1() throws Exception {

		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDRESTRICTION\",\"dbId\":84,\"data\":{\"nodePid\":507000096,\"inLinkPid\":409000129,\"restricType\":0,\"infos\":[{\"arrow\":2}]}}";
		
		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}
	
	@Test
	public void run_0323_2() throws Exception {

		String parameter = "{\"command\":\"UPDOWNDEPART\",\"type\":\"RDLINK\",\"dbId\":84,\"distance\":\"6.6\",\"data\":{\"linkPids\":[410000168,410000169]}}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}
	
	@Test
	public void run_0323_1() throws Exception {

		String parameter = "{\"command\":\"REPAIR\",\"dbId\":84,\"objId\":401000187,\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.42841696739197,40.01905957371441],[116.42871,40.01931],[116.42881,40.01925],[116.42892,40.01918],[116.42908,40.01929],[116.42919,40.01929],[116.42935,40.01928],[116.42956,40.01927]]},\"catchInfos\":[{\"nodePid\":401000140,\"longitude\":116.42841696739197,\"latitude\":40.01905957371441}]},\"type\":\"RDLINK\"}";
		
		
		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}
	
	@Test
	public void run_0324_1() throws Exception {

		String parameter = "{\"command\":\"DELETE\",\"dbId\":84,\"type\":\"RDNODE\",\"objId\":503000185,\"infect\":0}";
		
		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}
	
	@Test
	public void run_0329_1() throws Exception {

		String parameter = "{\"command\":\"REPAIR\",\"type\":\"RDLINK\",\"objId\":408000016,\"dbId\":13,\"data\":{\"type\":\"RDLINK\",\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.52361,39.74859],[116.52406454086304,39.748262345723944]]},\"catchInfos\":[{\"nodePid\":400000020,\"longitude\":116.52361,\"latitude\":39.74859},{\"nodePid\":400000019,\"longitude\":116.52406454086304,\"latitude\":39.748262345723944}]}}";
		
		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}
	@Test
	public void run_00418_2() throws Exception {

		String parameter = "{\"command\":\"CREATE\",\"type\":\"CMGBUILDING\",\"data\":{\"kind\":1001,\"facePids\":[400000005]},\"dbId\":13}";
		
		
		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}
	
	@Test
	public void run_00419_1() throws Exception {

		String parameter = "{\"command\":\"DELETE\",\"type\":\"CMGBUILDING\",\"objId\":400000011,\"dbId\":13}";
		
		
		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}
	
	@Test
	public void run_00419_2() throws Exception {

		String parameter = "{\"command\":\"UPDATE\",\"type\":\"CMGBUILDING\",\"dbId\":13,\"data\":{\"kind\":1002,\"pid\":400000012,\"objStatus\":\"UPDATE\"}}";
		
		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}
	
}
