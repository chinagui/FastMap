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
* @ClassName: RdTrafficSignalTest 
* @author Zhang Xiaolong
* @date 2016年7月21日 上午9:25:13 
* @Description: TODO
*/
public class RdTrafficSignalTest extends InitApplication{

	@Before
	@Override
	public void init() {
		//调用父类初始化contex方法
				initContext();
	}
	
	@Test
	public void testAddTrafficSignal() {
		String parameter = "{\"command\":\"CREATE\",\"dbId\":42,\"type\":\"RDTRAFFICSIGNAL\",\"data\":{\"nodePid\":\"100027911\"}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			
			String log = t.getLogs();
			
			System.out.println(log);
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
	
	@Test
	public void testUpdateTrafficSignal()
	{
		String parameter = "{\"command\":\"UPDATE\",\"type\":\"RDTRAFFICSIGNAL\",\"dbId\":42,\"data\":{\"type\":2,\"rowId\":\"78D984F13079444FB6DDBA25A52FAF69\",\"pid\":100000089,\"objStatus\":\"UPDATE\",\"location\":\"10\"}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testDeleteTrafficSignal()
	{
		String parameter = "{\"command\":\"DELETE\",\"type\":\"RDTRAFFICSIGNAL\",\"dbId\":42,\"objId\":100000191}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
