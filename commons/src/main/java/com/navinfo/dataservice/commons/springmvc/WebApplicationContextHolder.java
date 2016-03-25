package com.navinfo.dataservice.commons.springmvc;

import javax.servlet.ServletContext;

import org.springframework.web.context.WebApplicationContext;

/**
 * 
 * @author liuqing
 * 
 */
public class WebApplicationContextHolder {
	private static String ATTRIBUTE_NAME = "org.springframework.web.servlet.FrameworkServlet.CONTEXT.springmvc";

	/**
	 * 获取spring的WebApplicationContext，如果返回值为空，请检查 web.xml是否有如下配置<init-param>
	 * <param-name>publishContext</param-name> <param-value>true</param-value>
	 * </init-param>
	 * 
	 * @param context
	 * @return
	 */
	public static WebApplicationContext getWebApplicationContext(ServletContext context) {
		return (WebApplicationContext) context.getAttribute(ATTRIBUTE_NAME);

	}

}
