package com.navinfo.dataservice.web.dropbox.controller;

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
import com.navinfo.dataservice.engine.dropbox.manger.DownloadManager;

@Controller
public class DownloadController {
	private static final Logger logger = Logger
			.getLogger(DownloadController.class);

	@RequestMapping(value = "/download/prjbasedata")
	public void getBasedata(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {

			JSONObject json = JSONObject.fromObject(parameter);

			String prjId = json.getString("prjId");

			DownloadManager manager = new DownloadManager();

			JSONObject data = manager.getBasedata(prjId);

			response.getWriter().println(
					ResponseUtils.assembleRegularResult(data));

		} catch (Exception e) {
			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtils.assembleFailResult(e.getMessage(), logid));
		}

	}

	@RequestMapping(value = "/download/fmgdblist")
	public void getBasedataList(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		try {

			DownloadManager manager = new DownloadManager();

			JSONArray data = manager.getBasedataList();

			response.getWriter().println(
					ResponseUtils.assembleRegularResult(data));

		} catch (Exception e) {
			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, null, e);

			response.getWriter().println(
					ResponseUtils.assembleFailResult(e.getMessage(), logid));
		}

	}

	@RequestMapping(value = "/download/fmndslist")
	public void getNdsList(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		try {

			DownloadManager manager = new DownloadManager();

			JSONArray data = manager.getNdsList();

			response.getWriter().println(
					ResponseUtils.assembleRegularResult(data));

		} catch (Exception e) {
			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, null, e);

			response.getWriter().println(
					ResponseUtils.assembleFailResult(e.getMessage(), logid));
		}

	}

	@RequestMapping(value = "/download/fmnds")
	public void getNds(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {

			JSONObject json = JSONObject.fromObject(parameter);

			String id = json.getString("id");

			DownloadManager manager = new DownloadManager();

			JSONObject data = manager.getNds(id);

			response.getWriter().println(
					ResponseUtils.assembleRegularResult(data));

		} catch (Exception e) {
			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, null, e);

			response.getWriter().println(
					ResponseUtils.assembleFailResult(e.getMessage(), logid));
		}

	}

	@RequestMapping(value = "/download/patternimage")
	public void getPatternimg(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		try {

			DownloadManager manager = new DownloadManager();

			JSONObject data = manager.getPatternimg();

			response.getWriter().println(
					ResponseUtils.assembleRegularResult(data));

		} catch (Exception e) {
			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, null, e);

			response.getWriter().println(
					ResponseUtils.assembleFailResult(e.getMessage(), logid));
		}

	}
}
