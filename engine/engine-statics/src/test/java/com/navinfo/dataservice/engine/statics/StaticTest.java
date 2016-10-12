package com.navinfo.dataservice.engine.statics;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.api.statics.model.SubtaskStatInfo;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.statics.poicollect.PoiCollectMain;
import com.navinfo.dataservice.engine.statics.roadcollect.RoadCollectMain;
import com.navinfo.dataservice.engine.statics.service.StaticsOperation;
import com.navinfo.dataservice.engine.statics.service.StaticsService;

import net.sf.json.JSONObject;

/** 
 * @ClassName: CheckRuleTest
 * @author songdongyan
 * @date 2016年8月20日
 * @Description: CheckRuleTest.java
 */
public class StaticTest {

	/**
	 * 
	 */
	public StaticTest() {
		// TODO Auto-generated constructor stub
	}

	@Before
	public void init() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-test.xml"});
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	
	
	@Test
	public void getStatBySubtask() throws Exception{
		StaticsService staticsService = StaticsService.getInstance();
		SubtaskStatInfo data = staticsService.getStatBySubtask(38);
		SubtaskStatInfo temp = data;
//		List<Integer> gridIds = new ArrayList<Integer>();
//		gridIds.add(60560303);
//		gridIds.add(123456);
//		String poiColName = PoiCollectMain.col_name_grid;
//		String roadColName = RoadCollectMain.col_name_grid;
//		SubtaskStatInfo result = StaticsOperation.getSubtaskStatByGrids(gridIds, poiColName, roadColName);
//		SubtaskStatInfo temp = result;
//		System.out.println("ok");
//		
//		result = StaticsOperation.getSubtaskStatByBlock(1, poiColName, roadColName);
//		temp = result;
	}
	
	@Test
	public void getOpen100TaskIdList() throws Exception{
		StaticsService staticsService = StaticsService.getInstance();
		List<Integer> data = staticsService.getOpen100TaskIdList();
		System.out.println("getOpen100TaskIdList:"+data);
//		List<Integer> gridIds = new ArrayList<Integer>();
//		gridIds.add(60560303);
//		gridIds.add(123456);
//		String poiColName = PoiCollectMain.col_name_grid;
//		String roadColName = RoadCollectMain.col_name_grid;
//		SubtaskStatInfo result = StaticsOperation.getSubtaskStatByGrids(gridIds, poiColName, roadColName);
//		SubtaskStatInfo temp = result;
//		System.out.println("ok");
//		
//		result = StaticsOperation.getSubtaskStatByBlock(1, poiColName, roadColName);
//		temp = result;
	}

}
