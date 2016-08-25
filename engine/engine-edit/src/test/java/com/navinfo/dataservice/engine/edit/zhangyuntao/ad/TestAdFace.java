package com.navinfo.dataservice.engine.edit.zhangyuntao.ad;

import org.junit.Test;

import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.zhangyuntao.eleceye.TestUtil;

/**
 * @Title: AdFace.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月18日 下午4:02:40
 * @version: v1.0
 */
public class TestAdFace extends InitApplication {

	public TestAdFace() {
	}

	@Test
	public void create() {
		String paramter = "{\"command\":\"CREATE\",\"type\":\"ADFACE\",\"dbId\":42,\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.47447049617767,40.01370427880928],[116.47439539432526,40.013544044868844],[116.47468239068984,40.01353582773357],[116.47466629743576,40.01368989885563],[116.47447049617767,40.01370427880928]]}}}";
		TestUtil.run(paramter);
	}

	@Override
	public void init() {
		super.initContext();
	}
}
