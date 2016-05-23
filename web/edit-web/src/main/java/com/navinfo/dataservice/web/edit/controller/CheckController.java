package com.navinfo.dataservice.web.edit.controller;

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

import com.navinfo.dataservice.commons.util.Log4jUtils;
import com.navinfo.dataservice.commons.util.ResponseUtils;
import com.navinfo.dataservice.dao.check.NiValExceptionOperator;
import com.navinfo.dataservice.dao.check.NiValExceptionSelector;
import com.navinfo.dataservice.dao.pool.GlmDbPoolManager;

@Controller
public class CheckController {
	private static final Logger logger = Logger
			.getLogger(CheckController.class);

	@RequestMapping(value = "/check/get")
	public void getCheck(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		Connection conn = null; 
		
		try {

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			int projectId = jsonReq.getInt("projectId");

			JSONArray grids = jsonReq.getJSONArray("grids");

			int pageSize = jsonReq.getInt("pageSize");

			int pageNum = jsonReq.getInt("pageNum");

			conn = GlmDbPoolManager.getInstance().getConnection(projectId);

			NiValExceptionSelector selector = new NiValExceptionSelector(conn);

			JSONArray result = selector.loadByGrid(grids, pageSize, pageNum);

			response.getWriter().println(
					ResponseUtils.assembleRegularResult(result));

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

	@RequestMapping(value = "/check/count")
	public void getCheckCount(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		Connection conn = null; 
		
		try {

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			int projectId = jsonReq.getInt("projectId");

			JSONArray grids = jsonReq.getJSONArray("grids");

			conn = GlmDbPoolManager.getInstance().getConnection(projectId);

			NiValExceptionSelector selector = new NiValExceptionSelector(conn);

			int result = selector.loadCountByGrid(grids);

			response.getWriter().println(
					ResponseUtils.assembleRegularResult(result));

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

	@RequestMapping(value = "/check/update")
	public void updateCheck(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		Connection conn = null;
		
		try {

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			int projectId = jsonReq.getInt("projectId");

			String id = jsonReq.getString("id");

			int type = jsonReq.getInt("type");
			
			conn = GlmDbPoolManager.getInstance().getConnection(projectId);

			NiValExceptionOperator selector = new NiValExceptionOperator(conn,projectId);

			selector.updateCheckLogStatus(id, type);

			response.getWriter().println(
					ResponseUtils.assembleRegularResult(null));

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
}
