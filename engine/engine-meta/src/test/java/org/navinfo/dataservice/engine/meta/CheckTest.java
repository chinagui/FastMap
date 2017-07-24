package org.navinfo.dataservice.engine.meta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.api.metadata.model.ScSensitiveWordsObj;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.ResponseUtils;
import com.navinfo.dataservice.engine.meta.ciParaKindword.CiParaKindKeyword;
import com.navinfo.dataservice.engine.meta.scFmControl.ScFmControl;
import com.navinfo.dataservice.engine.meta.scPointAdminarea.ScPointAdminarea;
import com.navinfo.dataservice.engine.meta.scPointChainCode.ScPointChainCode;
import com.navinfo.dataservice.engine.meta.scPointCode2Level.ScPointCode2Level;
import com.navinfo.dataservice.engine.meta.scPointFocus.ScPointFocus;
import com.navinfo.dataservice.engine.meta.scPointFoodtype.ScPointFoodtype;
import com.navinfo.dataservice.engine.meta.scPointKindNew.ScPointKindNew;
import com.navinfo.dataservice.engine.meta.scPointKindRule.ScPointKindRule;
import com.navinfo.dataservice.engine.meta.scPointPoiCodeNew.ScPointPoiCodeNew;
import com.navinfo.dataservice.engine.meta.scSensitiveWords.ScSensitiveWords;
import com.navinfo.dataservice.engine.meta.service.MetadataApiImpl;

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
			Map<String, Integer> map = ScPointPoiCodeNew.getInstance().searchScPointPoiCodeNew();
			Map<String, String> kindNameByKindCode = ScPointPoiCodeNew.getInstance().getKindNameByKindCode();
			for (String key : map.keySet()) {
				System.out.println("kindCode---"+key+"kindUse"+map.get(key));
			}
			for (String key : kindNameByKindCode.keySet()) {
				System.out.println("kindCode---"+key+"kindUse"+kindNameByKindCode.get(key));
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
	
	@Test
	public void test03() {
		try {
			List<Map<String, String>> scPointKindNew5List = ScPointKindNew.getInstance().scPointKindNew5List();
			System.out.println(scPointKindNew5List.toString());
			List<Map<String, String>> scPointKindNewChainKind5Map = ScPointKindNew.getInstance().scPointKindNewChainKind5Map();
			List<Map<String, String>> scPointKindNewChainKind6Map = ScPointKindNew.getInstance().scPointKindNewChainKind6Map();
			System.out.println(scPointKindNewChainKind5Map.toString());
			System.out.println(scPointKindNewChainKind6Map.toString());
		} catch (Exception e) {
			System.out.println(ResponseUtils.assembleFailResult(e.getMessage()));
			e.printStackTrace();
		}
	}
	
	@Test
	public void test04() {
		try {
			List<Map<String, Object>> scPointKindNew5List = ScPointKindRule.getInstance().scPointKindRule();
			System.out.println(scPointKindNew5List.toString());
		} catch (Exception e) {
			System.out.println(ResponseUtils.assembleFailResult(e.getMessage()));
			e.printStackTrace();
		}
	}
	
	@Test
	public void test05() {
		try {
			String kindCode = "120101";
			Map<String, String> scPointKindNew5List = ScPointCode2Level.getInstance().scPointCode2Level();
			System.out.println(scPointKindNew5List.toString());
		} catch (Exception e) {
			System.out.println(ResponseUtils.assembleFailResult(e.getMessage()));
			e.printStackTrace();
		}
	}
	
	@Test
	public void test06() {
		try {
			Map<String, List<String>> scPointKindNew5List = CiParaKindKeyword.getInstance().ciParaKindKeywordMap();
			System.out.println(scPointKindNew5List.toString());
		} catch (Exception e) {
			System.out.println(ResponseUtils.assembleFailResult(e.getMessage()));
			e.printStackTrace();
		}
	}
	@Test
	public void test07() {
		try {
			Map<String, String> scPointKindNew5List = ScPointKindRule.getInstance().scPointKindRule5();
			Map<String, String> chainNameMap = ScPointChainCode.getInstance().getChainNameMap();
			Map<String, String> foodtypeNameMap = ScPointFoodtype.getInstance().getFoodtypeNameMap();
			System.out.println(scPointKindNew5List.toString());
			System.out.println(chainNameMap.toString());
			System.out.println(foodtypeNameMap.toString());
		} catch (Exception e) {
			System.out.println(ResponseUtils.assembleFailResult(e.getMessage()));
			e.printStackTrace();
		}
	}
	
	@Test
	public void test08() {
		try {
			Map<String, Map<String, String>> data = ScPointAdminarea.getInstance().scPointAdminareaByAdminId();
			System.out.println(data.toString());
		} catch (Exception e) {
			System.out.println(ResponseUtils.assembleFailResult(e.getMessage()));
			e.printStackTrace();
		}
	}
	
	@Test
	public void test09() {
		try {
			MetadataApi metadataApi = new MetadataApiImpl();
			Map<String, Map<String, String>> map = metadataApi.scPointAdminareaByAdminId();
			
			System.out.println(map.toString());
		} catch (Exception e) {
			System.out.println(ResponseUtils.assembleFailResult(e.getMessage()));
			e.printStackTrace();
		}
	}
}
