package com.navinfo.dataservice.edit.job;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.api.job.iface.JobApi;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.column.job.MultiSrc2FmDaySyncJob;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.dataservice.jobframework.service.JobService;

import net.sf.json.JSONObject;

/** 
 * @ClassName: MultiSrc2FmDaySyncJobTest
 * @author songdongyan
 * @date 2017年5月18日
 * @Description: MultiSrc2FmDaySyncJobTest.java
 */
public class MultiSrc2FmDaySyncJobTest {
	@Before
	public void before(){
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(  
                new String[] {"dubbo-consumer-man-test.xml"}); 
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
		System.out.println();
	}
	@Test
	public void execute() throws Exception{
		JobApi apiService=(JobApi) ApplicationContextUtil.getBean("jobApi");
		JSONObject request=new JSONObject();
		request.element("remoteZipFile", "2");
	    int jobId=(int) apiService.createJob("multisrc2FmDay", request, 0, 0,"创建多源日库增量包导入FM");
	    System.out.println(jobId);
	    try{
	
			JobInfo jobInfo = JobService.getInstance().getJobById(jobId);
			AbstractJob job = new MultiSrc2FmDaySyncJob(jobInfo);
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
