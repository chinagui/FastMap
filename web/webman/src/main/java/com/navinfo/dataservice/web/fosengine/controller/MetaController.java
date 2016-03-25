package com.navinfo.dataservice.web.fosengine.controller;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.navinfo.dataservice.engine.meta.MeshSelector;
import org.navinfo.dataservice.engine.meta.PatternImageExporter;
import org.navinfo.dataservice.engine.meta.PatternImageSelector;
import org.navinfo.dataservice.engine.meta.PinyinConverter;
import org.navinfo.dataservice.engine.meta.RdNameSelector;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.navinfo.dataservice.commons.db.DBOraclePoolManager;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.util.Log4jUtils;
import com.navinfo.dataservice.commons.util.ResponseUtils;
import com.navinfo.dataservice.engine.man.version.VersionSelector;

@Controller
public class MetaController extends BaseController {
	private static final Logger logger = Logger.getLogger(MetaController.class);

	@RequestMapping(value = "/meta/rdname/search")
	public void searchRdName(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		Connection conn = null;

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String name = jsonReq.getString("name");

			int pageSize = jsonReq.getInt("pageSize");

			int pageNum = jsonReq.getInt("pageNum");

			conn = DBOraclePoolManager.getConnectionByName("meta");

			RdNameSelector selector = new RdNameSelector(conn);

			JSONObject result = selector.searchByName(name, pageSize, pageNum);

			response.getWriter().println(
					ResponseUtils.assembleRegularResult(result));

		} catch (Exception e) {

			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtils.assembleFailResult(e.getMessage(), logid));
		}
		finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	@RequestMapping(value = "/meta/pinyin/convert")
	public void convertPinyin(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		Connection conn = null;

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String word = jsonReq.getString("word");

			conn = DBOraclePoolManager.getConnectionByName("meta");

			PinyinConverter py = new PinyinConverter(conn);

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
		finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	@RequestMapping(value = "/meta/province/getByLocation")
	public void getProvinceByLocation(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		Connection conn = null;

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			double lon = jsonReq.getDouble("lon");

			double lat = jsonReq.getDouble("lat");

			conn = DBOraclePoolManager.getConnectionByName("meta");

			MeshSelector selector = new MeshSelector(conn);

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
		finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	@RequestMapping(value = "/meta/patternImage/checkUpdate")
	public void checkPatternImage(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		Connection conn = null;

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String date = jsonReq.getString("date");

			conn = DBOraclePoolManager.getConnectionByName("meta");

			PatternImageSelector selector = new PatternImageSelector(conn);

			boolean flag = selector.checkUpdate(date);

			response.getWriter().println(
					ResponseUtils.assembleRegularResult(flag));

		} catch (Exception e) {

			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtils.assembleFailResult(e.getMessage(), logid));
		}
		finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	@RequestMapping(value = "/meta/patternImage/download")
	public void downloadPatternImage(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		Connection conn = null;

		Connection metaConn = null;

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			metaConn = DBOraclePoolManager
					.getConnectionByName("meta");

			PatternImageExporter exporter = new PatternImageExporter(metaConn);

			String fileName = "";

			String url = "http://192.168.4.130:8080/download/patternimage";

			String path = "";

			if (jsonReq.containsKey("names")) {
				JSONArray names = jsonReq.getJSONArray("names");

				path = "/root/download/patternimage/byname";

				fileName = exporter.export2SqliteByNames(path, names);

				url += "/byname/" + fileName;
			} else if (jsonReq.containsKey("date")) {
				String date = jsonReq.getString("date");

				path = "/root/download/patternimage/bydate";

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

			conn = DBOraclePoolManager.getConnectionByName("man");

			VersionSelector selector = new VersionSelector(conn);

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
		finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (metaConn != null) {
				try {
					metaConn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	@RequestMapping(value = "/meta/patternImage/getById")
	public void getPatternImageById(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("image/jpeg;charset=GBK");

		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods",
				"POST, GET, OPTIONS, DELETE,PUT");

		String parameter = request.getParameter("parameter");
		
		Connection conn = null;

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String id = jsonReq.getString("id");

			conn = DBOraclePoolManager
					.getConnectionByName("meta");

			PatternImageSelector selector = new PatternImageSelector(conn);

			byte[] data = selector.getById(id);

			response.getOutputStream().write(data);

		} catch (Exception e) {

			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtils.assembleFailResult(e.getMessage(), logid));
		}
		finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	@RequestMapping(value = "/meta/patternImage/search")
	public void searchPatternImage(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		Connection conn = null;
				
		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String name = jsonReq.getString("name");

			int pageSize = jsonReq.getInt("pageSize");

			int pageNum = jsonReq.getInt("pageNum");

			conn = DBOraclePoolManager
					.getConnectionByName("meta");

			PatternImageSelector selector = new PatternImageSelector(conn);

			JSONObject obj = selector.searchByName(name, pageSize, pageNum);

			response.getWriter().println(
					ResponseUtils.assembleRegularResult(obj));

		} catch (Exception e) {

			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtils.assembleFailResult(e.getMessage(), logid));
		}
		finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
