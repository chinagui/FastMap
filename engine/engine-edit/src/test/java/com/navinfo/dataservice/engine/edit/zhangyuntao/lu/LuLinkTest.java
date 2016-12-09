package com.navinfo.dataservice.engine.edit.zhangyuntao.lu;

import java.sql.Connection;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.search.SearchProcess;
import com.navinfo.dataservice.engine.edit.zhangyuntao.eleceye.TestUtil;

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
//		String parameter = "{\"command\":\"CREATE\",\"dbId\":43,\"data\":{\"eNodePid\":0,\"sNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.23831272125244,40.91574217557029],[116.23770117759705,40.913731485583625]]},\"catchLinks\":[]},\"type\":\"LULINK\"}";
//		String parameter = "{\"command\":\"CREATE\",\"dbId\":43,\"data\":{\"eNodePid\":0,\"sNodePid\":100034583,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.23769044876099,40.91373148558362],[116.24097347259521,40.914599008521996]]},\"catchLinks\":[{\"nodePid\":100034583,\"lon\":116.23769044876099,\"lat\":40.91373148558362}]},\"type\":\"LULINK\"}";
		String parameter = "{\"command\":\"CREATE\",\"dbId\":43,\"data\":{\"eNodePid\":100034582,\"sNodePid\":100034584,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.24096274375916,40.9146071161593],[116.23830199241638,40.91574217557027]]},\"catchLinks\":[{\"nodePid\":100034584,\"lon\":116.24096274375916,\"lat\":40.9146071161593},{\"nodePid\":100034582,\"lon\":116.23830199241638,\"lat\":40.91574217557027}]},\"type\":\"LULINK\"}";
		parameter = "{\"command\":\"UPDATE\",\"dbId\":43,\"type\":\"LULINK\",\"objId\":100034802,\"data\":{\"linkKinds\":[{\"linkPid\":100034802,\"kind\":1,\"form\":1,\"rowId\":\"1B2894A566D341BEABD33ABD7A61A964\",\"objStatus\":\"INSERT\"},{\"kind\":4,\"rowId\":\"1B2894A566D341BEABD33ABD7A61A964\",\"objStatus\":\"UPDATE\",\"linkPid\":100034802}],\"rowId\":\"B7CC708D9C4B487097CE4901C315E4EF\",\"pid\":100034802}}";
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
	public void update(){
		String requester = "{\"command\":\"UPDATE\",\"dbId\":42,\"type\":\"LULINK\",\"objId\":100034774,\"data\":{\"linkKinds\":[{\"kind\":3,\"rowId\":\"62EE6A6C0F7E47DDA278FAD24FFD071D\",\"objStatus\":\"UPDATE\"}],\"rowId\":\"150CA45F49BD414B8AADBA44B97398FC\",\"pid\":100034774}}";
		TestUtil.run(requester);
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
		String parameter = "{\"command\":\"BREAK\",\"dbId\":17,\"objId\":202000020,\"data\":{\"longitude\":116.46865242657762,\"latitude\":39.87563511890222},\"type\":\"LULINK\"}";
		TestUtil.run(parameter);
	}

	@Test
	public void tesRepairtLuLink() {
		String requester = "{\"command\":\"REPAIR\",\"dbId\":17,\"objId\":320000009,\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[115.625,39.41894],[115.62467,39.41878],[115.62544405460356,39.41844578116937],[115.62462,39.41816],[115.6246,39.4178],[115.62463,39.4175],[115.62467,39.41736],[115.62469,39.41728]]},\"interLinks\":[],\"interNodes\":[]},\"type\":\"LULINK\"}";
		TestUtil.run(requester);
	}

	@Test
	public void testSearchLuLink() {
		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(43);

			SearchProcess p = new SearchProcess(conn);

			System.out.println(p.searchDataByPid(ObjType.LULINK, 100034528).Serialize(ObjLevel.FULL));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void move(){
		String parameter = "{\"command\":\"MOVE\",\"dbId\":17,\"objId\":202000040,\"data\":{\"longitude\":116.62445425987244,\"latitude\":40.398529177904926},\"type\":\"LUNODE\"}";
		TestUtil.run(parameter);
	}
	
}
