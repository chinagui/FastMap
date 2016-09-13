package com.navinfo.dataservice.web.man.controller;

import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.api.statics.model.BlockExpectStatInfo;
import com.navinfo.dataservice.api.statics.model.GridChangeStatInfo;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.BaseController;
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
			JSONObject data = StaticsService.getInstance()
					.subtaskStatQuery(subtaskId);
			return new ModelAndView("jsonView", success(data));
		} catch (Exception e) {
			log.error("创建失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
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
			JSONObject data = StaticsService.getInstance().queryTaskOverView();
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
}
