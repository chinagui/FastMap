package com.navinfo.dataservice.edit.job;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.api.job.iface.JobApi;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.column.job.Day2MonthPoiMergeJob;
import com.navinfo.dataservice.column.job.MonthPoiBatchSyncJob;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.dataservice.jobframework.runjob.JobThreadPoolExecutor;
import com.navinfo.dataservice.jobframework.service.JobService;

import net.sf.json.JSONObject;

public class MonthPoiBatchSyncTest {
	@Before
	public void before(){
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(  
                new String[] {"dubbo-consumer-man-test.xml"}); 
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	@Test
	public void execute() throws Exception{
		//JobApi apiService=(JobApi) ApplicationContextUtil.getBean("jobApi");
		//JSONObject request=new JSONObject();
		//request.element("taskId", "305");
	    //int jobId=(int) apiService.createJob("monthPoiBatch", request, 305,4577, "poi管理字段批处理");
	   // System.out.println(jobId);
	    try{
	
			//JobInfo jobInfo = JobService.getInstance().getJobById(1);
			AbstractJob job = new MonthPoiBatchSyncJob(null);
			job.execute();
			//job.getJobInfo().getResponse();
			
			System.out.println("Over.");
			System.exit(0);
		}catch(Exception e){
			System.out.println("Oops, something wrong:"+e.getMessage());
			e.printStackTrace();
		}
	}
}
