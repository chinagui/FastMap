package com.navinfo.dataservice.engine.limit.commons.config;

/** 
* @ClassName: SystemConfigFactory 
* @author Xiao Xiaowen 
* @date 2016年3月25日 下午6:20:24 
* @Description: TODO
*/
public class SystemConfigFactory {
	public static SystemConfig getSystemConfig(){
		return DynamicSystemConfig.getInstance();
	}
}	
