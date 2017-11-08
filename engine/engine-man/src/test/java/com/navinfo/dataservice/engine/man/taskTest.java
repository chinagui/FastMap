package com.navinfo.dataservice.engine.man;

import java.sql.Clob;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Program;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.TimestampUtils;
import com.navinfo.dataservice.engine.man.program.ProgramService;
import com.navinfo.dataservice.engine.man.service.ManApiImpl;
import com.navinfo.dataservice.engine.man.subtask.SubtaskService;
import com.navinfo.dataservice.engine.man.task.TaskService;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;
import com.navinfo.navicommons.geo.computation.GridUtils;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class taskTest extends InitApplication{

	@Test
	public void taskTestCreate() throws Exception {
		try {
			// TODO Auto-generated constructor stub
			String parameter = "{\"tasks\":[{\"name\":\"天津市天津市东丽区郊区城区_20170713\",\"blockId\":652,\"programId\":317,\"workKind\":[],\"lot\":0,\"poiPlanTotal\":0,\"roadPlanTotal\":0,\"producePlanStartDate\":\"20170713\",\"producePlanEndDate\":\"20170713\",\"planStartDate\":\"20170713\",\"planEndDate\":\"20170713\",\"type\":0},{\"name\":\"天津市天津市东丽区郊区城区_20170713\",\"blockId\":652,\"programId\":317,\"lot\":0,\"poiPlanTotal\":0,\"roadPlanTotal\":0,\"producePlanStartDate\":\"20170713\",\"producePlanEndDate\":\"20170713\",\"planStartDate\":\"20170713\",\"planEndDate\":\"20170713\",\"type\":2}]}";
			JSONObject dataJson = JSONObject.fromObject(parameter);				
			TaskService.getInstance().create(0, dataJson);
		} catch (Exception e) {
			e.printStackTrace();
		}
				
	}
	
	@Test
	public void taskTestQuery() throws Exception {
		try {
			// TODO Auto-generated constructor stub
			String parameter = "{\"tasks\":[{\"name\":\"天津市天津市东丽区郊区城区_20170713\",\"blockId\":652,\"programId\":317,\"workKind\":[],\"lot\":0,\"poiPlanTotal\":0,\"roadPlanTotal\":0,\"producePlanStartDate\":\"20170713\",\"producePlanEndDate\":\"20170713\",\"planStartDate\":\"20170713\",\"planEndDate\":\"20170713\",\"type\":0},{\"name\":\"天津市天津市东丽区郊区城区_20170713\",\"blockId\":652,\"programId\":317,\"lot\":0,\"poiPlanTotal\":0,\"roadPlanTotal\":0,\"producePlanStartDate\":\"20170713\",\"producePlanEndDate\":\"20170713\",\"planStartDate\":\"20170713\",\"planEndDate\":\"20170713\",\"type\":2}]}";
			JSONObject dataJson = JSONObject.fromObject(parameter);				
			TaskService.getInstance().query(22);
		} catch (Exception e) {
			e.printStackTrace();
		}
				
	}
	@Test
	public void taskTestUpdate() throws Exception {
		// TODO Auto-generated constructor stub
		String parameter = "{\"taskId\":2366,\"descp\":\"\",\"name\":\"天津市天津市东丽区郊区城区_20170713\",\"blockId\":652,\"programId\":317,\"workKind\":[],\"lot\":0,\"poiPlanTotal\":0,\"roadPlanTotal\":0,\"producePlanStartDate\":\"20170713\",\"producePlanEndDate\":\"20170713\",\"planStartDate\":\"20170713\",\"planEndDate\":\"20170713\",\"type\":0}";
		if (StringUtils.isEmpty(parameter)){
			throw new IllegalArgumentException("parameter参数不能为空。");
		}		
		JSONObject dataJson = JSONObject.fromObject(parameter);			
		if(dataJson==null){
			throw new IllegalArgumentException("parameter参数不能为空。");
		}
		TaskService.getInstance().update(0, dataJson);			
	}
	
	@Test
	public void testGetByTask() throws ServiceException
	{
		JSONObject dataJson = JSONObject.fromObject("{\"subtaskId\":\"22\"}");
		if(dataJson==null){
			throw new IllegalArgumentException("parameter参数不能为空。");
		}
		
		Subtask bean = (Subtask)JSONObject.toBean(dataJson, Subtask.class);
		
//		Subtask subtask = SubtaskService.getInstance().query(bean);	
//		
//		//根据需要的返回字段拼装结果
//		HashMap<String, Object> data = new HashMap<String, Object>();
//		if(subtask!=null&&subtask.getSubtaskId()!=null){
//			data.put("subtaskId", subtask.getSubtaskId());
//			data.put("stage", subtask.getStage());
//			data.put("type", subtask.getType());
//			data.put("planStartDate", subtask.getPlanStartDate());
//			data.put("planEndDate", subtask.getPlanEndDate());
//			data.put("descp", subtask.getDescp());
//			data.put("name", subtask.getName());
//			data.put("gridIds", subtask.getGridIds());
//			data.put("dbId", subtask.getDbId());
//			data.put("geometry", subtask.getGeometry());
//		}
//		else{
//		}
//		
//		JSONObject result = JsonOperation.beanToJson(data);
//		
//		System.out.println(result);
	}
	
	@Test
	public void testReOpen() throws ServiceException
	{
		TaskService.getInstance().reOpen(Long.valueOf(0), 514);
	}
	
	@Test
	public void testClose() throws Exception
	{
		TaskService.getInstance().close(2190, 10001, "", "");
	}
	
	@Test
	public void testList() throws Exception
	{
		String parameter="{\"condition\":{\"sMarkStatus\":[5],\"programId\":294},\"pageNum\":1,\"pageSize\":15,\"snapshot\":0}";
		JSONObject dataJson = JSONObject.fromObject(parameter);			
		JSONObject condition = new JSONObject();	
		if(dataJson.containsKey("condition")){
			condition=dataJson.getJSONObject("condition");
		}			
		int curPageNum= 1;//默认为第一页
		if (dataJson.containsKey("pageNum")){
			curPageNum = dataJson.getInt("pageNum");
		}
		int curPageSize= 20;//默认为20条记录/页
		if (dataJson.containsKey("pageSize")){
			curPageSize = dataJson.getInt("pageSize");
		}
		Page data = TaskService.getInstance().list(condition,curPageNum,curPageSize);
		System.out.println(data.getResult());
	}
	
	@Test
	public void testCreateCmsProgress() throws Exception
	{
		Connection conn= DBConnector.getInstance().getManConnection();
		JSONObject parameter=new JSONObject();
		parameter.put("TEST", 1);
		TaskService.getInstance().createCmsProgress(conn,514,1,parameter);
		DbUtils.commitAndCloseQuietly(conn);
	}
	
	@Test
	public void testPushMsg() throws Exception
	{
		JSONArray taskIds=new JSONArray();
		taskIds.add(77);
		String message = TaskService.getInstance().taskPushMsg(0, taskIds);
		System.out.println(message);
	}

	@Override
	@Before
	public void init() {
		initContext();
	}
	
	public void testQueryAdmin() throws ServiceException
	{
		SubtaskService subService = SubtaskService.getInstance(); 
		
		subService.queryAdminIdBySubtask(162);
	}
	
	@Test
	public void quick2Mid()throws Exception{
		Connection conn= DBConnector.getInstance().getManConnection();
		int quickProgramId=60;
		JSONObject condition=new JSONObject();
		JSONArray programIds=new JSONArray();
		programIds.add(quickProgramId);
		condition.put("programIds",programIds);
		List<Program> programList = ProgramService.getInstance().queryProgramTable(conn, condition);
		Program quickProgram = programList.get(0);
		Program program=new Program();
		program.setName("test");
		program.setCityId(39);
		program.setType(1);
		program.setDescp("快线项目："+quickProgram.getName()+"转中线");
		program.setCollectPlanStartDate(quickProgram.getCollectPlanStartDate());
		program.setCollectPlanEndDate(quickProgram.getCollectPlanEndDate());
		program.setMonthEditPlanStartDate(TimestampUtils.addDays(quickProgram.getProducePlanEndDate(),1));
		program.setMonthEditPlanEndDate(TimestampUtils.addDays(program.getMonthEditPlanStartDate(),1));
		program.setProducePlanStartDate(TimestampUtils.addDays(program.getMonthEditPlanEndDate(),1));
		program.setProducePlanEndDate(TimestampUtils.addDays(program.getProducePlanEndDate(),10));
		program.setPlanStartDate(quickProgram.getCollectPlanStartDate());
		program.setPlanEndDate(program.getProducePlanEndDate());
		program.setCreateUserId(0);
		int now=ProgramService.getInstance().create(conn,program);
		System.out.println(now);
	}
	
	@Test
	public void test() throws ServiceException
	{
		try {
			Geometry geo = GridUtils.grid2Geometry("60566122");
			System.out.println(GeoTranslator.jts2Wkt(geo));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testUnPlanQualitylist() throws Exception
	{
		try {
			System.out.println(TaskService.getInstance().unPlanQualitylist(57));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	//获取待规划子任务的任务列表
	@Test
	public void testUnPlanSubtasklist() throws Exception
	{
		JSONObject data = TaskService.getInstance().unPlanSubtasklist(71);
		System.out.println(data);
	}
	
	@Test
	public void testBatchMidTaskByTaskId() throws Exception
	{
		int data = TaskService.getInstance().batchMidTaskByTaskId(135);
		System.out.println(data);
	}	
	
	@Test
	public void testGeo() throws Exception
	{
		String wkt="POLYGON ((118.125 33.02083,118.125 33.04167,118.125 33.0625,118.125 33.08333,118.125 33.10417,118.125 33.125,118.125 33.14583,118.125 33.16667,118.15625 33.16667,118.1875 33.16667,118.21875 33.16667,118.25 33.16667,118.28125 33.16667,118.3125 33.16667,118.34375 33.16667,118.375 33.16667,118.40625 33.16667,118.4375 33.16667,118.46875 33.16667,118.5 33.16667,118.5 33.1875,118.5 33.20833,118.5 33.22917,118.5 33.25,118.53125 33.25,118.5625 33.25,118.59375 33.25,118.625 33.25,118.625 33.27083,118.625 33.29167,118.625 33.3125,118.625 33.33333,118.65625 33.33333,118.6875 33.33333,118.71875 33.33333,118.75 33.33333,118.75 33.35417,118.75 33.375,118.75 33.39583,118.75 33.41667,118.75 33.4375,118.75 33.45833,118.75 33.47917,118.75 33.5,118.75 33.52083,118.75 33.54167,118.75 33.5625,118.75 33.58333,118.75 33.60417,118.75 33.625,118.75 33.64583,118.75 33.66667,118.75 33.6875,118.75 33.70833,118.75 33.72917,118.75 33.75,118.75 33.77083,118.75 33.79167,118.75 33.8125,118.75 33.83333,118.78125 33.83333,118.8125 33.83333,118.84375 33.83333,118.875 33.83333,118.875 33.85417,118.875 33.875,118.875 33.89583,118.875 33.91667,118.90625 33.91667,118.9375 33.91667,118.96875 33.91667,119 33.91667,119 33.9375,119 33.95833,119 33.97917,119 34,119.03125 34,119.0625 34,119.09375 34,119.125 34,119.125 34.02083,119.125 34.04167,119.125 34.0625,119.125 34.08333,119.15625 34.08333,119.1875 34.08333,119.21875 34.08333,119.25 34.08333,119.25 34.0625,119.25 34.04167,119.25 34.02083,119.25 34,119.28125 34,119.3125 34,119.34375 34,119.375 34,119.40625 34,119.4375 34,119.46875 34,119.5 34,119.53125 34,119.5625 34,119.59375 34,119.625 34,119.625 33.97917,119.625 33.95833,119.625 33.9375,119.625 33.91667,119.625 33.89583,119.625 33.875,119.625 33.85417,119.625 33.83333,119.59375 33.83333,119.5625 33.83333,119.53125 33.83333,119.5 33.83333,119.5 33.8125,119.5 33.79167,119.5 33.77083,119.5 33.75,119.5 33.72917,119.5 33.70833,119.5 33.6875,119.5 33.66667,119.5 33.64583,119.5 33.625,119.5 33.60417,119.5 33.58333,119.5 33.5625,119.5 33.54167,119.5 33.52083,119.5 33.5,119.53125 33.5,119.5625 33.5,119.59375 33.5,119.625 33.5,119.625 33.47917,119.625 33.45833,119.625 33.4375,119.625 33.41667,119.59375 33.41667,119.5625 33.41667,119.53125 33.41667,119.5 33.41667,119.46875 33.41667,119.4375 33.41667,119.40625 33.41667,119.375 33.41667,119.375 33.39583,119.375 33.375,119.375 33.35417,119.375 33.33333,119.34375 33.33333,119.3125 33.33333,119.28125 33.33333,119.25 33.33333,119.25 33.3125,119.25 33.29167,119.25 33.27083,119.25 33.25,119.21875 33.25,119.1875 33.25,119.15625 33.25,119.125 33.25,119.125 33.22917,119.125 33.20833,119.125 33.1875,119.125 33.16667,119.15625 33.16667,119.1875 33.16667,119.21875 33.16667,119.25 33.16667,119.25 33.14583,119.25 33.125,119.25 33.10417,119.25 33.08333,119.28125 33.08333,119.3125 33.08333,119.34375 33.08333,119.375 33.08333,119.375 33.0625,119.375 33.04167,119.375 33.02083,119.375 33,119.375 32.97917,119.375 32.95833,119.375 32.9375,119.375 32.91667,119.375 32.89583,119.375 32.875,119.375 32.85417,119.375 32.83333,119.34375 32.83333,119.3125 32.83333,119.28125 32.83333,119.25 32.83333,119.21875 32.83333,119.1875 32.83333,119.15625 32.83333,119.125 32.83333,119.125 32.8125,119.125 32.79167,119.125 32.77083,119.125 32.75,119.09375 32.75,119.0625 32.75,119.03125 32.75,119 32.75,119 32.72917,119 32.70833,119 32.6875,119 32.66667,118.96875 32.66667,118.9375 32.66667,118.90625 32.66667,118.875 32.66667,118.84375 32.66667,118.8125 32.66667,118.78125 32.66667,118.75 32.66667,118.71875 32.66667,118.6875 32.66667,118.65625 32.66667,118.625 32.66667,118.59375 32.66667,118.5625 32.66667,118.53125 32.66667,118.5 32.66667,118.46875 32.66667,118.4375 32.66667,118.40625 32.66667,118.375 32.66667,118.34375 32.66667,118.3125 32.66667,118.28125 32.66667,118.25 32.66667,118.25 32.6875,118.25 32.70833,118.25 32.72917,118.25 32.75,118.25 32.77083,118.25 32.79167,118.25 32.8125,118.25 32.83333,118.21875 32.83333,118.1875 32.83333,118.15625 32.83333,118.125 32.83333,118.125 32.85417,118.125 32.875,118.125 32.89583,118.125 32.91667,118.125 32.9375,118.125 32.95833,118.125 32.97917,118.125 33,118.125 33.02083],[119.09375 33.79167,119.09375 33.77083,119.09375 33.75,119.125 33.75,119.125 33.72917,119.15625 33.72917,119.1875 33.72917,119.1875 33.70833,119.15625 33.70833,119.15625 33.6875,119.125 33.6875,119.09375 33.6875,119.0625 33.6875,119.03125 33.6875,119.03125 33.66667,119 33.66667,118.96875 33.66667,118.9375 33.66667,118.9375 33.64583,118.9375 33.625,118.9375 33.60417,118.9375 33.58333,118.9375 33.5625,118.9375 33.54167,118.96875 33.54167,118.96875 33.52083,118.96875 33.5,118.96875 33.47917,118.96875 33.45833,118.9375 33.45833,118.9375 33.4375,118.9375 33.41667,118.90625 33.41667,118.90625 33.39583,118.90625 33.375,118.90625 33.35417,118.9375 33.35417,118.9375 33.375,118.96875 33.375,118.96875 33.39583,119 33.39583,119.03125 33.39583,119.03125 33.41667,119.0625 33.41667,119.0625 33.4375,119.09375 33.4375,119.09375 33.45833,119.125 33.45833,119.125 33.47917,119.15625 33.47917,119.15625 33.5,119.1875 33.5,119.21875 33.5,119.21875 33.52083,119.25 33.52083,119.25 33.54167,119.25 33.5625,119.25 33.58333,119.21875 33.58333,119.1875 33.58333,119.1875 33.60417,119.1875 33.625,119.15625 33.625,119.15625 33.64583,119.1875 33.64583,119.1875 33.66667,119.21875 33.66667,119.21875 33.6875,119.21875 33.70833,119.21875 33.72917,119.21875 33.75,119.1875 33.75,119.1875 33.77083,119.15625 33.77083,119.15625 33.79167,119.125 33.79167,119.09375 33.79167))";
		Connection dailyConn = DBConnector.getInstance().getConnectionById(13);
		String selectPid = "select pes.pid"
				 + " from ix_poi ip, poi_edit_status pes"
				 + " where ip.pid = pes.pid"
				 + " and pes.status != 0"
				 + " AND sdo_within_distance(ip.geometry, sdo_geometry(?, 8307), 'mask=anyinteract') = 'TRUE' and pes.medium_task_id = 0 and pes.quick_task_id = 0 and pes.quick_subtask_id = 0";
		String updateSql = "update poi_edit_status set medium_task_id= "+1+ ",medium_subtask_id="+1+ " where pid in ("+selectPid+")";
		QueryRunner run=new QueryRunner();
		Clob clob = ConnectionUtil.createClob(dailyConn);
		clob.setString(1, wkt);
		run.update(dailyConn, updateSql,clob);
	}

	@Test
	public void test01() throws Exception{
		
		ManApi manApi= new ManApiImpl();
		String objName = "subtask";
		Map<Integer, Map<String, Object>> data = manApi.queryManTimelineByObjName(objName,0);
		System.out.println(data.toString());
		
	}
	
	@Test
	public void test02() throws Exception{
		
		int taskId = 2;
		ManApi manApi= new ManApiImpl();
		List<Map<String, Object>> set = manApi.querySubtaskByTaskId(taskId);
		System.out.println(set.toString());
		
	}
	
	@Test
	public void test03() throws Exception{
		
		ManApi manApi= new ManApiImpl();
		Map<Integer, Integer> set = manApi.queryProgramTypes();
		System.out.println(set.toString());
		
	}
}
