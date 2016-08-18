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
	public void update() {
		String parameter = "{'command':'UPDATE', 'dbId':42, 'type':'RDTOLLGATE', data:{'pid':100034747, 'type':2, 'names':[{'nameGroupid':11, 'rowId':'7AA43E40216E4B06829B2A89D049A6F9', 'objStatus':'UPDATE'}]}}";
		parameter = "{\"command\":\"UPDATE\",\"type\":\"RDVARIABLESPEED\",\"dbId\":43,\"data\":{\"location\":6,\"pid\":100000075,\"objStatus\":\"UPDATE\",\"vehicle\":2147483648}}";
		TestUtil.run(parameter);
	}

	@Test
	public void searchById() {
		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(43);

			SearchProcess p = new SearchProcess(conn);

			System.out.println(p.searchDataByPid(ObjType.RDVARIABLESPEED, 100000075).Serialize(ObjLevel.FULL));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void insertName() {
		String parameter = "{\"command\":\"UPDATE\",\"type\":\"RDTOLLGATE\",\"dbId\":42,\"data\":{\"names\":[{\"pid\":0,\"nameId\":0,\"nameGroupid\":1,\"langCode\":\"CHI\",\"name\":\"收费站\",\"phonetic\":\"Shou+Fei+Zhan\",\"uFields\":null,\"uDate\":null,\"rowId\":null,\"uRecord\":0,\"objStatus\":\"INSERT\"}],\"rowId\":\"51CDF2FF7CF84FA693BC9E06053D0CDF\",\"pid\":100034754}}";
		TestUtil.run(parameter);
	}
}
