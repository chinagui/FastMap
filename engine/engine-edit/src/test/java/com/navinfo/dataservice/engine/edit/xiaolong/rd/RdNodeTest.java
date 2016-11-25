package com.navinfo.dataservice.engine.edit.xiaolong.rd;

import java.sql.Connection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.search.RdNodeSearch;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.Transaction;

import net.sf.json.JSONArray;

public class RdNodeTest extends InitApplication{
	
	@Override
	@Before
	public void init() {
		initContext();
	}
	
	@Test
	public void testCreate()
	{
		String parameter = "{\"command\":\"CREATE\",\"dbId\":17,\"objId\":301002706,\"data\":{\"longitude\":116.37573358621606,\"latitude\":40.04361587518659},\"type\":\"RDNODE\"}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testMove() {
		String parameter = "{\"command\":\"MOVE\",\"dbId\":17,\"objId\":15430054,\"data\":{\"longitude\":116.62668853998183,\"latitude\":40.333333333333336},\"type\":\"RDNODE\"}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	
	public void testDelete() {
		String parameter = "{\"command\":\"DELETE\",\"type\":\"RDTRAFFICSIGNAL\",\"dbId\":19,\"objId\":201000009}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testRdNodeRender()
	{
		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(17);

			RdNodeSearch search = new RdNodeSearch(conn);
			
			search.searchDataByTileWithGap(432090, 197946, 19, 10);

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void testGetRdNodeBySpatial()
	{
		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(17);

			RdNodeSearch search = new RdNodeSearch(conn);
			
			String wtk = "POLYGON ((116.69263064861298 40.28212436356775,116.69337093830109 40.28212436356775,116.69337093830109 40.2826891060652,116.69263064861298 40.2826891060652,116.69263064861298 40.28212436356775))";
			
			List<SearchSnapshot> searchSnapshot = search.searchDataBySpatial(wtk);
			
			JSONArray array = new JSONArray();

			for (SearchSnapshot snap : searchSnapshot) {

				array.add(snap.Serialize(ObjLevel.BRIEF), JsonUtils.getJsonConfig());
			}
			
			System.out.println(array);

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}	
