package com.navinfo.dataservice.web.fosengine.controller;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.navinfo.dataservice.commons.config.SystemConfig;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.util.Log4jUtils;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.commons.util.ZipUtils;
import com.navinfo.dataservice.fosengine.fcc.tips.TipsExporter;
import com.navinfo.dataservice.fosengine.fcc.tips.TipsOperator;
import com.navinfo.dataservice.fosengine.fcc.tips.TipsSelector;
import com.navinfo.dataservice.web.util.ResponseUtil;

@Controller
public class TipsController {

	private static final Logger logger = Logger
			.getLogger(TipsController.class);

	@RequestMapping(value = "/fcc/tip/checkUpdate")
	public void checkUpdate(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String grid = jsonReq.getString("grid");

			String date = jsonReq.getString("date");

			TipsSelector selector = new TipsSelector(
					SystemConfig.getSystemConfig().getValue(PropConstant.solrAddress));

			response.getWriter().println(
					ResponseUtil.assembleRegularResult(selector.checkUpdate(
							grid, date)));

		} catch (Exception e) {

			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtil.assembleFailResult(e.getMessage(), logid));
		}
	}

	@RequestMapping(value = "/fcc/tip/edit")
	public void edit(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String rowkey = jsonReq.getString("rowkey");

			int stage = jsonReq.getInt("stage");

			int handler = jsonReq.getInt("handler");

			int pid = -1;

			if (jsonReq.containsKey("pid")) {
				pid = jsonReq.getInt("pid");
			}

			TipsOperator op = new TipsOperator(
					SystemConfig.getSystemConfig().getValue(PropConstant.solrAddress));
			
			op.update(rowkey, stage, handler, pid);

			response.getWriter().println(
					ResponseUtil.assembleRegularResult(null));

		} catch (Exception e) {

			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtil.assembleFailResult(e.getMessage(), logid));
		}
	}

	@RequestMapping(value = "/fcc/tip/export")
	public void export(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String day = StringUtils.getCurrentDay();

			String uuid = UuidUtils.genUuid();

			String parentPath = "/root/download/tips/" + day + "/";

			String filePath = parentPath + uuid + "/";

			File file = new File(filePath);

			if (!file.exists()) {
				file.mkdirs();
			}

			String date = jsonReq.getString("date");

			TipsExporter op = new TipsExporter(
					SystemConfig.getSystemConfig().getValue(PropConstant.solrAddress));

			JSONArray grids = jsonReq.getJSONArray("grids");

			op.export(grids, date, filePath, uuid + ".txt");

			String zipFileName = uuid + ".zip";

			String zipFullName = parentPath + zipFileName;

			ZipUtils.zipFile(filePath, zipFullName);

			String url = "http://192.168.4.130:8080/download/tips/" + day + "/"
					+ zipFileName;

			response.getWriter().println(
					ResponseUtil.assembleRegularResult(url));

		} catch (Exception e) {

			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtil.assembleFailResult(e.getMessage(), logid));
		}
	}

	@RequestMapping(value = "/fcc/tip/getByRowkey")
	public void getByRowkey(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String rowkey = jsonReq.getString("rowkey");

			TipsSelector selector = new TipsSelector(
					SystemConfig.getSystemConfig().getValue(PropConstant.solrAddress));

			JSONObject data = selector.searchDataByRowkey(rowkey);

			response.getWriter().println(
					ResponseUtil.assembleRegularResult(data));

		} catch (Exception e) {

			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtil.assembleFailResult(e.getMessage(), logid));
		}
	}

	@RequestMapping(value = "/fcc/tip/getBySpatial")
	public void getBySpatial(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String wkt = jsonReq.getString("wkt");

			TipsSelector selector = new TipsSelector(
					SystemConfig.getSystemConfig().getValue(PropConstant.solrAddress));

			JSONArray array = selector.searchDataBySpatial(wkt);

			response.getWriter().println(
					ResponseUtil.assembleRegularResult(array));

		} catch (Exception e) {

			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtil.assembleFailResult(e.getMessage(), logid));
		}
	}

	@RequestMapping(value = "/fcc/tip/getSnapshot")
	public void getSnapshot(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			JSONArray grids = jsonReq.getJSONArray("grids");

			int type = jsonReq.getInt("type");

			JSONArray stage = jsonReq.getJSONArray("stage");

			int projectId = jsonReq.getInt("projectId");

			TipsSelector selector = new TipsSelector(
					SystemConfig.getSystemConfig().getValue(PropConstant.solrAddress));

			JSONArray array = selector.getSnapshot(grids, stage, type,
					projectId);

			response.getWriter().println(
					ResponseUtil.assembleRegularResult(array));

		} catch (Exception e) {

			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtil.assembleFailResult(e.getMessage(), logid));
		}
	}

	@RequestMapping(value = "/fcc/tip/getStats")
	public void getStats(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			JSONArray grids = jsonReq.getJSONArray("grids");

			JSONArray stages = jsonReq.getJSONArray("stage");

			TipsSelector selector = new TipsSelector(
					SystemConfig.getSystemConfig().getValue(PropConstant.solrAddress));

			JSONObject jo = selector.getStats(grids, stages);

			response.getWriter().println(
					ResponseUtil.assembleRegularResult(jo));

		} catch (Exception e) {

			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtil.assembleFailResult(e.getMessage(), logid));
		}
	}
}
