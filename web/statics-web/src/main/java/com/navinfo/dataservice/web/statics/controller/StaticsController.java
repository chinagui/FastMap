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

import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.engine.statics.service.StaticsApiImpl;
import com.navinfo.dataservice.engine.statics.service.StaticsService;

import net.sf.json.JSONArray;
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
			
			int cityId =0;
			if(dataJson.containsKey("cityId")){cityId=dataJson.getInt("cityId");}
			int programId =0;
			if(dataJson.containsKey("programId")){programId=dataJson.getInt("programId");}
			Map<String, Object> data = StaticsService.getInstance().cityMonitor(cityId,programId);
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
	
	/**
	 *应用场景：管理/监控_大屏众包展示
	 */
	@RequestMapping(value = "/crowdInfo")
	public ModelAndView crowdInfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			
			JSONArray data = StaticsService.getInstance().crowdInfoList();
			
			return new ModelAndView("jsonView", success(data));
		} catch (Exception e) {
			logger.error("查询失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	/**
	 *应用场景：管理/监控_大屏自采展示
	 *原则：创建外业采集子任务的city返回（subtask表work_kind=1，status in (1,0)的city）
	 */
	@RequestMapping(value = "/commonInfo")
	public ModelAndView commonInfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			
			JSONArray data = StaticsService.getInstance().commonInfoListCity();
			
			return new ModelAndView("jsonView", success(data));
		} catch (Exception e) {
			logger.error("查询失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	/**
	 *应用场景: 管理/监控_大屏详细统计
	 *原则: Man_config中有默认值,若没有具体的统计结果,则取默认值返回
	 */
	@RequestMapping(value = "/productMonitor")
	public ModelAndView productMonitor(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			
			JSONObject data = new JSONObject();
			String platForm = "productMonitor";
			data.putAll(StaticsService.getInstance().getOracleMonitorData(platForm));
			data.putAll(StaticsService.getInstance().getMongoMonitorData());
//			JSONObject mongoData = StaticsService.getInstance().getMongoMonitorData();
//			@SuppressWarnings("unchecked")
//			Map<String, Object> map = mongoData;
//			if(map != null && !map.isEmpty()){
//				for(Map.Entry<String, Object> entry: map.entrySet()){
//					if(data.containsKey(entry.getKey())){
//						if(entry.getValue() instanceof Integer){
//							if((Integer)entry.getValue() != 0){
//								data.put(entry.getKey(), entry.getValue());
//							}
//						}else{
//							data.put(entry.getKey(), entry.getValue());
//						}
//					}else{
//						data.put(entry.getKey(), entry.getValue());
//					}
//				}
//			}
			
			return new ModelAndView("jsonView", success(data));
		} catch (Exception e) {
			logger.error("查询失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	
}
