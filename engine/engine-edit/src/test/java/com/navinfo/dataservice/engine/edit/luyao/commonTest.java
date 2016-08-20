package com.navinfo.dataservice.engine.edit.luyao;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchSelector;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.search.SearchProcess;

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
		String parameter = "{\"command\":\"MOVE\",\"dbId\":42,\"objId\":100025193,\"data\":{\"longitude\":116.62476941943167,\"latitude\":39.999848815977785},\"type\":\"RDNODE\"}";

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

			String parameter = "{\"dbId\":42,\"type\":\"RDLINK\",\"data\":{\"queryType\":\"RDSPEEDLIMIT\",\"linkPid\":735584,\"direct\":2}}";

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

		String parameter = "{\"command\":\"BATCH\",\"dbId\":42,\"type\":\"RDLINKSPEEDLIMIT\",\"direct\":2,\"linkPids\":[100008842,100008844,100008845],\"linkSpeedLimit\":{\"speedType\":0,\"fromSpeedLimit\":100,\"fromLimitSrc\":2,\"toSpeedLimit\":100,\"toLimitSrc\":3,\"speedClassWork\":1}}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}
}
