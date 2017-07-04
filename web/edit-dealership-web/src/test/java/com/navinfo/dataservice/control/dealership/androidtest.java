package com.navinfo.dataservice.control.dealership;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.token.AccessToken;
//import com.navinfo.dataservice.control.dealership.service.DataPrepareService;
import com.navinfo.dataservice.control.dealership.service.DataPrepareService;
import com.navinfo.dataservice.control.dealership.service.model.ExpIxDealershipResult;

import net.sf.json.JSONObject;

public class androidtest {
	
	@Before
	public void before() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-consumer-datahub-test.xml" });
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	
	@Test
	public void test() {
		

		try {
			DataPrepareService control = DataPrepareService.getInstance();
			List<ExpIxDealershipResult> dealerBrandList = control.searchTableDiff("");
			

			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	

}
