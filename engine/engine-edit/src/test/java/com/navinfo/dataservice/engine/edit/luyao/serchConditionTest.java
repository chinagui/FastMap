package com.navinfo.dataservice.engine.edit.luyao;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.AngleCalculator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchSelector;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.search.SearchProcess;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;

public class serchConditionTest extends InitApplication {

	@Override
	@Before
	public void init() {
		initContext();
	}

	@Test
	public void getByCondition() {

		Connection conn = null;

		try {

			String parameter = "{\"dbId\":17,\"type\":\"RDLINK\",\"data\":{\"queryType\":\"RDLINKSPEEDLIMIT\",\"linkPid\":220000997,\"direct\":2}}";

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String objType = jsonReq.getString("type");

			int dbId = jsonReq.getInt("dbId");

			JSONObject data = jsonReq.getJSONObject("data");

			conn = DBConnector.getInstance().getConnectionById(dbId);

			SearchProcess p = new SearchProcess(conn);

			JSONArray array = p.searchDataByCondition(ObjType.valueOf(objType),
					data);

			System.out.println(array);

		} catch (Exception e) {

			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

}
