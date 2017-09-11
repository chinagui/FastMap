package com.navinfo.dataservice.engine.man;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.api.man.model.FmDay2MonSync;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.man.day2Month.Day2MonthSyncService;
import com.navinfo.dataservice.engine.man.job.JobService;
import com.navinfo.dataservice.engine.man.job.Day2Month.CloseMeshPhase;
import com.navinfo.dataservice.engine.man.job.bean.ItemType;
import com.navinfo.dataservice.engine.man.job.bean.JobProgressStatus;
import com.navinfo.dataservice.engine.man.job.bean.JobType;

import net.sf.json.JSONObject;

public class JobTest2 {
	@Before
	public void before(){
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(  
				new String[] { "dubbo-consumer.xml"}); 
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	
	@Test
	public void testUpdateJob() throws Exception {
		JSONObject detailjson=new JSONObject();
		detailjson.put("tipsNum", 12);
		JSONObject returnjson=new JSONObject();
		returnjson.put("detail", detailjson);
		JobService.getInstance().updateJobProgress(718, JobProgressStatus.NODATA, returnjson.toString());
	}
	
	@Test
	public void testRunCommonJob() throws Exception {		
		JobService.getInstance().runCommonJob(JobType.DAY2MONTH,535, ItemType.PROJECT, 0, true, "{\"lot\":2}");
	}
}
