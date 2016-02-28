package com.navinfo.dataservice.web.fosengine.controller;

import java.io.IOException;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.navinfo.dataservice.FosEngine.edit.check.NiValExceptionOperator;
import com.navinfo.dataservice.FosEngine.edit.check.NiValExceptionSelector;
import com.navinfo.dataservice.commons.db.DBOraclePoolManager;
import com.navinfo.dataservice.commons.util.Log4jUtils;
import com.navinfo.dataservice.web.util.ResponseUtil;

@Controller
public class CheckController {
	private static final Logger logger = Logger
			.getLogger(CheckController.class);

	@RequestMapping(value = "/check/get")
	public void getCheck(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		ResponseUtil.setResponseHeader(response);

		String parameter = request.getParameter("parameter");

		try {

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			int projectId = jsonReq.getInt("projectId");

			JSONArray meshes = jsonReq.getJSONArray("meshes");

			int pageSize = jsonReq.getInt("pageSize");

			int pageNum = jsonReq.getInt("pageNum");

			Connection conn = DBOraclePoolManager.getConnection(projectId);

			NiValExceptionSelector selector = new NiValExceptionSelector(conn);

			JSONArray result = selector.queryException(meshes, pageSize,
					pageNum);

			response.getWriter().println(
					ResponseUtil.assembleRegularResult(result));

		} catch (Exception e) {

			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtil.assembleFailResult(e.getMessage(), logid));
		}
	}

	@RequestMapping(value = "/check/count")
	public void getCheckCount(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		ResponseUtil.setResponseHeader(response);

		String parameter = request.getParameter("parameter");

		try {

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			int projectId = jsonReq.getInt("projectId");

			JSONArray meshes = jsonReq.getJSONArray("meshes");

			Connection conn = DBOraclePoolManager.getConnection(projectId);

			NiValExceptionSelector selector = new NiValExceptionSelector(conn);

			int result = selector.queryExceptionCount(meshes);

			response.getWriter().println(
					ResponseUtil.assembleRegularResult(result));

		} catch (Exception e) {

			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtil.assembleFailResult(e.getMessage(), logid));
		}
	}

	@RequestMapping(value = "/check/update")
	public void updateCheck(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		ResponseUtil.setResponseHeader(response);

		String parameter = request.getParameter("parameter");

		try {

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			int projectId = jsonReq.getInt("projectId");

			String id = jsonReq.getString("id");

			int type = jsonReq.getInt("type");

			NiValExceptionOperator selector = new NiValExceptionOperator();

			selector.updateCheckLogStatus(id, projectId, type);

			response.getWriter().println(
					ResponseUtil.assembleRegularResult(null));

		} catch (Exception e) {

			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtil.assembleFailResult(e.getMessage(), logid));
		}
	}
}
