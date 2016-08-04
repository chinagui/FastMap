package com.navinfo.dataservice.engine.edit.zhangyuntao.selector;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdEleceyePair;

/**
 * @Title: DemoSelectro.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年7月26日 下午3:25:24
 * @version: v1.0
 */
public class DemoSelectro extends TestAbstractSelector<RdEleceyePair> {

	public DemoSelectro(Connection conn) {
		super(conn, RdEleceyePair.class);
	}

	public DemoSelectro(Class<RdEleceyePair> clazz) {
		super(clazz);
	}
}
