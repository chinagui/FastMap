package com.navinfo.dataservice.engine.edit.zhangyuntao.lc;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.util.ResponseUtils;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.search.SearchProcess;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @Title: LcNodeTest.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月8日 下午12:51:44
 * @version: v1.0
 */
public class LcNodeTest extends InitApplication {

	public LcNodeTest() {
	}

	@Override
	public void init() {
		super.initContext();
	}

	@Test
	public void testSearchByGap() {
		String parameter = "{\"projectId\":11,\"gap\":80,\"types\":[\"ADNODE\"],\"z\":17,\"x\":107945,\"y\":49615}";
		parameter = "{\"dbId\":43,\"gap\":80,\"types\":[\"LCNODE\"],\"z\":19,\"x\":431743,\"y\":198519}";
		JSONObject jsonReq = JSONObject.fromObject(parameter);

		JSONArray type = jsonReq.getJSONArray("types");

		int dbId = jsonReq.getInt("dbId");

		int x = jsonReq.getInt("x");

		int y = jsonReq.getInt("y");

		int z = jsonReq.getInt("z");

		int gap = jsonReq.getInt("gap");

		List<ObjType> types = new ArrayList<ObjType>();

		for (int i = 0; i < type.size(); i++) {
			types.add(ObjType.valueOf(type.getString(i)));
		}

		try {
			SearchProcess p = new SearchProcess(DBConnector.getInstance().getConnectionById(dbId));
			JSONObject data = p.searchDataByTileWithGap(types, x, y, z, gap);

			System.out.println(ResponseUtils.assembleRegularResult(data));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
