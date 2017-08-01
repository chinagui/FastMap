package com.navinfo.dataservice.job.statics;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;

public class StatTest {

	@Before
	public void before() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-consumer-datahub-test.xml" });
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	
	@Test
	public void test01() throws Exception{
		
		ManApi manApi=(ManApi)ApplicationContextUtil.getBean("manApi");
		String objName = "subtask";
		Map<Long, Map<String, Object>> data = manApi.queryManTimelineByObjName(objName);
		System.out.println(data.toString());
		
	}
}
