package com.navinfo.dataservice.web.metadata.controller;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.navinfo.dataservice.engine.meta.PoiCode.KindCodeSelector;
import org.navinfo.dataservice.engine.meta.area.ScPointAdminArea;
import org.navinfo.dataservice.engine.meta.mesh.MeshSelector;
import org.navinfo.dataservice.engine.meta.patternimage.PatternImageExporter;
import org.navinfo.dataservice.engine.meta.patternimage.PatternImageSelector;
import org.navinfo.dataservice.engine.meta.pinyin.PinyinConverter;
import org.navinfo.dataservice.engine.meta.rdname.RdNameSelector;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.commons.config.SystemConfig;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.util.ResponseUtils;
import com.navinfo.dataservice.engine.man.version.VersionSelector;

@Controller
public class MetaController extends BaseController {
	private static final Logger logger = Logger.getLogger(MetaController.class);

	@RequestMapping(value = "/rdname/search")
	public ModelAndView searchRdName(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String name = jsonReq.getString("name");

			int pageSize = jsonReq.getInt("pageSize");

			int pageNum = jsonReq.getInt("pageNum");

			RdNameSelector selector = new RdNameSelector();

			JSONObject data = selector.searchByName(name, pageSize, pageNum);

			return new ModelAndView("jsonView", success(data));

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}

	@RequestMapping(value = "/pinyin/convert")
	public ModelAndView convertPinyin(HttpServletRequest request)
			throws ServletException, IOException {

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

				return new ModelAndView("jsonView", success(json));
			} else {
				throw new Exception("转拼音失败");
			}

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}

	@RequestMapping(value = "/province/getByLocation")
	public ModelAndView getProvinceByLocation(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			double lon = jsonReq.getDouble("lon");

			double lat = jsonReq.getDouble("lat");

			MeshSelector selector = new MeshSelector();

			JSONObject data = selector.getProvinceByLocation(lon, lat);

			if (data != null) {
				return new ModelAndView("jsonView", success(data));
			} else {
				throw new Exception("不在中国省市范围内");
			}

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}

	@RequestMapping(value = "/patternImage/checkUpdate")
	public ModelAndView checkPatternImage(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String date = jsonReq.getString("date");

			PatternImageSelector selector = new PatternImageSelector();

			boolean flag = selector.checkUpdate(date);

			return new ModelAndView("jsonView", success(flag));

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}

	@RequestMapping(value = "/patternImage/export")
	public ModelAndView exportPatternImage(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			PatternImageExporter exporter = new PatternImageExporter();

			String fileName = "";

			SystemConfig config = SystemConfigFactory.getSystemConfig();

			String url = config.getValue(PropConstant.serverUrl);

			url += config.getValue(PropConstant.downloadUrlPathPatternimg);

			String path = config
					.getValue(PropConstant.downloadFilePathPatternimg);

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

			return new ModelAndView("jsonView", success(json));

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
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

			logger.error(e.getMessage(), e);

			response.getWriter().println(
					ResponseUtils.assembleFailResult(e.getMessage()));
		}
	}

	@RequestMapping(value = "/patternImage/search")
	public ModelAndView searchPatternImage(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String name = jsonReq.getString("name");

			int pageSize = jsonReq.getInt("pageSize");

			int pageNum = jsonReq.getInt("pageNum");

			PatternImageSelector selector = new PatternImageSelector();

			JSONObject data = selector.searchByName(name, pageSize, pageNum);

			return new ModelAndView("jsonView", success(data));

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}

	@RequestMapping(value = "/meta/queryTelByProvince")
	public ModelAndView searchTelByProvince(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String name = jsonReq.getString("province");

			ScPointAdminArea selector = new ScPointAdminArea();

			JSONArray data = selector.searchByProvince(name);

			return new ModelAndView("jsonView", success(data));

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}

	@RequestMapping(value = "/meta/queryTelLength")
	public ModelAndView searchTelLength(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String code = jsonReq.getString("code");

			ScPointAdminArea selector = new ScPointAdminArea();

			String data = selector.searchTelLength(code);

			return new ModelAndView("jsonView", success(data));

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}

	@RequestMapping(value = "/meta/queryFoodType")
	public ModelAndView searchFoodType(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String kindId = jsonReq.getString("kindId");

			ScPointAdminArea selector = new ScPointAdminArea();

			JSONArray data = selector.searchFoodType(kindId);

			return new ModelAndView("jsonView", success(data));

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}

	@RequestMapping(value = "/meta/kindLevel")
	public ModelAndView searchKindLevel(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String kindId = jsonReq.getString("kindCode");

			ScPointAdminArea selector = new ScPointAdminArea();

			JSONArray data = selector.searchFoodType(kindId);

			return new ModelAndView("jsonView", success(data));

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}

	@RequestMapping(value = "/queryTopKind")
	public ModelAndView QueryTopKind(HttpServletRequest request)
			throws ServletException, IOException {

		try {
			KindCodeSelector selector = new KindCodeSelector();

			JSONArray data = selector.queryTopKindInfo();

			return new ModelAndView("jsonView", success(data));

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}

	@RequestMapping(value = "/queryMediumKind")
	public ModelAndView queryMediumKind(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String topId = jsonReq.getString("topId");

			KindCodeSelector selector = new KindCodeSelector();

			JSONArray data = selector.queryMediumKindInfo(topId);

			return new ModelAndView("jsonView", success(data));

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}

	@RequestMapping(value = "/queryKind")
	public ModelAndView queryKind(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String topId = jsonReq.getString("topId");

			String mediumId = jsonReq.getString("mediumId");

			int region = jsonReq.getInt("region");

			KindCodeSelector selector = new KindCodeSelector();

			JSONArray data = selector.queryKindInfo(topId, mediumId, region);

			return new ModelAndView("jsonView", success(data));

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}

}
