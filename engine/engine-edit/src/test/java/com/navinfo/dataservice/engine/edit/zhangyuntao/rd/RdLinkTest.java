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
public class RdLinkTest extends InitApplication{

	public RdLinkTest() {
	}

	@Override
	public void init() {
		super.initContext();
	}

	@Test
	public void testDelete(){
		String parameter = "{\"command\":\"DELETE\",\"dbId\":42,\"type\":\"RDLINK\",\"objId\":100008436}";
		TestUtil.run(parameter);
	}
}
