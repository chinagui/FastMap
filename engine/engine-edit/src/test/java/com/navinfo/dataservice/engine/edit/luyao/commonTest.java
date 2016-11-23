package com.navinfo.dataservice.engine.edit.luyao;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.AngleCalculator;
import com.navinfo.dataservice.commons.util.ExcelReader;
import com.navinfo.dataservice.commons.util.ResponseUtils;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkName;
import com.navinfo.dataservice.dao.glm.search.RdObjectSearch;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkNameSelector;
import com.navinfo.dataservice.engine.edit.InitApplication;
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
	public void testGetByPid_0() {
		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(17);

			SearchProcess p = new SearchProcess(conn);

			IObj obj = p.searchDataByPid(ObjType.RDLANECONNEXITY, 6579808);
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
	public void selectorTest() {

		Connection conn = null;

		try {
			conn = DBConnector.getInstance().getConnectionById(17);

			RdLinkNameSelector p = new RdLinkNameSelector(conn);
			Set<Integer> pids = new HashSet<Integer>();

			pids.add(236838);
			pids.add(299148);

			Map<Integer, List<RdLinkName>> mapSort = p.loadNameByLinkPids(pids);

			System.out.print("");

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
	public void josnArrayTest() {
		JSONArray array = new JSONArray();

		array.add("123");
		array.add(123);
		array.add("a");
		array.add(1.2);

		JSONArray array2 = new JSONArray();
		array2.add(array);
		array2.add("1");
		array2.add(1);
	}

	@Test
	public void ListTest() {
		List<Integer> pids = new ArrayList<Integer>();
		pids.add(1);
		pids.add(1);
		pids.add(1);
		pids.add(1);
		pids.add(1);
		pids.add(1);
		pids.add(1);
		pids.add(1);
		pids.add(1);

	}

	@Test
	public void jsonTest() {

		JSONObject parameter = new JSONObject();

		parameter.put("command", "CREATE");
		parameter.put("dbId", 17);

		parameter.put("data", new JSONObject());

		parameter.put("type", "RDLINK");

		JSONObject requester = new JSONObject();

		requester.put("parameter", parameter);
		String StrRrequester = requester.getString("parameter");
		int i = 1;
	}

	@Test
	public void treeMapTest() {

		TreeMap<Integer, String> map = new TreeMap<Integer, String>();

		map.put(1, "1");
		map.put(1, "2");
		map.put(1, "3");
		map.put(1, "4");
		map.put(1, "5");

		System.out.print("");

	}
	
	@Test
	public void isNuber() {
		
		String value="aaa5";
		String regex = ".*[0-9].*"; 
		Pattern pattern = Pattern.compile(regex);
		//需要转换成半角在匹配
		Matcher matcher=pattern.matcher(ExcelReader.f2h(value));
		boolean key= matcher.matches();
		
		System.out.print("");
	}
	
	@Test
	public void isEng() {
		
		String value="我的*aa&……%￥5";
		String regex = ".*[a-zA-Z].*"; 
		Pattern pattern = Pattern.compile(regex);
		//需要转换成半角在匹配
		Matcher matcher=pattern.matcher(ExcelReader.f2h(value));
		boolean key= matcher.matches();
		
		System.out.print("");
	}

}
