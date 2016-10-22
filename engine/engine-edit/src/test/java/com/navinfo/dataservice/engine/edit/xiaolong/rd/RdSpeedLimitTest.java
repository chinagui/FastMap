package com.navinfo.dataservice.engine.edit.xiaolong.rd;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.util.ResponseUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.search.SearchProcess;

import net.sf.json.JSONObject;

public class RdSpeedLimitTest extends InitApplication{
	
	@Override
	@Before
	public void init() {
		initContext();
	}
	
	@Test
	public void testSpeed() throws Exception {

		Connection conn = DBConnector.getInstance().getConnectionById(11);

		Connection conn2 = DBConnector.getInstance().getConnectionById(11);
		try {

			FileWriter fw = null;
			BufferedWriter writer = null;

			fw = new FileWriter(new File("c:\\add.txt"));

			writer = new BufferedWriter(fw);

			conn.setAutoCommit(false);
			String sql = "SELECT DISTINCT pid FROM rd_speedlimit";

			PreparedStatement stms = conn.prepareStatement(sql);

			ResultSet result = stms.executeQuery();

			while (result.next()) {
				int pid = result.getInt("pid");

				String uuid = UuidUtils.genUuid();
				writer.write("update rd_speedlimit set row_id ='" + uuid + "' where pid = " + pid);
				System.out.println(pid);
				writer.newLine();// 换行
			}
			writer.flush();
		} catch (SQLException e) {
			conn.rollback();
			conn2.rollback();
			e.printStackTrace();
		}
	}
	
	@Test
	public void testSearch()
	{
		String parameter = "{\"projectId\":11,\"type\":\"RDSPEEDLIMIT\",\"pid\":20177}";
		JSONObject jsonReq = JSONObject.fromObject(parameter);

		String objType = jsonReq.getString("type");

		int projectId = jsonReq.getInt("projectId");

		int pid = jsonReq.getInt("pid");

		SearchProcess p;
		try {
			p = new SearchProcess(
					DBConnector.getInstance().getConnectionById(projectId));
			IObj obj = p.searchDataByPid(ObjType.valueOf(objType), pid);

			System.out.println(ResponseUtils.assembleRegularResult(obj.Serialize(ObjLevel.FULL)));
		} catch (Exception e) {
			e.printStackTrace();
		}

		
	}
	
	@Test
	public void testUpdate()
	{
		String parameter = "{\"type\":\"RDBRANCH\",\"command\":\"UPDATE\",\"dbId\":17,\"data\":{\"relationshipType\":1,\"pid\":5645128,\"objStatus\":\"UPDATE\"}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();

			String log = t.getLogs();

			JSONObject json = new JSONObject();

			json.put("result", msg);

			json.put("log", log);

			json.put("check", t.getCheckLog());

			json.put("pid", t.getPid());

			System.out.println(json.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
