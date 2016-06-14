package com.navinfo.dataservice.jobframework.test;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.IRegion;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.jobframework.service.JobService;

/** 
* @ClassName: JobServiceTest 
* @author Xiao Xiaowen 
* @date 2016年6月12日 下午2:19:01 
* @Description: TODO
*  
*/
public class JobServiceTest {
	@Before
	public void before(){
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(  
                new String[] {"dubbo-consumer-man-test.xml"}); 
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	@Test
	public void hello_001(){
		try{
			System.out.println(JobService.getInstance().hello());
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	@Test
	public void hello_002(){
		try{
			ManApi man = (ManApi)ApplicationContextUtil.getBean("manApi");
			IRegion region = man.queryByRegionId(1);
			System.out.println("**********"+region.getDailyDbId());
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
