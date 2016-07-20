package com.navinfo.dataservice.engine.edit.xiaolong.rd;

import java.sql.Connection;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.search.SearchProcess;

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

			String parameter = "{\"type\":\"RWLINK\",\"dbId\":42,\"objId\":100006019}";

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			SearchProcess p = new SearchProcess(conn);

			System.out.println(p.searchDataByPid(ObjType.RWLINK, 100006019).Serialize(ObjLevel.BRIEF));

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

			String parameter = "{\"dbId\":42,\"type\":\"RWLINK\",\"nodePid\":100005919}";

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
		String parameter = "{\"command\":\"CREATE\",\"dbId\":42,\"data\":{\"eNodePid\":0,\"sNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.49462997913359,40.00015291028798],[116.49539172649382,40.00005428525468],[116.49646461009979,40.000157019661266]]},\"catchLinks\":[]},\"type\":\"RWLINK\"}";
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
		String parameter = "{\"command\":\"UPDATE\",\"dbId\":42,\"type\":\"RWLINK\",\"objId\":100005906,\"data\":{\"names\":[{\"linkPid\":100005906,\"nameGroupid\":0,\"name\":\"北京西路下拉槽\",\"objStatus\":\"INSERT\"}],\"pid\":100005906}}";
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
		String parameter = "{\"command\":\"DELETE\",\"dbId\":25,\"type\":\"RWLINK\",\"objId\":100007122}";
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
		String parameter = "{\"command\":\"REPAIR\",\"dbId\":42,\"objId\":100006797,\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.49725,40.03237],[116.49772,40.03206],[116.50189876556396,40.03059657720054],[116.49799,40.03058],[116.49644,40.03009]]},\"interLinks\":[],\"interNodes\":[]},\"type\":\"RWLINK\"}";
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
	
	@Test
	public void testCreateRwLinkByLineBreak()
	{
		String parameter = "{\"command\":\"CREATE\",\"dbId\":42,\"data\":{\"eNodePid\":100006280,\"sNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.4770722389221,40.0339153893155],[116.47953987121582,40.03479435458311]]},\"catchLinks\":[{\"linkPid\":100006207,\"lon\":116.47953987121582,\"lat\":40.03479435458311}]},\"type\":\"RWLINK\"}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testMoveRwPoint()
	{
		String parameter = "{\"command\":\"MOVE\",\"dbId\":25,\"objId\":100007115,\"data\":{\"longitude\":116.47797346115112,\"latitude\":40.02029408726943},\"type\":\"RWNODE\"}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
