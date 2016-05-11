package com.navinfo.dataservice.web.fcc.controller;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.navinfo.dataservice.commons.config.SystemConfig;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.photo.Photo;
import com.navinfo.dataservice.commons.util.Log4jUtils;
import com.navinfo.dataservice.commons.util.ResponseUtils;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.commons.util.ZipUtils;
import com.navinfo.dataservice.engine.dropbox.manger.UploadManager;
import com.navinfo.dataservice.engine.fcc.tips.TipsExporter;
import com.navinfo.dataservice.engine.fcc.tips.TipsOperator;
import com.navinfo.dataservice.engine.fcc.tips.TipsSelector;
import com.navinfo.dataservice.engine.fcc.tips.TipsUpload;
import com.navinfo.dataservice.engine.photo.CollectorImport;

@Controller
public class TipsController {

	private static final Logger logger = Logger.getLogger(TipsController.class);

	@RequestMapping(value = "/tip/checkUpdate")
	public void checkUpdate(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String grid = jsonReq.getString("grid");

			String date = jsonReq.getString("date");

			TipsSelector selector = new TipsSelector();

			response.getWriter().println(
					ResponseUtils.assembleRegularResult(selector.checkUpdate(
							grid, date)));

		} catch (Exception e) {

			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtils.assembleFailResult(e.getMessage(), logid));
		}
	}

	@RequestMapping(value = "/tip/edit")
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

			TipsOperator op = new TipsOperator();

			op.update(rowkey, stage, handler, pid);

			response.getWriter().println(
					ResponseUtils.assembleRegularResult(null));

		} catch (Exception e) {

			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtils.assembleFailResult(e.getMessage(), logid));
		}
	}
	
	@RequestMapping(value = "/tip/import")
	public void importTips(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {

			JSONObject json = JSONObject.fromObject(parameter);

			int jobId = json.getInt("jobId");

			UploadManager upload = new UploadManager();

			String filePath = upload.unzipByJobId(jobId);

			TipsUpload tipsUploader = new TipsUpload();

			Map<String, Photo> map = tipsUploader.run(filePath + "/"
					+ "tips.txt");

			CollectorImport.importPhoto(map, filePath + "/photo");

			JSONObject result = new JSONObject();

			result.put("total", tipsUploader.getTotal());

			result.put("failed", tipsUploader.getFailed());

			result.put("reasons", tipsUploader.getReasons());

			response.getWriter().println(
					ResponseUtils.assembleRegularResult(result));

		} catch (Exception e) {
			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtils.assembleFailResult(e.getMessage(), logid));
		}

	}

	@RequestMapping(value = "/tip/export")
	public void exportTips(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String day = StringUtils.getCurrentDay();

			String uuid = UuidUtils.genUuid();
			
			String downloadFilePath = SystemConfigFactory.getSystemConfig().getValue(
					PropConstant.downloadFilePathTips);

			String parentPath = downloadFilePath + day + "/";

			String filePath = parentPath + uuid + "/";

			File file = new File(filePath);

			if (!file.exists()) {
				file.mkdirs();
			}

			String date = jsonReq.getString("date");

			TipsExporter op = new TipsExporter();

			JSONArray grids = jsonReq.getJSONArray("grids");

			op.export(grids, date, filePath, uuid + ".txt");

			String zipFileName = uuid + ".zip";

			String zipFullName = parentPath + zipFileName;

			ZipUtils.zipFile(filePath, zipFullName);
			
			String serverUrl =  SystemConfigFactory.getSystemConfig().getValue(
					PropConstant.serverUrl);
			
			String downloadUrlPath = SystemConfigFactory.getSystemConfig().getValue(
					PropConstant.downloadUrlPathTips);

			String url = serverUrl + downloadUrlPath + day + "/"
					+ zipFileName;

			response.getWriter().println(
					ResponseUtils.assembleRegularResult(url));

		} catch (Exception e) {

			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtils.assembleFailResult(e.getMessage(), logid));
		}
	}

	@RequestMapping(value = "/tip/getByRowkey")
	public void getByRowkey(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String rowkey = jsonReq.getString("rowkey");

			TipsSelector selector = new TipsSelector();

			JSONObject data = selector.searchDataByRowkey(rowkey);

			response.getWriter().println(
					ResponseUtils.assembleRegularResult(data));

		} catch (Exception e) {

			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtils.assembleFailResult(e.getMessage(), logid));
		}
	}

	@RequestMapping(value = "/tip/getBySpatial")
	public void getBySpatial(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String wkt = jsonReq.getString("wkt");

			TipsSelector selector = new TipsSelector();

			JSONArray array = selector.searchDataBySpatial(wkt);

			response.getWriter().println(
					ResponseUtils.assembleRegularResult(array));

		} catch (Exception e) {

			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtils.assembleFailResult(e.getMessage(), logid));
		}
	}

	@RequestMapping(value = "/tip/getSnapshot")
	public void getSnapshot(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			JSONArray grids = jsonReq.getJSONArray("grids");

			int type = jsonReq.getInt("type");

			JSONArray stage = jsonReq.getJSONArray("stage");

			int projectId = jsonReq.getInt("projectId");

			TipsSelector selector = new TipsSelector();

			JSONArray array = selector.getSnapshot(grids, stage, type,
					projectId);

			response.getWriter().println(
					ResponseUtils.assembleRegularResult(array));

		} catch (Exception e) {

			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtils.assembleFailResult(e.getMessage(), logid));
		}
	}

	@RequestMapping(value = "/tip/getStats")
	public void getStats(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			JSONArray grids = jsonReq.getJSONArray("grids");

			JSONArray stages = jsonReq.getJSONArray("stage");

			TipsSelector selector = new TipsSelector();

			JSONObject jo = selector.getStats(grids, stages);

			response.getWriter().println(
					ResponseUtils.assembleRegularResult(jo));

		} catch (Exception e) {

			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtils.assembleFailResult(e.getMessage(), logid));
		}
	}
}
