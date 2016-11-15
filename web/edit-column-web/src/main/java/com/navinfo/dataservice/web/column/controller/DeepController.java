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
import com.navinfo.dataservice.control.column.core.DeepCoreControl;

import net.sf.json.JSONObject;

public class DeepController extends BaseController {
	private static final Logger logger = Logger.getLogger(DeepController.class);
	
	
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
}
