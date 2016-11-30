package com.navinfo.dataservice.control.column;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.control.column.core.ColumnCoreControl;

import net.sf.json.JSONObject;

public class androidtest {
	
	@Before
	public void before() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-consumer-datahub-test.xml" });
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	
	@Test
	public void test() {
		

		try {
			ColumnCoreControl control = new ColumnCoreControl();

			//control.applyData(0, "poi_address", 123);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	

}
