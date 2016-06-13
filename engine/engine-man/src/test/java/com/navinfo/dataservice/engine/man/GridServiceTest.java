package com.navinfo.dataservice.engine.man;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.collections.map.MultiValueMap;
import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.engine.man.grid.GridService;

/*
 * @author mayunfei
 * 2016年6月6日
 * 描述：engine-manGridSelectorTest.java
 */
public class GridServiceTest {

	@Before
	public void setUp() throws Exception {
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

}

