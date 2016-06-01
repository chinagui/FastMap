package com.navinfo.dataservice.datahub;


import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.api.ApplicationContextUtil;
import com.navinfo.dataservice.api.job.iface.JobExternalService;

/** 
* @ClassName: TestClass 
* @author Xiao Xiaowen 
* @date 2016年3月22日 下午6:15:16 
* @Description: TODO
*/
public class TestClass {
	public String help()throws Exception{
		
		JobExternalService service = (JobExternalService)ApplicationContextUtil.getBean("jobExternalService");
		return service.help();
	}
	
	public static void main(String[] args){
		//ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:TestBeans.xml");  
		  
        //context.start();
		String result = null;
		try{
			result = new TestClass().help();
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println(result);
	}
}	
