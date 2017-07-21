package com.navinfo.dataservice.engine.man;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
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
import com.navinfo.navicommons.database.DataBaseUtils;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.exception.ServiceException;
import com.navinfo.navicommons.geo.computation.GridUtils;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.util.GeometryTransformer;
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
		String parameter="{\"condition\":{\"programId\":106,\"name\":\"云南省昭通市鲁甸县郊\"},\"pageNum\":1,\"pageSize\":15,\"snapshot\":0}";
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
		taskIds.add(78);
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
	
}
