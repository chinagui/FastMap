package com.navinfo.dataservice.engine.edit.xiaolong.rd;

import java.sql.Connection;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchSelector;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.Transaction;

import net.sf.json.JSONObject;

public class RdBranchTest extends InitApplication {

	@Override
	@Before
	public void init() {
		initContext();
	}

	@Test
	public void testAdd3dBranch() {
		String parameter = "{\"type\":\"RDBRANCH\",\"command\":\"UPDATE\",\"dbId\":259,\"data\":{\"details\":[{\"names\":[{\"pid\":0,\"seqNum\":1,\"nameGroupid\":1,\"nameClass\":0,\"langCode\":\"CHI\",\"codeType\":0,\"name\":\"西安\",\"phonetic\":\"Xi+An\",\"srcFlag\":0,\"voiceFile\":\"Xi'an\",\"objStatus\":\"INSERT\"}],\"rowId\":\"C7BECE60FC5349CBADE5EC638024F738\",\"pid\":509000008}],\"pid\":507000012}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(t.getLogs());
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testGetByPid() {
		Connection conn;
		try {

			String parameter = "{\"dbId\":42,\"type\":\"RDBRANCH\",\"detailId\":\"97660\",\"rowId\":\"\",\"branchType\":3}";

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
	
	@Test
	public void testDeleteBranch()
	{
		String parameter = "{\"command\":\"DELETE\",\"dbId\":42,\"type\":\"RDBRANCH\",\"detailId\":100005938,\"rowId\":\"\",\"branchType\":8}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(t.getLogs());
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testUpdateBranch()
	{
		String parameter = "{\"dbId\":25,\"type\":\"RDBRANCH\",\"detailId\":\"111401467052\",\"rowId\":\"\",\"branchType\":9}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(t.getLogs());
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
