/**
 * 
 */
package com.navinfo.dataservice.engine.edit.xiaolong.rd;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.Transaction;

/** 
* @ClassName: RdTmcLocationTest 
* @author Zhang Xiaolong
* @date 2016年11月17日 下午7:22:17 
* @Description: TODO
*/
public class RdTmcLocationTest extends InitApplication{
	@Override
	@Before
	public void init() {
		initContext();
	}
	
	@Test
	public void testRdTmcLocation()
	{
		String parameter = "{\"command\":\"CREATE\",\"dbId\":17,\"data\":{\"tmcId\":522005190,\"locDirect\":1,\"loctableId\":32,\"direct\":1,\"linkPids\":[205002726,200002715]},\"type\":\"RDTMCLOCATION\"}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
