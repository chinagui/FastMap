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
import com.navinfo.dataservice.dao.glm.search.RdCrossSearch;
import com.navinfo.dataservice.dao.glm.search.RdLinkSearch;
import com.navinfo.dataservice.dao.glm.search.RdObjectSearch;
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

			System.out.println(p.searchDataByPid(ObjType.RDLANECONNEXITY, 306000005).Serialize(ObjLevel.BRIEF));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testAddRdObject() {
		String parameter = "{\"command\":\"CREATE\",\"dbId\":42,\"type\":\"RDSAMENODE\",\"data\":{\"nodes\":[{\"nodePid\":\"100027134\",\"type\":\"RDNODE\",\"isMain\":1},{\"nodePid\":\"100025966\",\"type\":\"ADNODE\",\"isMain\":0}]}}";
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
		String parameter = "{\"command\":\"UPDATE\",\"type\":\"RDINTER\",\"dbId\":42,\"data\":{\"objStatus\":\"UPDATE\",\"pid\":100000759,\"links\":[100006596,100006598,100006599,100006614,100006613],\"nodes\":[100023749,100023753,100023754,100023755,100023764]}}";
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
		String parameter = "{\"command\":\"DELETE\",\"type\":\"RDOBJECT\",\"dbId\":42,\"objId\":100000041}";
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
		String parameter = "{\"command\":\"UPDATE\",\"type\":\"RDOBJECT\",\"dbId\":42,\"data\":{\"names\":[{\"pid\":100000046,\"nameGroupid\":1,\"langCode\":\"CHI\",\"name\":\"西红门南桥\",\"phonetic\":\"Xi+Hong+Men+Nan+Qiao\",\"srcFlag\":0,\"objStatus\":\"INSERT\"}],\"pid\":100000046}}";
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
	public void testCrfObjectRender()
	{
		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(17);

			RdObjectSearch search = new RdObjectSearch(conn);
			
			List<SearchSnapshot> data = search.searchDataByTileWithGap(108004, 49481, 17, 80);
			
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
