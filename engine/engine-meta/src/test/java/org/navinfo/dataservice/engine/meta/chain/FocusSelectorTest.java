package org.navinfo.dataservice.engine.meta.chain;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.meta.chain.ChainSelector;
import com.navinfo.dataservice.engine.meta.chain.FocusSelector;

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
	
	@Test
	public void testGetChain()
	{
		try {
			ChainSelector selector = new ChainSelector();
			
			String kindCode =null;	

			JSONObject data = selector.getChainByKindCode(kindCode);
			
			System.out.println(data);			
			
			kindCode ="210204";	
			
			data = selector.getChainByKindCode(kindCode);
			
			if(kindCode!=null&&data.has(kindCode))
			{
				JSONArray array = data.getJSONArray(kindCode);
				
				System.out.println(array);
			}	
			
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
