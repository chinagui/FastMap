package com.navinfo.dataservice.engine.edit.xiaolong.rd;

import java.sql.Connection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.search.LuNodeSearch;
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
		String parameter = "{\"command\":\"DEPART\",\"dbId\":17,\"objId\":210002203,\"data\":{\"catchNodePid\":0,\"catchLinkPid\":0,\"linkPid\":\"301002875\",\"longitude\":116.38789415359497,\"latitude\":40.24269122410369},\"type\":\"RDLINK\"}";
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
		String parameter = "{\"command\":\"CREATE\",\"dbId\":17,\"objId\":19688120,\"data\":{\"longitude\":116.51232412837189,\"latitude\":39.76244903245604},\"type\":\"RDNODE\"}";
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
			
			String wkt = "{\"type\":\"Polygon\",\"coordinates\":[[[116.47654116153716,40.01106242504529],[116.47654116153716,40.01114049061955],[116.47666454315184,40.01114049061955],[116.47666454315184,40.01106242504529],[116.47654116153716,40.01106242504529]]]}}";
			
			List<SearchSnapshot> searchSnapshot = search.searchDataBySpatial(Geojson.geojson2Wkt(wkt));
			
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
