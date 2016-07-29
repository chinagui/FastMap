package com.navinfo.dataservice.commons.springmvc;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/** 
* @ClassName: ClassPathXmlAppContextInit 
* @author Xiao Xiaowen 
* @date 2016年7月7日 下午8:08:17 
* @Description: TODO
*/
public abstract class ClassPathXmlAppContextInit {
	
	public void initContext(String[] xml)
	{
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				xml);
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
}
