package com.navinfo.dataservice.control.row.quality;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONObject;

public class PoiQualityTest {
	@Before
	public void before() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-consumer-datahub-test.xml" });
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	
	@Test
	public void initQualityData() throws Exception{
		PoiQuality poiQuality = new PoiQuality();
//		int qualityId = 10;
		Geometry geometry = poiQuality.getGeometryByQualityId(10);
		System.out.println(geometry);
	}
	
	//获取质检问题属性值(测试)
	@Test
	public void testQueryInitValueForProblem() throws Exception{
		JSONObject queryInitValueForProblem = QualityService.getInstance().queryInitValueForProblem(3964, 51, 71);
		System.out.println(queryInitValueForProblem);
	}
}
