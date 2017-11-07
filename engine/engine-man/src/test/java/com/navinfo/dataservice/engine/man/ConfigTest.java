package com.navinfo.dataservice.engine.man;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.man.config.ConfigService;

import net.sf.json.JSONObject;

public class ConfigTest {
	@Before
	public void before(){
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(  
				new String[] { "dubbo-consumer.xml"}); 
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	
	@Test
	public void testMangeMesh() throws Exception {
		JSONObject dataJson=JSONObject.fromObject("{\"meshList\":\"455104,455105\",\"openFlag\":1}");
		ConfigService.getInstance().mangeMesh(dataJson);
	}
}
