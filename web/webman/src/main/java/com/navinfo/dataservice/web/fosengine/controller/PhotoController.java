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

import com.navinfo.dataservice.commons.util.Log4jUtils;
import com.navinfo.dataservice.commons.util.ResponseUtils;
import com.navinfo.dataservice.engine.photo.PhotoGetter;

@Controller
public class PhotoController {

	private static final Logger logger = Logger
			.getLogger(PhotoController.class);

	@RequestMapping(value = "/fcc/photo/getBySpatial")
	public void getVersion(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String wkt = jsonReq.getString("wkt");

			PhotoGetter getter = new PhotoGetter();

			JSONArray array = getter.getPhotoBySpatial(wkt);

			response.getWriter().println(
					ResponseUtils.assembleRegularResult(array));

		} catch (Exception e) {

			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtils.assembleFailResult(e.getMessage(), logid));
		}

	}

	@RequestMapping(value = "/fcc/photo/getDetailByRowkey")
	public void getDetailByRowkey(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String uuid = jsonReq.getString("rowkey");

			PhotoGetter getter = new PhotoGetter();

			JSONObject photo = getter.getPhotoDetailByRowkey(uuid);

			response.getWriter().println(
					ResponseUtils.assembleRegularResult(photo));

		} catch (Exception e) {

			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtils.assembleFailResult(e.getMessage(), logid));
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

			PhotoGetter getter = new PhotoGetter();

			byte[] data = getter.getPhotoByRowkey(uuid, type);

			response.getOutputStream().write(data);

		} catch (Exception e) {

			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtils.assembleFailResult(e.getMessage(), logid));
		}

	}
}
