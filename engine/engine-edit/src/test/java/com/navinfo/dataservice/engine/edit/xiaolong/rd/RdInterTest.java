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
import com.navinfo.dataservice.dao.glm.search.RdInterSearch;
import com.navinfo.dataservice.dao.glm.search.RwLinkSearch;
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
public class RdInterTest extends InitApplication {
	@Override
	@Before
	public void init() {
		initContext();
	}

	@Test
	public void testGetByPid() {
		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(42);

			SearchProcess p = new SearchProcess(conn);

			System.out.println(p.searchDataByPid(ObjType.RDINTER, 46933234).Serialize(ObjLevel.BRIEF));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testAddRdInter() {
		String parameter = "{\"command\":\"CREATE\",\"dbId\":42,\"type\":\"RDINTER\",\"data\":{\"links\":[100008413],\"nodes\":[100025497,100025498]}}";
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
	public void testDelRdInter() {
		String parameter = "{\"command\":\"DELETE\",\"type\":\"RDINTER\",\"dbId\":42,\"objId\":100000750}";
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
	public void testCRFINTERRender()
	{
		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(42);

			RdInterSearch search = new RdInterSearch(conn);
			
			List<SearchSnapshot> data = search.searchDataByTileWithGap(107943, 49613, 17, 80);
			
			System.out.println("data:"+ResponseUtils.assembleRegularResult(data));

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
