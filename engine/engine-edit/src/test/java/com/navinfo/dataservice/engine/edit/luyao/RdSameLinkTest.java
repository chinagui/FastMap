package com.navinfo.dataservice.engine.edit.luyao;

import java.sql.Connection;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.util.ResponseUtils;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.search.RdSameLinkSearch;
import com.navinfo.dataservice.dao.glm.search.RdVoiceguideSearch;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.search.SearchProcess;

public class RdSameLinkTest extends InitApplication{

	protected Logger log = Logger.getLogger(this.getClass());
	
	@Override
	public void init() {
		initContext();
	}
	
	@Test
	public void testRender() {
		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(42);

			RdSameLinkSearch search = new RdSameLinkSearch(conn);

			List<SearchSnapshot> data = search.searchDataByTileWithGap(107943,
					49613, 17, 80);

			System.out.println("data:"
					+ ResponseUtils.assembleRegularResult(data));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	
	@Test
	public void testGetByPid() {
		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(42);

			SearchProcess p = new SearchProcess(conn);

			System.out.println(p.searchDataByPid(ObjType.RDSAMELINK, 4831925).Serialize(ObjLevel.BRIEF));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void createTest_1() throws Exception {
		String parameter = "{\"command\":\"CREATE\",\"dbId\":42,\"type\":\"RDSAMELINK\",\"data\":{\"links\":[{\"linkPid\":\"100008704\",\"type\":\"RDLINK\",\"isMain\":1},{\"linkPid\":\"100036564\",\"type\":\"ADLINK\",\"isMain\":0}]}}";
		
		log.info(parameter);
		
		Transaction t = new Transaction(parameter);
		
		String msg = t.run();
	}
	
	@Test
	public void createTest_2() throws Exception {
		String parameter = "{\"command\":\"CREATE\",\"dbId\":42,\"type\":\"RDSAMELINK\",\"data\":{\"links\":[{\"linkPid\":100008802,\"type\":\"RDLINK\",\"isMain\":1},{\"linkPid\":100036565,\"type\":\"ADLINK\",\"isMain\":0}]}}";
		
		log.info(parameter);
		Transaction t = new Transaction(parameter);
		String msg = t.run();
	}
	

	@Test
	public void deleteTest_1() throws Exception {
		String parameter = "{\"command\":\"DELETE\",\"type\":\"RDSAMELINK\",\"dbId\":42,\"objId\":100000014}";
		Transaction t = new Transaction(parameter);
		String msg = t.run();
	}


}
