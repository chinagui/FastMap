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

import com.navinfo.dataservice.api.man.model.City;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.engine.man.city.CityService;

import net.sf.json.JSONObject;

/**
 * @ClassName: CityController
 * @author code generator
 * @date 2016年4月6日 下午6:25:24
 * @Description: TODO
 */
@Controller
public class CityController extends BaseController {
	private Logger log = LoggerRepos.getLogger(this.getClass());
	@Autowired
	private CityService service;

	@RequestMapping(value = "/city/create")
	public ModelAndView create(HttpServletRequest request) {
		try {
			String parameter = request.getParameter("param");
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("param参数不能为空。");
			}
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));
			if (dataJson == null) {
				throw new IllegalArgumentException("param参数不能为空。");
			}
			service.create(dataJson);
			return new ModelAndView("jsonView", success("创建成功"));
		} catch (Exception e) {
			log.error("创建失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}

	@RequestMapping(value = "/city/update")
	public ModelAndView update(HttpServletRequest request) {
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("param")));
			if (dataJson == null) {
				throw new IllegalArgumentException("param参数不能为空。");
			}
			service.update(dataJson);
			return new ModelAndView("jsonView", success("修改成功"));
		} catch (Exception e) {
			log.error("修改失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}

	@RequestMapping(value = "/city/delete")
	public ModelAndView delete(HttpServletRequest request) {
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("param")));
			if (dataJson == null) {
				throw new IllegalArgumentException("param参数不能为空。");
			}
			JSONObject obj = JSONObject.fromObject(dataJson);
			City bean = (City) JSONObject.toBean(obj, City.class);
			service.delete(bean);
			return new ModelAndView("jsonView", success("删除成功"));
		} catch (Exception e) {
			log.error("删除失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}

	@RequestMapping(value = "/city/listByWkt")
	public ModelAndView queryListByWkt(HttpServletRequest request) {
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			final String wkt = dataJson.getString("wkt");
			int planningStatus = dataJson.getInt("planningStatus");
			List<HashMap> data = service.queryListByWkt(dataJson);
			return new ModelAndView("jsonView", success(data));
		} catch (Exception e) {
			log.error("获取城市列表失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}

	@RequestMapping(value = "/city/query")
	public ModelAndView query(HttpServletRequest request) {
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("param")));
			if (dataJson == null) {
				throw new IllegalArgumentException("param参数不能为空。");
			}
			JSONObject obj = null;
			City bean = (City) JSONObject.toBean(obj, City.class);
			HashMap data = service.query(dataJson);
			return new ModelAndView("jsonView", success(data));
		} catch (Exception e) {
			log.error("获取明细失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
}
