package com.navinfo.dataservice.engine.edit.zhangyuntao.lu;

import java.sql.Connection;

import org.apache.log4j.Logger;
import org.junit.Test;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.search.SearchProcess;
import com.navinfo.dataservice.engine.edit.zhangyuntao.eleceye.TestUtil;

public class LuFaceTest extends InitApplication {

	@Override
	public void init() {
		initContext();
	}

	protected Logger log = Logger.getLogger(this.getClass());

	@Test
	public void createFaceByGeometryTest() {
		String parameter = "";
		TestUtil.run(parameter);
	}

	@Test
	public void createFaceByLuLInkTest() {
		String parameter = "{\"command\":\"CREATE\",\"type\":\"LUFACE\",\"dbId\":17,\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.37564182281494,40.59147827180602],[116.37549161911011,40.590916117076524],[116.37657523155212,40.59088352825164],[116.37564182281494,40.59147827180602]]}}}";
		TestUtil.run(parameter);
	}
	
	@Test
	public void testSearchLuFace() {
		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(42);

			SearchProcess p = new SearchProcess(conn);

			System.out.println(p.searchDataByPid(ObjType.LUFACE, 100034535).Serialize(ObjLevel.FULL));

		} catch (Exception e) {
		}
	}
	
	@Test
	public void update(){
		String requester = "{\"command\":\"UPDATE\",\"dbId\":17,\"type\":\"LUFACE\",\"objId\":202000007,\"data\":{\"kind\":1,\"pid\":202000007,\"objStatus\":\"UPDATE\"}}";
		TestUtil.run(requester);
	}

	@Test
	public void delete(){
		String parameter = "{\"command\":\"DELETE\",\"dbId\":17,\"type\":\"ZONEFACE\",\"objId\":220000033}";
		TestUtil.run(parameter);
	}
	
	
}
