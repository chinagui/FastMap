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
import com.navinfo.dataservice.dao.glm.search.RdVoiceguideSearch;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.search.SearchProcess;

public class RdVoiceguideTest  extends InitApplication{

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

			RdVoiceguideSearch search = new RdVoiceguideSearch(conn);

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

			System.out.println(p.searchDataByPid(ObjType.RDVOICEGUIDE, 100000007).Serialize(ObjLevel.BRIEF));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void createTest_1() throws Exception {
		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDVOICEGUIDE\",\"dbId\":42,\"data\":{\"inLinkPid\":712681,\"nodePid\": 478408,\"outLinkPids\":[720432,50113339]}}";
		
		log.info(parameter);
		
		Transaction t = new Transaction(parameter);
		
		String msg = t.run();
	}
	
	@Test
	public void createTest_2() throws Exception {
		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDVOICEGUIDE\",\"dbId\":42,\"data\":{\"inLinkPid\":607312,\"nodePid\": 742411,\"outLinkPid\":607304}}";
		log.info(parameter);
		Transaction t = new Transaction(parameter);
		String msg = t.run();
	}
	@Test
	public void updateTest_1() throws Exception {
		String parameter = "{\"command\":\"UPDATE\",\"type\":\"RDVOICEGUIDE\",\"dbId\":42,\"data\":{\"details\":[{\"objStatus\":\"UPDATE\",\"guideCode\":4,\"rowId\":\"482FA9D70A2246D09FE6464663B88128\",\"pid\":100000006}],\"objStatus\":\"UPDATE\",\"pid\":100000004}}";
		log.info(parameter);
		Transaction t = new Transaction(parameter);	
		String msg = t.run();
	}
	
	
	@Test
	public void updateTest_2() throws Exception {
		String parameter = "{\"command\":\"UPDATE\",\"type\":\"RDVOICEGUIDE\",\"dbId\":42,\"data\":{\"details\":[{\"objStatus\":\"DELETE\",\"guideCode\":4,\"rowId\":\"482FA9D70A2246D09FE6464663B88128\",\"pid\":100000006}],\"objStatus\":\"UPDATE\",\"pid\":100000004}}";
		log.info(parameter);
		Transaction t = new Transaction(parameter);	
		String msg = t.run();
	}
	@Test
	public void deleteTest_1() throws Exception {
		String parameter = "{\"command\":\"DELETE\",\"type\":\"RDVOICEGUIDE\",\"dbId\":42,\"objId\":100000009}";
		Transaction t = new Transaction(parameter);
		String msg = t.run();
	}

}
