package com.navinfo.dataservice.engine.man;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.api.man.model.FmDay2MonSync;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.man.config.ConfigService;
import com.navinfo.dataservice.engine.man.day2Month.Day2MonthSyncService;
import com.navinfo.dataservice.engine.man.job.JobService;
import com.navinfo.dataservice.engine.man.job.bean.JobProgressStatus;
import com.navinfo.dataservice.engine.man.job.bean.JobType;

import net.sf.json.JSONObject;

public class ConfigTest {
	@Before
	public void before(){
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(  
				new String[] { "dubbo-consumer.xml"}); 
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	
	@Test
	public void testMangeMesh() throws Exception {
		JSONObject dataJson=JSONObject.fromObject("{\"meshList\":\"455104,455105\",\"openFlag\":1}");
		ConfigService.getInstance().mangeMesh(dataJson);
	}
}
