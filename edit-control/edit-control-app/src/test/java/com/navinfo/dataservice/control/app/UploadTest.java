package com.navinfo.dataservice.control.app;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import com.navinfo.dataservice.commons.photo.Photo;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.control.app.upload.UploadOperation;
import com.navinfo.dataservice.control.service.PaUploadManager;
import com.navinfo.dataservice.control.service.UploadManager;
import com.navinfo.dataservice.control.service.UploadResult;
import com.navinfo.dataservice.engine.photo.CollectorImport;
import net.sf.json.JSONObject;

public class UploadTest {
	
	public UploadTest() {
		
	}
	
	@Before
	public void before() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-consumer-test.xml" });
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
		
	}
	
	//@Test
	public void test() {
		UploadOperation operation = new UploadOperation(11L);
		try {
			Date startTime = new Date();
			JSONObject ret = operation.importPoi("");
			System.out.println(ret);
			Date endTime = new Date();
			System.out.println("total time:"+ (endTime.getTime() - startTime.getTime()));
//			System.out.println(UuidUtils.genUuid());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	@Test
	public void testUpload() {
		try {
			long t1 = System.currentTimeMillis();
			//初始化存储图片属性的map
			Map<String, Photo> photoMap=new HashMap<String, Photo>();
			//1.2 解压文件
			String filePath ="F:/upload";
			UploadManager upMan = new UploadManager(0,filePath);
			upMan.setSubtaskId(26);
			UploadResult result = upMan.upload();
			//读取poi文件，导入...
			long t2 = System.currentTimeMillis();
			System.out.println("poi import total time:"+(t2-t1)+"ms.");
			//2.1 
			//读取照片文件，导入hbase
//			CollectorImport.importPhoto(filePath);
			System.out.println("photoMap.size(): "+photoMap.size());
			CollectorImport.importPhoto(photoMap,filePath);
			long t3 = System.currentTimeMillis();
			System.out.println("photo import total time:"+(t3)+"ms.");
			
//			UploadManager upMan = new UploadManager(4127L,"F:\\data\\collector\\poi20_1.txt");
			/*UploadManager upMan = new UploadManager(4127L,"F:\\poi003.txt");
			upMan.setSubtaskId(26);
			UploadResult result = upMan.upload(null);*/
			System.out.println(JSONObject.fromObject(result).toString());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	@Test
	public void testPaUpload() {
		try {
			long t1 = System.currentTimeMillis();
			//1.2 解压文件
			String filePath ="F:/upload";
			PaUploadManager upMan = new PaUploadManager(0,filePath);
			upMan.setSubtaskId(123);
			UploadResult result = upMan.upload();
			//读取poi文件，导入...
			long t2 = System.currentTimeMillis();
			System.out.println("poi import total time:"+(t2-t1)+"ms.");
			
//			UploadManager upMan = new UploadManager(4127L,"F:\\data\\collector\\poi20_1.txt");
			/*UploadManager upMan = new UploadManager(4127L,"F:\\poi003.txt");
			upMan.setSubtaskId(26);
			UploadResult result = upMan.upload(null);*/
			System.out.println(JSONObject.fromObject(result).toString());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
}
