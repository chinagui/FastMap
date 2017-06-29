package com.navinfo.dataservice.engine.man;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Program;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.api.man.model.Task;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.json.JsonOperation;
import com.navinfo.dataservice.commons.util.TimestampUtils;
import com.navinfo.dataservice.engine.man.grid.GridService;
import com.navinfo.dataservice.engine.man.program.ProgramService;
import com.navinfo.dataservice.engine.man.service.ManApiImpl;
import com.navinfo.dataservice.engine.man.subtask.SubtaskService;
import com.navinfo.dataservice.engine.man.task.TaskService;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.exception.ServiceException;
import com.navinfo.navicommons.geo.computation.GridUtils;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.util.GeometryTransformer;
import com.vividsolutions.jts.io.ParseException;

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
