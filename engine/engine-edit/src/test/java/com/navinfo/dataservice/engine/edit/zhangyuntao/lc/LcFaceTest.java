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
		String requester = "{\"command\":\"UPDATE\",\"dbId\":17,\"type\":\"LCFACE\",\"objId\":207000003,\"data\":{\"names\":[{\"pid\":303000000,\"nameGroupid\":1,\"langCode\":\"CHI\",\"name\":\"土地覆盖面名\",\"phonetic\":\"Tu+Di+Fu+Gai+Mian+Ming\",\"srcFlag\":0,\"rowId\":\"7D027C09644745A0A0718B609A9F40CB\",\"objStatus\":\"INSERT\"},{\"pid\":303000001,\"nameGroupid\":1,\"langCode\":\"CHI\",\"name\":\"土地覆盖面名\",\"phonetic\":\"Tu+Di+Fu+Gai+Mian+Ming\",\"srcFlag\":0,\"rowId\":\"41ABFB8EE38647749A5C9EA62A729205\",\"objStatus\":\"INSERT\"},{\"pid\":303000001,\"rowId\":\"41ABFB8EE38647749A5C9EA62A729205\",\"objStatus\":\"UPDATE\",\"nameGroupid\":3},{\"pid\":303000000,\"rowId\":\"7D027C09644745A0A0718B609A9F40CB\",\"objStatus\":\"UPDATE\",\"nameGroupid\":2}],\"pid\":207000003}}";
		TestUtil.run(requester);
	}

	@Test
	public void update() {
		String requester = "{\"command\":\"UPDATE\",\"dbId\":43,\"type\":\"LCFACE\",\"objId\":100042655,\"data\":{\"kind\":17,\"pid\":100042655,\"objStatus\":\"UPDATE\",\"form\":10,\"displayClass\":7,\"scale\":2,\"detailFlag\":3}}";
		requester = "{\"command\":\"UPDATE\",\"dbId\":43,\"type\":\"LCFACE\",\"objId\":238944,\"data\":{\"names\":[{\"pid\":0,\"nameGroupid\":191926,\"langCode\":\"CHI\",\"name\":\"1212\",\"phonetic\":\"12121212\",\"srcFlag\":0,\"objStatus\":\"INSERT\"}],\"pid\":238944}}";
		TestUtil.run(requester);
	}

}
