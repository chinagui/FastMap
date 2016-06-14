package org.navinfo.dataservice.engine.meta.chain;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class FocusSelectorTest {
	
	@Before
	public void before() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-consumer-datahub-test.xml" });
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	
	@Test
	public void testLoadPoiNum() {
		try {
			FocusSelector selector = new FocusSelector();

			JSONArray jsonObject = selector.getPoiNum();

			System.out.println(jsonObject);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
