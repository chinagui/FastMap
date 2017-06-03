package com.navinfo.dataservice.scripts;

import java.util.Date;

import org.junit.Test;
import com.navinfo.dataservice.api.job.iface.JobApi;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.edit.check.CheckService;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.dataservice.jobframework.runjob.JobCreateStrategy;
import com.navinfo.dataservice.jobframework.service.JobService;

import net.sf.json.JSONObject;

public class JobTest {

	public JobTest() {
		// TODO Auto-generated constructor stub
	}
	
	/* @Before
	    public void init() {
	        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
	                new String[]{"dubbo-test.xml"});
	        context.start();
	        new ApplicationContextUtil().setApplicationContext(context);
	    }*/
	
	/*public static void main(String[] args){
		try{
			
			String dir = SystemConfigFactory.getSystemConfig().getValue("scripts.dir");
			//初始化context
			JobScriptsInterface.initContext();
			//执行job
			int jobId=777;
			JobInfo jobInfo = JobService.getInstance().getJobById(jobId);
			AbstractJob job = JobCreateStrategy.createAsMethod(jobInfo);
//			job.run();
			job.execute();
			job.getJobInfo().getResponse();
			
			System.out.println("Over.");
			System.exit(0);
		}catch(Exception e){
			System.out.println("Oops, something wrong...");
			e.printStackTrace();
		}
	}*/
	@Test
	public  void JobTest() throws Exception {
		//初始化context
		JobScriptsInterface.initContext();

	    try{
			//执行job
//			JSONObject jobPra = new JSONObject();
//			jobPra.put("timestamp", new Date());
//
//			long jobId = JobService.getInstance().create("MultiSrc2FmDaySyncJob", jobPra, 0,0, "创建FM日库多源增量包");
	    	int jobId = 696;

			JobApi apiService=(JobApi) ApplicationContextUtil.getBean("jobApi");
			JobInfo jobInfo=apiService.getJobById(jobId);
//			JobInfo jobInfo = JobService.getInstance().getJobById(jobId);
			AbstractJob job = JobCreateStrategy.createAsMethod(jobInfo);
			job.run();
//			job.execute();
			job.getJobInfo().getResponse();
			
			System.out.println("Over.");
			System.exit(0);
		}catch(Exception e){
			System.out.println("Oops, something wrong...");
			e.printStackTrace();
		}
		}
//	}

}
