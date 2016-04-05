package com.navinfo.dataservice.commons.springmvc;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/** 
* @ClassName: StartupListener 
* @author Xiao Xiaowen 
* @date 2016年3月30日 下午1:58:04 
* @Description: TODO
*/
public class StartupListener implements ApplicationListener<ContextRefreshedEvent> {
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		if(event.getApplicationContext().getParent() == null){
			//...
		}
	}
	
}
