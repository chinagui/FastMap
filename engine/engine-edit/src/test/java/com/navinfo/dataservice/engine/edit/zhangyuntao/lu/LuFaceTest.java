package com.navinfo.dataservice.engine.edit.zhangyuntao.lu;

import java.sql.Connection;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.search.SearchProcess;

import net.sf.json.JSONObject;

public class LuFaceTest extends InitApplication {

	@Override
	@Before
	public void init() {
		initContext();
	}

	protected Logger log = Logger.getLogger(this.getClass());

	@Test
	public void createFaceByGeometryTest() {
//		String parameter = "{\"command\":\"CREATE\",\"type\":\"LUFACE\",\"dbId\":43,\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.46976590156555,40.09303544721415],[116.47152543067932,40.093027239657374],[116.47086024284361,40.09217364834912],[116.46976590156555,40.09303544721415]]}}}";
//		String parameter = "{\"command\":\"CREATE\",\"type\":\"LUFACE\",\"dbId\":43,\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.24781847000122,40.918117629692105],[116.2487518787384,40.91345582170584],[116.25728130340576,40.91704746980017],[116.24781847000122,40.918117629692105]]}}}";
		String parameter = "{\"command\":\"CREATE\",\"type\":\"LUFACE\",\"dbId\":43,\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.5012764930725,40.500314951988074],[116.5032935142517,40.50040469248713],[116.50247812271118,40.50141630435382],[116.5012764930725,40.500314951988074]]}}}";
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
	public void createFaceByLuLInkTest() {
		String parameter = "{\"command\":\"CREATE\",\"type\":\"ADFACE\",\"linkType\":\"ADLINK\",\"dbId\":43,\"data\":{\"linkPids\":[\"100035661\",\"100035663\",\"100035666\"]}}";
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
	public void testSearchLuFace() {
		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(43);

			SearchProcess p = new SearchProcess(conn);

			System.out.println(p.searchDataByPid(ObjType.LUFACE, 100034508).Serialize(ObjLevel.FULL));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
}
