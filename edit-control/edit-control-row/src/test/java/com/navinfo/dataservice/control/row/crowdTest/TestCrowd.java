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
			String str = "{\"data\": {    \"REAUDITNAME\": \"开发用例04\",    \"REAUDITADDRESS\": \"锦业路12\",    \"REAUDITPHONE\": \"010-88888888\",    \"RECLASSCODE\": \"110101\",    \"GEOX\": 116.35961,    \"GEOY\": 39.91454,    \"GEOX1\": 116.35959,    \"GEOY1\": 39.91456,    \"GEOX2\": 116.35859,    \"GEOY2\": 39.91456,    \"GEOX3\": 0,    \"GEOY3\": 0,    \"GEOX4\": 0,    \"GEOY4\": 0,    \"FID\": \"30020170605151823001\",    \"EDITHISTORY\": [{\"newValue\": {\"location\":{ \"latitude\": 31.409682494153515, \"longitude\": 121.42497241170153}},\"oldValue\": {\"location\":{ \"latitude\": 31.4097374341083, \"longitude\": 121.424965706179}}}],    \"PHOTO\": {        \"p1\": \"137ddc9149c511e78c23a4db305c0475.jpg\",        \"p2\": \"284fc1ae49c511e7a6f4a4db305c0475.jpg\"    },    \"BATCHTASK_ID\": 0,    \"GATHERUSERID\": 78,    \"DESCP\": null,    \"STATE\": 1}}";
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
		String photoName = "1qaz2wsx3edc4rfvvfr4cde3xsw2zaq1.jpg";
		System.out.println(photoName.substring(0, photoName.indexOf(".")));
	}
}
