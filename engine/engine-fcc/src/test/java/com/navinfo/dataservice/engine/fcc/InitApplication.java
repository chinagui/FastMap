package com.navinfo.dataservice.engine.fcc;


import org.junit.Before;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;

public abstract class InitApplication {
	
	@Before
	public abstract void init();
	
	public void initContext()
	{
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-consumer-datahub-test.xml"});
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
}
