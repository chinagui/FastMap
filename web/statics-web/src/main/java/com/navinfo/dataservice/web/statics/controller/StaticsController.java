package com.navinfo.dataservice.web.statics.controller;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.api.statics.model.SubtaskStatInfo;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.engine.statics.service.StaticsApiImpl;
import com.navinfo.dataservice.engine.statics.service.StaticsService;

import net.sf.json.JSONObject;


@Controller
public class StaticsController extends BaseController {

	private static final Logger logger = Logger
			.getLogger(StaticsController.class);

	StaticsApiImpl staticsApiImpl = new StaticsApiImpl();

	@RequestMapping(value = "/message/list")
	public ModelAndView list(HttpServletRequest request) {
		try {
			AccessToken tokenObj=(AccessToken) request.getAttribute("token");
			long userId = tokenObj.getUserId();
			
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			
			int subtaskId = dataJson.getInt("subtaskId");

			//SubtaskStatInfo data = staticsApiImpl.getStatBySubtask(subtaskId);
			return new ModelAndView("jsonView", success());
		} catch (Exception e) {
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	/**
	 * 应用场景：管理平台监控快线监控
	 */
	@RequestMapping(value = "/noRealStatics/quickMonitor")
	public ModelAndView quickMonitor(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			Map<String, Object> data = StaticsService.getInstance().quickMonitor();
			return new ModelAndView("jsonView", success(data));
		} catch (Exception e) {
			logger.error("查询失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	/**
	 *应用场景：管理平台监控全国中线项目生产监控
	 */
	@RequestMapping(value = "/noRealStatics/mediumMonitor")
	public ModelAndView mediumMonitor(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			Map<String, Object> data = StaticsService.getInstance().mediumMonitor();
			return new ModelAndView("jsonView", success(data));
		} catch (Exception e) {
			logger.error("查询失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	/**
	 *应用场景：管理平台监控快线监控—〉项目详情
	 *管理平台监控中线监控—〉项目详情
	 */
	@RequestMapping(value = "/noRealStatics/cityMonitor")
	public ModelAndView cityMonitor(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			
			int cityId = dataJson.getInt("cityId");
			Map<String, Object> data = StaticsService.getInstance().cityMonitor(cityId);
			return new ModelAndView("jsonView", success(data));
		} catch (Exception e) {
			logger.error("查询失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	/**
	 *应用场景：管理平台监控快线监控—〉项目详情
	 *管理平台监控中线监控—〉项目详情
	 */
	@RequestMapping(value = "/noRealStatics/blockMonitor")
	public ModelAndView blockMonitor(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			
			int blockId = dataJson.getInt("blockId");
			Map<String, Object> data = StaticsService.getInstance().blockMonitor(blockId);
			return new ModelAndView("jsonView", success(data));
		} catch (Exception e) {
			logger.error("查询失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
}
