/**
 * 
 */
package com.navinfo.dataservice.engine.edit.xiaolong.rd;

import java.sql.Connection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.util.ResponseUtils;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.search.RdObjectSearch;
import com.navinfo.dataservice.dao.glm.search.RdSeSearch;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.search.SearchProcess;

import net.sf.json.JSONObject;

/**
 * @ClassName: RdInterTest
 * @author Zhang Xiaolong
 * @date 2016年8月3日 下午2:14:08
 * @Description: TODO
 */
public class RdObjectTest extends InitApplication {
	@Override
	@Before
	public void init() {
		initContext();
	}

	@Test
	public void testGetByPid() {
		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(17);

			SearchProcess p = new SearchProcess(conn);

			System.out.println(p.searchDataByPid(ObjType.RDOBJECT, 8229).Serialize(ObjLevel.BRIEF));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testAddRdObject() {
		String parameter = "{\"command\":\"CREATE\",\"dbId\":17,\"type\":\"RDINTER\",\"data\":{\"links\":[210003523],\"nodes\":[208002756,305002719]}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();

			String log = t.getLogs();

			JSONObject json = new JSONObject();

			json.put("result", msg);

			json.put("log", log);

			json.put("check", t.getCheckLog());

			json.put("pid", t.getPid());

			System.out.println(json.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testBreakRdObjectLink()
	{
		String parameter = "{\"command\":\"BREAK\",\"type\":\"RDLINK\",\"dbId\":42,\"objId\":100008941,\"data\":{\"longitude\":116.41262214724813,\"latitude\":40.03393250878109}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();

			String log = t.getLogs();

			JSONObject json = new JSONObject();

			json.put("result", msg);

			json.put("log", log);

			json.put("check", t.getCheckLog());

			json.put("pid", t.getPid());

			System.out.println(json.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testUpdateRdInter() {
		String parameter = "{\"command\":\"UPDATE\",\"type\":\"RDINTER\",\"dbId\":17,\"data\":{\"objStatus\":\"UPDATE\",\"pid\":307000003,\"links\":[305002913,201002827],\"nodes\":[201002212,306002155,201002211,302002186]}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();

			String log = t.getLogs();

			JSONObject json = new JSONObject();

			json.put("result", msg);

			json.put("log", log);

			json.put("check", t.getCheckLog());

			json.put("pid", t.getPid());

			System.out.println(json.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testDelRdObject() {
		String parameter = "{\"command\":\"CREATE\",\"dbId\":19,\"objId\":19613202,\"data\":{\"longitude\":116.44155588183112,\"latitude\":40.02823890353918},\"type\":\"RDNODE\"}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();

			String log = t.getLogs();

			JSONObject json = new JSONObject();

			json.put("result", msg);

			json.put("log", log);

			json.put("check", t.getCheckLog());

			json.put("pid", t.getPid());
			
			System.out.println(json.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testDelRdLink() {
		String parameter = "{\"command\":\"DELETE\",\"type\":\"RDROAD\",\"dbId\":17,\"objId\":200000004}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();

			String log = t.getLogs();

			JSONObject json = new JSONObject();

			json.put("result", msg);

			json.put("log", log);

			json.put("check", t.getCheckLog());

			json.put("pid", t.getPid());
			
			System.out.println(json.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void RdLaneRender()
	{
		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(17);

			RdSeSearch search = new RdSeSearch(conn);
			
			List<SearchSnapshot> data = search.searchDataByTileWithGap(215720, 98684, 18, 40);
			
			System.out.println("data:"+ResponseUtils.assembleRegularResult(data));

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	@Test
	public void testRdSpeedLimitRender()
	{
		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(17);

			RdObjectSearch search = new RdObjectSearch(conn);
			
			List<SearchSnapshot> data = search.searchDataByTileWithGap(215685, 98715, 18, 80);
			
			System.out.println("data:"+ResponseUtils.assembleRegularResult(data));

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void testGetRdObjectName()
	{
		Connection conn;
		try {
			
			String parameter = "{\"dbId\":17,\"type\":\"RDLANEVIA\",\"data\":{\"inLinkPid\":390487,\"nodePid\":282934,\"outLinkPid\":391661,\"type\":\"RDRESTRICTION\"}}";
			
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String objType = jsonReq.getString("type");

			int dbId = jsonReq.getInt("dbId");

			JSONObject data = jsonReq.getJSONObject("data");

			conn = DBConnector.getInstance().getConnectionById(dbId);

			SearchProcess p = new SearchProcess(conn);

			System.out.println(p.searchDataByCondition(ObjType.valueOf(objType), data));

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
