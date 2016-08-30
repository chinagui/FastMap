package com.navinfo.dataservice.engine.edit.zhangyuntao.lc;

import org.junit.Test;

import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.zhangyuntao.eleceye.TestUtil;

/**
 * @Title: LcFaceTest.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月9日 下午5:00:10
 * @version: v1.0
 */
public class LcFaceTest extends InitApplication {

	public LcFaceTest() {
	}

	@Override
	public void init() {
		super.initContext();
	}

	@Test
	public void createTest() {
		String requester = "{\"command\":\"CREATE\",\"type\":\"LCFACE\",\"dbId\":43,\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.34715676307678,40.05199350679047],[116.34680271148682,40.05118867667575],[116.34814381599425,40.05112297583864],[116.34851932525636,40.05154181759012],[116.34810090065002,40.05223988145623],[116.34760737419128,40.05228915628253],[116.34715676307678,40.05199350679047]]}}}";
		TestUtil.run(requester);
	}

	@Test
	public void update() {
		String requester = "{\"command\":\"UPDATE\",\"dbId\":43,\"type\":\"LCFACE\",\"objId\":100042655,\"data\":{\"kind\":17,\"pid\":100042655,\"objStatus\":\"UPDATE\",\"form\":10,\"displayClass\":7,\"scale\":2,\"detailFlag\":3}}";
		requester = "{\"command\":\"UPDATE\",\"dbId\":43,\"type\":\"LCFACE\",\"objId\":238944,\"data\":{\"names\":[{\"pid\":0,\"nameGroupid\":191926,\"langCode\":\"CHI\",\"name\":\"1212\",\"phonetic\":\"12121212\",\"srcFlag\":0,\"objStatus\":\"INSERT\"}],\"pid\":238944}}";
		TestUtil.run(requester);
	}

}
