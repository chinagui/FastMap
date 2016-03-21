package com.navinfo.dataservice.web.fosengine.controller;

import java.io.IOException;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.navinfo.dataservice.commons.db.DBOraclePoolManager;
import com.navinfo.dataservice.commons.util.Log4jUtils;
import com.navinfo.dataservice.man.grid.GridSelector;
import com.navinfo.dataservice.man.project.ProjectSelector;
import com.navinfo.dataservice.man.version.VersionSelector;
import com.navinfo.dataservice.web.util.ResponseUtil;

@Controller
public class ManController {

	private static final Logger logger = Logger
			.getLogger(ManController.class);

	@RequestMapping(value = "/man/version/get")
	public void getVersion(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		Connection conn = null;
		
		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			conn = DBOraclePoolManager.getConnectionByName("man");

			VersionSelector selector = new VersionSelector(conn);

			if (jsonReq.containsKey("type")) {

				int type = jsonReq.getInt("type");

				String version = selector.getByType(type);

				JSONObject json = new JSONObject();

				json.put("specVersion", version);

				json.put("type", type);

				response.getWriter().println(
						ResponseUtil.assembleRegularResult(json));
			}

			else {
				JSONArray array = selector.getList();

				response.getWriter().println(
						ResponseUtil.assembleRegularResult(array));

			}

		} catch (Exception e) {

			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtil.assembleFailResult(e.getMessage(), logid));
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

	@RequestMapping(value = "/man/project/getByUser")
	public void getProjectByUser(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		Connection conn = null;
		
		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			int userId = jsonReq.getInt("userId");

			conn = DBOraclePoolManager.getConnectionByName("man");

			ProjectSelector selector = new ProjectSelector(conn);

			JSONArray array = selector.getByUser(userId);

			response.getWriter().println(
					ResponseUtil.assembleRegularResult(array));

		} catch (Exception e) {

			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtil.assembleFailResult(e.getMessage(), logid));
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

	@RequestMapping(value = "/man/grid/getByUser")
	public void getGridByUser(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		Connection conn = null;
		
		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			int userId = jsonReq.getInt("userId");

			int projectId = jsonReq.getInt("projectId");

			conn = DBOraclePoolManager.getConnectionByName("man");

			GridSelector selector = new GridSelector(conn);

			JSONObject result = selector.getByUser(userId, projectId);

			response.getWriter().println(
					ResponseUtil.assembleRegularResult(result));

		} catch (Exception e) {

			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtil.assembleFailResult(e.getMessage(), logid));
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
