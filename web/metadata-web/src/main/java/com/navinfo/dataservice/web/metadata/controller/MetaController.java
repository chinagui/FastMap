package com.navinfo.dataservice.web.metadata.controller;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.navinfo.dataservice.engine.meta.mesh.MeshSelector;
import org.navinfo.dataservice.engine.meta.patternimage.PatternImageExporter;
import org.navinfo.dataservice.engine.meta.patternimage.PatternImageSelector;
import org.navinfo.dataservice.engine.meta.pinyin.PinyinConverter;
import org.navinfo.dataservice.engine.meta.rdname.RdNameSelector;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.navinfo.dataservice.commons.config.SystemConfig;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.db.DBOraclePoolManager;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.util.Log4jUtils;
import com.navinfo.dataservice.commons.util.ResponseUtils;
import com.navinfo.dataservice.engine.man.version.VersionSelector;

@Controller
public class MetaController extends BaseController {
	private static final Logger logger = Logger.getLogger(MetaController.class);

	@RequestMapping(value = "/rdname/search")
	public void searchRdName(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String name = jsonReq.getString("name");

			int pageSize = jsonReq.getInt("pageSize");

			int pageNum = jsonReq.getInt("pageNum");

			RdNameSelector selector = new RdNameSelector();

			JSONObject result = selector.searchByName(name, pageSize, pageNum);

			response.getWriter().println(
					ResponseUtils.assembleRegularResult(result));

		} catch (Exception e) {

			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtils.assembleFailResult(e.getMessage(), logid));
		}
	}

	@RequestMapping(value = "/pinyin/convert")
	public void convertPinyin(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String word = jsonReq.getString("word");

			PinyinConverter py = new PinyinConverter();

			String[] result = py.convert(word);

			if (result != null) {
				JSONObject json = new JSONObject();

				json.put("voicefile", result[0]);

				json.put("phonetic", result[1]);

				response.getWriter().println(
						ResponseUtils.assembleRegularResult(json));
			} else {
				throw new Exception("转拼音失败");
			}

		} catch (Exception e) {

			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtils.assembleFailResult(e.getMessage(), logid));
		}
	}

	@RequestMapping(value = "/province/getByLocation")
	public void getProvinceByLocation(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			double lon = jsonReq.getDouble("lon");

			double lat = jsonReq.getDouble("lat");

			MeshSelector selector = new MeshSelector();

			JSONObject data = selector.getProvinceByLocation(lon, lat);

			if (data != null) {
				response.getWriter().println(
						ResponseUtils.assembleRegularResult(data));
			} else {
				throw new Exception("不在中国省市范围内");
			}

		} catch (Exception e) {

			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtils.assembleFailResult(e.getMessage(), logid));
		}
	}

	@RequestMapping(value = "/patternImage/checkUpdate")
	public void checkPatternImage(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String date = jsonReq.getString("date");

			PatternImageSelector selector = new PatternImageSelector();

			boolean flag = selector.checkUpdate(date);

			response.getWriter().println(
					ResponseUtils.assembleRegularResult(flag));

		} catch (Exception e) {

			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtils.assembleFailResult(e.getMessage(), logid));
		}
	}

	@RequestMapping(value = "/patternImage/export")
	public void exportPatternImage(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			PatternImageExporter exporter = new PatternImageExporter();

			String fileName = "";
			
			SystemConfig config = SystemConfigFactory.getSystemConfig();
			
			String url = config.getValue(PropConstant.serverUrl);
			
			url += config.getValue(PropConstant.downloadUrlPathPatternimg);

			String path = config.getValue(PropConstant.downloadFilePathPatternimg);

			if (jsonReq.containsKey("names")) {
				JSONArray names = jsonReq.getJSONArray("names");

				path += "/byname";

				fileName = exporter.export2SqliteByNames(path, names);

				url += "/byname/" + fileName;
			} else if (jsonReq.containsKey("date")) {
				String date = jsonReq.getString("date");

				path += "/bydate";

				fileName = exporter.export2SqliteByDate(path, date);

				url += "/bydate/" + fileName;
			} else {
				throw new Exception("错误的参数");
			}

			String fullPath = path + "/" + fileName;

			File f = new File(fullPath);

			long filesize = f.length();

			String version = fileName.replace(".zip", "");

			JSONObject json = new JSONObject();

			VersionSelector selector = new VersionSelector();

			String specVersion = selector.getByType(3);

			json.put("version", version);

			json.put("url", url);

			json.put("filesize", filesize);

			json.put("specVersion", specVersion);

			response.getWriter().println(
					ResponseUtils.assembleRegularResult(json));

		} catch (Exception e) {

			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtils.assembleFailResult(e.getMessage(), logid));
		}
	}

	@RequestMapping(value = "/patternImage/getById")
	public void getPatternImageById(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("image/jpeg;charset=GBK");

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String id = jsonReq.getString("id");

			PatternImageSelector selector = new PatternImageSelector();

			byte[] data = selector.getById(id);

			response.getOutputStream().write(data);

		} catch (Exception e) {

			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtils.assembleFailResult(e.getMessage(), logid));
		}
	}

	@RequestMapping(value = "/patternImage/search")
	public void searchPatternImage(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String name = jsonReq.getString("name");

			int pageSize = jsonReq.getInt("pageSize");

			int pageNum = jsonReq.getInt("pageNum");

			PatternImageSelector selector = new PatternImageSelector();

			JSONObject obj = selector.searchByName(name, pageSize, pageNum);

			response.getWriter().println(
					ResponseUtils.assembleRegularResult(obj));

		} catch (Exception e) {

			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtils.assembleFailResult(e.getMessage(), logid));
		}
	}
}
