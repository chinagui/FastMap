package com.navinfo.dataservice.engine.edit.xiaolong.rd;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.SelectorUtils;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.search.SearchProcess;

import net.sf.json.JSONObject;

public class RdLinkTest extends InitApplication {

	@Override
	@Before
	public void init() {
		initContext();
	}

	public RdLinkTest() throws Exception {
	}

	@Test
	public void testGetByPid() {
		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(19);

			String parameter = "{\"type\":\"RWLINK\",\"dbId\":42,\"objId\":100007138}";

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			SearchProcess p = new SearchProcess(conn);

			System.out.println(p.searchDataByPid(ObjType.RDLANECONNEXITY, 32060).Serialize(ObjLevel.BRIEF));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testLoadByIds() throws SQLException {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getConnectionById(42);

			AbstractSelector selector = new AbstractSelector(IxPoi.class, conn);
			
			List<Integer> pidList = new ArrayList<>();
			
			pidList.add(1152117237);
			
			pidList.add(472);
			
			List<IRow> list = selector.loadByIds(pidList, false,false);
			
			System.out.println(list.get(0).Serialize(null));
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DbUtils.close(conn);
		}

	}

	@Test
	public void testDelete() {
		String parameter = "{\"command\":\"CREATE\",\"dbId\":17,\"data\":{\"eNodePid\":0,\"sNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.64945781230925,40.298704548344496],[116.64975178850665,40.298351028340484],[116.65014982223511,40.298205401778034]]},\"catchLinks\":[{\"linkPid\":308002476,\"lon\":116.64975178850665,\"lat\":40.298351028340484}]},\"type\":\"RDLINK\"}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testAddRdLink() {
		String parameter = "{\"command\":\"CREATE\",\"dbId\":17,\"objId\":303000015,\"data\":{\"longitude\":116.25269451011694,\"latitude\":40.08360598240627},\"type\":\"RWNODE\"}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testRepairLink() {
		String parameter = "{\"command\":\"UPDATE\",\"type\":\"RDSLOPE\",\"dbId\":17,\"data\":{\"objStatus\":\"UPDATE\",\"pid\":203000000,\"linkPids\":[301000097,304000077,205000088]}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testBreakRdLink() {
		String parameter = "{\"command\":\"CREATE\",\"dbId\":17,\"type\":\"RDSAMENODE\",\"data\":{\"nodes\":[{\"nodePid\":\"210001823\",\"type\":\"RDNODE\",\"isMain\":1},{\"nodePid\":\"309000026\",\"type\":\"LUNODE\",\"isMain\":0}]}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testGetByElementCondition()
	{
		String parameter = "{\"dbId\":17,\"pageNum\":1,\"pageSize\":5,\"data\":{\"linkPid\":\"323024\"},\"type\":\"RDLINK\"}";

		Connection conn = null;

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String tableName = jsonReq.getString("type");
			int pageNum = jsonReq.getInt("pageNum");
			int pageSize = jsonReq.getInt("pageSize");
			int dbId = jsonReq.getInt("dbId");
			JSONObject data = jsonReq.getJSONObject("data");
			conn = DBConnector.getInstance().getConnectionById(dbId);
			SelectorUtils selectorUtils = new SelectorUtils(conn);
			JSONObject jsonObject = selectorUtils.loadByElementCondition(data,tableName, pageSize, pageNum, false);
			System.out.println(jsonObject.toString());

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
	
	@Test
	public void testBatch()
	{
		String parameter = "{\"command\":\"CREATE\",\"type\":\"IXPOI\",\"dbId\":17,\"data\":{\"longitude\":116.20599746704102,\"latitude\":40.580030566358204,\"x_guide\":116.20599746704102,\"y_guide\":40.580030566358204,\"linkPid\":0,\"name\":\"asdas\",\"kindCode\":\"210105\"}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
