package com.navinfo.dataservice.edit.job;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.api.job.iface.JobApi;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.column.job.Day2MonthPoiMergeJob;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.dataservice.jobframework.runjob.JobThreadPoolExecutor;
import com.navinfo.dataservice.jobframework.service.JobService;

import net.sf.json.JSONObject;

public class Day2MonthPoiMergeJobTest {
	@Before
	public void before(){
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(  
                new String[] {"dubbo-consumer-man-test.xml"}); 
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	@Test
	public void execute() throws Exception{
		JobApi apiService=(JobApi) ApplicationContextUtil.getBean("jobApi");
		JSONObject request=new JSONObject();
		request.element("cityId", "2");
	    int jobId=(int) apiService.createJob("day2MonSyncJob", request, 3,0, "日落月");
	    System.out.println(jobId);
try{
	
			JobInfo jobInfo = JobService.getInstance().getJobById(jobId);
			AbstractJob job = new Day2MonthPoiMergeJob(jobInfo);
			job.execute();
			job.getJobInfo().getResponse();
			
			System.out.println("Over.");
			System.exit(0);
		}catch(Exception e){
			System.out.println("Oops, something wrong:"+e.getMessage());
			e.printStackTrace();
		}
	}
}
