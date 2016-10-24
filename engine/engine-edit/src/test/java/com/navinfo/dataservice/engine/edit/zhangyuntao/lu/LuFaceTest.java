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
		String parameter = "{\"command\":\"UPDATE\",\"dbId\":17,\"type\":\"LUFACE\",\"objId\":305000002,\"data\":{\"faceNames\":[{\"pid\":305000002,\"nameGroupid\":1,\"langCode\":\"CHI\",\"name\":\"\",\"phonetic\":\"\",\"srcFlag\":0,\"objStatus\":\"INSERT\"}],\"pid\":305000002}}";
		TestUtil.run(parameter);
	}

	@Test
	public void createFaceByLuLInkTest() {
		String parameter = "{\"command\":\"CREATE\",\"type\":\"ADFACE\",\"linkType\":\"ADLINK\",\"dbId\":43,\"data\":{\"linkPids\":[\"100035661\",\"100035663\",\"100035666\"]}}";
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
		String requester = "{\"command\":\"UPDATE\",\"dbId\":43,\"type\":\"LUFACE\",\"objId\":378,\"data\":{\"faceNames\":[{\"pid\":0,\"nameGroupid\":756,\"langCode\":\"CHI\",\"name\":\"44444\",\"phonetic\":\"4444\",\"srcFlag\":0,\"objStatus\":\"INSERT\"}],\"pid\":378}}";
		TestUtil.run(requester);
	}

	@Test
	public void delete(){
		String parameter = "{\"command\":\"DELETE\",\"dbId\":17,\"type\":\"LUFACE\",\"objId\":301000000}";
		TestUtil.run(parameter);
	}
	
	
}
