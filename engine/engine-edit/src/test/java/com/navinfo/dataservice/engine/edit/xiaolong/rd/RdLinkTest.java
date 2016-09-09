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
			conn = DBConnector.getInstance().getConnectionById(42);

			String parameter = "{\"type\":\"RWLINK\",\"dbId\":42,\"objId\":100007138}";

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			SearchProcess p = new SearchProcess(conn);

			System.out.println(p.searchDataByPid(ObjType.RDLINK, 13677569).Serialize(ObjLevel.BRIEF));

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
		String parameter = "{\"command\":\"DELETE\",\"dbId\":42,\"type\":\"RDLINK\",\"objId\":100008767}";
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
		String parameter = "{\"command\":\"CREATE\",\"dbId\":2003,\"data\":{\"eNodePid\":0,\"sNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.37338876724243,40.206369330693555],[116.3740525057207,40.206167667477494],[116.37430443388934,40.205892923340635],[116.37451849556325,40.20561731965343],[116.37602163457522,40.2053759162552],[116.37642502784728,40.20532870089845],[116.37734538052707,40.205776221511954],[116.37713055558888,40.2059764486889],[116.37757120715328,40.20612280452726],[116.37797573653047,40.20624524558387],[116.37871026992798,40.20621364693729],[116.37861265622945,40.206061352241285],[116.3787337471457,40.20597727686031],[116.37928962707518,40.20576298141666],[116.37872691321284,40.20563688942185],[116.37810957022839,40.20544657697806],[116.37780904769899,40.205189402784406],[116.3767683506012,40.20551716259713],[116.37690587124291,40.20604000343057],[116.37626133998123,40.20621282402136],[116.37589931488036,40.20624642249464]]},\"catchLinks\":[{\"linkPid\":100013856,\"lon\":116.3740525057207,\"lat\":40.206167667477494},{\"linkPid\":100013864,\"lon\":116.37430443388934,\"lat\":40.205892923340635},{\"linkPid\":100013877,\"lon\":116.37451849556325,\"lat\":40.20561731965343},{\"linkPid\":100013876,\"lon\":116.37602163457522,\"lat\":40.2053759162552},{\"linkPid\":100013870,\"lon\":116.37734538052707,\"lat\":40.205776221511954},{\"linkPid\":100013860,\"lon\":116.37713055558888,\"lat\":40.2059764486889},{\"linkPid\":100013859,\"lon\":116.37757120715328,\"lat\":40.20612280452726},{\"linkPid\":100013874,\"lon\":116.37797573653047,\"lat\":40.20624524558387},{\"linkPid\":100013865,\"lon\":116.37861265622945,\"lat\":40.206061352241285},{\"linkPid\":100013865,\"lon\":116.3787337471457,\"lat\":40.20597727686031},{\"linkPid\":100013861,\"lon\":116.37872691321284,\"lat\":40.20563688942185},{\"linkPid\":100013875,\"lon\":116.37810957022839,\"lat\":40.20544657697806},{\"linkPid\":100013860,\"lon\":116.37690587124291,\"lat\":40.20604000343057},{\"linkPid\":100013868,\"lon\":116.37626133998123,\"lat\":40.20621282402136}]},\"type\":\"RDLINK\"}";
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
		String parameter = "{\"command\":\"REPAIR\",\"dbId\":42,\"objId\":100009905,\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.41885,40.03103],[116.41926,40.03054],[116.41975,40.03115],[116.42048,40.02972],[116.42100870609283,40.02980381724441],[116.42156,40.02957],[116.42024,40.02951]]},\"interLinks\":[],\"interNodes\":[]},\"type\":\"RDLINK\"}";
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
		String parameter = "{\"command\":\"BREAK\",\"dbId\":42,\"objId\":100009852,\"data\":{\"longitude\":116.3935117845917,\"latitude\":40.01393783844508},\"type\":\"RDLINK\"}";
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
		String parameter = "{\"dbId\":42,\"pageNum\":1,\"pageSize\":5,\"data\":{\"linkPid\":\"11111\"},\"type\":\"RDLINK\"}";

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
		String parameter = "{\"command\":\"ONLINEBATCH\",\"type\":\"FACE\",\"dbId\":17,\"pid\":210000001,\"ruleId\":\"BATCHDELZONEID\"}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
