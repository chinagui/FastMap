package com.navinfo.dataservice.web.man.controller;

import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.api.man.model.Task;
import com.navinfo.dataservice.commons.json.JsonOperation;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.commons.util.DateUtils;

import com.navinfo.dataservice.engine.man.subtask.SubtaskService;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.geo.computation.GridUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
 * @ClassName: SubtaskController
 * @author songdongyan
 * @date 2016年6月6日
 * @Description: SubtaskController.java
 */
@Controller
public class SubtaskController extends BaseController {
	private Logger log = LoggerRepos.getLogger(this.getClass());
	@Autowired
	private SubtaskService service;

	/*
	 * 创建一个子任务。
	 */
	@RequestMapping(value = "/subtask/create")
	public ModelAndView create(HttpServletRequest request){
		try{	

			AccessToken tokenObj=(AccessToken) request.getAttribute("token");
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)){
				throw new IllegalArgumentException("param参数不能为空。");
			}		
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));

			if(dataJson==null){
				throw new IllegalArgumentException("param参数不能为空。");
			}
			
			long userId = tokenObj.getUserId();
			//根据gridIds获取wkt
			JSONArray gridIds = dataJson.getJSONArray("gridIds");
			String wkt = GridUtils.grids2Wkt(gridIds);
			ArrayList<Integer> gridIdList = (ArrayList<Integer>)JSONArray.toList(dataJson.getJSONArray("gridIds"),int.class);
			
			dataJson.remove("gridIds");
			
//			Subtask bean = (Subtask)JSONObject.toBean(dataJson, Subtask.class);
			Subtask bean = (Subtask) JsonOperation.jsonToBean(dataJson,Subtask.class);
			bean.setCreateUserId((int)userId);
			bean.setGeometry(wkt);
			
			service.create(bean,gridIdList);	
			
			return new ModelAndView("jsonView", success("创建成功"));
		}catch(Exception e){
			log.error("创建失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	
	/*
	 * 根据几何范围,任务类型，作业阶段查询任务列表
	 */
	@RequestMapping(value = "/subtask/listByWkt")
	public ModelAndView listByWkt(HttpServletRequest request){
		try{	

			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)){
				throw new IllegalArgumentException("param参数不能为空。");
			}		
		
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));

			if(dataJson==null){
				throw new IllegalArgumentException("param参数不能为空。");
			}
			
			//获取几何范围,任务类型，作业阶段
			ArrayList<Integer> types = (ArrayList<Integer>)JSONArray.toList(dataJson.getJSONArray("types"),int.class);
			int stage = dataJson.getInt("stage");
			String wkt = dataJson.getString("wkt");
			
			List<Subtask> subtaskList = service.listByWkt(wkt,types,stage);
			
			//根据需要的返回字段拼装结果
			List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();

			for(int i=0;i<subtaskList.size();i++){
				HashMap<String, Object> subtask = new HashMap<String, Object>();
				subtask.put("subtaskId", subtaskList.get(i).getSubtaskId());
				subtask.put("geometry", subtaskList.get(i).getGeometry());
				subtask.put("descp", subtaskList.get(i).getDescp());
				list.add(subtask);
			}
	
			return new ModelAndView("jsonView", success(list));
			
		}catch(Exception e){
			log.error("查询失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	@RequestMapping(value = "/subtask/list")
	public ModelAndView list(HttpServletRequest request){
		try{		
			
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)){
				throw new IllegalArgumentException("param参数不能为空。");
			}		
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));

			if(dataJson==null){
				throw new IllegalArgumentException("param参数不能为空。");
			}
			
			int curPageNum= 1;//默认为第一页
			if(dataJson.containsKey("pageNum")){
				curPageNum = dataJson.getInt("pageNum");
				dataJson.remove("pageNum");
			}
			
			int pageSize = 20;//默认页容量为10
			if(dataJson.containsKey("pageSize")){
				pageSize = dataJson.getInt("pageSize");
				dataJson.remove("pageSize");
			}
			
			List<String> sortby = new ArrayList<String>();
			if(dataJson.containsKey("sortBy")){
				sortby = (ArrayList<String>)JSONArray.toList(dataJson.getJSONArray("sortBy"),String.class);
				dataJson.remove("sortBy");
			}

			Subtask bean = (Subtask)JSONObject.toBean(dataJson, Subtask.class);
			
			List<Subtask> subtaskList = service.list(bean,sortby,pageSize,curPageNum);
			
			List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
			
			Page page = new Page(curPageNum);
            page.setPageSize(pageSize);
			
			for(int i=0;i<subtaskList.size();i++){
				HashMap<String, Object> subtask = new HashMap<String, Object>();
				page.setTotalCount(subtaskList.size());
				subtask.put("subtaskId", subtaskList.get(i).getSubtaskId());
				subtask.put("subtaskName", subtaskList.get(i).getName());
				subtask.put("geometry", subtaskList.get(i).getGeometry());
				subtask.put("stage", subtaskList.get(i).getStage());
				subtask.put("type", subtaskList.get(i).getType());
				subtask.put("planStartDate", DateUtils.dateToString(subtaskList.get(i).getPlanStartDate()));
				subtask.put("planEndDate", DateUtils.dateToString(subtaskList.get(i).getPlanEndDate()));
				subtask.put("descp", subtaskList.get(i).getDescp());
				if (subtaskList.get(i).getBlock()!=null && StringUtils.isNotEmpty(subtaskList.get(i).getBlock().toString())){
					subtask.put("blockId", subtaskList.get(i).getBlock().getBlockId());
					subtask.put("blockName", subtaskList.get(i).getBlock().getBlockName());
					if(0 == bean.getStage()){
						subtask.put("BlockCollectPlanStartDate", subtaskList.get(i).getBlockMan().getCollectPlanStartDate());
						subtask.put("BlockCollectPlanEndDate", subtaskList.get(i).getBlockMan().getCollectPlanEndDate());
					}else if(1 == bean.getStage()){
						subtask.put("BlockDayEditPlanStartDate", subtaskList.get(i).getBlockMan().getDayEditPlanStartDate());
						subtask.put("BlockDayEditPlanEndDate", subtaskList.get(i).getBlockMan().getDayEditPlanEndDate());
					}else if(2 == bean.getStage()){
						subtask.put("BlockCMonthEditPlanStartDate", subtaskList.get(i).getBlockMan().getMonthEditPlanStartDate());
						subtask.put("BlockCMonthEditPlanEndDate", subtaskList.get(i).getBlockMan().getMonthEditPlanEndDate());
					}
				}
				if (subtaskList.get(i).getTask()!=null && StringUtils.isNotEmpty(subtaskList.get(i).getTask().toString())){
					subtask.put("taskId", subtaskList.get(i).getTask().getTaskId());
					subtask.put("taskDescp", subtaskList.get(i).getTask().getDescp());
					subtask.put("taskName", subtaskList.get(i).getTask().getName());
					if(0 == bean.getStage()){
						subtask.put("TaskCollectPlanStartDate", subtaskList.get(i).getTask().getCollectPlanStartDate());
						subtask.put("TaskCollectPlanEndDate", subtaskList.get(i).getTask().getCollectPlanEndDate());
					}else if(1 == bean.getStage()){
						subtask.put("TaskDayEditPlanStartDate", subtaskList.get(i).getTask().getDayEditPlanStartDate());
						subtask.put("TaskDayEditPlanEndDate", subtaskList.get(i).getTask().getDayEditPlanEndDate());
					}else if(2 == bean.getStage()){
						subtask.put("TaskCMonthEditPlanStartDate", subtaskList.get(i).getTask().getCMonthEditPlanStartDate());
						subtask.put("TaskCMonthEditPlanEndDate", subtaskList.get(i).getTask().getCMonthEditPlanEndDate());
					}
				}
				list.add(subtask);
			}
	

            page.setResult(list);
            
            return new ModelAndView("jsonView", success(page));
			
		}catch(Exception e){
			log.error("查询失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	@RequestMapping(value = "/subtask/listByUser")
	public ModelAndView listByUser(HttpServletRequest request){
		try{	

			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)){
				throw new IllegalArgumentException("param参数不能为空。");
			}		
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));

			if(dataJson==null){
				throw new IllegalArgumentException("param参数不能为空。");
			}
			
			int curPageNum= 1;//默认为第一页
			if(dataJson.containsKey("pageNum")){
				curPageNum = dataJson.getInt("pageNum");
				dataJson.remove("pageNum");
			}
			
			int pageSize = 20;//默认页容量为10
			if(dataJson.containsKey("pageSize")){
				pageSize = dataJson.getInt("pageSize");
				dataJson.remove("pageSize");
			}
			
			int snapshot = dataJson.getInt("snapshot");
			dataJson.remove("snapshot");
			
			Page page = new Page(curPageNum);
            page.setPageSize(pageSize);
            
            Subtask bean = (Subtask)JSONObject.toBean(dataJson, Subtask.class);
			
			List<Subtask> subtaskList = service.listByUser(bean,snapshot,pageSize,curPageNum);
			
			List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
			for(int i=0;i<subtaskList.size();i++){
				HashMap<String, Object> subtask = new HashMap<String, Object>();
				page.setTotalCount(subtaskList.size());
				subtask.put("subtaskId", subtaskList.get(i).getSubtaskId());
				subtask.put("name", subtaskList.get(i).getName());
				subtask.put("stage", subtaskList.get(i).getStage());
				subtask.put("type", subtaskList.get(i).getType());
				subtask.put("planStartDate", DateUtils.dateToString(subtaskList.get(i).getPlanStartDate()));
				subtask.put("planEndDate", DateUtils.dateToString(subtaskList.get(i).getPlanEndDate()));
				subtask.put("descp", subtaskList.get(i).getDescp());
				subtask.put("status", subtaskList.get(i).getStatus());
				subtask.put("dbId", subtaskList.get(i).getDbId());
				if(0==snapshot){
					subtask.put("geometry", subtaskList.get(i).getGeometry());
					subtask.put("gridIds", subtaskList.get(i).getGridIds());
				}
				list.add(subtask);
			}
	
            page.setResult(list);
			
			return new ModelAndView("jsonView", success(page));
		}catch(Exception e){
			log.error("查询失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	

	/*
	 * 根据subtaskId查询一个任务的详细信息。
	 */
	@RequestMapping(value = "/subtask/query")
	public ModelAndView query(HttpServletRequest request){
		try{
			
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)){
				throw new IllegalArgumentException("param参数不能为空。");
			}										
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));

			if(dataJson==null){
				throw new IllegalArgumentException("param参数不能为空。");
			}
			
			Subtask bean = (Subtask)JSONObject.toBean(dataJson, Subtask.class);
			
			Subtask subtask = service.query(bean);	
			
			//根据需要的返回字段拼装结果
			HashMap<String, Object> data = new HashMap<String, Object>();

			data.put("subtaskId", subtask.getSubtaskId());
			data.put("geometry", subtask.getGeometry());
			data.put("stage", subtask.getStage());
			data.put("type", subtask.getType());
			data.put("planStartDate", subtask.getPlanStartDate());
			data.put("planEndDate", subtask.getPlanEndDate());
			data.put("descp", subtask.getDescp());

			return new ModelAndView("jsonView", success(data));
			
		}catch(Exception e){
			log.error("获取明细失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	/*
	 * 批量修改子任务详细信息。
	 */
	@RequestMapping(value = "/subtask/update")
	public ModelAndView update(HttpServletRequest request){
		try{

			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)){
				throw new IllegalArgumentException("param参数不能为空。");
			}
			
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));			
			if(dataJson==null){
				throw new IllegalArgumentException("param参数不能为空。");
			}
			
			if(!dataJson.containsKey("subtasks")){
				return new ModelAndView("jsonView", success("修改成功"));
			}
			
			JSONArray subtaskArray=dataJson.getJSONArray("subtasks");
			List<Subtask> subtaskList = new ArrayList<Subtask>();
			for(int i = 0;i<subtaskArray.size();i++){
				Subtask subtask = (Subtask)JsonOperation.jsonToBean(subtaskArray.getJSONObject(i),Subtask.class);
				subtaskList.add(subtask);
			}
			
			service.update(subtaskList);
			
			return new ModelAndView("jsonView", success("修改成功"));
			
		}catch(Exception e){
			log.error("更新失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	/*
	 * 关闭多个子任务。
	 */
	@RequestMapping(value = "/subtask/close")
	public ModelAndView close(HttpServletRequest request){
		try{		
			
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)){
				throw new IllegalArgumentException("param参数不能为空。");
			}		
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));

			if(dataJson==null){
				throw new IllegalArgumentException("param参数不能为空。");
			}
			
			if(!dataJson.containsKey("subtaskIds")){
				return new ModelAndView("jsonView", success("关闭成功"));
			}
			
			JSONArray subtaskIds = dataJson.getJSONArray("subtaskIds");
			
			List<Integer> subtaskIdList = (List<Integer>)JSONArray.toCollection(subtaskIds,Integer.class); 
			
//			List<Subtask> subtaskArray = new ArrayList<Subtask>();
//			
//			for(int i = 0;i<subtasks.size();i++){
//				Subtask subtask = (Subtask)JSONObject.toBean(subtasks.getJSONObject(i), Subtask.class);
//				subtaskArray.add(subtask);
//			}
//			
//			List<Integer> unClosedSubtaskList = service.close(subtaskArray);
			
			List<Integer> unClosedSubtaskList = service.close(subtaskIdList);
			
			if(unClosedSubtaskList.isEmpty()){
				return new ModelAndView("jsonView", success("关闭成功"));
			}else{
				return new ModelAndView("jsonView", success(unClosedSubtaskList));
			}
			
			
		}catch(Exception e){
			log.error("批量关闭失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
}
