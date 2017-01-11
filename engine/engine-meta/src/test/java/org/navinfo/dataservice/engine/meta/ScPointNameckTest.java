package org.navinfo.dataservice.engine.meta;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.api.metadata.model.ScPointNameckObj;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.ResponseUtils;
import com.navinfo.dataservice.engine.meta.area.ScPointAdminArea;
import com.navinfo.dataservice.engine.meta.kindcode.KindCodeSelector;
import com.navinfo.dataservice.engine.meta.patternimage.PatternImageSelector;
import com.navinfo.dataservice.engine.meta.scPointNameck.ScPointNameck;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ScPointNameckTest {
	
	@Before
	public void before() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-consumer-datahub-test.xml" });
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	
	@Test
	public void testScPointNameckTypeD1() {
		try {
			List<ScPointNameckObj> tt = ScPointNameck.getInstance().scPointNameckTypeD1();			
			System.out.println();
		} catch (Exception e) {
			System.out.println(ResponseUtils.assembleFailResult(e.getMessage()));
			e.printStackTrace();
		}

	}
}
