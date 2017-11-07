package com.navinfo.dataservice.engine.man;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.engine.man.program.ProgramService;
import com.navinfo.navicommons.database.Page;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class programTest extends InitApplication{
	
	@Test
	public void testPushMsg() throws Exception
	{
		JSONArray programIds=new JSONArray();
		programIds.add(191);
		//long userId=2;
		String msg=ProgramService.getInstance().pushMsg(0, programIds);
		System.out.println(msg);
	}
	
	@Test
	public void testList() throws Exception
	{
		//long userId=2;
		Page msg=ProgramService.getInstance().list(1, 2, null, null, 1, 15);
		System.out.println(msg);
	}
	
	@Test
	public void testUnPlanQualitylist() throws Exception
	{
		JSONObject data = ProgramService.getInstance().unPlanQualitylist();
		System.out.println(data);
	}

	@Override
	@Before
	public void init() {
		initContext();
	}
	
	//获取待规划子任务的项目列表（测试）
	@Test
	public void testUnPlanSubtasklist() throws Exception
	{
		JSONObject data = ProgramService.getInstance().unPlanSubtasklist();
		System.out.println(data);
	}
	
}
