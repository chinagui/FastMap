package com.navinfo.dataservice.engine.edit.xiaolong.ad;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Before;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.util.ResponseUtils;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.search.SearchProcess;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class AdNodeTest extends InitApplication {

	@Override
	@Before
	public void init() {
		initContext();
	}

	protected Logger log = Logger.getLogger(this.getClass());

	public void createAdNodeTest() throws Exception {
		String parameter = "{\"command\":\"DELETE\",\"dbId\":17,\"type\":\"ADNODE\",\"objId\":44489}";
		Transaction t = new Transaction(parameter);
		String msg = t.run();
	}

	public void deleteAdNodeTest() throws Exception {
		String parameter = "{\"command\":\"DELETE\",\"type\":\"ADNODE\",\"projectId\":11,\"objId\":100021877}";
		Transaction t = new Transaction(parameter);
		String msg = t.run();
	}

	// "{"command":"UPDATE","type":"ADNODE","projectId":11,"data":{"kind":"12","pid":100021403,"objStatus":"UPDATE"}}"
	public void updateAdNodeTest() throws Exception {
		String parameter = "{\"command\":\"UPDATE\",\"type\":\"ADNODE\",\"projectId\":11,\"data\":{\"kind\":\"3\",\"pid\":100021717,\"objStatus\":\"UPDATE\"}}";
		log.info(parameter);
		Transaction t = new Transaction(parameter);
		;
		String msg = t.run();
	}

	public void moveAdNodeTest() throws Exception {
		String parameter = "{\"command\":\"MOVE\",\"projectId\":11,\"objId\":100021744,\"data\":{\"longitude\":116.4743861820221,\"latitude\":40.02400009432636},\"type\":\"ADNODE\"}";
		log.info(parameter);
		Transaction t = new Transaction(parameter);
		;
		String msg = t.run();
	}

	public static void testSearchByGap() {
		String parameter = "{\"projectId\":11,\"gap\":80,\"types\":[\"ADNODE\"],\"z\":17,\"x\":107945,\"y\":49615}";
		JSONObject jsonReq = JSONObject.fromObject(parameter);

		JSONArray type = jsonReq.getJSONArray("types");

		int projectId = jsonReq.getInt("projectId");

		int x = jsonReq.getInt("x");

		int y = jsonReq.getInt("y");

		int z = jsonReq.getInt("z");

		int gap = jsonReq.getInt("gap");

		List<ObjType> types = new ArrayList<ObjType>();

		for (int i = 0; i < type.size(); i++) {
			types.add(ObjType.valueOf(type.getString(i)));
		}

		try {
			SearchProcess p = new SearchProcess(DBConnector.getInstance().getConnectionById(projectId));
			JSONObject data = p.searchDataByTileWithGap(types, x, y, z, gap);

			System.out.println(ResponseUtils.assembleRegularResult(data));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) throws Exception {
		// new AdNodeTest().createAdNodeTest();
		// new AdNodeTest().deleteAdNodeTest();
		// new AdNodeTest().updateAdNodeTest();
		new AdNodeTest().createAdNodeTest();

	}
}
