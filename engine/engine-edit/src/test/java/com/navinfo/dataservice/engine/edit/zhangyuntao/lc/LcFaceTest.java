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
public class LcFaceTest extends InitApplication{

	public LcFaceTest() {
	}

	@Override
	public void init() {
		super.initContext();
	}
	
	@Test
	public void createTest(){
		String requester = "{\"command\":\"CREATE\",\"type\":\"ADFACE\",\"dbId\":43,\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.39921307563782,40.062590918798804],[116.3987785577774,40.062020236157714],[116.40007138252257,40.06193401750131],[116.40042006969452,40.06269766466143],[116.39921307563782,40.062590918798804]]}}}";
		TestUtil.run(requester);
	}

}
