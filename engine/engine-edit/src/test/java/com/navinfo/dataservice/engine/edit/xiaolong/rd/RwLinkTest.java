package com.navinfo.dataservice.engine.edit.xiaolong.rd;

import java.sql.Connection;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.edit.search.SearchProcess;

import net.sf.json.JSONObject;

public class RwLinkTest extends InitApplication {

	@Override
	@Before
	public void init() {
		initContext();
	}

	@Test
	public void testGetByPid()
	{
		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(42);

			String parameter = "{\"type\":\"RWLINK\",\"dbId\":42,\"objId\":33}";

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			SearchProcess p = new SearchProcess(conn);

			System.out.println(p.searchDataByCondition(ObjType.RWLINK, jsonReq));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testSearchByCondition()
	{
		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(42);

			String parameter = "{\"dbId\":42,\"type\":\"RWLINK\",\"nodePid\":100005922}";

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			SearchProcess p = new SearchProcess(conn);

			System.out.println(p.searchDataByCondition(ObjType.RWLINK, jsonReq));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test//不跨图幅
	public void testAddRwLink()
	{
		String parameter = "{\"command\":\"CREATE\",\"dbId\":42,\"data\":{\"eNodePid\":0,\"sNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.46778374910355,40.01895481363257],[116.46753162145615,40.018648748942766]]},\"catchLinks\":[]},\"type\":\"RWLINK\"}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test//跨图幅
	public void testAddRwLinkWith2Mesh()
	{
		String parameter = "{\"command\":\"CREATE\",\"dbId\":42,\"data\":{\"eNodePid\":0,\"sNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.49946063756941,40.000736438820205],[116.50044769048691,40.00065836134782]]},\"catchLinks\":[]},\"type\":\"RWLINK\"}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test//更新rw_link_name
	public void testUpdateRwLink()
	{
		String parameter = "{\"command\":\"UPDATE\",\"type\":\"RWLINK\",\"dbId\":42,\"data\":{\"names\":[{\"linkPid\":100005904,\"rowId\":\"\",\"nameGroupid\":40555592,\"objStatus\":\"INSERT\"}],\"objId\":100005904}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test//删除rw_link_name 自测问题：rw_node的点在图幅上的时候删除有问题，图幅计算的老问题
	public void testDeleteRwLink()
	{
		String parameter = "{\"command\":\"DELETE\",\"type\":\"RWLINK\",\"dbId\":42,\"objId\":100005909}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test //测试线的修行
	public void testRepairRwLink()
	{
		String parameter = "{\"command\":\"REPAIR\",\"dbId\":42,\"objId\":100005906,\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.46848,40.01558],[116.46901488304137,40.0154709357768],[116.46973,40.01552]]},\"interLinks\":[],\"interNodes\":[]},\"type\":\"RWLINK\"}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testBreakRwLink()
	{
		String parameter = "{\"command\":\"BREAK\",\"dbId\":42,\"objId\":100005910,\"data\":{\"longitude\":116.47772530228006,\"latitude\":40.01352131144351},\"type\":\"RWLINK\"}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
