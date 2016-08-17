/**
 * 
 */
package com.navinfo.dataservice.engine.edit.xiaolong.rd;

import java.sql.Connection;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.search.SearchProcess;

import net.sf.json.JSONObject;

/** 
* @ClassName: RdVariableSpeedTest 
* @author Zhang Xiaolong
* @date 2016年8月15日 下午8:49:43 
* @Description: TODO
*/
public class RdVariableSpeedTest extends InitApplication {

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

			System.out.println(p.searchDataByPid(ObjType.RDVARIABLESPEED, 31800146).Serialize(ObjLevel.BRIEF));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testAdd()
	{
		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDVARIABLESPEED\",\"dbId\":42,\"data\":{\"inLinkPid\":100008725,\"outLinkPid\":100008727,\"nodePid\":100025838,\"vias\":[100008729,100008730]}}";
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
	public void testBreakRdLink()
	{
		String parameter = "{\"command\":\"BREAK\",\"dbId\":42,\"objId\":100008729,\"data\":{\"longitude\":116.42629419345741,\"latitude\":40.023691997213426},\"type\":\"RDLINK\"}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
