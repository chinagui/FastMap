package com.navinfo.dataservice.web.render.controller;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.engine.edit.edit.search.SearchProcess;
import com.navinfo.dataservice.engine.fcc.tile.TileSelector;
import com.navinfo.dataservice.engine.fcc.tips.TipsSelector;
import com.navinfo.dataservice.engine.photo.PhotoGetter;

@Controller
public class RenderController extends BaseController {

	private static final Logger logger = Logger
			.getLogger(RenderController.class);

	@RequestMapping(value = "/obj/getByTileWithGap")
	public ModelAndView getObjByTile(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		Connection conn = null;

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			JSONArray type = jsonReq.getJSONArray("types");

			int dbId = jsonReq.getInt("dbId");

			int x = jsonReq.getInt("x");

			int y = jsonReq.getInt("y");

			int z = jsonReq.getInt("z");

			int gap = 0;

			if (jsonReq.containsKey("gap")) {
				gap = jsonReq.getInt("gap");
			}

			List<ObjType> types = new ArrayList<ObjType>();

			for (int i = 0; i < type.size(); i++) {
				types.add(ObjType.valueOf(type.getString(i)));
			}

			JSONObject data = null;

			if (z <= 16) {

				data = TileSelector.getByTiles(types, x, y, z, dbId);

			} else {
				conn = DBConnector.getInstance().getConnectionById(dbId);

				SearchProcess p = new SearchProcess(conn);

				data = p.searchDataByTileWithGap(types, x, y, z, gap);
			}

			return new ModelAndView("jsonView", success(data));

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	@RequestMapping(value = "/tip/getByTileWithGap")
	public ModelAndView getTipsByTile(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			int x = jsonReq.getInt("x");

			int y = jsonReq.getInt("y");

			int z = jsonReq.getInt("z");

			int gap = jsonReq.getInt("gap");

			JSONArray types = new JSONArray();

			if (jsonReq.containsKey("types")) {
				types = jsonReq.getJSONArray("types");
			}

			TipsSelector selector = new TipsSelector();

			JSONArray array = selector.searchDataByTileWithGap(x, y, z, gap,
					types);

			return new ModelAndView("jsonView", success(array));

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}

	@RequestMapping(value = "/photo/getByTileWithGap")
	public ModelAndView getPhotoByTile(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			int x = jsonReq.getInt("x");

			int y = jsonReq.getInt("y");

			int z = jsonReq.getInt("z");

			int gap = jsonReq.getInt("gap");

			PhotoGetter getter = new PhotoGetter();

			JSONArray array = getter.getPhotoByTileWithGap(x, y, z, gap);

			return new ModelAndView("jsonView", success(array));

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}

	@RequestMapping(value = "/photo/heatmap")
	public ModelAndView getPhotoHeatmap(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			double minLon = jsonReq.getDouble("minLon");

			double maxLon = jsonReq.getDouble("maxLon");

			double minLat = jsonReq.getDouble("minLat");

			double maxLat = jsonReq.getDouble("maxLat");

			int zoom = jsonReq.getInt("zoom");

			PhotoGetter getter = new PhotoGetter();

			JSONArray array = getter.getPhotoTile(minLon, minLat, maxLon,
					maxLat, zoom);

			return new ModelAndView("jsonView", success(array));

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}

}
