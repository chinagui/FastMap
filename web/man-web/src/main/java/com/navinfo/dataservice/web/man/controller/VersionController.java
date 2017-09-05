package com.navinfo.dataservice.web.man.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.engine.man.version.VersionService;

@Controller
public class VersionController extends BaseController {
	private Logger log = LoggerRepos.getLogger(this.getClass());
	@Autowired
	private VersionService service;

	@RequestMapping(value = "/version/get")
	public ModelAndView getVersion(HttpServletRequest request) {
		try {
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			int type = dataJson.getInt("type");
			
			String version = service.query(type);
			
			JSONObject json = new JSONObject();
			
			json.put("specVersion", version);
			
			json.put("type", type);
			
			return new ModelAndView("jsonView", success(json));
		} catch (Exception e) {
			log.error("创建失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	@RequestMapping(value = "/version/getAppVersion")
	public ModelAndView getAppVersion(HttpServletRequest request) {
		try {
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			//android,ios
			String appPlatform = dataJson.getString("appPlatform");
			//1.一体化  2.精细化
			int appType=dataJson.getInt("appType");
			
			Map<String, Object> res= service.getAppVersion(appPlatform,appType);
			return new ModelAndView("jsonView", success(res));
		} catch (Exception e) {
			log.error("创建失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	@RequestMapping(value = "/version/getAppVersionNoToken")
	public ModelAndView getAppVersionNoToken(HttpServletRequest request) {
		try {
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			//android,ios
			String appPlatform = dataJson.getString("appPlatform");
			//1.一体化  2.精细化
			int appType=dataJson.getInt("appType");
			
			Map<String, Object> res= service.getAppVersion(appPlatform,appType);
			return new ModelAndView("jsonView", success(res));
		} catch (Exception e) {
			log.error("创建失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
}
