package com.navinfo.dataservice.control.app;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.control.app.upload.UploadOperation;

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
		UploadOperation operation = new UploadOperation();
		try {
			operation.importPoi("F://poi.txt");
//			System.out.println(UuidUtils.genUuid());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
