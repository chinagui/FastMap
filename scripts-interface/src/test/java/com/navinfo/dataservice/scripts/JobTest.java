package com.navinfo.dataservice.scripts;


import org.junit.Test;
import com.navinfo.dataservice.api.job.iface.JobApi;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.edit.check.CheckService;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.dataservice.jobframework.runjob.JobCreateStrategy;
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
//	    System.out.println(jobId);286,2955
		
//		Connection conn = null;
		long jobId =0;
		try {
			/*String parameter = "{'isMetaFlag':1,'ckRules':'CHR60009, CHR60010, CHR60011, CHR60012, CHR60013, CHR60014, CHR61009, CHR61010, CHR61011, "
					+ "CHR61012, CHR61013, CHR61014, CHR63415, CHR63419, CHR63423, CHR63428, CHR63436, CHR70107, CHR70108, CHR70109, CHR70110, CHR70111, "
					+ "CHR70112, CHR71024, CHR71025, CHR71026, CHR71027, CHR71028, CHR71041, CHR73040, CHR73041, CHR73042, CHR73043, CHR73044, CHR73045, "
					+ "GLM02115, GLM02129, GLM02130, GLM02131, GLM02132, GLM02137, GLM02138, GLM02139, GLM02142, GLM02145, GLM02150, GLM02154, GLM02156, "
					+ "GLM02157, GLM02166, GLM02167, GLM02170, GLM02183, GLM02187, GLM02191, GLM02197, GLM02198, GLM02209, GLM02213, GLM02214, GLM02215, "
					+ "GLM02216, GLM02223, GLM02224, GLM02227, GLM02228, GLM02230, GLM02233, GLM02234, GLM02235, GLM02236, GLM02254, GLM02260, GLM02261, "
					+ "GLM02262, GLM02269, GLM02270, GLM90216, CHR73040, CHR73041, CHR73042, CHR73043, CHR73044, CHR73045, CHR74093, CHR74094,"
					+ " CHR74095, CHR74096, CHR74097, CHR74098','checkType':7,'name':'雲嶺山莊','nameGroupid':'','adminId':'','roadTypes':[0,2]}";*/

			String parameter ="{'checkType':5,'jobName':'测试检查选中数据123','ckRules':'CHR60009,CHR60010,CHR60011,CHR60012,CHR60013,CHR60014,"
					+ "CHR61009,CHR61010,CHR61011,CHR61012,CHR61013,CHR61014,CHR63415,CHR63419,CHR63423,CHR63428,CHR63436,CHR70107,"
					+ "CHR70108,CHR70109,CHR70110,CHR70111,CHR70112,CHR71024,CHR71025,CHR71026,CHR71027,CHR71028,CHR71041,CHR73040,"
					+ "CHR73041,CHR73042,CHR73043,CHR73044,CHR73045,COM01001,COM01003,COM20552,COM60104,GLM02115,GLM02129,GLM02130,"
					+ "GLM02131,GLM02132,GLM02137,GLM02138,GLM02139,GLM02142,GLM02145,GLM02150,GLM02154,GLM02156,GLM02157,GLM02166,"
					+ "GLM02167,GLM02170,GLM02173,GLM02183,GLM02187,GLM02191,GLM02197,GLM02198,GLM02209,GLM02213,GLM02214,GLM02215,"
					+ "GLM02216,GLM02223,GLM02224,GLM02227,GLM02228,GLM02230,GLM02233,GLM02234,GLM02235,GLM02236,GLM02248,GLM02254,"
					+ "GLM02260,GLM02261,GLM02262,GLM02269,GLM02270,GLM90216,CHR73040,CHR73041,CHR73042,CHR73043,CHR73044,CHR73045,"
					+ "CHR74083,CHR74084,CHR74085,CHR74086,CHR74087,CHR74088,CHR74093,CHR74094,CHR74095,CHR74096,CHR74097,CHR74098',"
					+ "'params':{'name':'','nameGroupid':'','adminId':'','roadTypes':[]},'nameIds':[520000010,402000009]}";
			JSONObject jsonReq = JSONObject.fromObject(parameter);
			int checkType=jsonReq.getInt("checkType");	
			
			//conn = DBConnector.getInstance().getConnectionById(19);

			jobId=CheckService.getInstance().metaCheckRun(2,checkType,jsonReq);
			System.out.println("jobId: "+jobId);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
//			DbUtils.closeQuietly(conn);
		}
		
		
		
	    try{
			//执行job
//			int jobId=24;
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
