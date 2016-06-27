package com.navinfo.dataservice.engine.edit.xiaolong.rd;

import java.sql.Connection;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.edit.search.SearchProcess;

public class RwNodeTest extends InitApplication{
	
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

			System.out.println(p.searchDataByPid(ObjType.RWNODE, 485593).Serialize(ObjLevel.BRIEF));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
