package com.navinfo.dataservice.engine.fcc;


import org.junit.After;
import org.junit.Before;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;

public abstract class InitApplication {
	ClassPathXmlApplicationContext context =null;
	
	@Before
	public abstract void init();
	
	public void initContext()
	{
		context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-consumer-datahub-test.xml"});
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	
	@After
	public  void after(){
		if(context!=null){
			context.close();
		}
	}
}
