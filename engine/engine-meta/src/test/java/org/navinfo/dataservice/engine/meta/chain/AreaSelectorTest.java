package org.navinfo.dataservice.engine.meta.chain;

import org.junit.Before;
import org.junit.Test;
import org.navinfo.dataservice.engine.meta.area.ScPointAdminArea;
import org.navinfo.dataservice.engine.meta.kindcode.KindCodeSelector;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class AreaSelectorTest {
	
	@Before
	public void before() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-consumer-datahub-test.xml" });
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	
	@Test
	public void testProvince() {
		try {
			ScPointAdminArea selector = new ScPointAdminArea();

			JSONArray jsonObject = selector.searchByProvince("陕西省");

			System.out.println(jsonObject);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	@Test
	public void testTelLength() {
		try {
			KindCodeSelector selector = new KindCodeSelector();

			JSONObject len = selector.searchkindLevel("210301");

			System.out.println(len+"----------------------");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
