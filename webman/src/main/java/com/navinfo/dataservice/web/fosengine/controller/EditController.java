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

import com.navinfo.dataservice.FosEngine.edit.model.IObj;
import com.navinfo.dataservice.FosEngine.edit.model.IRow;
import com.navinfo.dataservice.FosEngine.edit.model.ObjLevel;
import com.navinfo.dataservice.FosEngine.edit.model.ObjType;
import com.navinfo.dataservice.FosEngine.edit.model.selector.rd.branch.RdBranchSelector;
import com.navinfo.dataservice.FosEngine.edit.operation.Transaction;
import com.navinfo.dataservice.FosEngine.edit.search.SearchProcess;
import com.navinfo.dataservice.commons.db.DBOraclePoolManager;
import com.navinfo.dataservice.commons.util.Log4jUtils;
import com.navinfo.dataservice.web.util.ResponseUtil;

@Controller
public class EditController {
	private static final Logger logger = Logger
			.getLogger(EditController.class);

	@RequestMapping(value = "/editsupport/edit")
	public void edit(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		ResponseUtil.setResponseHeader(response);

		String parameter = request.getParameter("parameter");

		try {

			Transaction t = new Transaction(parameter);

			String msg = t.run();

			String log = t.getLogs();

			JSONObject json = new JSONObject();

			json.put("result", msg);

			json.put("log", log);

			json.put("check", t.getCheckLog());

			json.put("pid", t.getPid());

			response.getWriter().println(
					ResponseUtil.assembleRegularResult(json));

		} catch (Exception e) {

			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtil.assembleFailResult(e.getMessage(), logid));
		}
	}

	@RequestMapping(value = "/editsupport/getByCondition")
	public void getByCondition(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		ResponseUtil.setResponseHeader(response);

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String objType = jsonReq.getString("type");

			int projectId = jsonReq.getInt("projectId");

			JSONObject data = jsonReq.getJSONObject("data");

			SearchProcess p = new SearchProcess(projectId);

			JSONArray array = p.searchDataByCondition(ObjType.valueOf(objType),
					data);

			response.getWriter().println(
					ResponseUtil.assembleRegularResult(array));

		} catch (Exception e) {

			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtil.assembleFailResult(e.getMessage(), logid));
		}
	}

	@RequestMapping(value = "/editsupport/getByPid")
	public void getByPid(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		ResponseUtil.setResponseHeader(response);

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String objType = jsonReq.getString("type");

			int projectId = jsonReq.getInt("projectId");

			if (jsonReq.containsKey("detailId")) {
				int detailId = jsonReq.getInt("detailId");

				Connection conn = DBOraclePoolManager.getConnection(projectId);

				RdBranchSelector selector = new RdBranchSelector(conn);

				IRow row = selector.loadByDetailId(detailId, false);

				if (row != null) {

					response.getWriter().println(
							ResponseUtil.assembleRegularResult(row
									.Serialize(ObjLevel.FULL)));

				} else {
					response.getWriter().println(
							ResponseUtil.assembleRegularResult(null));
				}

			} else {
				int pid = jsonReq.getInt("pid");

				SearchProcess p = new SearchProcess(projectId);

				IObj obj = p.searchDataByPid(ObjType.valueOf(objType), pid);

				if (obj != null) {

					response.getWriter().println(
							ResponseUtil.assembleRegularResult(obj
									.Serialize(ObjLevel.FULL)));

				} else {
					response.getWriter().println(
							ResponseUtil.assembleRegularResult(null));
				}
			}
		} catch (Exception e) {

			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtil.assembleFailResult(e.getMessage(), logid));
		}
	}

	@RequestMapping(value = "/editsupport/getBySpatial")
	public void getBySpatial(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		ResponseUtil.setResponseHeader(response);

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String wkt = jsonReq.getString("wkt");

			JSONArray type = jsonReq.getJSONArray("type");

			int projectId = jsonReq.getInt("projectId");

			List<ObjType> types = new ArrayList<ObjType>();

			for (int i = 0; i < type.size(); i++) {
				types.add(ObjType.valueOf(type.getString(i)));
			}

			SearchProcess p = new SearchProcess(projectId);

			JSONObject data = p.searchDataBySpatial(types, wkt);

			response.getWriter().println(
					ResponseUtil.assembleRegularResult(data));

		} catch (Exception e) {

			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtil.assembleFailResult(e.getMessage(), logid));

		}

	}
}
