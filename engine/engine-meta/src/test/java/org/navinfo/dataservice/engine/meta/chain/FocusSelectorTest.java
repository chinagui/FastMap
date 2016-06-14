package org.navinfo.dataservice.engine.meta.chain;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;

import net.sf.json.JSONArray;

public class FocusSelectorTest {
	
	@Before
	public void before() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-consumer-datahub-test.xml" });
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	
	public void testLoadPoiNum() {
		try {
			FocusSelector selector = new FocusSelector();

			JSONArray jsonObject = selector.getPoiNum();

			System.out.println(jsonObject);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public void testGetChain()
	{
		try {
			ChainSelector selector = new ChainSelector();

			JSONArray jsonObject = selector.getChainByKindCode("210204");

			System.out.println(jsonObject);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	 
	@Test
	public void testGetLevelByChain()
	{
		try {
			ChainSelector selector = new ChainSelector();

			String jsonObject = selector.getLevelByChain("3347", "110101");

			System.out.println(jsonObject);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
