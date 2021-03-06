package com.navinfo.dataservice.scripts;

import java.util.Date;

import com.navinfo.dataservice.job.statics.manJob.PersonTipsJob;
import org.junit.Test;
import com.navinfo.dataservice.api.job.iface.JobApi;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.column.job.InfoPoiMultiSrc2FmDayJob;
import com.navinfo.dataservice.column.job.MultiSrc2FmDaySyncJob;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.edit.check.CheckService;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.dataservice.jobframework.runjob.JobCreateStrategy;
import com.navinfo.dataservice.jobframework.service.JobService;
import com.navinfo.dataservice.row.job.PoiRowValidationJob;

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
			JSONObject jobPra = new JSONObject();
			jobPra.put("timestamp", new Date());

			long jobId = JobService.getInstance().create("multisrc2FmDay", jobPra, 0,0, "创建FM日库多源增量包");
//	    	int jobId = 3998;
			System.out.println("jobId: "+jobId);
			JobApi apiService=(JobApi) ApplicationContextUtil.getBean("jobApi");
			JobInfo jobInfo=apiService.getJobById(jobId);
//			JobInfo jobInfo = JobService.getInstance().getJobById(jobId);
			AbstractJob job = new MultiSrc2FmDaySyncJob(jobInfo);
			job.execute();
			job.getJobInfo().getResponse();
			/*AbstractJob job = JobCreateStrategy.createAsMethod(jobInfo);
			job.run();
//			job.execute();
			job.getJobInfo().getResponse();*/
			
			System.out.println("Over.");
			System.exit(0);
		}catch(Exception e){
			System.out.println("Oops, something wrong...");
			e.printStackTrace();
		}
		}
	
//	@Test
	public  void JobTest3() throws Exception {
		//初始化context
		JobScriptsInterface.initContext();

	    try{
			//执行job
			int jobId = 1717;

			JobApi apiService=(JobApi) ApplicationContextUtil.getBean("jobApi");
			JobInfo jobInfo=apiService.getJobById(jobId);
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
	
//	@Test
	public  void JobTest2() throws Exception {
		//初始化context
		JobScriptsInterface.initContext();

	    try{
			//执行job
//			JSONObject jobPra = new JSONObject();
//			jobPra.put("pids", null);
//			jobPra.put("rules", null);
//			jobPra.put("targetDbId", 13);
//			  
//			long jobId = JobService.getInstance().create("poiRowValidation", jobPra, 1664,142, "测试poi检查");

	    	long jobId = 1804;
			
			JobApi apiService=(JobApi) ApplicationContextUtil.getBean("jobApi");
			JobInfo jobInfo=apiService.getJobById(jobId);
			AbstractJob job = new PoiRowValidationJob(jobInfo);
			job.execute();
			job.getJobInfo().getResponse();
			
			System.out.println("Over.");
			System.exit(0);
		}catch(Exception e){
			System.out.println("Oops, something wrong...");
			e.printStackTrace();
		}
		}
//	}

//    @Test
    public  void JobTestPersonTips() throws Exception {
        //初始化context
        JobScriptsInterface.initContext();

        try{
            //执行job
            JSONObject jobPra = new JSONObject();
            jobPra.put("timestamp", new Date());

            long jobId = 16582;
            JobApi apiService=(JobApi) ApplicationContextUtil.getBean("jobApi");
            JobInfo jobInfo = apiService.getJobById(jobId);
            AbstractJob job = new PersonTipsJob(jobInfo);
            job.execute();
            job.getJobInfo().getResponse();

            System.out.println("Over.");
            System.exit(0);
        }catch(Exception e){
            System.out.println("Oops, something wrong...");
            e.printStackTrace();
        }
    }
    
//    @Test
	public  void JobTestInfoPoi() throws Exception {
		//初始化context
		JobScriptsInterface.initContext();

	    try{
	    	String dataStr = "{'fid':'0010171031DXJ201710305','indoorType':0,'regionInfo':'D','remark':'','guidelat':40.27304,'foodType':'3000','lng':116.24536,'kind':'110101','contacts':[],'guidelon':116.44536,'level':'B2','name':'多源Ｂ１11111','sourceProvider':'001000030002','lat':40.07304,'website':'','updateTime':'20170728080000','status':'已发布','postCode':'888888','addFlag':1,'log':'','englishName':'','aliasName':'','adminId':'110108','open24H':2,'chain':'313A','address':'９９９９９９','batch':'大陆','fatherson':'','rating':-1,'delFlag':0}";
	    	
	    	JSONObject dataJson = JSONObject.fromObject(dataStr);
	    	
			//执行job
			JSONObject jobPra = new JSONObject();
			jobPra.put("dbId", 13);
			jobPra.put("taskId", 2158);
			jobPra.put("subtaskId", 2158);
			jobPra.put("bSourceId", 11);
			jobPra.put("data", dataJson);
			  
			long jobId = JobService.getInstance().create("infoPoiMultiSrc2FmDay", jobPra, 0,0, "测试InfoPoi job");
			
//	    	long jobId = 1804;
			System.out.println("jobId: "+jobId);
			
			JobApi apiService=(JobApi) ApplicationContextUtil.getBean("jobApi");
			JobInfo jobInfo=apiService.getJobById(jobId);
			AbstractJob job = new InfoPoiMultiSrc2FmDayJob(jobInfo);
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
