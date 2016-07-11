package com.navinfo.dataservice.web.man.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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

import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.json.JsonOperation;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.engine.man.grid.GridService;
import com.navinfo.dataservice.engine.man.subtask.SubtaskService;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.geo.computation.GridUtils;

/** 
 * @ClassName: SubtaskController
 * @author songdongyan
 * @date 2016年6月6日
 * @Description: SubtaskController.java
 */
@Controller
public class SubtaskController extends BaseController {
	private Logger log = LoggerRepos.getLogger(this.getClass());
//	@Autowired
//	private SubtaskService service;

	/*
	 * 创建一个子任务。
	 */
	@RequestMapping(value = "/subtask/create")
	public ModelAndView create(HttpServletRequest request){
		try{	

			AccessToken tokenObj=(AccessToken) request.getAttribute("token");

			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			
			long userId = tokenObj.getUserId();
			JSONArray gridIds = new JSONArray();
			
			//创建区域专项子任务
			if(dataJson.containsKey("taskId")){
				List<Integer> gridIdList = GridService.getInstance().getGridListByTaskId(dataJson.getInt("taskId"));
				gridIds.addAll(gridIdList);
			}else{
				gridIds = dataJson.getJSONArray("gridIds");
			}
			//根据gridIds获取wkt
			String wkt = GridUtils.grids2Wkt(gridIds);
			if(wkt.contains("MULTIPOLYGON")){
				return new ModelAndView("jsonView",exception("请输入符合条件的grids"));
			}
			
			Object[] gridIdList = gridIds.toArray();
			dataJson.put("gridIds",gridIdList);
			
			Subtask bean = (Subtask) JsonOperation.jsonToBean(dataJson,Subtask.class);
			bean.setCreateUserId((int)userId);
			bean.setGeometry(wkt);
				
			SubtaskService.getInstance().create(bean);	
			
			return new ModelAndView("jsonView", success("创建成功"));
		}catch(Exception e){
			log.error("创建失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	
	/*
	 * 根据几何范围查询任务列表
	 */
	@RequestMapping(value = "/subtask/listByWkt")
	public ModelAndView listByWkt(HttpServletRequest request){
		try{	

			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			
			//获取几何范围
			String wkt = dataJson.getString("wkt");
			
			List<Subtask> subtaskList = SubtaskService.getInstance().listByWkt(wkt);
			
			//根据需要的返回字段拼装结果
			List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();

			for(int i=0;i<subtaskList.size();i++){
				HashMap<String, Object> subtask = new HashMap<String, Object>();
				subtask.put("subtaskId", subtaskList.get(i).getSubtaskId());
				subtask.put("geometry", subtaskList.get(i).getGeometry());
				subtask.put("descp", subtaskList.get(i).getDescp());
				subtask.put("name", subtaskList.get(i).getName());
				subtask.put("stage", subtaskList.get(i).getStage());
				subtask.put("status", subtaskList.get(i).getStatus());
				subtask.put("type", subtaskList.get(i).getType());
				subtask.put("gridIds", subtaskList.get(i).getGridIds());
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
			
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			
			int curPageNum= 1;//默认为第一页
			if(dataJson.containsKey("pageNum")){
				curPageNum = dataJson.getInt("pageNum");
			}
			
			int pageSize = 20;//默认页容量为10
			if(dataJson.containsKey("pageSize")){
				pageSize = dataJson.getInt("pageSize");
			}
			
			JSONObject order =null; 
			if(dataJson.containsKey("order")){
				order=dataJson.getJSONObject("order");
			}
			
			JSONObject condition =null; 
			if(dataJson.containsKey("condition")){
				condition=dataJson.getJSONObject("condition");
			}
			
			//作业阶段
			int stage = dataJson.getInt("stage");
			
//			Page page = SubtaskService.getInstance().listPage(stage,condition,order,pageSize,curPageNum);
			Page page = SubtaskService.getInstance().list(stage,condition,order,pageSize,curPageNum);
			
            return new ModelAndView("jsonView", success(page));
		
		}catch(Exception e){
			log.error("查询失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	@RequestMapping(value = "/subtask/listByUser")
	public ModelAndView listByUser(HttpServletRequest request){
		try{	

			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
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

            Subtask bean = (Subtask)JSONObject.toBean(dataJson, Subtask.class);
            
            Page page = SubtaskService.getInstance().listByUserPage(bean,snapshot,pageSize,curPageNum);
            
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
			
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			
			Subtask bean = (Subtask)JSONObject.toBean(dataJson, Subtask.class);
			
			Subtask subtask = SubtaskService.getInstance().query(bean);	
			
			//根据需要的返回字段拼装结果
			HashMap<String, Object> data = new HashMap<String, Object>();
			if(subtask!=null&&subtask.getSubtaskId()!=null){
				data.put("subtaskId", subtask.getSubtaskId());
				data.put("stage", subtask.getStage());
				data.put("type", subtask.getType());
				data.put("planStartDate", subtask.getPlanStartDate());
				data.put("planEndDate", subtask.getPlanEndDate());
				data.put("descp", subtask.getDescp());
				data.put("name", subtask.getName());
				data.put("gridIds", subtask.getGridIds());
				data.put("dbId", subtask.getDbId());
				data.put("geometry", subtask.getGeometry());
			}
			else{
				throw new Exception("该任务不存在");
			}
			
			JSONObject result = JsonOperation.beanToJson(data);
			
			return new ModelAndView("jsonView", success(result));
			
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

			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
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
			
			List<Integer> updatedSubtaskIdList = SubtaskService.getInstance().update(subtaskList);
			
			String message = "批量修改子任务：" + updatedSubtaskIdList.size() + "个成功，" + (subtaskList.size() - updatedSubtaskIdList.size()) + "个失败。";
			
			return new ModelAndView("jsonView", success(message));
			
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
			
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			
			if(!dataJson.containsKey("subtaskIds")){
				return new ModelAndView("jsonView", exception("请传subtaskId"));
			}
			
			JSONArray subtaskIds = dataJson.getJSONArray("subtaskIds");
			
			List<Integer> subtaskIdList = (List<Integer>)JSONArray.toCollection(subtaskIds,Integer.class);
			List<Integer> unClosedSubtaskList = SubtaskService.getInstance().close(subtaskIdList);
			
			String message = "批量关闭子任务：" + (subtaskIdList.size() - unClosedSubtaskList.size()) + "个成功，" + unClosedSubtaskList.size() + "个失败。";
			
			return new ModelAndView("jsonView", success(message));
		
		}catch(Exception e){
			log.error("批量关闭失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
}
