package com.navinfo.dataservice.engine.edit.zhangyuntao.rd;

import org.junit.Test;

import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.zhangyuntao.eleceye.TestUtil;

/**
 * @Title: RdLinkTest.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月3日 上午10:32:29
 * @version: v1.0
 */
public class RdLinkTest extends InitApplication {

	public RdLinkTest() {
	}

	@Override
	public void init() {
		super.initContext();
	}

	@Test
	public void testDelete() {
		String parameter = "{\"command\":\"DELETE\",\"dbId\":42,\"type\":\"RDLINK\",\"objId\":100008436}";
		TestUtil.run(parameter);
	}

	@Test
	public void update() {
		String parameter = "{\"command\":\"BREAK\",\"dbId\":42,\"objId\":100008435,\"data\":{\"longitude\":116.4702206995429,\"latitude\":40.08258242178863},\"type\":\"RDLINK\"}";
		TestUtil.run(parameter);
	}

	@Test
	public void create() {
		String parameter = "{\"command\":\"CREATE\",\"dbId\":42,\"data\":{\"eNodePid\":0,\"sNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.46699,40.08309],[116.46714,40.08249]]},\"catchLinks\":[]},\"type\":\"RDLINK\"}";
		parameter = "{\"command\":\"CREATE\",\"dbId\":42,\"data\":{\"eNodePid\":0,\"sNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.46726,40.08326],[116.46621,40.0831]]},\"catchLinks\":[]},\"type\":\"RDLINK\"}";
		parameter = "{\"command\":\"CREATE\",\"dbId\":42,\"data\":{\"eNodePid\":0,\"sNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.46726,40.08326],[116.46661,40.083]]},\"catchLinks\":[]},\"type\":\"RDLINK\"}";
		TestUtil.run(parameter);
	}
}
