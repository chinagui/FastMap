package com.navinfo.dataservice.web.edit.row.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.control.row.quality.QualityService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
public class QualityController extends BaseController {

	private static final Logger log = Logger
			.getLogger(RowCrowdsController.class);

	/**
	 * 获取质检问题属性值
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/qc/queryInitValueForProblem")
	public ModelAndView queryInitValueForProblem(HttpServletRequest request){
		try{
			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			String parameter = request.getParameter("parameter");
			
			if (StringUtils.isEmpty(parameter)){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));			
			if(dataJson == null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			long userId = tokenObj.getUserId();
			int pid = dataJson.getInt("pid");
			int subtaskId = dataJson.getInt("subtaskId");
			
			JSONObject data = QualityService.getInstance().queryInitValueForProblem(userId, pid, subtaskId);
			return new ModelAndView("jsonView", success(data));
		}catch(Exception e){
			log.error("获取质检问题失败，原因："+ e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	/**
	 * poi质检问题操作（新增、修改、删除）
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/qc/operateProblem")
	public ModelAndView operateProblem(HttpServletRequest request){
		try{
			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			String parameter = request.getParameter("parameter");
			
			if (StringUtils.isEmpty(parameter)){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}		
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));
			if(dataJson == null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			long userId = tokenObj.getUserId();
			
			QualityService.getInstance().operateProblem(userId, dataJson);
			return new ModelAndView("jsonView", success());
		}catch(Exception e){
			log.error("质检问题新增、删除、修改失败，原因："+ e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	/**
	 * poi质检问题查看
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/qc/queryProblemList")
	public ModelAndView queryProblemList(HttpServletRequest request){
		try{
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));			
			if(dataJson == null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			
			JSONArray data = QualityService.getInstance().queryProblemList(dataJson);
			return new ModelAndView("jsonView", success(data));
		}catch(Exception e){
			log.error("获取poi质检问题失败，原因："+ e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	@RequestMapping(value = "/qc/queryQualityRelation")
	public ModelAndView queryQualityRelation(HttpServletRequest request){
		try{
			JSONArray data = QualityService.getInstance().queryQualityRelation();
			return new ModelAndView("jsonView", success(data));
		}catch(Exception e){
			log.error("获取质检关系失败，原因为："+ e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
}
