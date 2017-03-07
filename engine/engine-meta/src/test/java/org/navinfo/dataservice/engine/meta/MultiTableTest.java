package org.navinfo.dataservice.engine.meta;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.ResponseUtils;
import com.navinfo.dataservice.engine.meta.ciParaKindword.CiParaKindKeyword;
import com.navinfo.dataservice.engine.meta.scPointAddrAdmin.ScPointAddrAdmin;

public class MultiTableTest {

	@Before
	public void before() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-consumer-datahub-test.xml" });
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	
	@Test
	public void test_SC_POINT_ADDR_ADMIN() {
		try {
			Map<String, Map<String,String>> map= ScPointAddrAdmin.getInstance().addrAdminMap();
			System.out.println(StringUtils.join(map.keySet(), ","));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void test_CI_PARA_KIND_KEYWORD() {
		try {
			Map<String, String> map= CiParaKindKeyword.getInstance().ciParaKindKeywordMap();
			System.out.println(StringUtils.join(map.keySet(), ","));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
}
