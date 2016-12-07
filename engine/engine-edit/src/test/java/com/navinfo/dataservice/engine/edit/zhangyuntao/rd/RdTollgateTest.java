package com.navinfo.dataservice.engine.edit.zhangyuntao.rd;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.search.SearchProcess;
import com.navinfo.dataservice.engine.edit.zhangyuntao.eleceye.TestUtil;
import org.junit.Test;

import java.sql.Connection;

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
		parameter = "{\"command\":\"CREATE\",\"type\":\"RDHGWGLIMIT\",\"dbId\":17,\"data\":{\"direct\":2,\"linkPid\":\"202002837\",\"latitude\":40.054281901702225,\"longitude\":116.43848979735054}}";
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
		parameter = "{\"command\":\"UPDATE\",\"type\":\"RDTOLLGATE\",\"dbId\":17,\"data\":{\"names\":[{\"pid\":0,\"nameId\":0,\"nameGroupid\":1,\"langCode\":\"CHI\",\"name\":\"收费站1111\",\"phonetic\":\"Shou+Fei+Zhan+Yi+Yi+Yi+Yi\",\"rowId\":\"\",\"objStatus\":\"INSERT\"},{\"pid\":0,\"nameId\":0,\"nameGroupid\":2,\"langCode\":\"CHI\",\"name\":\"收费站2222\",\"phonetic\":\"Shou+Fei+Zhan\",\"rowId\":\"\",\"objStatus\":\"INSERT\"}],\"rowId\":\"398CB320F2CA4B098A037EA62EBF4095\",\"pid\":100034814}}";
		TestUtil.run(parameter);
	}

	@Test
	public void searchById() {
		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(17);

			SearchProcess p = new SearchProcess(conn);

			System.out.println(p.searchDataByPid(ObjType.LUFACE, 203000011).Serialize(ObjLevel.BRIEF));

			// RdTollgateSelector selector = new RdTollgateSelector(conn);
			// RdTollgate tollgate = selector.loadById(1055, true);

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
