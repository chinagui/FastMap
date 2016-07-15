package com.navinfo.dataservice.engine.edit.utils;
import com.navinfo.dataservice.api.man.iface.ManApi;

import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
/**
 * 对外接口开发调用公共类
 * @author zhaokk
 *
 */
public class ApiUtils {

	private static class SingletonHolder {
		private static final ApiUtils INSTANCE = new ApiUtils();
	}

	public static final ApiUtils getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	public ManApi getManApi(){
		return (ManApi)ApplicationContextUtil.getBean("manApi");
	}
	

}