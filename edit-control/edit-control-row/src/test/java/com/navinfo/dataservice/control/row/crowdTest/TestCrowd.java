package com.navinfo.dataservice.control.row.crowdTest;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.control.row.crowds.RowCrowdsControl;

import net.sf.json.JSONObject;

public class TestCrowd {
	@Before
	public void before() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-consumer-datahub-test.xml" });
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	
	@Test
	public void testCrowdData2Day() {

		try {
			RowCrowdsControl control = new RowCrowdsControl();
			String str = "{\"data\":{ \"REAUDITNAME\": \"开发自测\", \"REAUDITADDRESS\": \"锦业路\", \"REAUDITPHONE\": \"029-88888888\", \"RECLASSCODE\": \"110101\", \"GEOX\": 116.24567, \"GEOY\": 40.07274, \"FID\": \"20160511QB00000403\", \"EDITHISTORY\":  [    {   \"newValue\": {\"kindCode\": \"110101\"},   \"oldValue\": {\"kindCode\": \"0\"}  } ], \"PHOTO\":  {  \"p1\": \"0/00000000-0a7e-6bad-ffff-fffff1d9b39b1492249000479.jpg\",  \"p2\": \"0/00000000-0a7e-6bad-ffff-fffff1d9b39b1492249200137.jpg\",  \"p3\": \"0/00000000-0a7e-6bad-ffff-fffff1d9b39b1492249167358.jpg\" }, \"BATCHTASK_ID\": 235, \"GATHERUSERID\": 78, \"DESCP\": \"test\", \"STATE\": 3}}";
			JSONObject reqJson = JSONObject.fromObject(str);
			String msg = control.release(reqJson);
			
			System.out.println(msg);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
