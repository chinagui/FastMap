package com.navinfo.dataservice.engine.edit.xiaolong.check;

import java.sql.Connection;

import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.check.NiValExceptionSelector;
import com.navinfo.dataservice.engine.edit.InitApplication;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class NiValExceptionTest extends InitApplication {

	@Override
	public void init() {
		initContext();
	}

	@Test
	public void testLoadByGrid() throws Exception {

		String parameter = "{\"dbId\":42,\"grids\":[60560301,60560302,60560303,60560311,60560312,60560313,60560322,60560323,60560331,60560332,60560333,60560320,60560330,60560300,60560321,60560310]}";

		Connection conn = null;

		try {

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			int dbId = jsonReq.getInt("dbId");

			JSONArray grids = jsonReq.getJSONArray("grids");

			conn = DBConnector.getInstance().getConnectionById(dbId);

			NiValExceptionSelector selector = new NiValExceptionSelector(conn);

			int data = selector.loadCountByGrid(grids);

			System.out.println("data:"+data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
