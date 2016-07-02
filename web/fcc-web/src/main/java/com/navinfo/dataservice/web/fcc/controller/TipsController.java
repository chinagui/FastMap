package com.navinfo.dataservice.web.fcc.controller;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.photo.Photo;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.util.ResponseUtils;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.commons.util.ZipUtils;
import com.navinfo.dataservice.engine.dropbox.manger.UploadService;
import com.navinfo.dataservice.engine.fcc.tips.TipsExporter;
import com.navinfo.dataservice.engine.fcc.tips.TipsOperator;
import com.navinfo.dataservice.engine.fcc.tips.TipsSelector;
import com.navinfo.dataservice.engine.fcc.tips.TipsUpload;
import com.navinfo.dataservice.engine.meta.patternimage.PatternImageExporter;
import com.navinfo.dataservice.engine.photo.CollectorImport;

@Controller
public class TipsController extends BaseController {

	private static final Logger logger = Logger.getLogger(TipsController.class);

	@RequestMapping(value = "/tip/checkUpdate")
	public ModelAndView checkUpdate(HttpServletRequest request
			) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String grid = jsonReq.getString("grid");

			String date = jsonReq.getString("date");

			TipsSelector selector = new TipsSelector();
			
			return new ModelAndView("jsonView", success(selector.checkUpdate(
					grid, date)));

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}

	@RequestMapping(value = "/tip/edit")
	public ModelAndView edit(HttpServletRequest request )
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

			return new ModelAndView("jsonView", success());

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}
	
	@RequestMapping(value = "/tip/import")
	public ModelAndView importTips(HttpServletRequest request
			) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {

			JSONObject json = JSONObject.fromObject(parameter);

			int jobId = json.getInt("jobId");

			UploadService upload = UploadService.getInstance();

			String filePath = upload.unzipByJobId(jobId);

			TipsUpload tipsUploader = new TipsUpload();

			Map<String, Photo> map = tipsUploader.run(filePath + "/"
					+ "tips.txt");

			CollectorImport.importPhoto(map, filePath + "/photo");

			JSONObject result = new JSONObject();

			result.put("total", tipsUploader.getTotal());

			result.put("failed", tipsUploader.getFailed());

			result.put("reasons", tipsUploader.getReasons());

			return new ModelAndView("jsonView", success(result));

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}

	}

	@RequestMapping(value = "/tip/export")
	public ModelAndView exportTips(HttpServletRequest request )
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String day = StringUtils.getCurrentDay();

			String uuid = UuidUtils.genUuid();
			
			String downloadFilePath = SystemConfigFactory.getSystemConfig().getValue(
					PropConstant.downloadFilePathTips);

			String parentPath = downloadFilePath +File.separator+ day + "/";

			String filePath = parentPath + uuid + "/";

			File file = new File(filePath);

			if (!file.exists()) {
				file.mkdirs();
			}

			String date = jsonReq.getString("date");

			TipsExporter op = new TipsExporter();

			JSONArray grids = jsonReq.getJSONArray("grids");
			
			Set<String> images = new HashSet<String>();

			op.export(grids, date, filePath, "tips.txt", images);
			
			if(images.size()>0){
			
				PatternImageExporter exporter = new PatternImageExporter();
				
				exporter.export2SqliteByNames(filePath, images);
			}

			String zipFileName = uuid + ".zip";

			String zipFullName = parentPath + zipFileName;

			ZipUtils.zipFile(filePath, zipFullName);
			
			String serverUrl =  SystemConfigFactory.getSystemConfig().getValue(
					PropConstant.serverUrl);
			
			String downloadUrlPath = SystemConfigFactory.getSystemConfig().getValue(
					PropConstant.downloadUrlPathTips);

			String url = serverUrl + downloadUrlPath +File.separator+ day + "/"
					+ zipFileName;

			return new ModelAndView("jsonView", success(url));

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}
	@RequestMapping(value = "/tip/getByRowkey")
	public void getByRowkey(HttpServletRequest request,HttpServletResponse response
			) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String rowkey = jsonReq.getString("rowkey");

			TipsSelector selector = new TipsSelector();

			JSONObject data = selector.searchDataByRowkey(rowkey);

			response.getWriter().println(
					ResponseUtils.assembleRegularResult(data));
		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			response.getWriter().println(
					ResponseUtils.assembleFailResult(e.getMessage()));
		}
	}

	@RequestMapping(value = "/tip/getBySpatial")
	public ModelAndView getBySpatial(HttpServletRequest request
			) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String wkt = jsonReq.getString("wkt");

			TipsSelector selector = new TipsSelector();

			JSONArray array = selector.searchDataBySpatial(wkt);

			return new ModelAndView("jsonView", success(array));

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}

	@RequestMapping(value = "/tip/getSnapshot")
	public void getSnapshot(HttpServletRequest request, HttpServletResponse response
			) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			JSONArray grids = jsonReq.getJSONArray("grids");

			int type = jsonReq.getInt("type");

			JSONArray stage = jsonReq.getJSONArray("stage");

			int dbId = jsonReq.getInt("dbId");

			TipsSelector selector = new TipsSelector();

			JSONArray array = selector.getSnapshot(grids, stage, type,
					dbId);

			response.getWriter().println(
					ResponseUtils.assembleRegularResult(array));

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			response.getWriter().println(
					ResponseUtils.assembleFailResult(e.getMessage()));
		}
	}

	@RequestMapping(value = "/tip/getStats")
	public ModelAndView getStats(HttpServletRequest request
			) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			JSONArray grids = jsonReq.getJSONArray("grids");

			JSONArray stages = jsonReq.getJSONArray("stage");

			TipsSelector selector = new TipsSelector();

			JSONObject data = selector.getStats(grids, stages);

			return new ModelAndView("jsonView", success(data));

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}
}
