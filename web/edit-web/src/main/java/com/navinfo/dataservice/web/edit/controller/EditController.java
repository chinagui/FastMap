package com.navinfo.dataservice.web.edit.controller;

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

import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchSelector;
import com.navinfo.dataservice.dao.pidservice.PidService;
import com.navinfo.dataservice.dao.pool.GlmDbPoolManager;
import com.navinfo.dataservice.engine.edit.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.edit.search.SearchProcess;

@Controller
public class EditController extends BaseController {
	private static final Logger logger = Logger.getLogger(EditController.class);

	@RequestMapping(value = "/run")
	public ModelAndView run(HttpServletRequest request)
			throws ServletException, IOException {

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

			return new ModelAndView("jsonView", success(json));
		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}

	@RequestMapping(value = "/getByCondition")
	public ModelAndView getByCondition(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		Connection conn = null;

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String objType = jsonReq.getString("type");

			int projectId = jsonReq.getInt("projectId");

			JSONObject data = jsonReq.getJSONObject("data");

			conn = GlmDbPoolManager.getInstance().getConnection(projectId);

			SearchProcess p = new SearchProcess(conn);

			JSONArray array = p.searchDataByCondition(ObjType.valueOf(objType),
					data);

			return new ModelAndView("jsonView", success(array));

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

	@RequestMapping(value = "/getByPid")
	public ModelAndView getByPid(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		Connection conn = null;

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String objType = jsonReq.getString("type");

			int projectId = jsonReq.getInt("projectId");

			conn = GlmDbPoolManager.getInstance().getConnection(projectId);

			if (jsonReq.containsKey("detailId")) {
				int detailId = jsonReq.getInt("detailId");

				RdBranchSelector selector = new RdBranchSelector(conn);

				IRow row = selector.loadByDetailId(detailId, false);

				if (row != null) {

					return new ModelAndView("jsonView",
							success(row.Serialize(ObjLevel.FULL)));

				} else {
					return new ModelAndView("jsonView", success());
				}

			} else {
				int pid = jsonReq.getInt("pid");

				SearchProcess p = new SearchProcess(conn);

				IObj obj = p.searchDataByPid(ObjType.valueOf(objType), pid);

				if (obj != null) {

					return new ModelAndView("jsonView",
							success(obj.Serialize(ObjLevel.FULL)));

				} else {
					return new ModelAndView("jsonView", success());
				}
			}
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

	@RequestMapping(value = "/getBySpatial")
	public ModelAndView getBySpatial(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		Connection conn = null;

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String wkt = jsonReq.getString("wkt");

			JSONArray type = jsonReq.getJSONArray("type");

			int projectId = jsonReq.getInt("projectId");

			List<ObjType> types = new ArrayList<ObjType>();

			for (int i = 0; i < type.size(); i++) {
				types.add(ObjType.valueOf(type.getString(i)));
			}

			conn = GlmDbPoolManager.getInstance().getConnection(projectId);

			SearchProcess p = new SearchProcess(conn);

			JSONObject data = p.searchDataBySpatial(types, wkt);

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

	@RequestMapping(value = "/applyPid")
	public ModelAndView applyPid(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		Connection conn = null;

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String type = jsonReq.getString("type");

			if (type.equals("rtic")) {

				int code = PidService.getInstance().applyRticCode();

				return new ModelAndView("jsonView", success(code));

			} else {
				throw new Exception("类型错误");
			}

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
}
