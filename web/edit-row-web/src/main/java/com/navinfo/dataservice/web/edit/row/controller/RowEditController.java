package com.navinfo.dataservice.web.edit.row.controller;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.control.row.day2mon.Day2Mon;
import com.navinfo.dataservice.control.row.query.PoiQuery;
import com.navinfo.dataservice.control.row.release.PoiRelease;
import com.navinfo.dataservice.control.row.save.PoiSave;

import net.sf.json.JSONObject;

@Controller
public class RowEditController extends BaseController {
	private static final Logger logger = Logger
			.getLogger(RowEditController.class);

	@RequestMapping(value = "/run")
	public ModelAndView run(HttpServletRequest request) throws Exception {

		String parameter = request.getParameter("parameter");
		AccessToken tokenObj = (AccessToken) request.getAttribute("token");
		try {

			PoiSave poiSave = new PoiSave();
			JSONObject result = poiSave.save(parameter, tokenObj.getUserId());

			return new ModelAndView("jsonView", success(result));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}

	@RequestMapping(value = "/poi/base/list")
	public ModelAndView getPoiList(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");
		JSONObject jsonReq = JSONObject.fromObject(parameter);
		try {
			if (StringUtils.isEmpty(parameter)||jsonReq == null|| jsonReq.isNullObject()){
				return new ModelAndView("jsonView", fail("parameter参数不能为空"));
			}
			if(jsonReq.has("type")){
				int type = jsonReq.getInt("type");
				Integer[] typeArray = {1,2,3};
				if (!(Arrays.asList(typeArray).contains(type))){
					return new ModelAndView("jsonView", fail("type参数值域不在[1,2,3]内"));
				}
			}
			PoiQuery poiQuery = new PoiQuery();
			JSONObject result = poiQuery.getPoiList(parameter);
			return new ModelAndView("jsonView", success(result));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}

	@RequestMapping(value = "/poi/base/count")
	public ModelAndView getPoiBaseCount(HttpServletRequest request)
			throws Exception {
		try {
			String parameter = request.getParameter("parameter");
			PoiQuery poiQuery = new PoiQuery();
			JSONObject jsonObject = poiQuery.getPoiCount(parameter);
			return new ModelAndView("jsonView", success(jsonObject));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}

	/**
	 * POI提交 根据所选grid进行POI数据的提交，自动执行检查、批处理
	 * 
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping(value = "/poi/base/release")
	public ModelAndView getPoiBaseRelease(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");
		AccessToken tokenObj = (AccessToken) request.getAttribute("token");
		try {
			PoiRelease poiRelease = new PoiRelease();
			long jobId = poiRelease.release(parameter, tokenObj.getUserId());
			return new ModelAndView("jsonView", success(jobId));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}
	@RequestMapping(value = "/poi/base/day2mon/")
	public ModelAndView day2mon(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");
		AccessToken tokenObj = (AccessToken) request.getAttribute("token");
		try {
			Day2Mon day2Mon = new Day2Mon();
			long jobId = day2Mon.sync(parameter, tokenObj.getUserId());
			return new ModelAndView("jsonView", success(jobId));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}
}
