package org.navinfo.dataservice.engine.meta;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.ResponseUtils;
import com.navinfo.dataservice.engine.meta.area.ScPointAdminArea;
import com.navinfo.dataservice.engine.meta.kindcode.KindCodeSelector;
import com.navinfo.dataservice.engine.meta.patternimage.PatternImageSelector;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class KindCodeTest {
	
	@Before
	public void before() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-consumer-datahub-test.xml" });
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	
	@Test
	public void testQueryTopKindInfo() {
		try {
			
			PatternImageSelector selector = new PatternImageSelector();

			byte[] data = selector.getById("ddd");


			if (data.length == 0) {
				throw new Exception("id值不存在");
			}
			
			System.out.println(data);
		} catch (Exception e) {
			System.out.println(ResponseUtils.assembleFailResult(e.getMessage()));
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
	
	@Test
	public void testAdmin() {
		ScPointAdminArea admin = new ScPointAdminArea();
		try {
			JSONObject ret = admin.getAdminArea(100, 1, "", "");
			System.out.println(ret);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
