package com.navinfo.dataservice.web.man.controller;

import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.engine.man.grid.GridService;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.BaseController;

import net.sf.json.JSONObject;

/**
 * @ClassName: GridController
 * @author code generator
 * @date 2016年4月6日 下午6:25:24
 * @Description: TODO
 */
@Controller
public class GridController extends BaseController {
	private Logger log = LoggerRepos.getLogger(this.getClass());
	@Autowired
	private GridService service;

	/**
	 * 作业管理--采集管理--采集子任务范围选择，可对待分配、已分配的所有grid进行操作
	 * 根据输入的几何，查询几何范围内的grid，获取grid的分配状态，并返回grid列表
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/grid/listByAlloc/")
	public ModelAndView queryListByWkt(HttpServletRequest request) {
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			if (!dataJson.containsKey("wkt") || !dataJson.containsKey("stage") || !dataJson.containsKey("type")){
				throw new IllegalArgumentException("wkt/stage/type不能为空");
			}
			List<HashMap> data = service.quryListByAlloc(dataJson);
			return new ModelAndView("jsonView", success(data));
		} catch (Exception e) {
			log.error("获取grid列表失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}

}
