package com.navinfo.dataservice.web.man.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.engine.man.program.ProgramService;
import com.navinfo.navicommons.database.Page;
@Controller
public class ProgramController extends BaseController {
	private Logger log = LoggerRepos.getLogger(this.getClass());
	
	private ProgramService service=ProgramService.getInstance();
	
	@RequestMapping(value = "/program/create")
	public ModelAndView create(HttpServletRequest request) {
		try {
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			long userId = tokenObj.getUserId();
			service.create(userId, dataJson);
			return new ModelAndView("jsonView", success());
		} catch (Exception e) {
			log.error("创建失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	@RequestMapping(value = "/program/update")
	public ModelAndView update(HttpServletRequest request) {
		try {
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			long userId = tokenObj.getUserId();
			service.update(userId,dataJson);
			return new ModelAndView("jsonView", success());
		} catch (Exception e) {
			log.error("创建失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	/**
	 * 关闭原则：判断该项目下面所有的任务，子项目均关闭，则可以关闭项目
	 *  city规划状态变更为已关闭
	 *  注：任务关闭原则：任务下的所有子任务关闭，所以此处仅判断任务关闭即可
	 *  项目无法关闭，返回原因：未关闭任务列表
	 * @param request：programId
	 * @return String null,关闭成功。not null,未关闭任务列表
	 */
	@RequestMapping(value = "/program/close")
	public ModelAndView close(HttpServletRequest request) {
		try {
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			long userId = tokenObj.getUserId();
			int programId=dataJson.getInt("programId");
			String msg=service.close(userId,programId);
			if(msg==null){
				return new ModelAndView("jsonView", success());
			}else{
				return new ModelAndView("jsonView", fail(msg));
			}
			
		} catch (Exception e) {
			log.error("创建失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	@RequestMapping(value = "/program/list")
	public ModelAndView list(HttpServletRequest request) {
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
			int planStatus = 1;	
			if(dataJson.containsKey("planStatus")){
				planStatus=dataJson.getInt("planStatus");
			}
			int type = 1;	
			if(dataJson.containsKey("type")){
				type=dataJson.getInt("type");
			}
			Page data = service.list(type,planStatus,condition,order,curPageNum,curPageSize);
			Map<String, Object> returnMap=new HashMap<String, Object>();
			returnMap.put("result", (List)data.getResult());
			returnMap.put("totalCount", data.getTotalCount());
			return new ModelAndView("jsonView", success(returnMap));
		}catch(Exception e){
			log.error("获取列表失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	@RequestMapping(value = "/program/query")
	public ModelAndView query(HttpServletRequest request) {
		try{	
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));			
			int programId= dataJson.getInt("programId");
			Map<String, Object> data = service.query(programId);
			return new ModelAndView("jsonView", success(data));
		}catch(Exception e){
			log.error("获取列表失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	@RequestMapping(value = "/program/nameList")
	public ModelAndView nameList(HttpServletRequest request) {
		try{	
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));	
			if(!dataJson.containsKey("name")){
				throw new IllegalArgumentException("parameter参数中taskName不能为空。");
			}
			String name = dataJson.getString("name");
			List<Map<String, Object>> data = service.queryNameList(name);
			return new ModelAndView("jsonView", success(data));
		}catch(Exception e){
			log.error("获取列表失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	@RequestMapping(value = "/program/pushMsg")
	public ModelAndView pushMsg(HttpServletRequest request) {
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
			JSONArray programIds=dataJson.getJSONArray("programIds");
			//long userId=2;
			String msg=service.pushMsg(userId, programIds);
			return new ModelAndView("jsonView", success(msg));
		}catch(Exception e){
			log.error("获取列表失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
}
