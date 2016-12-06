package com.navinfo.dataservice.web.man.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.api.statics.model.GridChangeStatInfo;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.util.ResponseUtils;
import com.navinfo.dataservice.engine.man.statics.StaticsService;

@Controller
public class StaticsController extends BaseController {
	private Logger log = LoggerRepos.getLogger(this.getClass());

	/**
	 * grid变迁图统计查询 根据输入的范围和类型，查询范围内的所有grid的相应的变迁图统计信息，并返回grid列表和统计信息。
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/statics/change/grid/query")
	public ModelAndView queryGridChangeStat(HttpServletRequest request) {
		try {
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			String wkt = dataJson.getString("wkt");
			int type = dataJson.getInt("type");
			int stage = dataJson.getInt("stage");
			String date = dataJson.getString("date");
			List<GridChangeStatInfo> gridStatObjList = StaticsService
					.getInstance()
					.gridChangeStaticQuery(wkt, stage, type, date);
			return new ModelAndView("jsonView", success(gridStatObjList));
		} catch (Exception e) {
			log.error("创建失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}

	/**
	 * 查询block的预期图状态，根据wkt查询wkt范围内的所有block的预期状态
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/statics/expect/block/listByWkt")
	public ModelAndView listBlockExpectStatByWkt(HttpServletRequest request) {
		try {
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			String wkt = dataJson.getString("wkt");
			List<HashMap> data = StaticsService.getInstance()
					.blockExpectStatQuery(wkt);
			return new ModelAndView("jsonView", success(data));
		} catch (Exception e) {
			log.error("创建失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	/**
	 * 获取单个block的预期统计信息
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/statics/expect/block/query")
	public ModelAndView queryBlockExpectStat(HttpServletRequest request) {
		try {
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			int blockId = dataJson.getInt("blockId");
			int stage = dataJson.getInt("stage");
			HashMap gridStatObjList = StaticsService
					.getInstance()
					.blockExpectStatQuery(blockId, stage);
			return new ModelAndView("jsonView", success(gridStatObjList));
		} catch (Exception e) {
			log.error("创建失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	@RequestMapping(value = "/statics/expect/city/listByWkt")
	public ModelAndView listCityExpectStatByWkt(HttpServletRequest request) {
		try {
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			String wkt = dataJson.getString("wkt");
			List<HashMap> data = StaticsService.getInstance()
					.cityExpectStatQuery(wkt);
			return new ModelAndView("jsonView", success(data));
		} catch (Exception e) {
			log.error("创建失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	@RequestMapping(value = "/statics/subtask/query")
	public ModelAndView querySubtaskStat(HttpServletRequest request) {
		try {
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			int subtaskId = dataJson.getInt("subtaskId");
//			SubtaskStatInfo data = StaticsService.getInstance()
//					.subtaskStatQuery(subtaskId);		
//			//拼结果
//			Map<String,Object> poi = new HashMap<String,Object>();
//			poi.put("total", data.getTotalPoi());
//			poi.put("finish", data.getFinishPoi());
//			poi.put("working", data.getWorkingPoi());
//			
//			Map<String,Object> road = new HashMap<String,Object>();
//			road.put("total", data.getTotalRoad());
//			road.put("finish", data.getFinishRoad());
//			road.put("working", data.getWorkingRoad());
//			
//			Map<String,Object> result = new HashMap<String,Object>();
//			result.put("subtaskId", data.getSubtaskId());
//			result.put("percent", data.getPercent());
//			result.put("poi", poi);
//			result.put("road", road);
			Map<String,Object> data = StaticsService.getInstance().subtaskStatQuery(subtaskId);
			return new ModelAndView("jsonView", success(data));
		} catch (Exception e) {
			log.error("创建失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	@RequestMapping(value = "/statics/monthTask/overview")
	public ModelAndView querymonthTaskOverView(HttpServletRequest request) {
		try {
			JSONObject data = StaticsService.getInstance().querymonthTaskOverView();
			return new ModelAndView("jsonView", success(data));
		} catch (Exception e) {
			log.error("创建失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	@RequestMapping(value = "/statics/collect/overview")
	public ModelAndView queryCollectOverView(HttpServletRequest request) {
		try {
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			int groupId=dataJson.getInt("groupId");
			JSONObject data = StaticsService.getInstance().queryCollectOverView(groupId);
			return new ModelAndView("jsonView", success(data));
		} catch (Exception e) {
			log.error("创建失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	@RequestMapping(value = "/statics/dayEdit/overview")
	public ModelAndView queryDayEidtOverView(HttpServletRequest request) {
		try {
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			int groupId=dataJson.getInt("groupId");
			JSONObject data = StaticsService.getInstance().queryDayEidtOverView(groupId);
			return new ModelAndView("jsonView", success(data));
		} catch (Exception e) {
			log.error("创建失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	//根据groupId获取block详情
	@RequestMapping(value = "/statics/block/overviewByGroup")
	public ModelAndView queryBlockOverViewByGroup(HttpServletRequest request) {
		try {
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			//groupId
			int groupId = dataJson.getInt("groupId");
			int stage = dataJson.getInt("stage");
			Map<String,Object> data = StaticsService.getInstance().queryBlockOverViewByGroup(groupId,stage);
			return new ModelAndView("jsonView", success(data));
		} catch (Exception e) {
			log.error("创建失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}


		/**
		 * @Title: queryGroupOverView
		 * @Description: 根据groupid获取block及group详情
		 * @param request
		 * @return  ModelAndView
		 * @throws 
		 * @author zl zhangli5174@navinfo.com
		 * @date 2016年10月19日 上午10:54:15 
		 */
		@RequestMapping(value = "/statics/groupOverview")
		public ModelAndView queryGroupOverView(HttpServletRequest request) {
			try {
				String parameter = request.getParameter("parameter");
				if (StringUtils.isEmpty(parameter)) {
					throw new IllegalArgumentException("parameter参数不能为空。");
				}
				JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));
				if (dataJson == null) {
					throw new IllegalArgumentException("parameter参数不能为空。");
				}
				//groupId
				int groupId = dataJson.getInt("groupId");
				int stage = dataJson.getInt("stage");
				//查询group详情
				Map<String,Object> data = StaticsService.getInstance().queryGroupOverView(groupId,stage);
				return new ModelAndView("jsonView", success(data));
			} catch (Exception e) {
				log.error("创建失败，原因：" + e.getMessage(), e);
				return new ModelAndView("jsonView", exception(e));
			}
		}
	
	
	//根据taskId获取block详情
	@RequestMapping(value = "/statics/block/overviewByTask")
	public ModelAndView queryBlockOverViewByTask(HttpServletRequest request) {
		try {
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			//taskId
			int taskId = dataJson.getInt("taskId");
			int type = dataJson.getInt("type");
			Map<String, Object> data = StaticsService.getInstance().queryBlockOverViewByTask(taskId,type);
			return new ModelAndView("jsonView", success(data));
		} catch (Exception e) {
			log.error("创建失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	//根据blockId获取subtask统计概览
	@RequestMapping(value = "/statics/subtask/overview")
	public ModelAndView querySubtaskOverView(HttpServletRequest request) {
		try {
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			//blockManId
			int blockManId = 0;
			int taskId = 0;
			//月编
			if(dataJson.containsKey("taskId")){
				taskId = dataJson.getInt("taskId");
			}else{
				blockManId = dataJson.getInt("blockManId");
			}
			Map<String, Object> data = StaticsService.getInstance().querySubtaskOverView(blockManId,taskId);
			return new ModelAndView("jsonView", success(data));
		} catch (Exception e) {
			log.error("创建失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	//全国task统计概览
	@RequestMapping(value = "/statics/task/overview")
	public ModelAndView queryTaskOverView(HttpServletRequest request) {
		try {
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			//1常规，2多源，3代理店，4情报
			int taskType=dataJson.getInt("taskType");
			Map<String, Object> data = StaticsService.getInstance().queryTaskOverView(taskType);
			return new ModelAndView("jsonView", success(data));
		} catch (Exception e) {
			log.error("创建失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	/**
	 * 各城市生产情况概览
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/statics/overview")
	public ModelAndView queryCityOverview(HttpServletRequest request){
		try {
			//查询数据
			Map<String, Object> data = StaticsService.getInstance().queryCityOverview();
			return new ModelAndView("jsonView", success(data));
		} catch (Exception e) {
			log.error("创建失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	@RequestMapping(value = "/statics/poiStatusMap")
	public void getPoiStatusMap(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String parameter = request.getParameter("parameter");
		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);
			String wkt="";
			if (jsonReq.containsKey("wkt")) {
				wkt=jsonReq.getString("wkt");
			}
			int stage=0;
			if (jsonReq.containsKey("stage")) {
				stage=jsonReq.getInt("stage");
			}
			List<Map<String, Object>> returnList =StaticsService.getInstance().getPoiStatusMap(wkt,stage);
			response.getWriter().println(
					ResponseUtils.assembleRegularResult(returnList));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			response.getWriter().println(
					ResponseUtils.assembleFailResult(e.getMessage()));
		} 
	}
}
