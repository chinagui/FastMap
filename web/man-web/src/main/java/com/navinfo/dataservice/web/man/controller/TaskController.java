package com.navinfo.dataservice.web.man.controller;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.commons.token.AccessTokenFactory;
import com.navinfo.dataservice.engine.man.task.TaskService;
import com.navinfo.navicommons.database.Page;

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
	@Autowired 
	private TaskService service;

	/*
	 * 规划管理页面--任务管理
	 * 1.进入规划管理页面后，默认city级图层（通过滑块可选择block或grid级别图层）：
	 * 2.显示全部city的规划状态：
	 * (1)未规划，未创建任务的均为未规划
	 * (2)已规划，所有已经创建任务的城市均为已规划
	 * 3.city图层可操作功能为：创建任务，查看任务（查看/编辑/关闭），规划推荐
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
			service.create(userId,dataJson);
			return new ModelAndView("jsonView", success("创建成功"));
		}catch(Exception e){
			log.error("创建失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	@RequestMapping(value = "/task/update")
	public ModelAndView update(HttpServletRequest request){
		try{
			String token = request.getParameter("access_token");
			if (StringUtils.isEmpty(token)){
				throw new IllegalArgumentException("access_token参数不能为空。");
			}
			//验证token是否有效，无效直接报异常
			AccessToken tokenObj=AccessTokenFactory.validate(token);
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));			
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			service.update(dataJson);					
			return new ModelAndView("jsonView", success("修改成功"));
		}catch(Exception e){
			log.error("修改失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	@RequestMapping(value = "/task/close")
	public ModelAndView delete(HttpServletRequest request){
		try{			
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("param")));			
			if(dataJson==null){
				throw new IllegalArgumentException("param参数不能为空。");
			}
			service.delete(dataJson);			
			return new ModelAndView("jsonView", success("删除成功"));
		}catch(Exception e){
			log.error("删除失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	@RequestMapping(value = "/task/list")
	public ModelAndView list(HttpServletRequest request){
		try{			
			JSONObject condition = JSONObject.fromObject(URLDecode(request.getParameter("condition")));			
			JSONObject order = JSONObject.fromObject(URLDecode(request.getParameter("order")));	
			
			int curPageNum= 1;//默认为第一页
			String curPage= request.getParameter("pageNum");
			if (StringUtils.isNotEmpty(curPage)){
				curPageNum = Integer.parseInt(curPage);
			}
			Page data = service.list(condition,order,curPageNum);			
			return new ModelAndView("jsonView", success(data));
		}catch(Exception e){
			log.error("获取列表失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	@RequestMapping(value = "/task/query")
	public ModelAndView query(HttpServletRequest request){
		try{
			String taskId = request.getParameter("taskId");			
			if(StringUtils.isEmpty(taskId)){
				throw new IllegalArgumentException("taskId参数不能为空。");
			}
			HashMap data = service.query(Integer.valueOf(taskId));			
			return new ModelAndView("jsonView", success(data));
		}catch(Exception e){
			log.error("获取明细失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
}
