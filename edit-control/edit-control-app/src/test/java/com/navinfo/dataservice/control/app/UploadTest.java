package com.navinfo.dataservice.control.app;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.control.app.upload.UploadOperation;

import net.sf.json.JSONObject;

public class UploadTest {
	@Before
	public void before() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-consumer-datahub-test.xml" });
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
		
	}
	
	@Test
	public void test() {
		UploadOperation operation = new UploadOperation(11L);
		try {
			Date startTime = new Date();
			JSONObject ret = operation.importPoi("F://testEditStatus.txt");
			System.out.println(ret);
			Date endTime = new Date();
			System.out.println("total time:"+ (endTime.getTime() - startTime.getTime()));
//			System.out.println(UuidUtils.genUuid());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
