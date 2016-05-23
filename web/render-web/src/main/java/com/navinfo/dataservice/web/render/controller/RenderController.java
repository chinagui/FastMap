package com.navinfo.dataservice.web.render.controller;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

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
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.pool.GlmDbPoolManager;
import com.navinfo.dataservice.engine.edit.edit.search.SearchProcess;
import com.navinfo.dataservice.engine.fcc.tile.TileSelector;
import com.navinfo.dataservice.engine.fcc.tips.TipsSelector;
import com.navinfo.dataservice.engine.photo.PhotoGetter;


@Controller
public class RenderController {

	private static final Logger logger = Logger
			.getLogger(RenderController.class);

	@RequestMapping(value = "/link/getByTileWithGap")
	public void getLinkByTile(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			int x = jsonReq.getInt("x");

			int y = jsonReq.getInt("y");

			int z = jsonReq.getInt("z");

			int projectId = jsonReq.getInt("projectId");

			List<String> list = TileSelector.getRdLinkTiles(x, y, z, projectId);

			if (list != null && list.size() > 0) {

				JSONObject json = new JSONObject();

				json.put("RDLINK", list.get(0));

				response.getWriter().println(
						ResponseUtils.assembleRegularResult(json));
			} else {
				response.getWriter().println(
						ResponseUtils.assembleRegularResult(null));
			}

		} catch (Exception e) {

			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtils.assembleFailResult(e.getMessage(), logid));

		}
	}

	@RequestMapping(value = "/obj/getByTileWithGap")
	public void getObjByTile(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		Connection conn = null;
		
		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			JSONArray type = jsonReq.getJSONArray("types");

			int projectId = jsonReq.getInt("projectId");

			int x = jsonReq.getInt("x");

			int y = jsonReq.getInt("y");

			int z = jsonReq.getInt("z");

			int gap = jsonReq.getInt("gap");

			List<ObjType> types = new ArrayList<ObjType>();

			for (int i = 0; i < type.size(); i++) {
				types.add(ObjType.valueOf(type.getString(i)));
			}

			conn = GlmDbPoolManager.getInstance().getConnection(projectId);
			
			SearchProcess p = new SearchProcess(conn);

			JSONObject data = p.searchDataByTileWithGap(types, x, y, z, gap);

			response.getWriter().println(
					ResponseUtils.assembleRegularResult(data));

		} catch (Exception e) {

			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtils.assembleFailResult(e.getMessage(), logid));

		}
		finally{
			if(conn!=null){
				try{
					conn.close();
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}

	@RequestMapping(value = "/tip/getByTileWithGap")
	public void getTipsByTile(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

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

			response.getWriter().println(
					ResponseUtils.assembleRegularResult(array));

		} catch (Exception e) {

			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtils.assembleFailResult(e.getMessage(), logid));
		}
	}

	@RequestMapping(value = "/photo/getByTileWithGap")
	public void getPhotoByTile(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			int x = jsonReq.getInt("x");

			int y = jsonReq.getInt("y");

			int z = jsonReq.getInt("z");

			int gap = jsonReq.getInt("gap");
			
			PhotoGetter getter = new PhotoGetter();

			JSONArray array = getter.getPhotoByTileWithGap(x, y, z, gap);

			response.getWriter().println(
					ResponseUtils.assembleRegularResult(array));

		} catch (Exception e) {

			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtils.assembleFailResult(e.getMessage(), logid));
		}
	}

	@RequestMapping(value = "/photo/heatmap")
	public void getPhotoHeatmap(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

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

			response.getWriter().println(
					ResponseUtils.assembleRegularResult(array));

		} catch (Exception e) {

			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtils.assembleFailResult(e.getMessage(), logid));
		}
	}

}
