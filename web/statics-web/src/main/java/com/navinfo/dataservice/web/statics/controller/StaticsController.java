package com.navinfo.dataservice.web.statics.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.api.statics.model.SubtaskStatInfo;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.engine.statics.service.StaticsApiImpl;

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

}
