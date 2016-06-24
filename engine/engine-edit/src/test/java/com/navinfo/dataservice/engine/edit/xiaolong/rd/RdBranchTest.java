package com.navinfo.dataservice.engine.edit.xiaolong.rd;

import java.sql.Connection;

import org.junit.Before;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchSelector;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.edit.search.SearchProcess;

import net.sf.json.JSONObject;

public class RdBranchTest extends InitApplication {

	@Override
	@Before
	public void init() {
		initContext();
	}

	@Test
	public void testAdd3dBranch() {
		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDBRANCH\",\"projectId\":11,\"data\":{\"branchType\":3,\"inLinkPid\":88026344,\"nodePid\":74186157,\"outLinkPid\":88026344}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testGetByPid() {
		Connection conn;
		try {

			String parameter = "{\"dbId\":42,\"type\":\"RDBRANCH\",\"detailId\":100000650,\"branchType\":5,\"rowId\":\"50ca9e99f5ac41d8bb58525977af70b0\"}";

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			int dbId = jsonReq.getInt("dbId");

			conn = DBConnector.getInstance().getConnectionById(dbId);

			if (jsonReq.containsKey("detailId")) {
				int detailId = jsonReq.getInt("detailId");
				int branchType = jsonReq.getInt("branchType");
				String rowId = jsonReq.getString("rowId");
				RdBranchSelector selector = new RdBranchSelector(conn);
				IRow row = selector.loadByDetailId(detailId, branchType, rowId, false);

				if (row != null) {

					System.out.println(row.Serialize(ObjLevel.FULL));
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
