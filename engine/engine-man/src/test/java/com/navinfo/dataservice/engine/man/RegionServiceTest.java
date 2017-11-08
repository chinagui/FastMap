package com.navinfo.dataservice.engine.man;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.api.man.model.Region;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.man.region.RegionService;

/*
 * @author MaYunFei
 * 2016年6月25日
 * 描述：engine-manRegionServiceTest.java
 */
public class RegionServiceTest {

	@Before
	public void before(){
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(  
                new String[] { "dubbo-consumer.xml"}); 
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}

	@Test
	public void testQueryRegionWithGrids() throws Exception {
		RegionService selector = RegionService.getInstance();
		List<Integer> gridList = new ArrayList<Integer>();
		for (int i=0;i<2000;i++){
			gridList.add(39550711);
			gridList.add(3502401);
		}
		List<Region> regionList = (selector.queryRegionWithGrids(gridList));
		System.out.println(regionList);
	}

}

