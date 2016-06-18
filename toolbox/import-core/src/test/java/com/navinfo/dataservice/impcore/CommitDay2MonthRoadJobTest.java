package com.navinfo.dataservice.impcore;

import net.sf.json.JSONObject;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.dataservice.jobframework.runjob.JobCreateStrategy;

/*
 * @author mayunfei
 * 2016年6月6日
 * 描述：job-frameworkCommitDay2MonthRoadJobTest.java
 */
public class CommitDay2MonthRoadJobTest {

	@Before
	public void before(){
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(  
                new String[] { "dubbo-consumer-datahub-test.xml"}); 
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}

	@Test
	public void testRun() throws Exception {
		JobInfo jobInfo = new JobInfo(123, java.util.UUID.randomUUID().toString());
		jobInfo.setType("commitDay2MonthRoadJob");
		String reqParams="{\"gridSet\":[59567330,60560301],\"stopTime\":\"20160612173800\"}";
		JSONObject request=JSONObject.fromObject(reqParams);
		jobInfo.setRequest(request);
		AbstractJob job = JobCreateStrategy.create(jobInfo);
		job.run();
	}

}

