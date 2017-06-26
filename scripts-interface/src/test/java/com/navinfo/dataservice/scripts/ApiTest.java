package com.navinfo.dataservice.scripts;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.springmvc.ClassPathXmlAppContextInit;

/** 
 * @ClassName: ApiTest
 * @author xiaoxiaowen4127
 * @date 2017年4月28日
 * @Description: ApiTest.java
 */
public class ApiTest  extends ClassPathXmlAppContextInit{
	@Before
	public void before(){
		initContext(new String[]{"dubbo-app-scripts.xml","dubbo-scripts.xml"});
	}
	
	@Test
	public void testManApi(){
		try{
			ManApi manApi = (ManApi)ApplicationContextUtil.getBean("manApi");
			Map<String,Integer> taskMap = manApi.getTaskBySubtaskId(415);
			for(String key:taskMap.keySet()){
				System.out.println(key+":"+taskMap.get(key));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
