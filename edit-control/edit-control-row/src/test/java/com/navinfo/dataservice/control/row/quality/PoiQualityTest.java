package com.navinfo.dataservice.control.row.quality;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;

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
	}
	
	//获取质检问题属性值(测试)
	@Test
	public void testQueryInitValueForProblem() throws Exception{
		JSONObject queryInitValueForProblem = QualityService.getInstance().queryInitValueForProblem(3964, 22623, 73);
		System.out.println(queryInitValueForProblem);
	}
	
	//poi质检问题操作（新增、修改、删除）(测试)
	@Test
	public void testOperateProblem() throws Exception {
//		String parameter = "{\"command\":\"DELETE\",\"problemNum\":\"aaaa\"}";
//		String parameter = "{\"command\":\"UPDATE\",\"data\":{\"problemNum\":\"222\",\"checkMode\":\"质检方式\",\"group\":\"ddddddd\",\"classBottom\":\"\",\"problemType\":\"问题类型\",\"problemPhenomenon\":\"问题现象\",\"problemLevel\":\"问题等级\",\"problemDescription\":\"问题描述\",\"intialCause\":\"初步原因\",\"rootCause\":\"深度原因\",\"memo\":\"备注\",\"version\":\"版本号\"}}";
//		String parameter = "{\"command\":\"UPDATE\",\"data\":{\"problemNum\":\"222\",\"checkMode\":\"质检\",\"group\":\"fffffffffff\",\"problemType\":\"问题类型\",\"problemPhenomenon\":\"\",\"problemLevel\":\"问题等级\",\"problemDescription\":\"问题描述\",\"intialCause\":\"初步原因\",\"rootCause\":\"深度原因\",\"memo\":\"备注\",\"version\":\"版本号\"}}";
		String parameter = "{\"command\":\"ADD\",\"data\":{\"group\":\"队名ccc\",\"province\":\"北京市\",\"subtaskId\":174,\"level\":\"aa\",\"meshId\":666666,\"poiNum\":\"IDCode设施id\",\"kindCode\":\"分类代码\",\"classTop\":\"大分类\",\"classMedium\":\"中分类\",\"classBottom\":\"小分类\",\"problemType\":\"问题分类\",\"problemPhenomenon\":\"问题现象\",\"problemDescription\":\"问题描述\",\"intialCause\":\"初步原因\",\"rootCause\":\"深度原因\",\"collectorUser\":\"bbb\",\"collectorTime\":\"2016.06.06\",\"checkMode\":\"质检方式\",\"confirmUser\":\"确认人\",\"version\":\"版本号\",\"problemLevel\":\"问题等级\",\"memo\":\"备注\"}}";
		JSONObject dataJson = JSONObject.fromObject(parameter);
		QualityService.getInstance().operateProblem(0L, dataJson);
	}
	
	//poi质检问题查看(测试)
	@Test
	public void testQueryProblemList() throws Exception {
		String parameter = "{\"poiNum\":\"0010060820MQ100633\",\"classTop\":\"POI\",\"subtaskId\":73}";
		JSONObject dataJson = JSONObject.fromObject(parameter);
		System.out.println(QualityService.getInstance().queryProblemList(dataJson));
	}
}
