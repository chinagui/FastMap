package com.navinfo.dataservice.engine.edit.zhangyuntao.lu;

import java.sql.Connection;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.search.SearchProcess;

import net.sf.json.JSONObject;

public class LuLinkTest extends InitApplication {

	@Override
	@Before
	public void init() {
		initContext();
	}

	// 初始化系统参数
	private Connection conn;
	protected Logger log = Logger.getLogger(this.getClass());
	// 创建一条link

	@Test
	public void createLuLinkTest() {
//		String parameter = "{\"command\":\"CREATE\",\"dbId\":43,\"data\":{\"eNodePid\":0,\"sNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.23831272125244,40.91574217557029],[116.23770117759705,40.913731485583625]]},\"catchLinks\":[]},\"type\":\"LULINK\"}";
//		String parameter = "{\"command\":\"CREATE\",\"dbId\":43,\"data\":{\"eNodePid\":0,\"sNodePid\":100034583,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.23769044876099,40.91373148558362],[116.24097347259521,40.914599008521996]]},\"catchLinks\":[{\"nodePid\":100034583,\"lon\":116.23769044876099,\"lat\":40.91373148558362}]},\"type\":\"LULINK\"}";
		String parameter = "{\"command\":\"CREATE\",\"dbId\":43,\"data\":{\"eNodePid\":100034582,\"sNodePid\":100034584,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.24096274375916,40.9146071161593],[116.23830199241638,40.91574217557027]]},\"catchLinks\":[{\"nodePid\":100034584,\"lon\":116.24096274375916,\"lat\":40.9146071161593},{\"nodePid\":100034582,\"lon\":116.23830199241638,\"lat\":40.91574217557027}]},\"type\":\"LULINK\"}";
		log.info(parameter);
		Transaction t = new Transaction(parameter);
		;
		try {
			String msg = t.run();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void deleteLuLinkTest() {
		String parameter = "{\"command\":\"DELETE\",\"type\":\"LULINK\",\"dbId\":43,\"projectId\":11,\"objId\":100034527}";
		log.info(parameter);
		System.out.println(parameter + "-------------------");
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
		} catch (Exception e) {	
			e.printStackTrace();
		}

	}

	// 打断一条LINK
	@Test
	public void breakLuLinkTest() {
		String parameter = "{\"command\":\"CREATE\",\"dbId\":43,\"objId\":100034525,\"data\":{\"longitude\":116.48408486591549,\"latitude\":40.30854271853128},\"type\":\"LUNODE\"}";

		log.info(parameter);
		System.out.println(parameter + "-------------------");
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testSearchLuLink() {
		String parameter = "{\"projectId\":11,\"type\":\"LULINK\",\"pid\":100034447}";

		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(43);

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			SearchProcess p = new SearchProcess(conn);

			System.out.println(p.searchDataByPid(ObjType.LULINK, 100034447).Serialize(ObjLevel.FULL));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void tesRepairtLuLink() {
		String parameter = "{\"command\":\"REPAIR\",\"dbId\":43,\"projectId\":11,\"objId\":100034528,\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.62528,39.25022],[116.62528,39.25006],[116.62535838820631,39.25011395094421],[116.62544,39.25017],[116.62528,39.25022]]},\"interLinks\":[],\"interNodes\":[]},\"type\":\"LULINK\"}";

		log.info(parameter);
		System.out.println(parameter + "-------------------");
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testSearchLuNode() {
		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(43);

			SearchProcess p = new SearchProcess(conn);

			System.out.println(p.searchDataByPid(ObjType.LUNODE, 100034469).Serialize(ObjLevel.FULL));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
