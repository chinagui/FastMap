package com.navinfo.dataservice.engine.man;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.engine.man.statics.StaticsService;

public class StaticsTest extends InitApplication{

	@Override
	@Before
	public void init() {
		initContext();
	}
	
	@Test
	public void taskOverviewDetail()throws Exception{
		Map<String,Object> data = StaticsService.getInstance().taskOverviewDetail(135);
		System.out.println(data);
	}
	
	@Test
	public void subtaskOverviewDetail()throws Exception{
		Map<String,Object> data = StaticsService.getInstance().subtaskOverviewDetail(279);
		System.out.println(data);
	}
	
}
