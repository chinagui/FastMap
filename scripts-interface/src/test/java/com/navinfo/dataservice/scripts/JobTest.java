package com.navinfo.dataservice.scripts;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.dataservice.jobframework.runjob.JobCreateStrategy;
import com.navinfo.dataservice.jobframework.service.JobService;

public class JobTest {

	public JobTest() {
		// TODO Auto-generated constructor stub
	}
	
	public static void main(String[] args){
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
	}

}
