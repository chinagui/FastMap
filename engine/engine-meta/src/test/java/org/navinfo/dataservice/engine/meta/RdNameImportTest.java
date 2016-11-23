package org.navinfo.dataservice.engine.meta;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.dbutils.DbUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import com.navinfo.dataservice.api.fcc.iface.FccApi;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.meta.rdname.RdNameImportor;
import com.navinfo.dataservice.engine.meta.rdname.RdNameOperation;
import com.navinfo.dataservice.engine.meta.rdname.RdNameSelector;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
 * @ClassName: RdNameImportTest.java
 * @author y
 * @date 2016-7-2下午3:07:28
 * @Description: TODO
 *  
 */
public class RdNameImportTest {
	
	@Before
	public void before() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-consumer-datahub-test.xml" });
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	
	
	//@Test
	public  void nameImportTest() {
		/*ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-consumer.xml"});
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);*/
		RdNameImportor importor = new RdNameImportor();
		try {
			/*importor.importName("A45", 116.49266, 40.20926, "test_imp1");
			importor.importName("测试高速公路", 116.49266, 40.20926, "test_imp1");
			importor.importName("测试高架路", 116.49266, 40.20926, "test_imp1");
			importor.importName("测试高架桥", 116.49266, 40.20926, "test_imp1");
			importor.importName("测试快速路", 116.49266, 40.20926, "test_imp1");
			
			importor.importName("N", 116.49266, 40.20926, "test_imp1");
			importor.importName("n", 116.49266, 40.20926, "test_imp1");
			importor.importName("NO", 116.49266, 40.20926, "test_imp1");
			importor.importName("no", 116.49266, 40.20926, "test_imp1");
			importor.importName("No", 116.49266, 40.20926, "test_imp1");
			importor.importName("无道路名", 116.49266, 40.20926, "test_imp1");
			importor.importName("无", 116.49266, 40.20926, "test_imp1");
			
			importor.importName("Ｎ", 116.49266, 40.20926, "test_imp1");
			importor.importName("ＮＯ", 116.49266, 40.20926, "test_imp1");
			
			importor.importName("测试1#路", 116.49266, 40.20926, "test_imp1");
			importor.importName("测试2＃路", 116.49266, 40.20926, "test_imp1");*/
			
			importor.importName("测试罗马 V 路", 116.49266, 40.20926, "test_imp1");
			importor.importName("测试123c东2路", 116.49266, 40.20926, "test_imp1");
			
			System.out.println("测试完成");
			
			
			
/*			DELETE FROM RD_NAME N
			 WHERE NAME_GROUPID IN
			       ( SELECT NAME_GROUPID
			                  FROM RD_NAME
			                 WHERE SRC_RESUME LIKE '%test_imp1%'
			        )
			        
			        
			        
			SELECT * FROM RD_NAME N
			 WHERE NAME_GROUPID IN
			       ( SELECT NAME_GROUPID
			                  FROM RD_NAME
			                 WHERE SRC_RESUME LIKE '%test_imp1%'
			        )*/
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	//@Test
	public void testGetRdName()
	{
		String parameter = "{\"subtaskId\":76,\"pageNum\":1,\"pageSize\":20,\"sortby\":\"\",\"flag\":1,\"params\":{\"name\":\"\",\"nameGroupid\":\"\",\"adminId\":\"\"}}";

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			RdNameSelector selector = new RdNameSelector();
			
			int subtaskId = jsonReq.getInt("subtaskId");
			
			ManApi apiService=(ManApi) ApplicationContextUtil.getBean("manApi");
			
			Subtask subtask = apiService.queryBySubtaskId(subtaskId);
			
			if (subtask == null) {
				throw new Exception("subtaskid未找到数据");
			}
			
//			int dbId = subtask.getDbId();
			
			FccApi apiFcc=(FccApi) ApplicationContextUtil.getBean("fccApi");
			
			JSONArray tips = apiFcc.searchDataBySpatial(subtask.getGeometry(),1901,new JSONArray());
			
			JSONObject data = selector.searchForWeb(jsonReq,tips);
			
			System.out.println("data  "+data.toString());
					
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testGetRdNameOneByNameID(){
		String parameter ="{'nameId':'647497'}";
		JSONObject jsonReq = JSONObject.fromObject(parameter);

		RdNameSelector selector = new RdNameSelector();
		
		String nameId = jsonReq.getString("nameId");
		
		
		JSONObject data = selector.searchForWebByNameId(nameId);
		
		System.out.println(data);
	}
	
	//@Test
	public void teilenName () {
		//String parameter = "{\"dbId\":9,\"data\":[{\"nameId\":40589343,\"nameGroupid\":40589344,\"langCode\":\"CHI\",\"roadType\":3}],\"flag\":1,\"subtaskId\":208}";
		//String parameter = "{\"dbId\":9,\"data\":[{\"nameId\":0,\"nameGroupid\":11111111,\"langCode\":\"CHI\",\"roadType\":3}],\"flag\":1,\"subtaskId\":208}";
		//String parameter = "{\"dbId\":9,\"data\":[{\"nameId\":82369,\"nameGroupid\":82369,\"langCode\":\"CHI\",\"roadType\":3}],\"flag\":1,\"subtaskId\":208}";
		//String parameter = "{\"dbId\":9,\"data\":[{\"nameId\":82371,\"nameGroupid\":82371,\"langCode\":\"CHI\",\"roadType\":3}],\"flag\":1,\"subtaskId\":208}";
		//三一二国道 String parameter = "{\"dbId\":9,\"data\":[{\"nameId\":2763,\"nameGroupid\":2763,\"langCode\":\"CHI\",\"roadType\":3}],\"flag\":1,\"subtaskId\":208}";
		//
		//String parameter = "{\"dbId\":9,\"data\":[{\"nameId\":264491,\"nameGroupid\":264491,\"langCode\":\"CHI\",\"roadType\":3}],\"flag\":1,\"subtaskId\":208}";
		//地铁13 号线 String parameter = "{\"dbId\":9,\"data\":[{\"nameId\":82374,\"nameGroupid\":82374,\"langCode\":\"CHI\",\"roadType\":3}],\"flag\":1,\"subtaskId\":208}";
		//轨道交通１号线  String parameter = "{\"dbId\":9,\"data\":[{\"nameId\":40611226,\"nameGroupid\":40611227,\"langCode\":\"CHI\",\"roadType\":3}],\"flag\":1,\"subtaskId\":208}";
		//
		String parameter = "{\"dbId\":9,\"data\":[{\"nameId\":40611226,\"nameGroupid\":40611227,\"langCode\":\"CHI\",\"roadType\":3}],\"flag\":1,\"subtaskId\":208}";
		
		Connection conn = null;
		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);
			
			int flag = jsonReq.getInt("flag");
			
//			int dbId = jsonReq.getInt("dbId");
//			
//			conn = DBConnector.getInstance().getConnectionById(dbId);
			
			conn = DBConnector.getInstance().getMetaConnection();
			
			RdNameOperation operation = new RdNameOperation(conn);
			
			
			if (flag>0) {
				JSONArray dataList = jsonReq.getJSONArray("data");
				
				operation.teilenRdName(dataList);
			} else {
				int subtaskId = jsonReq.getInt("subtaskId");
				
				ManApi apiService=(ManApi) ApplicationContextUtil.getBean("manApi");
				
				Subtask subtask = apiService.queryBySubtaskId(subtaskId);
				
				FccApi apiFcc=(FccApi) ApplicationContextUtil.getBean("fccApi");
				
				JSONArray tips = apiFcc.searchDataBySpatial(subtask.getGeometry(),1901,new JSONArray());
				
				operation.teilenRdNameByTask(tips);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	//@Test
	public void saveRdName(){
		RdNameImportor a = new RdNameImportor();
		JSONObject jsonReq = JSONObject.fromObject("{'data':{'options':{},'geoLiveType':'ROADNAME','pid':null,'nameId':null,'nameGroupid':null,'langCode':'CHI','name':'33333张莉测试','type':'','base':'','prefix':'','infix':'','suffix':'','namePhonetic':'','typePhonetic':'','basePhonetic':'','prefixPhonetic':'','infixPhonetic':'','suffixPhonetic':'','srcFlag':0,'roadType':0,'adminId':110000,'codeType':0,'voiceFile':'','srcResume':'','paRegionId':null,'splitFlag':0,'memo':'','routeId':0,'uRecord':null,'uFields':'','city':'','adminName':'北京','_initHooksCalled':true},'subtaskId':76,'dbId':17}");
		
		JSONObject data = jsonReq.getJSONObject("data");
		
		int subtaskId = jsonReq.getInt("subtaskId");
		try {
			JSONObject jobj =a.importRdNameFromWeb(data, subtaskId);
			System.out.println(jobj);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
//	@Test
//	public  void JobTest() throws Exception {
//		List<String> ruleList=new ArrayList<String>();
//		ruleList.add("COM60104");
//		ruleList.add("COM60104");
//		ruleList.add("GLM02216");
//		ruleList.add("GLM02262");
//		ruleList.add("GLM02261");
//		
//		
//		JobApi apiService=(JobApi) ApplicationContextUtil.getBean("jobApi");
//		JSONObject metaValidationRequestJSON=new JSONObject();
//		metaValidationRequestJSON.put("executeDBId", 106);//元数据库dbId
//		metaValidationRequestJSON.put("kdbDBId", 106);//元数据库dbId
//		metaValidationRequestJSON.put("ruleIds", ruleList);
//		metaValidationRequestJSON.put("timeOut", 600);
//	    int jobId=(int) apiService.createJob("checkCore", metaValidationRequestJSON, 3, "元数据库检查");
//try{
//	
//			//初始化context
//			JobScriptsInterface.initContext();
//			//执行job
//			//int jobId=777;
//			JobInfo jobInfo = JobService.getInstance().getJobById(jobId);
//			AbstractJob job = JobCreateStrategy.createAsMethod(jobInfo);
////			job.run();
//			job.execute();
//			job.getJobInfo().getResponse();
//			
//			System.out.println("Over.");
//			System.exit(0);
//		}catch(Exception e){
//			System.out.println("Oops, something wrong...");
//			e.printStackTrace();
//		}
//	}

}
