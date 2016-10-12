package com.navinfo.dataservice.web.man.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.api.man.model.Task;
import com.navinfo.dataservice.commons.json.JsonOperation;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.engine.man.task.TaskService;
import com.navinfo.navicommons.database.Page;

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
			String msg=TaskService.getInstance().taskPushMsg(userId, taskIds);
			return new ModelAndView("jsonView", success(msg));
		}catch(Exception e){
			log.error("创建失败，原因："+e.getMessage(), e);
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
	 * 规划管理页面--任务管理--关闭任务
	 * 关闭按钮：
	 * 1.选中需要关闭的任务，点击“关闭任务”按钮，后台判断该任务是否可以关闭
	 * 【关闭原则：判断该city下面的所有block均关闭且所有的月编区域作业子任务均关闭，则可以关闭任务】：
	 * (1)如果可以关闭，页面弹出提示框
	 * (2)不可以关闭，页面弹出提示框
	 */
	@RequestMapping(value = "/task/close")
	public ModelAndView close(HttpServletRequest request){
		try{			
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));			
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			JSONArray taskIds=dataJson.getJSONArray("taskIds");
			HashMap<String,String> errorTask=TaskService.getInstance().close(JSONArray.toList(taskIds));			
			String msg="任务批量关闭"+(taskIds.size()-errorTask.size())+"个成功，"+errorTask.size()+"个失败";
			return new ModelAndView("jsonView", success(msg));
		}catch(Exception e){
			log.error("任务批量关闭失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
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
			JSONObject order = new JSONObject();	
			if(dataJson.containsKey("order")){
				order=dataJson.getJSONObject("order");
			}			
			int curPageNum= 1;//默认为第一页
			if (dataJson.containsKey("pageNum")){
				curPageNum = dataJson.getInt("pageNum");
			}
			int curPageSize= 20;//默认为20条记录/页
			if (dataJson.containsKey("pageSize")){
				curPageSize = dataJson.getInt("pageSize");
			}
			int snapshot = 0; 
			if(dataJson.containsKey("snapshot")){
				snapshot=dataJson.getInt("snapshot");
			}
			//snapshot=0时，显示全部常规/情报任务，taskType,planStatus失效，可不传
			int planStatus = 1;	
			if(dataJson.containsKey("planStatus")){
				planStatus=dataJson.getInt("planStatus");
			}
			//snapshot=0时，显示全部常规/情报任务，taskType,planStatus失效，可不传
			int taskType = 1;	
			if(dataJson.containsKey("taskType")){
				taskType=dataJson.getInt("taskType");
			}
			Page data = TaskService.getInstance().list(taskType,planStatus,condition,order,curPageNum,curPageSize,snapshot);
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
			
			Map<String, Object> data = TaskService.getInstance().query(taskId);
			return new ModelAndView("jsonView", success(data));
		}catch(Exception e){
			log.error("获取列表失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	/*
	 * 规划管理页面--月编管理
	 */
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/task/queryMonthTask")
	public ModelAndView queryMonthTask(HttpServletRequest request){
		try{	
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			//taskId,taskType
			int monthEditGroupId= dataJson.getInt("monthEditGroupId");
			int curPageNum= 1;//默认为第一页
			if (dataJson.containsKey("pageNum")){
				curPageNum = dataJson.getInt("pageNum");
			}
			int curPageSize= 20;//默认为20条记录/页
			if (dataJson.containsKey("pageSize")){
				curPageSize = dataJson.getInt("pageSize");
			}
			JSONObject condition = new JSONObject();	
			if(dataJson.containsKey("condition")){
				condition=dataJson.getJSONObject("condition");
			}
			
			Page data = TaskService.getInstance().queryMonthTask(monthEditGroupId,condition,curPageNum,curPageSize);
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
	 * 规划管理页面--任务管理--查看任务页面
	 */
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/task/listAll")
	public ModelAndView listAll(HttpServletRequest request){
		try{	
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));			
			JSONObject condition = dataJson.getJSONObject("condition");			
			JSONObject order = dataJson.getJSONObject("order");
			
			List<Task> data = TaskService.getInstance().listAll(condition,order);			
			return new ModelAndView("jsonView", success(JsonOperation.beanToJsonList(data)));
			//return new ModelAndView("jsonView", success(data.getResult()));
		}catch(Exception e){
			log.error("获取全部列表失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
}
