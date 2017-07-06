package com.navinfo.dataservice.engine.edit.xiaolong.rd;

import java.sql.Connection;

import com.navinfo.dataservice.engine.edit.zhangyuntao.eleceye.TestUtil;
import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.search.SearchProcess;

public class RwNodeTest extends InitApplication{
	
	@Override
	@Before
	public void init() {
		initContext();
	}

	@Test
    public void create () {
	    String param = "{\"command\":\"CREATE\",\"type\":\"RWLINK\",\"dbId\":13,\"subtaskId\":1,\"data\":{\"sNodePid\":0,\"eNodePid\":0," +
                "\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.55947372317313,39.75031528153438],[116.5593396127224," +
                "39.75046066542736]]},\"catchLinks\":[{\"linkPid\":407000002,\"lon\":116.5593396127224,\"lat\":39.75046066542736}]}}";
        TestUtil.run(param);
    }

    @Test
    public void repair() {
	    String param = "{\"command\":\"REPAIR\",\"type\":\"RWLINK\",\"objId\":507000005,\"dbId\":13,\"subtaskId\":1," +
                "\"data\":{\"type\":\"RWLINK\",\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.55954,39.7502]," +
                "[116.5593396127224,39.75046066542736]]},\"catchInfos\":[{\"nodePid\":507000007,\"catchLinkPid\":407000002," +
                "\"longitude\":116.5593396127224,\"latitude\":39.75046066542736}]}}";
	    TestUtil.run(param);
    }

	@Test
	public void testGetByPid()
	{
		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(42);

			SearchProcess p = new SearchProcess(conn);

			System.out.println(p.searchDataByPid(ObjType.RWNODE, 485593).Serialize(ObjLevel.BRIEF));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
