/**
 * 
 */
package com.navinfo.dataservice.engine.edit.xiaolong.rd;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.Transaction;

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
		String parameter = "{\"command\":\"CREATE\",\"dbId\":42,\"type\":\"RDTRAFFICSIGNAL\",\"data\":{\"nodePid\":\"741991\"}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testUpdateTrafficSignal()
	{
		String parameter = "{\"command\":\"UPDATE\",\"type\":\"RDTRAFFICSIGNAL\",\"dbId\":42,\"data\":{\"type\":2,\"rowId\":\"78D984F13079444FB6DDBA25A52FAF69\",\"pid\":100000089,\"objStatus\":\"UPDATE\",\"kgFlag\":\"2\"}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
