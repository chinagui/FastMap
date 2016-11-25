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
		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDTMCLOCATION\",\"dbId\":17,\"data\":{\"tmcId\":\"522001558\",\"direct\":\"1\",\"locDirect\":\"1\",\"loctableId\":\"32\",\"linkPids\":[565603,565604,681841,681842,611966,678760,678759,15481020,15481019,49043863,671528,681844,606299,671718,685606,685605,671456,12566922,12566924,12566923,671526,88075237,88075238,682639,682640,583413,682642,682643,682657,682658]}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
