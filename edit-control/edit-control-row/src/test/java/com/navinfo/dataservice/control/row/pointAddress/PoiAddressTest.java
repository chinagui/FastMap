package com.navinfo.dataservice.control.row.pointAddress;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.control.row.pointaddress.PointAddressSave;

public class PoiAddressTest {
	@Before
	public void before() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-consumer-datahub-test.xml" });
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	
	
	@Test
	public void testPointAddressSave() throws Exception {
		long userId = 1674;
//		String parameter = "{\"subtaskId\":649,\"dbId\":13,\"type\":\"IXPOINTADDRESS\",\"command\":\"UPDATE\",\"objId\":505000001,\"data\":{\"longitude\":117.19050,\"latitude\":39.14393,\"xGuide\":117.19049,\"yGuide\":39.14381,\"guideLinkPid\":683072,\"dprName\":\"BuBc\",\"dpName\":\"666s6\",\"memoire\":\"IDCode\",\"memo\":\"备注fsdfs\"}}";
//		String parameter = "{\"subtaskId\":649,\"dbId\":13,\"type\":\"IXPOINTADDRESS\",\"command\":\"CREATE\",\"data\":{\"longitude\":117.19050,\"latitude\":39.14393,\"xGuide\":117.19049,\"yGuide\":39.14381,\"guideLinkPid\":683072,\"dprName\":\"BBc\",\"dpName\":\"666s6\",\"memoire\":\"IDCode\",\"memo\":\"备注\"}}";
//		String parameter = "{\"subtaskId\":649,\"dbId\":13,\"type\":\"IXPOINTADDRESS\",\"command\":\"DELETE\",\"objId\":505000001}";
		String parameter = "{\"command\": \"BATCHMOVE\",\"type\": \"IXPOINTADDRESS\",\"data\": [{\"pid\": 639857,\"longitude\": 116.26255989074707,\"latitude\": 40.173126838288,\"xGuide\": 116.26161,\"yGuide\": 40.17344,\"guideLinkPid\": 1533810},{\"pid\": 639860,\"longitude\": 116.26255989074707,\"latitude\": 40.173126838288,\"xGuide\": 116.26161,\"yGuide\": 40.17344,\"guideLinkPid\": 1533810}],\"dbId\": 13,\"subtaskId\": 171}";
		PointAddressSave.getInstance().save(parameter, userId);
	}
}
