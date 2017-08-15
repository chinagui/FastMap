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

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.util.ResponseUtils;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.search.specialMap.SpecialMapUtils;
import com.navinfo.dataservice.engine.edit.search.SearchProcess;
import com.navinfo.dataservice.engine.fcc.tile.TileSelector;
import com.navinfo.dataservice.engine.fcc.tips.TipsSelector;
import com.navinfo.dataservice.engine.photo.PhotoGetter;

@Controller
public class RenderController extends BaseController {

	private static final Logger logger = Logger
			.getLogger(RenderController.class);

	@RequestMapping(value = "/obj/getByTileWithGap")
	public void getObjByTile(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");
		JSONObject jsonReq = JSONObject.fromObject(parameter);
		
		if (jsonReq.containsKey("platform")
				&& jsonReq.getString("platform") != null
				&& jsonReq.getString("platform").equals("dataPlan")) {
			response.getWriter().println(manRender(jsonReq));
		}else {
			response.getWriter().println(commonRender(jsonReq));
		}
	}
	
	private String commonRender(JSONObject jsonReq) {
		Connection conn = null;

		try {
			JSONArray array = null;

			JSONArray type = jsonReq.getJSONArray("types");

			int dbId = jsonReq.getInt("dbId");
			if (jsonReq.containsKey("noQFilter")) {
				array = jsonReq.getJSONArray("noQFilter");
			}

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

			if (z <= 14) {

				List<ObjType> tileTypes = new ArrayList<ObjType>();

				for (ObjType t : types) {
					if (t == ObjType.RDLINK || t == ObjType.ADLINK
							|| t == ObjType.RWLINK) {
						tileTypes.add(t);
					}
				}
				if (!tileTypes.isEmpty()) {
					JSONObject jo = TileSelector.getByTiles(tileTypes, x, y, z,
							dbId);

					if (data == null) {
						data = new JSONObject();
					}

					data.putAll(jo);
				}

			} else {
				SearchProcess p = new SearchProcess();
				p.setArray(array);
				p.setDbId(dbId);
				data = p.searchDataByTileWithGap(types, x, y, z, gap);
			}
			return ResponseUtils.assembleRegularResult(data);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return ResponseUtils.assembleFailResult(e.getMessage());
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}
	
	private String manRender(JSONObject jsonReq){
		try {
			JSONArray array = null;
			JSONArray type = jsonReq.getJSONArray("types");

			int dbId = jsonReq.getInt("dbId");
			if (jsonReq.containsKey("noQFilter")) {
				array = jsonReq.getJSONArray("noQFilter");
			}

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

			if (z <= 10) {

				List<ObjType> tileTypes = new ArrayList<ObjType>();

				for (ObjType t : types) {
					if (t == ObjType.RDLINK || t == ObjType.ADLINK
							|| t == ObjType.RWLINK) {
						tileTypes.add(t);
					}
				}
				if (!tileTypes.isEmpty()) {
					JSONObject jo = TileSelector.getByTiles(tileTypes, x, y, z,
							dbId);

					if (data == null) {
						data = new JSONObject();
					}

					data.putAll(jo);
				}

			} else {
				int taskId = jsonReq.getInt("taskId");
				SearchProcess p = new SearchProcess();
				p.setArray(array);
				p.setDbId(dbId);
				p.setZ(z);
				p.setTaskId(taskId);
				data = p.searchDataByTileWithGapForMan(types, x, y, z, gap);
			}
			return ResponseUtils.assembleRegularResult(data);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return ResponseUtils.assembleFailResult(e.getMessage());
		}
	}

	@RequestMapping(value = "/specia/getByTileWithGap")
	public void getSpeciaByTile(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		Connection conn = null;

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			List<String> types = new ArrayList<String>();

			if (jsonReq.containsKey("type")) {
				types.add(jsonReq.getString("type"));
			}
			if (jsonReq.containsKey("types")) {
				JSONArray typeArray = jsonReq.getJSONArray("types");

				for (int i = 0; i < typeArray.size(); i++) {
					types.add(typeArray.getString(i));
				}
			}

			int dbId = jsonReq.getInt("dbId");

			int x = jsonReq.getInt("x");

			int y = jsonReq.getInt("y");

			int z = jsonReq.getInt("z");

			int gap = 0;

			if (jsonReq.containsKey("gap")) {
				gap = jsonReq.getInt("gap");
			}

			JSONObject data = null;

			if (z > 9) {

				conn = DBConnector.getInstance().getConnectionById(dbId);

				SpecialMapUtils specialMap = new SpecialMapUtils(conn);

				data = specialMap.searchDataByTileWithGap(types, x, y, z, gap);

				response.getWriter().println(
						ResponseUtils.assembleRegularResult(data));
			}
		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			response.getWriter().println(
					ResponseUtils.assembleFailResult(e.getMessage()));

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
	public void getTipsByTile(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			// JSONObject jsonReq = JSONObject.fromObject(parameter);

			// int x = jsonReq.getInt("x");
			//
			// int y = jsonReq.getInt("y");
			//
			// int z = jsonReq.getInt("z");
			//
			// int gap = jsonReq.getInt("gap");
			//
			// String mdFlag = jsonReq.getString("mdFlag");

			// JSONArray types = new JSONArray();
			// if (jsonReq.containsKey("types")) {
			// types = jsonReq.getJSONArray("types");
			// }
			//
			// JSONArray noQFilter = new JSONArray();
			// if (jsonReq.containsKey("noQFilter")) {
			// noQFilter = jsonReq.getJSONArray("noQFilter");
			// }

			TipsSelector selector = new TipsSelector();

			JSONArray array = selector.searchDataByTileWithGap(parameter);

			response.getWriter().println(
					ResponseUtils.assembleRegularResult(array));

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			response.getWriter().println(
					ResponseUtils.assembleFailResult(e.getMessage()));
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

			logger.error(e.getMessage(), e);

			response.getWriter().println(
					ResponseUtils.assembleFailResult(e.getMessage()));
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

			logger.error(e.getMessage(), e);

			response.getWriter().println(
					ResponseUtils.assembleFailResult(e.getMessage()));
		}
	}

	/**
	 * @Title: getinfoByTile
	 * @Description: 情报图层的render 服务
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 *             void
	 * @throws
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年6月28日 下午4:10:00
	 */
	@RequestMapping(value = "/info/getByTileWithGap")
	public void getinfoByTile(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		Connection conn = null;

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);
			// JSONArray array = null;

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

			if (z >= 13) {

				// conn = DBConnector.getInstance().getConnectionById(dbId);
				conn = DBConnector.getInstance().getRenderConnection();
				System.out.println(conn);
				SearchProcess p = new SearchProcess(conn);
				// p.setArray(array);
				data = p.searchInfoByTileWithGap(types, x, y, z, gap);

			}
			response.getWriter().println(
					ResponseUtils.assembleRegularResult(data));
		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			response.getWriter().println(
					ResponseUtils.assembleFailResult(e.getMessage()));

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
}
