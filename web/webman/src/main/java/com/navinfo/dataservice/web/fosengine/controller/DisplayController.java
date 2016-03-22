package com.navinfo.dataservice.web.fosengine.controller;

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

import com.navinfo.dataservice.commons.config.SystemConfig;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.db.DBOraclePoolManager;
import com.navinfo.dataservice.commons.util.Log4jUtils;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.engine.edit.edit.display.TileSelector;
import com.navinfo.dataservice.engine.edit.edit.search.SearchProcess;
import com.navinfo.dataservice.engine.fcc.tips.TipsSelector;
import com.navinfo.dataservice.engine.photo.PhotoGetter;
import com.navinfo.dataservice.web.util.ResponseUtil;


@Controller
public class DisplayController {

	private static final Logger logger = Logger
			.getLogger(DisplayController.class);

	@RequestMapping(value = "/display/link/getByTile")
	public void getLinkByTile(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			int x = jsonReq.getInt("x");

			int y = jsonReq.getInt("y");

			int z = jsonReq.getInt("z");

			int projectId = jsonReq.getInt("projectId");

			List<String> list = TileSelector.getTiles(x, y, z, projectId);

			if (list != null && list.size() > 0) {

				JSONObject json = new JSONObject();

				json.put("RDLINK", list.get(0));

				response.getWriter().println(
						ResponseUtil.assembleRegularResult(json));
			} else {
				response.getWriter().println(
						ResponseUtil.assembleRegularResult(null));
			}

		} catch (Exception e) {

			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtil.assembleFailResult(e.getMessage(), logid));

		}
	}

	@RequestMapping(value = "/display/obj/getByTileWithGap")
	public void getObjByTile(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		Connection conn = null;
		
		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			JSONArray type = jsonReq.getJSONArray("type");

			int projectId = jsonReq.getInt("projectId");

			int x = jsonReq.getInt("x");

			int y = jsonReq.getInt("y");

			int z = jsonReq.getInt("z");

			int gap = jsonReq.getInt("gap");

			List<ObjType> types = new ArrayList<ObjType>();

			for (int i = 0; i < type.size(); i++) {
				types.add(ObjType.valueOf(type.getString(i)));
			}

			conn = DBOraclePoolManager.getConnection(projectId);
			
			SearchProcess p = new SearchProcess(conn);

			JSONObject data = p.searchDataByTileWithGap(types, x, y, z, gap);

			response.getWriter().println(
					ResponseUtil.assembleRegularResult(data));

		} catch (Exception e) {

			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtil.assembleFailResult(e.getMessage(), logid));

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

	@RequestMapping(value = "/display/tip/getByTileWithGap")
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

			TipsSelector selector = new TipsSelector(
					SystemConfig.getSystemConfig().getValue(PropConstant.solrAddress));

			JSONArray array = selector.searchDataByTileWithGap(x, y, z, gap,
					types);

			response.getWriter().println(
					ResponseUtil.assembleRegularResult(array));

		} catch (Exception e) {

			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtil.assembleFailResult(e.getMessage(), logid));
		}
	}

	@RequestMapping(value = "/display/photo/getByTileWithGap")
	public void getPhotoByTile(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			int x = jsonReq.getInt("x");

			int y = jsonReq.getInt("y");

			int z = jsonReq.getInt("z");

			int gap = jsonReq.getInt("gap");

			JSONArray array = PhotoGetter.getPhotoByTileWithGap(x, y, z, gap);

			response.getWriter().println(
					ResponseUtil.assembleRegularResult(array));

		} catch (Exception e) {

			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtil.assembleFailResult(e.getMessage(), logid));
		}
	}

	@RequestMapping(value = "/display/photo/heatmap")
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

			JSONArray array = PhotoGetter.getPhotoTile(minLon, minLat, maxLon,
					maxLat, zoom);

			response.getWriter().println(
					ResponseUtil.assembleRegularResult(array));

		} catch (Exception e) {

			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtil.assembleFailResult(e.getMessage(), logid));
		}
	}

}
