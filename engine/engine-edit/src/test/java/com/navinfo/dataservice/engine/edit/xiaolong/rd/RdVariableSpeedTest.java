/**
 * 
 */
package com.navinfo.dataservice.engine.edit.xiaolong.rd;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.Transaction;

import net.sf.json.JSONObject;

/** 
* @ClassName: RdVariableSpeedTest 
* @author Zhang Xiaolong
* @date 2016年8月15日 下午8:49:43 
* @Description: TODO
*/
public class RdVariableSpeedTest extends InitApplication {

	@Override
	@Before
	public void init() {
		initContext();
	}
	
	@Test
	public void testAdd()
	{
		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDVARIABLESPEED\",\"dbId\":43,\"data\":{\"inLinkPid\":572675,\"outLinkPid\":88026338,\"nodePid\":470000}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();

			String log = t.getLogs();

			JSONObject json = new JSONObject();

			json.put("result", msg);

			json.put("log", log);

			json.put("check", t.getCheckLog());

			json.put("pid", t.getPid());

			System.out.println(json.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
