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
//		String requester = "{\"command\":\"CREATE\",\"type\":\"LCFACE\",\"dbId\":17,\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.25478148460388,40.00035837864984],[116.25629425048827,40.00030906629938],[116.25563979148865,39.99930637411606],[116.25563979148865,39.99930637411606],[116.25478148460388,40.00035837864984]]}}}";
		String requester = "{\"command\":\"CREATE\",\"type\":\"LCFACE\",\"dbId\":19,\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.34263992309572,40.0003830348117],[116.34406685829163,40.00033372247905],[116.34321928024292,39.99948718854876],[116.34263992309572,40.0003830348117]]}}}";
		TestUtil.run(requester);
	}

	@Test
	public void update() {
		String requester = "{\"command\":\"UPDATE\",\"dbId\":43,\"type\":\"LCFACE\",\"objId\":100042655,\"data\":{\"kind\":17,\"pid\":100042655,\"objStatus\":\"UPDATE\",\"form\":10,\"displayClass\":7,\"scale\":2,\"detailFlag\":3}}";
		requester = "{\"command\":\"UPDATE\",\"dbId\":43,\"type\":\"LCFACE\",\"objId\":238944,\"data\":{\"names\":[{\"pid\":0,\"nameGroupid\":191926,\"langCode\":\"CHI\",\"name\":\"1212\",\"phonetic\":\"12121212\",\"srcFlag\":0,\"objStatus\":\"INSERT\"}],\"pid\":238944}}";
		TestUtil.run(requester);
	}

	@Test
	public void move(){
		String paramaeter = "{\"command\":\"MOVE\",\"dbId\":17,\"objId\":209000016,\"data\":{\"longitude\":116.37395,\"latitude\":40.5906},\"type\":\"LUNODE\"}";
		TestUtil.run(paramaeter);
	}

}
