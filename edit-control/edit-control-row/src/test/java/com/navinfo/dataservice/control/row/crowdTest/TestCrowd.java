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
			String str = "{\"data\":{ \"REAUDITNAME\": \"盈樾国际12\", \"REAUDITADDRESS\": \"锦业路12\", \"REAUDITPHONE\": \"010-88888888\", \"RECLASSCODE\": \"120101\", \"GEOX\": 1.00083, \"GEOY\": 1.00028, \"GEOX1\": 116.35959, \"GEOY1\": 39.91456, \"GEOX2\": 116.35959, \"GEOY2\": 39.91456, \"GEOX3\": 0, \"GEOY3\": 0, \"GEOX4\": 0, \"GEOY4\": 0, \"FID\": \"01020170526zgq03\", \"EDITHISTORY\": [], \"PHOTO\":  {  \"p1\": \"1/tooopen_23024520.jpg\",  \"p2\": \"1/tooopen_09132386.jpg\" }, \"BATCHTASK_ID\": 0, \"GATHERUSERID\": 78, \"DESCP\": null, \"STATE\": 3}}";
			JSONObject reqJson = JSONObject.fromObject(str);
			String msg = control.release(reqJson);
			
			System.out.println(msg);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testUUid(){
		String uuid = "996c-8bbc-45ba-13e25f979a971490665840966".replace("-", "");
		System.out.println(uuid.length());
	}
}
