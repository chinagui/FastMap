package com.navinfo.dataservice.engine.edit.zhangyuntao.rd;

import java.sql.Connection;

import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.search.SearchProcess;
import com.navinfo.dataservice.engine.edit.zhangyuntao.eleceye.TestUtil;

/**
 * @Title: RdTollgateTest.java
 * @Description: 收费站测试类
 * @author zhangyt
 * @date: 2016年8月11日 上午9:33:24
 * @version: v1.0
 */
public class RdTollgateTest extends InitApplication {

	public RdTollgateTest() {
	}

	@Override
	public void init() {
		super.initContext();
	}

	@Test
	public void insert() {
		String parameter = "{'command':'CREATE',` 'dbId':42, 'type':'RDTOLLGATE', 'data':{'inLinkPid':123, 'nodePid':213, 'outLinkPid':321}}";
		TestUtil.run(parameter);
	}

	@Test
	public void delete() {
		String parameter = "{'command':'DELETE', 'dbId':42, 'type':'RDTOLLGATE', 'objId':100034747}";
		TestUtil.run(parameter);
	}
	
	@Test
	public void update(){
		String parameter = "{'command':'UPDATE', 'dbId':42, 'type':'RDTOLLGATE', data:{'pid':100034747, 'type':2, 'names':[{'nameGroupid':11, 'rowId':'7AA43E40216E4B06829B2A89D049A6F9', 'objStatus':'UPDATE'}]}}";
		TestUtil.run(parameter);
	}
	
	@Test
	public void searchById(){
		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(42);

			SearchProcess p = new SearchProcess(conn);

			System.out.println(p.searchDataByPid(ObjType.RDTOLLGATE, 100034747).Serialize(ObjLevel.FULL));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
