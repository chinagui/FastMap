package com.navinfo.dataservice.web.fosengine.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.navinfo.dataservice.FosEngine.photos.PhotoGetter;
import com.navinfo.dataservice.commons.util.Log4jUtils;
import com.navinfo.dataservice.web.util.ResponseUtil;

@Controller
public class PhotoController {

	private static final Logger logger = Logger
			.getLogger(PhotoController.class);

	@RequestMapping(value = "/fcc/photo/getBySpatial")
	public void getVersion(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		ResponseUtil.setResponseHeader(response);

		String parameter = request.getParameter("parameter");

		try {

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String wkt = jsonReq.getString("wkt");

			JSONArray array = PhotoGetter.getPhotoBySpatial(wkt);

			response.getWriter().println(
					ResponseUtil.assembleRegularResult(array));

		} catch (Exception e) {

			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtil.assembleFailResult(e.getMessage(), logid));
		}

	}

	@RequestMapping(value = "/fcc/photo/getDetailByRowkey")
	public void getDetailByRowkey(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		ResponseUtil.setResponseHeader(response);

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String uuid = jsonReq.getString("rowkey");

			JSONObject photo = PhotoGetter.getPhotoDetailByUuid(uuid);

			response.getWriter().println(
					ResponseUtil.assembleRegularResult(photo));

		} catch (Exception e) {

			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtil.assembleFailResult(e.getMessage(), logid));
		}

	}

	@RequestMapping(value = "/fcc/photo/getSnapshotByRowkey")
	public void getSnapshotByRowkey(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("image/jpeg;charset=GBK");

		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods",
				"POST, GET, OPTIONS, DELETE,PUT");

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String uuid = jsonReq.getString("rowkey");

			String type = jsonReq.getString("type");

			byte[] data = PhotoGetter.getPhotoByUuid(uuid, type);

			response.getOutputStream().write(data);

		} catch (Exception e) {

			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtil.assembleFailResult(e.getMessage(), logid));
		}

	}
}
