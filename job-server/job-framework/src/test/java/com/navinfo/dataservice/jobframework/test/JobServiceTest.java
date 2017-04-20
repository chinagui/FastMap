package com.navinfo.dataservice.jobframework.test;

import java.sql.Connection;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.bizcommons.glm.Glm;
import com.navinfo.dataservice.bizcommons.glm.GlmCache;
import com.navinfo.dataservice.bizcommons.glm.GlmTable;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.json.JsonOperation;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.DateUtilsEx;
import com.navinfo.dataservice.jobframework.service.JobService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
* @ClassName: JobServiceTest 
* @author Xiao Xiaowen 
* @date 2016年6月12日 下午2:19:01 
* @Description: TODO
*  
*/
public class JobServiceTest {
	@Before
	public void before(){
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(  
                new String[] {"dubbo-consumer-man-test.xml"}); 
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	//@Test
	public void hello_001(){
		try{
			System.out.println(JobService.getInstance().hello());
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	//@Test
	public void hello_002(){
		try{
			List<Integer> list = new ArrayList<Integer>();
			list.add(43);
			for (int i : list) {
				Connection conn = DBConnector.getInstance().getConnectionById(i);
				System.out.println(conn + "----------------test1");

			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	//@Test
	public void hello_003(){
		Glm glm = GlmCache.getInstance().getGlm("250+");
		Map<String,GlmTable> tables = glm.getEditTables();
		List<String> tableNames = glm.getEditTableNames(GlmTable.FEATURE_TYPE_ALL);
		for(String name:tables.keySet()){
			System.out.println(name);
//			List<GlmColumn> cols = table.getColumns();
//			for(GlmColumn col:cols){
//				System.out.println("--"+col.getName()+":"+col.getDataType()+":"+col.isPk());
//			}
		}
		System.out.println(StringUtils.join(tableNames,","));
		System.out.println("Over.");
	}
	//@Test
		public void testSearch(){
			try{
				JSONObject obj = JobService.getInstance().getLatestJob(6);
				System.out.println(obj);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		//@Test
		public void testSearchJobList(){
			try{
				JSONObject jsonReq = JSONObject.fromObject("{'tableName':'rdName','jobName':''}");	
				
				List<JobInfo> obj = JobService.getInstance().getJobInfoList(jsonReq);
				System.out.println(obj);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		
		//@Test
		public void getLatestJobByDescp(){
			try{
				
				JobInfo obj = JobService.getInstance().getLatestJobByDescp("rdName");
				System.out.println(obj);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		@Test
		public void getLatestJobByDescp2(){
			try{
				JSONObject jsonReq = JSONObject.fromObject("{'tableName':'rdName','taskName':''}");	
		
				System.out.println("taskName : "+ jsonReq.getString("taskName"));
				
				if(jsonReq.getString("taskName") == null || StringUtils.isEmpty(jsonReq.getString("taskName"))){
					String tableName  = jsonReq.getString("tableName");
					JobInfo obj = JobService.getInstance().getLatestJobByDescp("rdName");
					System.out.println(obj);
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		
		
		
		public static void main(String[] args) {
			/*String startDate= "";
			String endDate= "";
			Timestamp curTime = DateUtilsEx.getCurTime();
			
			Timestamp beginTime = DateUtilsEx.getDayOfDelayMonths(curTime, -1);
			
			 SimpleDateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd");
		        if (null == beginTime) //modified by dengfasheng 2006-08-01
		        return df.format(getDate(time));
			
			
			startDate=DateUtilsEx.getTimeStr(beginTime, "yyyy-MM-dd");
			endDate = DateUtilsEx.getTimeStr(curTime, "yyyy-MM-dd");
			System.out.println(startDate+" "+endDate);*/
			
			JSONArray groupDate = new JSONArray();
			groupDate.add("rdName");
			groupDate.add("rdName2");
			groupDate.add("rdName3");
			List<String> groupList = new ArrayList<String>();
			List<String> groupList2 = new ArrayList<String>();
			
			if(groupDate != null && groupDate.size() > 0){
				groupList = (List<String>) JSONArray.toCollection(groupDate);
				for(Object obj : groupDate){
					groupList2.add((String) obj);
				}
			}
		}
	
}
