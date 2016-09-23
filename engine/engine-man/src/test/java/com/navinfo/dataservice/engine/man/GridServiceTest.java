package com.navinfo.dataservice.engine.man;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.collections.map.MultiValueMap;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.man.grid.GridService;

/*
 * @author mayunfei
 * 2016年6月6日
 * 描述：engine-manGridSelectorTest.java
 */
public class GridServiceTest {

	@Before
	public void before(){
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(  
                new String[] { "dubbo-consumer-datahub-test.xml"}); 
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}

	@Test
	public void testQueryRegionGridMapping() throws Exception {
		GridService selector = GridService.getInstance();
		List<Integer> gridList = new ArrayList<Integer>();
		gridList.add(39550711);
		gridList.add(3502401);
		MultiValueMap regionGridMapping = (MultiValueMap) (selector.queryRegionGridMapping(gridList));
		Assert.assertNotNull(regionGridMapping);
		Assert.assertTrue(regionGridMapping.containsValue(39550711));
	}

//	public void testQueryRegionGridMappingOfSubtasks() throws Exception {
//		GridService selector = GridService.getInstance();
//		List<Integer> gridList = new ArrayList<Integer>();
//		gridList.add(34);
//		gridList.add(35);
//		MultiValueMap regionGridMapping = (MultiValueMap) (selector.queryRegionGridMappingOfSubtasks(gridList));
//		Assert.assertNotNull(regionGridMapping);
//	}

}

