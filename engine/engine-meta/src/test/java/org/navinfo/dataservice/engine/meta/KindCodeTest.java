package org.navinfo.dataservice.engine.meta;

import net.sf.json.JSONArray;

import org.junit.Before;
import org.junit.Test;
import org.navinfo.dataservice.engine.meta.kindcode.KindCodeSelector;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;

public class KindCodeTest {
	
	@Before
	public void before() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-consumer-datahub.xml" });
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	
	@Test
	public void testQueryTopKindInfo() {
		try {
			
			KindCodeSelector selector = new KindCodeSelector();

			JSONArray jsonObject = selector.queryTopKindInfo();


			System.out.println(jsonObject);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	@Test
	public void testQueryMediumKind() {
		try {
			
			String topId = "21";

			KindCodeSelector selector = new KindCodeSelector();

			JSONArray data = selector.queryMediumKindInfo(topId);


			System.out.println(data);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	@Test
	public void testQueryKindById() {
		try {
			
			String topId = "21";

			String mediumId ="02";

			int region = 0;

			KindCodeSelector selector = new KindCodeSelector();

			JSONArray data = selector.queryKindInfo(topId, mediumId, region);

			System.out.println(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testQueryKind() {
		try {
			int region = 0;

			KindCodeSelector selector = new KindCodeSelector();

			JSONArray data = selector.queryKindInfo(region);

			System.out.println(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
