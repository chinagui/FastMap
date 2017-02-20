package org.navinfo.dataservice.engine.meta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.api.metadata.model.ScSensitiveWordsObj;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.ResponseUtils;
import com.navinfo.dataservice.engine.meta.scFmControl.ScFmControl;
import com.navinfo.dataservice.engine.meta.scPointFocus.ScPointFocus;
import com.navinfo.dataservice.engine.meta.scPointPoiCodeNew.ScPointPoiCodeNew;
import com.navinfo.dataservice.engine.meta.scSensitiveWords.ScSensitiveWords;

public class CheckTest {

	@Before
	public void before() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-consumer-datahub-test.xml" });
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	
	@Test
	public void test01() {
		try {
			List<String> list = new ArrayList<String>();
			list.add("230227");
			list.add("210105");
			Map<String, Integer> map = ScPointPoiCodeNew.getInstance().searchScPointPoiCodeNew(list);
			for (String key : map.keySet()) {
				System.out.println("kindCode---"+key+"kindUse"+map.get(key));
			}
		} catch (Exception e) {
			System.out.println(ResponseUtils.assembleFailResult(e.getMessage()));
			e.printStackTrace();
		}

	}
	
	@Test
	public void test02() {
		try {
			Map<String, Integer> map = ScPointFocus.getInstance().searchScPointFocus("0027061004YCJ01269");
			for (String key : map.keySet()) {
				System.out.println("kindCode---"+key+"kindUse"+map.get(key));
			}
			Map<String, Integer> map1 = ScFmControl.getInstance().searchScFmControl("210204");
			for (String key : map1.keySet()) {
				System.out.println("kindCode---"+key+"kindUse"+map1.get(key));
			}
		} catch (Exception e) {
			System.out.println(ResponseUtils.assembleFailResult(e.getMessage()));
			e.printStackTrace();
		}

	}
}
