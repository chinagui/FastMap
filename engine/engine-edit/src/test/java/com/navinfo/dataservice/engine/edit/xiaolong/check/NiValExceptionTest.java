package com.navinfo.dataservice.engine.edit.xiaolong.check;

import java.sql.Connection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.check.NiValExceptionOperator;
import com.navinfo.dataservice.dao.check.NiValExceptionSelector;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.navicommons.database.Page;

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
	
	@Test
	public void testCheck() throws Exception
	{
		String parameter = "{\"dbId\":42,\"type\":2,\"id\":\"9aab29cf60bbbc997f12d8368b5920c2\"}";
		
		JSONObject jsonReq = JSONObject.fromObject(parameter);

		int dbId = jsonReq.getInt("dbId");

		String id = jsonReq.getString("id");

		int type = jsonReq.getInt("type");

		Connection conn = DBConnector.getInstance().getConnectionById(dbId);

		NiValExceptionOperator selector = new NiValExceptionOperator(conn);

		selector.updateCheckLogStatus(id, type);
	}

	@Test
	public void testList() throws Exception
	{
		Connection conn = null;
		try{
			Set<String> grids = new HashSet<String>();
			grids.add("60560303");

			conn = DBConnector.getInstance().getConnectionById(17);

			NiValExceptionSelector selector = new NiValExceptionSelector(conn);

			Page page = selector.list(0, grids,20,1);
			System.out.println(page.getResult());
		}finally{
			DbUtils.closeQuietly(conn);
		}
	}
}
