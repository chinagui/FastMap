package org.navinfo.dataservice.engine.meta;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.api.fcc.iface.FccApi;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.meta.rdname.RdNameImportor;
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
		String parameter = "{\"subtaskId\":43,\"pageNum\":1,\"pageSize\":20,\"sortby\":\"\",\"params\":{\"name\":\"\",\"adminId\":\"\"}}";

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			RdNameImportor importor = new RdNameImportor();
			
//			JSONObject data = importor.importRdNameFromWeb(jsonReq);
			
//			System.out.println(data);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
