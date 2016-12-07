package com.navinfo.dataservice.web.column.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.control.column.core.DeepCoreControl;

import net.sf.json.JSONObject;

public class DeepController extends BaseController {
	private static final Logger logger = Logger.getLogger(DeepController.class);
	
	/**
	 * 深度信息库存统计接口
	 * 
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping(value = "/poi/deep/queryKcLog")
	public ModelAndView getLogCount(HttpServletRequest request) throws ServletException, IOException {
		
		String parameter = request.getParameter("parameter");
		
		logger.debug("深度信息库存总量统计");
		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);
			
			logger.debug("parameter="+jsonReq);
			
			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			
			int subtaskId = jsonReq.getInt("subtaskId");
			
			int dbId = jsonReq.getInt("dbId");
			
			ManApi apiService=(ManApi) ApplicationContextUtil.getBean("manApi");
			
			Subtask subtask = apiService.queryBySubtaskId(subtaskId);
			
			if (subtask == null) {
				throw new Exception("subtaskid未找到数据");
			}
			
			DeepCoreControl deepCore = new DeepCoreControl();
			
			JSONObject result = deepCore.getLogCount(subtask, dbId);
			
			return new ModelAndView("jsonView", success(result));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}
	
	/**
	 * 深度信息申请数据接口
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/poi/deep/applyData")
	public ModelAndView applyData(HttpServletRequest request) throws ServletException, IOException {
		String parameter = request.getParameter("parameter");
		logger.debug("深度信息申请数据");
		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);
			logger.debug("parameter="+jsonReq);
			
			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			
			long userId = tokenObj.getUserId();
			int taskId = jsonReq.getInt("taskId");
			
			String firstWorkItem = jsonReq.getString("firstWorkItem");
			String secondWorkItem = jsonReq.getString("secondWorkItem");
			
			if (StringUtils.isEmpty(firstWorkItem) || StringUtils.isEmpty(secondWorkItem)) {
				throw new Exception("firstWorkItem和secondWorkItem不能为空");
			}
			DeepCoreControl deepCore = new DeepCoreControl();
			
			//申请数据，返回本次申请成功的数据条数
			int applyNum = deepCore.applyData(taskId, userId, firstWorkItem, secondWorkItem);
			
			return new ModelAndView("jsonView", success(applyNum));
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			
			return new ModelAndView("jsonView",fail(e.getMessage()));
		}
	}
	
	/**
	 * 深度信息poi保存接口
	 * 
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping(value = "/poi/deep/poiSave")
	public ModelAndView poiSave(HttpServletRequest request) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");
		AccessToken tokenObj = (AccessToken) request.getAttribute("token");
		try {
			DeepCoreControl deepCore = new DeepCoreControl();
			JSONObject result = deepCore.save(parameter, tokenObj.getUserId());

			return new ModelAndView("jsonView", success(result));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}
	/**
	 * 深度信息poi提交接口
	 * 
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping(value = "/poi/deep/release")
	public ModelAndView poiRelease(HttpServletRequest request) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");
		AccessToken tokenObj = (AccessToken) request.getAttribute("token");
		try {
			DeepCoreControl deepCore = new DeepCoreControl();
			JSONObject result = deepCore.release(parameter, tokenObj.getUserId());

			return new ModelAndView("jsonView", success(result));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}
	
	/**
	 * 深度信息poi查询接口
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping(value = "/poi/deep/queryDataList")
	public ModelAndView queryDataList(HttpServletRequest request) throws ServletException, IOException {
		
		String parameter = request.getParameter("parameter");
		logger.debug("深度信息查询数据");
		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);
			logger.debug("parameter="+jsonReq);
			
			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			
			long userId = tokenObj.getUserId();
			
			DeepCoreControl deepCore = new DeepCoreControl();
			
			JSONObject result = deepCore.queryPoi(jsonReq, userId);
			
			return new ModelAndView("jsonView", success(result));
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			
			return new ModelAndView("jsonView",fail(e.getMessage()));
		}
	}
	
}
