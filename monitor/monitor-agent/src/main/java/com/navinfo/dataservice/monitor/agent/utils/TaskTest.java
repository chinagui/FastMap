package com.navinfo.dataservice.monitor.agent.utils;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;

public class TaskTest {
	public static void main(String[] args) {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(  
				new String[] {"applicationContext-quartz.xml"}); 
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}

}
