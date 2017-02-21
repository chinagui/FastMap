package com.navinfo.dataservice.scripts;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.job.iface.JobApi;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.dataservice.jobframework.runjob.JobCreateStrategy;
import com.navinfo.dataservice.jobframework.service.JobService;

public class JobTest {

	public JobTest() {
		// TODO Auto-generated constructor stub
	}
	
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
//		List<String> ruleList=new ArrayList<String>();
//		ruleList.add("CHR60009");
//		ruleList.add("CHR60010");
//		ruleList.add("CHR60011");
//		ruleList.add("CHR60012");
//		ruleList.add("CHR60013");
//		ruleList.add("CHR60014");
//		ruleList.add("CHR61009");
//		ruleList.add("CHR61010");
//		ruleList.add("CHR61011");
//		ruleList.add("CHR61012");
//		ruleList.add("CHR61013");
//		ruleList.add("CHR61014");
//		ruleList.add("CHR63415");
//		ruleList.add("CHR63419");
//		ruleList.add("CHR63423");
//		ruleList.add("CHR63428");
//		ruleList.add("CHR63436");
//		ruleList.add("CHR70107");
//		ruleList.add("CHR70108");
//		ruleList.add("CHR70109");
//		ruleList.add("CHR70110");
//		ruleList.add("CHR70111");
//		ruleList.add("CHR70112");
//		ruleList.add("CHR71024");
//		ruleList.add("CHR71025");
//		ruleList.add("CHR71026");
//		ruleList.add("CHR71027");
//		ruleList.add("CHR71028");
//		ruleList.add("CHR71041");
//		ruleList.add("CHR73040");
//		ruleList.add("CHR73041");
//		ruleList.add("CHR73042");
//		ruleList.add("CHR73043");
//		ruleList.add("CHR73044");
//		ruleList.add("CHR73045");
//		ruleList.add("COM01001");
//		ruleList.add("COM01003");
//		ruleList.add("COM20552");
//		ruleList.add("COM60104");
//		
//		
//		JobApi apiService=(JobApi) ApplicationContextUtil.getBean("jobApi");
//		DatahubApi datahub = (DatahubApi) ApplicationContextUtil
//				.getBean("datahubApi");
//		DbInfo metaDb = datahub.getOnlyDbByType("metaRoad");
//		Integer metaDbid = metaDb.getDbId();
//		if(metaDbid != null && metaDbid >0){
//			System.out.println("metaDbid: "+metaDbid);
//		JSONObject metaValidationRequestJSON=new JSONObject();
//		metaValidationRequestJSON.put("executeDBId", 106);//元数据库dbId
//		metaValidationRequestJSON.put("kdbDBId", 106);//元数据库dbId
//		metaValidationRequestJSON.put("ruleIds", ruleList);
//		metaValidationRequestJSON.put("timeOut", 600);
//	    int jobId=(int) apiService.createJob("checkCore", metaValidationRequestJSON, 3,0, "元数据库检查");
//	    System.out.println(jobId);
	    try{
			//执行job
			int jobId=7223;
			JobInfo jobInfo = JobService.getInstance().getJobById(jobId);
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
