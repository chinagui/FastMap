package org.navinfo.dataservice.engine.meta;

import java.sql.Connection;
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
	
	
//	@Test
//	public static void main(String[] args) {
//		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
//				new String[] { "dubbo-consumer.xml"});
//		context.start();
//		new ApplicationContextUtil().setApplicationContext(context);
//		RdNameImportor importor = new RdNameImportor();
//		try {
//			/*importor.importName("A45", 116.49266, 40.20926, "test_imp1");
//			importor.importName("测试高速公路", 116.49266, 40.20926, "test_imp1");
//			importor.importName("测试高架路", 116.49266, 40.20926, "test_imp1");
//			importor.importName("测试高架桥", 116.49266, 40.20926, "test_imp1");
//			importor.importName("测试快速路", 116.49266, 40.20926, "test_imp1");
//			
//			importor.importName("N", 116.49266, 40.20926, "test_imp1");
//			importor.importName("n", 116.49266, 40.20926, "test_imp1");
//			importor.importName("NO", 116.49266, 40.20926, "test_imp1");
//			importor.importName("no", 116.49266, 40.20926, "test_imp1");
//			importor.importName("No", 116.49266, 40.20926, "test_imp1");
//			importor.importName("无道路名", 116.49266, 40.20926, "test_imp1");
//			importor.importName("无", 116.49266, 40.20926, "test_imp1");
//			
//			importor.importName("Ｎ", 116.49266, 40.20926, "test_imp1");
//			importor.importName("ＮＯ", 116.49266, 40.20926, "test_imp1");
//			
//			importor.importName("测试1#路", 116.49266, 40.20926, "test_imp1");
//			importor.importName("测试2＃路", 116.49266, 40.20926, "test_imp1");*/
//			
//			importor.importName("测试罗马 V 路", 116.49266, 40.20926, "test_imp1");
//			importor.importName("测试123c路", 116.49266, 40.20926, "test_imp1");
//			
//			System.out.println("测试完成");
//			
//			
//			
///*			DELETE FROM RD_NAME N
//			 WHERE NAME_GROUPID IN
//			       ( SELECT NAME_GROUPID
//			                  FROM RD_NAME
//			                 WHERE SRC_RESUME LIKE '%test_imp1%'
//			        )
//			        
//			        
//			        
//			SELECT * FROM RD_NAME N
//			 WHERE NAME_GROUPID IN
//			       ( SELECT NAME_GROUPID
//			                  FROM RD_NAME
//			                 WHERE SRC_RESUME LIKE '%test_imp1%'
//			        )*/
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//	
	
	@Test
	public void testGetRdName()
	{
		String parameter = "{\"subtaskId\":27,\"pageNum\":1,\"pageSize\":20,\"sortby\":\"\",\"flag\":1,\"params\":{\"name\":\"\",\"nameGroupid\":\"\",\"adminId\":\"\"}}";

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			RdNameSelector selector = new RdNameSelector();
			
			int subtaskId = jsonReq.getInt("subtaskId");
			
			ManApi apiService=(ManApi) ApplicationContextUtil.getBean("manApi");
			
			Subtask subtask = apiService.queryBySubtaskId(subtaskId);
			
			if (subtask == null) {
				throw new Exception("subtaskid未找到数据");
			}
			
			int dbId = subtask.getDbId();
			
			FccApi apiFcc=(FccApi) ApplicationContextUtil.getBean("fccApi");
			
			JSONArray tips = apiFcc.searchDataBySpatial(subtask.getGeometry(),1901,new JSONArray());
			
			JSONObject data = selector.searchForWeb(jsonReq,tips,dbId);
			
			System.out.println(data);
					
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void teilenName () {
		String parameter = "{\"dbId\":9,\"data\":[{\"nameId\":420000001,\"nameGroupid\":503000002,\"langCode\":\"CHI\",\"roadType\":0}],\"flag\":1,\"subtaskId\":208}";
		
		Connection conn = null;
		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);
			
			int flag = jsonReq.getInt("flag");
			
			int dbId = jsonReq.getInt("dbId");
			
			conn = DBConnector.getInstance().getConnectionById(dbId);
			
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

}
