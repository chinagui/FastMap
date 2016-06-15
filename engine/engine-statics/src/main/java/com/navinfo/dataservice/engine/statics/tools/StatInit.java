package com.navinfo.dataservice.engine.statics.tools;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;

public  class StatInit {
	
	/**
	 * 初始化datahub 连接环境
	 */
	public static void initDatahubDb() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "dubbo-consumer-datahub-test.xml" });
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
}
