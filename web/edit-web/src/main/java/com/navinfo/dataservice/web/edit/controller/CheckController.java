package com.navinfo.dataservice.web.edit.controller;

import java.io.IOException;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.dao.check.NiValExceptionOperator;
import com.navinfo.dataservice.dao.check.NiValExceptionSelector;
import com.navinfo.dataservice.engine.edit.check.CheckService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
public class CheckController extends BaseController {
	private static final Logger logger = Logger
			.getLogger(CheckController.class);
	
	@RequestMapping(value = "/check/get")
	public ModelAndView getCheck(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		Connection conn = null;

		try {

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			int dbId = jsonReq.getInt("dbId");

			JSONArray grids = jsonReq.getJSONArray("grids");

			int pageSize = jsonReq.getInt("pageSize");

			int pageNum = jsonReq.getInt("pageNum");

			conn = DBConnector.getInstance().getConnectionById(dbId);

			NiValExceptionSelector selector = new NiValExceptionSelector(conn);

			JSONArray data = selector.loadByGrid(grids, pageSize, pageNum);

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

	@RequestMapping(value = "/check/count")
	public ModelAndView getCheckCount(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		Connection conn = null;

		try {

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			int dbId = jsonReq.getInt("dbId");

			JSONArray grids = jsonReq.getJSONArray("grids");

			conn = DBConnector.getInstance().getConnectionById(dbId);

			NiValExceptionSelector selector = new NiValExceptionSelector(conn);

			int data = selector.loadCountByGrid(grids);

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

	@RequestMapping(value = "/check/update")
	public ModelAndView updateCheck(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		Connection conn = null;

		try {

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			int dbId = jsonReq.getInt("dbId");

			String id = jsonReq.getString("id");

			int type = jsonReq.getInt("type");

			conn = DBConnector.getInstance().getConnectionById(dbId);

			NiValExceptionOperator selector = new NiValExceptionOperator(conn,
					dbId);

			selector.updateCheckLogStatus(id, type);

			return new ModelAndView("jsonView", success());

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
	
	/**
	 * 检查执行
	 * dbId	是	子任务id
	 * type	是	检查类型（0 poi行编，1poi精编, 2道路）
	 * 根据输入的子任务和检查类型，对任务范围内的数据执行，执行检查。不执行检查结果清理
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping(value = "/check/run")
	public ModelAndView checkRun(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");
		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);
			int subtaskId=jsonReq.getInt("subtaskId");
			int checkType=jsonReq.getInt("checkType");
			AccessToken tokenObj=(AccessToken) request.getAttribute("token");
			long userId=tokenObj.getUserId();
			//long userId=2;
			long jobId=CheckService.getInstance().checkRun(subtaskId,userId,checkType);				
			return new ModelAndView("jsonView", success(jobId));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}
	
}
