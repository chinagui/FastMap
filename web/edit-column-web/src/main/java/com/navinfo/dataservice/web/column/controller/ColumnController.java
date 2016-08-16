package com.navinfo.dataservice.web.column.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.control.column.core.ColumCoreControl;

import net.sf.json.JSONObject;

@Controller
public class ColumnController extends BaseController {
	private static final Logger logger = Logger.getLogger(ColumnController.class);
	
	@RequestMapping(value = "/poi/deep/applyPoi")
	public ModelAndView applyPoi(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);
			
			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			
			long userId = tokenObj.getUserId();

			String firstWorkItem = jsonReq.getString("firstWorkItem");

			int groupId = jsonReq.getInt("groupId");


			ColumCoreControl control = new ColumCoreControl();

			control.applyData(groupId, firstWorkItem, userId);

			return new ModelAndView("jsonView", success());

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}

}
