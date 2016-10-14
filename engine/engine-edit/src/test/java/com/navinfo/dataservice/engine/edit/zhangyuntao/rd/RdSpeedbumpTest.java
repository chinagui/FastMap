package com.navinfo.dataservice.engine.edit.zhangyuntao.rd;

import org.junit.Test;

import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.zhangyuntao.eleceye.TestUtil;

/**
 * @Title: Create.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月8日 上午11:32:06
 * @version: v1.0
 */
public class RdSpeedbumpTest extends InitApplication {

	@Override
	public void init() {
		super.initContext();
	}

	@Test
	public void testCreate() {
		String requester = "{\"command\":\"CREATE\",\"type\":\"RDSPEEDBUMP\",\"dbId\":42,\"data\":{\"inLinkPid\":\"49101387\",\"inNodePid\":\"780981\"}}";
		requester = "{\"command\":\"CREATE\",\"type\":\"RDSPEEDLIMIT\",\"dbId\":17,\"data\":{\"direct\":2,\"linkPid\":404077,\"longitude\":116.29543304443355,\"latitude\":40.562313315848485}}";
		requester = "{\"command\":\"CREATE\",\"type\":\"RDSPEEDLIMIT\",\"dbId\":17,\"data\":{\"direct\":2,\"linkPid\":404077,\"longitude\":116.29448725083296,\"latitude\":40.5633894051524}}";
		TestUtil.run(requester);
	}
	
	@Test
	public void testDelete() {
		String requester = "{\"command\":\"DELETE\",\"type\":\"RDSPEEDBUMP\",\"dbId\":42,\"objId\":100034638}";
		TestUtil.run(requester);
	}


}
