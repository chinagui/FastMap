/**
 * 
 */
package com.navinfo.dataservice.engine.edit.xiaolong.rd;

import java.sql.Connection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.util.ResponseUtils;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.search.RdSameNodeSearch;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.search.SearchProcess;

import net.sf.json.JSONObject;

/** 
* @ClassName: RdSameNodeTest 
* @author Zhang Xiaolong
* @date 2016年8月8日 下午5:26:43 
* @Description: TODO
*/
public class RdSameNodeTest extends InitApplication {
	@Override
	@Before
	public void init() {
		initContext();
	}
	
	@Test
	public void testGetByPid() {
		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(42);

			SearchProcess p = new SearchProcess(conn);

			System.out.println(p.searchDataByPid(ObjType.RDSAMENODE, 47464364).Serialize(ObjLevel.BRIEF));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testRdSameNodeRRender()
	{
		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(42);

			RdSameNodeSearch search = new RdSameNodeSearch(conn);
			
			List<SearchSnapshot> data = search.searchDataByTileWithGap(863505, 396921, 20, 80);
			
			System.out.println("data:"+ResponseUtils.assembleRegularResult(data));

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void testAddRdSameNode() {
		String parameter = "{\"command\":\"CREATE\",\"dbId\":17,\"type\":\"RDSAMENODE\",\"data\":{\"nodes\":[{\"nodePid\":469282,\"type\":\"RDNODE\",\"isMain\":1},{\"nodePid\":203000032,\"type\":\"ADNODE\",\"isMain\":0}]}}";
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
