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

			System.out.println(p.searchDataByPid(ObjType.RDRESTRICTION, 29119).Serialize(ObjLevel.BRIEF));

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
		String parameter = "{\"command\":\"UPDATE\",\"dbId\":25,\"type\":\"RDLINK\",\"objId\":577195,\"data\":{\"limits\":[false,false,{\"linkPid\":577195,\"rowId\":\"3657658936FF2443E050A8C083049593\",\"type\":9,\"limitDir\":0,\"timeDomain\":\"[[(t2)(t6)]*[(h7m0)(h20m0)]]\",\"vehicle\":0,\"tollType\":9,\"weather\":9,\"inputTime\":\"\",\"processFlag\":0,\"objStatus\":\"DELETE\"}],\"pid\":577195}}";
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
		String parameter = "{\"command\":\"UPDATE\",\"dbId\":25,\"type\":\"RDLINK\",\"objId\":50113355,\"data\":{\"names\":[{\"linkPid\":50113355,\"rowId\":\"\",\"nameGroupid\":2625347,\"name\":\"京宝三纬路\",\"seqNum\":1,\"nameClass\":1,\"inputTime\":\"\",\"nameType\":0,\"srcFlag\":9,\"routeAtt\":0,\"code\":0,\"objStatus\":\"INSERT\"}],\"pid\":50113355}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
