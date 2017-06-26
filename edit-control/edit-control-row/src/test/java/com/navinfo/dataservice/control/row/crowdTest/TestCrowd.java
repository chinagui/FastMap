package com.navinfo.dataservice.control.row.crowdTest;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.JtsGeometryFactory;
import com.navinfo.dataservice.control.row.crowds.RowCrowdsControl;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.vividsolutions.jts.geom.Geometry;

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
	
	@Test
	public void testDbId() throws Exception{
		String polygon = "POLYGON((116.28429651260376 39.9897471840457,116.50264978408813 39.990799335838034,116.50676965713501 39.902362098539705,116.39003992080688 39.825413103424786,116.21013879776001 39.84966661865515,116.28429651260376 39.9897471840457))";
		Geometry g = JtsGeometryFactory.read(polygon);
		Set<String> results = CompGeometryUtil.geo2GridsWithoutBreak(g);
		Iterator it = results.iterator();
		while(it.hasNext()){
			String grid = (String) it.next();
			String dbId = RowCrowdsControl.getDailyDbId(grid);
			System.out.println(dbId);
			break;
		}
	}
	
	@Test
	public void testGetTelephone() throws SQLException{
		double x = 116.4300328;
		double y = 39.85880944;
		RowCrowdsControl control = new RowCrowdsControl();
		JSONObject result = control.getTelephone(x, y);
		System.out.println(result);
	}
}
