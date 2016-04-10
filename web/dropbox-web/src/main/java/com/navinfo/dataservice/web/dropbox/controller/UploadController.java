package com.navinfo.dataservice.web.dropbox.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.navinfo.dataservice.commons.util.Log4jUtils;
import com.navinfo.dataservice.commons.util.ResponseUtils;
import com.navinfo.dataservice.engine.dropbox.manger.UploadManager;

@Controller
public class UploadController {
	private static final Logger logger = Logger
			.getLogger(UploadController.class);

	@RequestMapping(value = "/upload/start")
	public void start(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {

			JSONObject json = JSONObject.fromObject(parameter);

			String fileName = json.getString("fileName");

			String md5 = json.getString("md5");

			int fileSize = json.getInt("fileSize");

			int chunkSize = json.getInt("chunkSize");

			UploadManager upload = new UploadManager();

			int jobId = upload.startUpload(fileName, md5, fileSize, chunkSize);

			response.getWriter().println(
					ResponseUtils.assembleRegularResult(jobId));

		} catch (Exception e) {
			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtils.assembleFailResult(e.getMessage(), logid));
		}

	}

	@RequestMapping(value = "/upload/check")
	public void check(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {

			JSONObject json = JSONObject.fromObject(parameter);

			int jobId = json.getInt("jobId");

			UploadManager upload = new UploadManager();

			String chunkList = upload.checkChunk(jobId);

			response.getWriter().println(
					ResponseUtils.assembleRegularResult(chunkList));

		} catch (Exception e) {
			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtils.assembleFailResult(e.getMessage(), logid));
		}

	}

	@RequestMapping(value = "/upload/chunk")
	public void chunk(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {

			UploadManager upload = new UploadManager();

			upload.uploadChunk(request);

			response.getWriter().println(
					ResponseUtils.assembleRegularResult(null));
		} catch (Exception e) {
			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtils.assembleFailResult(e.getMessage(), logid));
		}
	}

	@RequestMapping(value = "/upload/finish")
	public void finish(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {

			JSONObject json = JSONObject.fromObject(parameter);

			int jobId = json.getInt("jobId");

			UploadManager upload = new UploadManager();

			upload.finishUpload(jobId);

			response.getWriter().println(
					ResponseUtils.assembleRegularResult(null));

		} catch (Exception e) {
			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtils.assembleFailResult(e.getMessage(), logid));
		}

	}
}
