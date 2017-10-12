package com.navinfo.dataservice.web.man.controller;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.engine.man.grid.GridService;
import com.navinfo.dataservice.engine.man.task.TaskProgressOperation;
import com.navinfo.dataservice.engine.man.task.TaskService;
import com.navinfo.navicommons.database.Page;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
* @ClassName: TaskController 
* @author code generator 
* @date 2016年4月6日 下午6:25:24 
* @Description: TODO
*/
@Controller
public class TaskController extends BaseController {
	private Logger log = LoggerRepos.getLogger(this.getClass());

	/*
	 * 规划管理页面--任务管理
	 * 1.进入规划管理页面后，默认city级图层（通过滑块可选择block或grid级别图层）：
	 * 2.显示全部city的规划状态：
	 * (1)未规划，未创建任务的均为未规划
	 * (2)已规划，所有已经创建任务的城市均为已规划
	 * 3.city图层可操作功能为：创建任务，查看任务（查看/编辑/关闭），规划推荐
	 * 1.	支持创建这四种类型的任务：1常规，2多源，3代理店，4情报
	 * 2.	常规任务创建和原来一致
	 * 3.	情报任务创建：task表创建情报任务后，需根据infor表中情报几何，计算情报涉及的大区block，在block_man表中，生成这些大区block对应的记录，为草稿状态
	 */
	@RequestMapping(value = "/task/create")
	public ModelAndView create(HttpServletRequest request){
		try{
			AccessToken tokenObj=(AccessToken) request.getAttribute("token");
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}		
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));			
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			long userId=tokenObj.getUserId();
			//long userId=2;
			String msg=TaskService.getInstance().create(userId,dataJson);
			return new ModelAndView("jsonView", success(msg));
		}catch(Exception e){
			log.error("创建失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	/*
	 * 规划管理页面--任务管理--创建任务
	 * 点击“发布”，变更内容作为消息推送，任务状态为“开启”；发布成功或失败弹出提示框“发布成功/发布失败”
	 * 1.选择某一条或多条记录，点击“发布”按钮，变更内容进行消息推送，发布成功或失败弹出提示框“发布成功/发布失败”
	 * 2.未选中记录时，提示“请选择任务”；
	 * 3.“关闭状态”记录不能进行发布
	 * ①原则：查找本次发布与上次发布之间的变更内容进行消息推送(暂无)
	 * ②记录创建后，未进行发布，任务状态为“草稿”，发布过一次后，状态为“开启”，再发布后状态不变更
	 */
	@RequestMapping(value = "/task/pushMsg")
	public ModelAndView pushMsg(HttpServletRequest request){
		try{
			AccessToken tokenObj=(AccessToken) request.getAttribute("token");
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}		
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));			
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			long userId=tokenObj.getUserId();
			JSONArray taskIds=dataJson.getJSONArray("taskIds");
			//long userId=2;
			String message = TaskService.getInstance().taskPushMsg(userId, taskIds);
			
			if(message!=null&&!message.isEmpty()&&message.equals("二代编辑任务发布失败，存在未关闭的采集任务")){
				return new ModelAndView("jsonView", fail(message));
			}else{
				return new ModelAndView("jsonView", success(message));
			}
		}catch(Exception e){
			log.error("发布失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	/*
	 * 规划管理页面--任务管理--修改任务页面
	 */
	@RequestMapping(value = "/task/update")
	public ModelAndView update(HttpServletRequest request){
		try{
			AccessToken tokenObj=(AccessToken) request.getAttribute("token");
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));			
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			long userId=tokenObj.getUserId();
			//long userId=2;
			String msg=TaskService.getInstance().update(userId,dataJson);					
			return new ModelAndView("jsonView", success(msg));
		}catch(Exception e){
			log.error("修改失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	
	
	/*
	 * 查询task
	 * 采集任务:
	 * 		常规采集任务关闭:调整任务范围;调整日编任务范围,调整区域子任务范围;调整二代编辑任务范围
	 * 		快速更新采集任务关闭:调整任务范围;调整日编任务范围,调整区域子任务范围;调整项目范围;
	 * 日编任务:
	 * 		快速更新日编任务关闭:调整项目范围.
	 * 发送消息
	 */
	@RequestMapping(value = "/task/close")
	public ModelAndView close(HttpServletRequest request){
		try{
			AccessToken tokenObj=(AccessToken) request.getAttribute("token");
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));			
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			long userId=tokenObj.getUserId();
			int taskId=dataJson.getInt("taskId");
			String overdueReason="";
			if(dataJson.containsKey("overdueReason")){
				overdueReason=dataJson.getString("overdueReason");
			}
			String overdueOtherReason="";
			if(dataJson.containsKey("overdueOtherReason")){
				overdueOtherReason=dataJson.getString("overdueOtherReason");
			}
			String message = TaskService.getInstance().close(taskId,userId,overdueReason,overdueOtherReason);			
			return new ModelAndView("jsonView", success(message));
		}catch(Exception e){
			log.error("任务关闭失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
//	/*
//	 * 规划管理页面--任务管理--查看任务页面
//	 */
//	@SuppressWarnings("rawtypes")
//	@RequestMapping(value = "/task/list")
//	public ModelAndView list(HttpServletRequest request){
//		try{	
//			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));			
//			JSONObject condition = new JSONObject();	
//			if(dataJson.containsKey("condition")){
//				condition=dataJson.getJSONObject("condition");
//			}
//			JSONObject order = new JSONObject();	
//			if(dataJson.containsKey("order")){
//				order=dataJson.getJSONObject("order");
//			}			
//			int curPageNum= 1;//默认为第一页
//			if (dataJson.containsKey("pageNum")){
//				curPageNum = dataJson.getInt("pageNum");
//			}
//			int curPageSize= 20;//默认为20条记录/页
//			if (dataJson.containsKey("pageSize")){
//				curPageSize = dataJson.getInt("pageSize");
//			}
//			int snapshot = 0; 
//			if(dataJson.containsKey("snapshot")){
//				snapshot=dataJson.getInt("snapshot");
//			}
//			//snapshot=0时，显示全部常规/情报任务，taskType,planStatus失效，可不传
//			int planStatus = 1;	
//			if(dataJson.containsKey("planStatus")){
//				planStatus=dataJson.getInt("planStatus");
//			}
//			//snapshot=0时，显示全部常规/情报任务，taskType,planStatus失效，可不传
//			int taskType = 1;	
//			if(dataJson.containsKey("taskType")){
//				taskType=dataJson.getInt("taskType");
//			}
//			Page data = TaskService.getInstance().list(taskType,planStatus,condition,order,curPageNum,curPageSize,snapshot);
//			Map<String, Object> returnMap=new HashMap<String, Object>();
//			returnMap.put("result", (List)data.getResult());
//			returnMap.put("totalCount", data.getTotalCount());
//			return new ModelAndView("jsonView", success(returnMap));
//		}catch(Exception e){
//			log.error("获取列表失败，原因："+e.getMessage(), e);
//			return new ModelAndView("jsonView",exception(e));
//		}
//	}
	
	/*
	 * 规划管理页面--任务管理--查看任务页面
	 */
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/task/list")
	public ModelAndView list(HttpServletRequest request){
		try{	
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));			
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
			Map<String, Object> returnMap=new HashMap<String, Object>();
			returnMap.put("result", (List)data.getResult());
			returnMap.put("totalCount", data.getTotalCount());
			return new ModelAndView("jsonView", success(returnMap));
		}catch(Exception e){
			log.error("获取列表失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	/*
	 * 规划管理页面--任务管理--3.1.2	查询任务详情
	 */
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/task/query")
	public ModelAndView query(HttpServletRequest request){
		try{	
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			//taskId,taskType
			int taskId= dataJson.getInt("taskId");
			
			Map<String,Object> data = TaskService.getInstance().query(taskId);
			return new ModelAndView("jsonView", success(data));
		}catch(Exception e){
			log.error("获取列表失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	/**
	 * 根据workKind判断：
	 * 1.	外业采集：判断task是否有groupid，有直接返回相关信息；
	 * 没有，获取admin_group_mapping的对应字段
	 * 2.	众包，情报，多源：获取admin_group_mapping的对应字段
	 * 应用场景：采集子任务创建组或者人员列表
	 * 
	 */
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/task/getCollectGroupByTask")
	public ModelAndView getCollectGroupByTask(HttpServletRequest request){
		try{	
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			//taskId,taskType
			int taskId= dataJson.getInt("taskId");
			int workKind= dataJson.getInt("workKind");
			int snapshot=dataJson.getInt("snapshot");
			
			Map<String,Object> data = TaskService.getInstance().getCollectGroupByTask(taskId,workKind,snapshot);
			return new ModelAndView("jsonView", success(data));
		}catch(Exception e){
			log.error("获取列表失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
//	/*
//	 * 规划管理页面--月编管理
//	 */
//	@SuppressWarnings("rawtypes")
//	@RequestMapping(value = "/task/queryMonthTask")
//	public ModelAndView queryMonthTask(HttpServletRequest request){
//		try{	
//			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
//			int curPageNum= 1;//默认为第一页
//			if (dataJson.containsKey("pageNum")){
//				curPageNum = dataJson.getInt("pageNum");
//			}
//			int curPageSize= 20;//默认为20条记录/页
//			if (dataJson.containsKey("pageSize")){
//				curPageSize = dataJson.getInt("pageSize");
//			}
//			JSONObject condition = new JSONObject();	
//			if(dataJson.containsKey("condition")){
//				condition=dataJson.getJSONObject("condition");
//			}
//			
//			Page data = TaskService.getInstance().queryMonthTask(condition,curPageNum,curPageSize);
//			Map<String, Object> returnMap=new HashMap<String, Object>();
//			returnMap.put("result", (List)data.getResult());
//			returnMap.put("totalCount", data.getTotalCount());
//			return new ModelAndView("jsonView", success(returnMap));
//		}catch(Exception e){
//			log.error("获取列表失败，原因："+e.getMessage(), e);
//			return new ModelAndView("jsonView",exception(e));
//		}
//	}
//	
//	/*
//	 * 规划管理页面--任务管理--查看任务页面
//	 */
//	@SuppressWarnings("rawtypes")
//	@RequestMapping(value = "/task/listAll")
//	public ModelAndView listAll(HttpServletRequest request){
//		try{	
//			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));			
//			JSONObject condition = dataJson.getJSONObject("condition");			
//			JSONObject order = dataJson.getJSONObject("order");
//			
//			List<Task> data = TaskService.getInstance().listAll(condition,order);			
//			return new ModelAndView("jsonView", success(JsonOperation.beanToJsonList(data)));
//			//return new ModelAndView("jsonView", success(data.getResult()));
//		}catch(Exception e){
//			log.error("获取全部列表失败，原因："+e.getMessage(), e);
//			return new ModelAndView("jsonView",exception(e));
//		}
//	}
	
	/**
	 * 查询任务名称列表
	 * 消息中心-业务申请(全部角色)-新的申请/查看申请
	 * @author Han Shaoming
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/task/nameList")
	public ModelAndView queryTaskNameList(HttpServletRequest request){
		try{
			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			long userId = tokenObj.getUserId();
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			JSONObject paraJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (paraJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			if(!paraJson.containsKey("taskName")){
				throw new IllegalArgumentException("parameter参数中taskName不能为空。");
			}
			String taskName = paraJson.getString("taskName");
			List<Map<String,Object>> taskNameList = TaskService.getInstance().queryTaskNameList(userId,taskName);
			return new ModelAndView("jsonView", success(taskNameList));
		}catch(Exception e){
			log.error("查询失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	/**
	 * 生管角色发布二代编辑任务后，点击打开小窗口可查看发布进度： 查询cms任务发布进度
	 * @author 
	 * @param request：task_id
	 * @return 进度
	 */
	@RequestMapping(value = "/task/cmsProgress")
	public ModelAndView queryTaskCmsProgress(HttpServletRequest request){
		try{
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			JSONObject paraJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (paraJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			if(!paraJson.containsKey("taskId")){
				throw new IllegalArgumentException("parameter参数中taskId不能为空。");
			}
			int taskId = paraJson.getInt("taskId");
			List<Map<String, Integer>> result = TaskService.getInstance().queryTaskCmsProgress(taskId);
			return new ModelAndView("jsonView", success(result));
		}catch(Exception e){
			log.error("查询失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	/**
	 * 生管角色发布二代编辑任务后，点击打开小窗口可查看发布进度： 查询cms任务发布进度
	 * 其中有关于tip转aumark的功能，有其他系统异步执行。执行成功后调用接口修改进度并执行下一步
	 * @author 
	 * @param request：phase_id
	 * @return 进度
	 */
	/*@RequestMapping(value = "/task/updateCmsProgress")
	public ModelAndView taskUpdateCmsProgress(HttpServletRequest request){
		try{
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			JSONObject paraJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (paraJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			if(!paraJson.containsKey("phaseId")){
				throw new IllegalArgumentException("parameter参数中taskId不能为空。");
			}
			int phaseId = paraJson.getInt("phaseId");
			int status = paraJson.getInt("status");
			TaskService.getInstance().taskUpdateCmsProgress(phaseId,status);
			return new ModelAndView("jsonView", success());
		}catch(Exception e){
			log.error("查询失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}*/
	
	/**
	 * 生管角色发布二代编辑任务后，点击打开小窗口可查看发布进度： 查询cms任务发布进度
	 * 其中有关于tip转aumark的功能，有其他系统异步执行。执行成功后调用接口修改进度并执行下一步
	 * @author 
	 * @param request：phase_id
	 * @return 进度
	 */
	@RequestMapping(value = "/task/runCmsProgress")
	public ModelAndView taskRunCmsProgress(HttpServletRequest request){
		try{
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			JSONObject paraJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (paraJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			if(!paraJson.containsKey("phaseId")){
				throw new IllegalArgumentException("parameter参数中taskId不能为空。");
			}
			int phaseId = paraJson.getInt("phaseId");
			int phase = paraJson.getInt("phase");
			TaskService.getInstance().exeCmsProgree(phaseId,phase);
			return new ModelAndView("jsonView", success());
		}catch(Exception e){
			log.error("查询失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	/**
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/task/wktByTaskId")
	public ModelAndView queryWktByBlockId(HttpServletRequest request) {
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			
			int taskId = dataJson.getInt("taskId");

			JSONObject geo = TaskService.getInstance().queryWktByTaskId(taskId);
			return new ModelAndView("jsonView", success(geo));
		} catch (Exception e) {
			log.error("获取明细失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	@RequestMapping(value = "/task/queryTaskIdsByGrid")
	public ModelAndView queryTaskIdsByGrid(HttpServletRequest request) {
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			
			String gridId = dataJson.getString("gridId");

			Map<String,Integer> map = GridService.getInstance().queryTaskIdsByGrid(gridId);
			return new ModelAndView("jsonView", success(map));
		} catch (Exception e) {
			log.error("获取明细失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	@RequestMapping(value = "/task/queryCollectTaskIdsByGridIdList")
	public ModelAndView queryCollectTaskIdsByGridIdList(HttpServletRequest request) {
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			
			JSONArray gridIds = dataJson.getJSONArray("gridIdList");
			List<Integer> gridIdList = JSONArray.toList(gridIds);

			Map<Integer,Map<String, Integer>> map = GridService.getInstance().queryCollectTaskIdsByGridIdList(gridIdList);
			return new ModelAndView("jsonView", success(map));
		} catch (Exception e) {
			log.error("获取明细失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	/**
	 * 任务关闭再开启，需要对应：任务关闭再开启后，生成一条新的任务记录，属性全部复制，状态=草稿。原来任务关联的子任务不继承到新任务中
	 * 应用场景：管理平台-生管角色
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/task/reOpen")
	public ModelAndView reOpen(HttpServletRequest request) {
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			
			int taskId=dataJson.getInt("taskId");
			AccessToken tokenObj=(AccessToken) request.getAttribute("token");
			long userId=tokenObj.getUserId();
			TaskService.getInstance().reOpen(userId,taskId);
			return new ModelAndView("jsonView", success());
		} catch (Exception e) {
			log.error("获取明细失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}	
	
	/**
	 * 成果调节--批快线任务号
	 */
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/task/batchQuickTask")
	public ModelAndView batchQuickTask(HttpServletRequest request){
		try{	
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			//taskId,taskType
			int dbId= dataJson.getInt("dbId");
			JSONArray pois = dataJson.getJSONArray("pois");
			JSONArray tips = dataJson.getJSONArray("tips");
			int subtaskId=dataJson.getInt("subtaskId");
			int taskId=dataJson.getInt("taskId");
			
			TaskService.getInstance().batchQuickTask(dbId,subtaskId,taskId,pois,tips);
			return new ModelAndView("jsonView", success());
		}catch(Exception e){
			log.error("获取列表失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	/**
	 * 
	 * 无任务数据(POI，TIPS)批中线任务号
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/task/batchMidTask")
	public ModelAndView batchMidTask(HttpServletRequest request) {
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			AccessToken tokenObj=(AccessToken) request.getAttribute("token");
			long userId=tokenObj.getUserId();
			int taskId = dataJson.getInt("taskId");
			//TaskService.getInstance().batchMidTaskByTaskId(taskId);
			int phaseId=TaskService.getInstance().createTaskOther2MediumJob(userId,taskId);
			return new ModelAndView("jsonView", success(phaseId));
		} catch (Exception e) {
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	/**
	 * 
	 * 测试socket用
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/task/testSocket")
	public ModelAndView socket(HttpServletRequest request) {
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			int phaseId = dataJson.getInt("phaseId");
			Connection conn=null;
			try{
				conn=DBConnector.getInstance().getManConnection();
				TaskProgressOperation.pushWebsocket(conn, phaseId);
			}finally{
				DbUtils.close(conn);
			}
			
			return new ModelAndView("jsonView", success());
		} catch (Exception e) {
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	/**
	 * 获取所有待发布的任务id的列表
	 * 应用场景：任务发布-全选按钮
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/task/allDraftTask")
	public ModelAndView allDraftTask(HttpServletRequest request) {
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			
			int programId = dataJson.getInt("programId");
			List<Integer> taskIds=TaskService.getInstance().allDraftTask(programId);
			return new ModelAndView("jsonView", success(taskIds));
		} catch (Exception e) {
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	/**
	 * 采集任务列表
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/task/midCollectTaskList")
	public ModelAndView midCollectTaskList(HttpServletRequest request) {
		try {
			List<Map<String, Object>> taskList = TaskService.getInstance().midCollectTaskList();
			return new ModelAndView("jsonView", success(taskList));
		} catch (Exception e) {
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	/**
	 * 获取待数据规划任务列表
	 * 中线项目下，同时满足草稿状态+未进行数据规划的采集任务列表
	 * 
	 * @param request
	 * @return Map
	 */
	@RequestMapping(value = "/task/unPlanlist")
	public ModelAndView unPlanlist(HttpServletRequest request){
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			if(!dataJson.containsKey("programId") || dataJson.getString("programId").length() < 1){
				throw new Exception("缺少programId");
			}
			List<Map<String, Object>> data =  TaskService.getInstance().unPlanlist(dataJson);
			Map<String, Object> result = new HashMap<String, Object>();
			result.put("totalCount", data.size());
			result.put("result", data);
			return new ModelAndView("jsonView", success(result));
		} catch (Exception e) {
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	/**
	 * 初始化规划数据列表
	 * 1.范围：采集任务对应的block的不规则范围
	 * 2.提取范围内的所有link/poi存入data_plan表中。默认全是作业数据
	 * 
	 * */
	@RequestMapping(value = "/task/initPlanData")
	public ModelAndView initPlanData(HttpServletRequest request){
		try{
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			if(!dataJson.containsKey("taskId") || dataJson.getString("taskId").length() < 1){
				throw new Exception("缺少taskId");
			}
			
			int taskId = dataJson.getInt("taskId");
			Map<String, Integer> result = TaskService.getInstance().initPlanData(taskId);
			
			return new ModelAndView("jsonView", success(result));
		}catch(Exception e){
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	
	/**
	 * 获取质检子任务的任务列表
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/task/unPlanQualitylist")
	public ModelAndView unPlanQualitylist(HttpServletRequest request) {
		try{	
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}		
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));			
			Integer programId = dataJson.getInt("programId");
			JSONObject data = TaskService.getInstance().unPlanQualitylist(programId);
			return new ModelAndView("jsonView", success(data));
		}catch(Exception e){
			log.error("获取列表失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	//获取待规划子任务的任务列表
	@RequestMapping(value = "/task/unPlanSubtasklist")
	public ModelAndView unPlanSubtasklist(HttpServletRequest request) {
		try {
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}		
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));			
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			
			int programId = dataJson.getInt("programId");

			JSONObject data = TaskService.getInstance().unPlanSubtasklist(programId);
			return new ModelAndView("jsonView", success(data));
		} catch (Exception e) {
			log.error("获取列表失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	//规划上传接口
	@RequestMapping(value = "/task/uploadPlan")
	public ModelAndView uploadPlan(HttpServletRequest request){
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			if(!dataJson.containsKey("taskId") || dataJson.getString("taskId").length() < 1){
				throw new Exception("缺少taskId");
			}
			
			int taskId = dataJson.getInt("taskId");
			Map<String, Object> result = TaskService.getInstance().uploadPlan(taskId);
			
			return new ModelAndView("jsonView", success(result));
		} catch (Exception e) {
			log.error("获取列表失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	//规划数据保存接口
	@RequestMapping(value = "/task/savePlan")
	public ModelAndView savePlan(HttpServletRequest request){
		try {
			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			long userId = tokenObj.getUserId();
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			if(!dataJson.containsKey("dataType") || dataJson.getString("dataType").length() < 1){
				throw new Exception("缺少dataType");
			}
			if(!dataJson.containsKey("isPlanStatus") || dataJson.getString("isPlanStatus").length() < 1){
				throw new Exception("缺少isPlanStatus");
			}
			if(!dataJson.containsKey("condition") || dataJson.getString("condition").length() < 1){
				throw new Exception("缺少condition");
			}
			if(!dataJson.containsKey("taskId") || dataJson.getString("taskId").length() < 1){
				throw new Exception("缺少taskId");
			}
			
			TaskService.getInstance().savePlan(dataJson, userId);
			
			return new ModelAndView("jsonView", success());
		} catch (Exception e) {
			log.error("获取列表失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
		
	}
	
	/**
	 * 获取条件规划
	 * @param HttpServletRequest
	 * @return ModelAndView
	 * 
	 * */
	@RequestMapping(value="/task/getPlan")
	public ModelAndView getPlan(HttpServletRequest request){
		try{
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			if(!dataJson.containsKey("taskId") || dataJson.getString("taskId").length() < 1){
				throw new Exception("缺少taskId");
			}
			
			int taskId = dataJson.getInt("taskId");
			JSONObject result = TaskService.getInstance().getPlan(taskId);
			
			return new ModelAndView("jsonView", success(result));
		}catch(Exception e){
			log.error("获取条件规划失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	/**
	 * 为ocms提供任务数据列表
	 * @param HttpServletRequest
	 * @return ModelAndView
	 * 
	 * */
	@RequestMapping(value="/task/forOcms")
	public ModelAndView forOcms(HttpServletRequest request){
		try{
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
						
			String date ="";
			if(dataJson.containsKey("date")){
				date=dataJson.getString("date");
			}
			List<Map<String, Object>> result = TaskService.getInstance().forOcms(date);
			
			return new ModelAndView("jsonView", success(result));
		}catch(Exception e){
			log.error("获取条件规划失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
}