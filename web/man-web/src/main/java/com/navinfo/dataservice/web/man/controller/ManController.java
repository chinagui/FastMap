package com.navinfo.dataservice.web.man.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.navinfo.dataservice.commons.util.Log4jUtils;
import com.navinfo.dataservice.commons.util.ResponseUtils;
import com.navinfo.dataservice.engine.man.grid.GridSelector;
import com.navinfo.dataservice.engine.man.project.ProjectSelector;
import com.navinfo.dataservice.engine.man.version.VersionSelector;

@Controller
public class ManController {

	private static final Logger logger = Logger
			.getLogger(ManController.class);

	@RequestMapping(value = "/getByUser")
	public void getProjectByUser(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			int userId = jsonReq.getInt("userId");

			ProjectSelector selector = new ProjectSelector();

			JSONArray array = selector.getByUser(userId);

			response.getWriter().println(
					ResponseUtils.assembleRegularResult(array));

		} catch (Exception e) {

			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtils.assembleFailResult(e.getMessage(), logid));
		}
	}

	@RequestMapping(value = "/grid/getByUser")
	public void getGridByUser(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			int userId = jsonReq.getInt("userId");

			int projectId = jsonReq.getInt("projectId");

			GridSelector selector = new GridSelector();

			JSONObject result = selector.getByUser(userId, projectId);

			response.getWriter().println(
					ResponseUtils.assembleRegularResult(result));

		} catch (Exception e) {

			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtils.assembleFailResult(e.getMessage(), logid));
		}
	}
	@RequestMapping(value = "/version/get")
	public void getVersion(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			VersionSelector selector = new VersionSelector();

			if (jsonReq.containsKey("type")) {

				int type = jsonReq.getInt("type");

				String version = selector.getByType(type);

				JSONObject json = new JSONObject();

				json.put("specVersion", version);

				json.put("type", type);

				response.getWriter().println(
						ResponseUtils.assembleRegularResult(json));
			}

			else {
				JSONArray array = selector.getList();

				response.getWriter().println(
						ResponseUtils.assembleRegularResult(array));

			}

		} catch (Exception e) {

			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtils.assembleFailResult(e.getMessage(), logid));
		}
	}
}
