package com.navinfo.dataservice.edit.job;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.column.job.Day2MonthPoiMergeJob;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.jobframework.exception.JobException;

public class Day2MonthPoiMergeJobTest {
	@Before
	public void before(){
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(  
                new String[] {"dubbo-consumer-man-test.xml"}); 
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	@Test
	public void execute() throws JobException{
		JobInfo jobInfo = new JobInfo(0, "Day2MonthPoiMergeJobTest");
		Day2MonthPoiMergeJob job = new Day2MonthPoiMergeJob(jobInfo );
		job.execute();
	}
}
