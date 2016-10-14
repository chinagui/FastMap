package com.navinfo.dataservice.engine.edit.xiaolong.rd;

import java.sql.Connection;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLink;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLinkName;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwNode;
import com.navinfo.dataservice.dao.glm.search.RwLinkSearch;
import com.navinfo.dataservice.dao.glm.selector.rd.rdname.RdNameSelector;
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

			SearchProcess p = new SearchProcess(conn);
			
			RwNode obj = (RwNode) p.searchDataByPid(ObjType.RWNODE, 100007634);

			System.out.println(obj.Serialize(ObjLevel.BRIEF));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testRwRender()
	{
		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(42);

			String parameter = "{\"type\":\"RWLINK\",\"dbId\":42,\"objId\":100007138}";

			RwLinkSearch search = new RwLinkSearch(conn);
			
			search.searchDataByTileWithGap(107951, 49621, 17, 20);

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void testSearchByCondition()
	{
		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(19);

			String parameter = "{\"name\":\"\",\"pageSize\":5,\"pageNum\":0,\"dbId\":17}";

			RdNameSelector selector = new RdNameSelector();

			System.out.println(selector.searchByName("", 5, 0, 17));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test//不跨图幅
	public void testAddRwLink()
	{
		String parameter = "{\"command\":\"CREATE\",\"dbId\":17,\"data\":{\"eNodePid\":0,\"sNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.49462997913359,40.00015291028798],[116.49539172649382,40.00005428525468],[116.49646461009979,40.000157019661266]]},\"catchLinks\":[]},\"type\":\"RWLINK\"}";
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
		String parameter = "{\"command\":\"DELETE\",\"dbId\":52,\"type\":\"RDTOLLGATE\",\"objId\":-1285967295}";
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
		String parameter = "{\"command\":\"REPAIR\",\"dbId\":42,\"objId\":100007141,\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.41928,40.0348],[116.41908824443816,40.034371302620706],[116.41819,40.03448]]},\"interLinks\":[],\"interNodes\":[]},\"type\":\"RWLINK\"}";
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
		String parameter = "{\"command\":\"MOVE\",\"dbId\":17,\"objId\":207000001,\"data\":{\"longitude\":116.25154137611388,\"latitude\":40.54194980053248},\"type\":\"RWNODE\"}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
