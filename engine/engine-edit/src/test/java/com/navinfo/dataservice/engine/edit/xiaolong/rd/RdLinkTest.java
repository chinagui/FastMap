package com.navinfo.dataservice.engine.edit.xiaolong.rd;

import java.sql.Connection;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.search.RdLinkSearch;
import com.navinfo.dataservice.dao.glm.search.RwLinkSearch;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.search.SearchProcess;

import net.sf.json.JSONObject;

public class RdLinkTest extends InitApplication{
	
	@Override
	@Before
	public void init() {
		initContext();
	}
	
	public RdLinkTest() throws Exception {
	}
	
	@Test
	public void testGetByPid()
	{
		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(42);

			String parameter = "{\"type\":\"RWLINK\",\"dbId\":42,\"objId\":100007138}";

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			SearchProcess p = new SearchProcess(conn);

			System.out.println(p.searchDataByPid(ObjType.RDLINK, 100007138).Serialize(ObjLevel.BRIEF));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testDelete() {
		String parameter = "{\"command\":\"DELETE\",\"dbId\":42,\"type\":\"RDLINK\",\"objId\":100007878}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testAddRdLink()
	{
		String parameter = "{\"command\":\"CREATE\",\"dbId\":42,\"data\":{\"eNodePid\":0,\"sNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.46895051002502,40.025075819137506],[116.46738632493417,40.02477644044782],[116.4666223526001,40.02477183584991]]},\"catchLinks\":,\"type\":\"RDLINK\"}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testRepairLink()
	{
		String parameter = "{\"command\":\"CREATE\",\"dbId\":42,\"objId\":100008040,\"data\":{\"longitude\":116.66654182223411,\"latitude\":40.283555172551544},\"type\":\"RDNODE\"}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
