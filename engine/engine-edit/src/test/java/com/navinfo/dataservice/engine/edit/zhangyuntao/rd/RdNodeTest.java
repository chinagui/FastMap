package com.navinfo.dataservice.engine.edit.zhangyuntao.rd;

import org.junit.Test;

import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.zhangyuntao.eleceye.TestUtil;

import static com.navinfo.dataservice.dao.glm.iface.ObjType.RDNODE;

/**
 * @Title: RdNodeTest.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月3日 上午9:43:51
 * @version: v1.0
 */
public class RdNodeTest extends InitApplication {

	public RdNodeTest() {
	}

	@Override
	public void init() {
		super.initContext();
	}
	
	@Test
	public void create(){
		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDINTER\",\"data\":{\"links\":[],\"nodes\":[51316756]}," +
                "\"dbId\":13,\"subtaskId\":61}";
		TestUtil.run(parameter);
	}

	@Test
	public void testBreak() {
		String parameter = "{\"command\":\"BREAK\",\"dbId\":42,\"objId\":100008261,\"data\":{\"longitude\":116.50592938748717,\"latitude\":40.14143148683055},\"type\":\"RDLINK\"}";
		parameter = "{\"command\":\"BREAK\",\"dbId\":42,\"objId\":100008402,\"data\":{\"longitude\":116.47662178273345,\"latitude\":40.08216305524021},\"type\":\"RDLINK\"}";
		TestUtil.run(parameter);
	}
	
	@Test
	public void testDelete(){
		String parameter = "{\"command\":\"DELETE\",\"type\":\"RDINTER\",\"objId\":504000013,\"dbId\":13,\"subtaskId\":61}";
		TestUtil.run(parameter);
	}
	
	@Test
	public void testMove(){
		String parameter = "{\"command\":\"MOVE\",\"type\":\"RDNODE\",\"objId\":409000589,\"data\":{\"longitude\":115.50000116229057," +
                "\"latitude\":36.06310145346231},\"dbId\":13,\"subtaskId\":61}";
		TestUtil.run(parameter);
	}

	@Test
	public void update(){
	    String parameter = "{\"command\":\"UPDATE\",\"type\":\"RDNODE\",\"objId\":409000605,\"data\":{\"srcFlag\":2,\"rowId\":\"54B4774360F545458B7B23E0788B414F\",\"pid\":409000605,\"objStatus\":\"UPDATE\"},\"dbId\":13,\"subtaskId\":61}";
	    TestUtil.run(parameter);
    }
}
