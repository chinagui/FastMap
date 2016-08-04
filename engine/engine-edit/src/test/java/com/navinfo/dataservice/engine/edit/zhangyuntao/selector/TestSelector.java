package com.navinfo.dataservice.engine.edit.zhangyuntao.selector;

import java.sql.Connection;

import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdEleceyePair;
import com.navinfo.dataservice.engine.edit.InitApplication;

/**
 * @Title: TestSelector.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年7月26日 下午3:30:25
 * @version: v1.0
 */
public class TestSelector extends InitApplication {

	@Override
	public void init() {
		super.initContext();
	}

	@Test
	public void testLoadById() throws Exception {
		Connection conn = DBConnector.getInstance().getConnectionById(43);
		DemoSelectro selector = new DemoSelectro(conn);
		RdEleceyePair rd = (RdEleceyePair)selector.loadById(100281935, false);
		System.out.println(rd.pid());
	}
	
	@Test
	public void testLoadById1() throws Exception {
		Connection conn = DBConnector.getInstance().getConnectionById(43);
		DemoSelectro selector = new DemoSelectro(conn);
		System.out.println(selector.loadById(100281916, false).rowId());
	}

	@Test
	public void testLoadByParentId() throws Exception {
//		Connection conn = DBConnector.getInstance().getConnectionById(43);
//		DemoSelectro selector = new DemoSelectro(RdElectroniceye.class, conn);
//		for (IRow r : selector.loadRowsByParentId(100281916, false)) {
//			System.out.println(r.rowId());
//		}
	}

	@Test
	public void testLoadByRowId() throws Exception {
		Connection conn = DBConnector.getInstance().getConnectionById(43);
		DemoSelectro selector = new DemoSelectro(conn);
		System.out.println(selector.loadByRowId("56776A0F59C34815A346B27034FDC154", false).rowId());
	}
	
}
