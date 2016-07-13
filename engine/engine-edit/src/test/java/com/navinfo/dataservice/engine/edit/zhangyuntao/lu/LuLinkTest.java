package com.navinfo.dataservice.engine.edit.zhangyuntao.lu;

import java.sql.Connection;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.edit.search.SearchProcess;
import com.navinfo.dataservice.engine.edit.edit.search.rd.utils.RdLinkSearchUtils;

import net.sf.json.JSONObject;

/**
 * @author zhaokk
 *
 */
public class LuLinkTest extends InitApplication {

	@Override
	@Before
	public void init() {
		initContext();
	}

	// 初始化系统参数
	private Connection conn;
	protected Logger log = Logger.getLogger(this.getClass());
	// 创建一条link

	@Test
	public void createLuLinkTest() {
		String parameter = "{\"command\":\"CREATE\",\"dbId\":43,\"data\":{\"eNodePid\":0,\"sNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.48640632629393,40.31965310390446],[116.48340225219727,40.305254482468364]]},\"catchLinks\":[]},\"type\":\"LULINK\"}";
		log.info(parameter);
		Transaction t = new Transaction(parameter);
		;
		try {
			String msg = t.run();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void deleteLuLinkTest() {
		String parameter = "{\"command\":\"DELETE\",\"type\":\"LULINK\",\"dbId\":43,\"projectId\":11,\"objId\":100034527}";
		log.info(parameter);
		System.out.println(parameter + "-------------------");
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
		} catch (Exception e) {	
			e.printStackTrace();
		}

	}

	// 打断一条LINK
	@Test
	public void breakLuLinkTest() {
		String parameter = "{\"command\":\"CREATE\",\"dbId\":43,\"objId\":100034525,\"data\":{\"longitude\":116.48408486591549,\"latitude\":40.30854271853128},\"type\":\"LUNODE\"}";

		log.info(parameter);
		System.out.println(parameter + "-------------------");
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testSearchLuLink() {
		String parameter = "{\"projectId\":11,\"type\":\"LULINK\",\"pid\":100034447}";

		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(43);

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			SearchProcess p = new SearchProcess(conn);

			System.out.println(p.searchDataByPid(ObjType.LULINK, 100034447).Serialize(ObjLevel.FULL));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void tesRepairtLuLink() {
		String parameter = "{\"command\":\"REPAIR\",\"dbId\":43,\"projectId\":11,\"objId\":100034528,\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.62528,39.25022],[116.62528,39.25006],[116.62535838820631,39.25011395094421],[116.62544,39.25017],[116.62528,39.25022]]},\"interLinks\":[],\"interNodes\":[]},\"type\":\"LULINK\"}";

		log.info(parameter);
		System.out.println(parameter + "-------------------");
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testSearchLuNode() {
		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(43);

			SearchProcess p = new SearchProcess(conn);

			System.out.println(p.searchDataByPid(ObjType.LUNODE, 100034469).Serialize(ObjLevel.FULL));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
