package com.navinfo.dataservice.web.edit.controller;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.commons.exception.DataNotChangeException;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.selector.SelectorUtils;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.rdname.RdNameSelector;
import com.navinfo.dataservice.engine.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.search.SearchProcess;
import com.navinfo.dataservice.engine.release.Release;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
public class EditController extends BaseController {
	private static final Logger logger = Logger.getLogger(EditController.class);

	@RequestMapping(value = "/run")
	public ModelAndView run(HttpServletRequest request) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");
		logger.info("parameter===="+parameter);
		AccessToken tokenObj = (AccessToken) request.getAttribute("token");

		try {
			long beginRunTime = System.currentTimeMillis();
			logger.info("BEGIN EDIT RUN");
			Transaction t = new Transaction(parameter);
			t.setUserId(tokenObj.getUserId());
			String msg = t.run();

			String log = t.getLogs();

			JSONObject json = new JSONObject();

			json.put("result", msg);

			json.put("log", log);

			json.put("check", t.getCheckLog());

			json.put("pid", t.getPid());
			long endRunTime = System.currentTimeMillis();
			logger.info("END EDIT RUN");
			logger.info("edit run total use time   " + String.valueOf(endRunTime - beginRunTime));
			if (parameter.contains("\"infect\":1")) {
				return new ModelAndView("jsonView", infect(json));
			} else {
				return new ModelAndView("jsonView", success(json));
			}
		} catch (DataNotChangeException e) {
			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", success(e.getMessage()));
		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}

	@RequestMapping(value = "/getByCondition")
	public ModelAndView getByCondition(HttpServletRequest request) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		Connection conn = null;

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String objType = jsonReq.getString("type");

			int dbId = jsonReq.getInt("dbId");

			JSONObject data = jsonReq.getJSONObject("data");

			conn = DBConnector.getInstance().getConnectionById(dbId);

			SearchProcess p = new SearchProcess(conn);

			JSONArray array = p.searchDataByCondition(ObjType.valueOf(objType), data);

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

	@RequestMapping(value = "/getByElementCondition")
	public ModelAndView getSearchBy(HttpServletRequest request) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		Connection conn = null;

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String tableName = jsonReq.getString("type");
			int pageNum = jsonReq.getInt("pageNum");
			int pageSize = jsonReq.getInt("pageSize");
			int dbId = jsonReq.getInt("dbId");
			JSONObject data = jsonReq.getJSONObject("data");
			conn = DBConnector.getInstance().getConnectionById(dbId);
			SelectorUtils selectorUtils = new SelectorUtils(conn);
			JSONObject jsonObject = selectorUtils.loadByElementCondition(data, tableName, pageSize, pageNum, false);
			return new ModelAndView("jsonView", success(jsonObject));

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
	public ModelAndView getByPid(HttpServletRequest request) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		Connection conn = null;

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String objType = jsonReq.getString("type");

			int dbId = jsonReq.getInt("dbId");

			conn = DBConnector.getInstance().getConnectionById(dbId);

			if (jsonReq.containsKey("detailId")) {
				int detailId = jsonReq.getInt("detailId");
				int branchType = jsonReq.getInt("branchType");
				String rowId = jsonReq.getString("rowId");
				RdBranchSelector selector = new RdBranchSelector(conn);
				IRow row = selector.loadByDetailId(detailId, branchType, rowId, false);

				if (row != null) {

					return new ModelAndView("jsonView", success(row.Serialize(ObjLevel.FULL)));

				} else {
					return new ModelAndView("jsonView", success());
				}

			} else {
				int pid = jsonReq.getInt("pid");

				SearchProcess p = new SearchProcess(conn);

				IObj obj = p.searchDataByPid(ObjType.valueOf(objType), pid);

				if (obj != null) {

					return new ModelAndView("jsonView", success(obj.Serialize(ObjLevel.FULL)));

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

	@RequestMapping(value = "/getByPids")
	public ModelAndView getByPids(HttpServletRequest request) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		Connection conn = null;

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String objType = jsonReq.getString("type");

			int dbId = jsonReq.getInt("dbId");

			conn = DBConnector.getInstance().getConnectionById(dbId);

			if (jsonReq.containsKey("detailId")) {
				int detailId = jsonReq.getInt("detailId");
				int branchType = jsonReq.getInt("branchType");
				String rowId = jsonReq.getString("rowId");
				RdBranchSelector selector = new RdBranchSelector(conn);
				IRow row = selector.loadByDetailId(detailId, branchType, rowId, false);

				if (row != null) {

					return new ModelAndView("jsonView", success(row.Serialize(ObjLevel.FULL)));

				} else {
					return new ModelAndView("jsonView", success());
				}

			} else {
				JSONArray pidArray = jsonReq.getJSONArray("pids");

				SearchProcess p = new SearchProcess(conn);

				List<? extends IObj> objList = p.searchDataByPids(ObjType.valueOf(objType), pidArray);

				JSONArray array = new JSONArray();

				if (objList != null) {

					for (IObj obj : objList) {
						array.add(obj.Serialize(ObjLevel.FULL));
					}
					return new ModelAndView("jsonView", success(array));
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
	public ModelAndView getBySpatial(HttpServletRequest request) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		Connection conn = null;

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String wkt = jsonReq.getString("wkt");

			JSONArray type = jsonReq.getJSONArray("types");

			int dbId = jsonReq.getInt("dbId");

			List<ObjType> types = new ArrayList<ObjType>();

			for (int i = 0; i < type.size(); i++) {
				types.add(ObjType.valueOf(type.getString(i)));
			}

			conn = DBConnector.getInstance().getConnectionById(dbId);

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
	public ModelAndView applyPid(HttpServletRequest request) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		Connection conn = null;

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String type = jsonReq.getString("type");

			if (type.equals("rtic")) {

				int code = PidUtil.getInstance().applyRticCode();

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

	@RequestMapping(value = "/rdname/search")
	public ModelAndView searchRdName(HttpServletRequest request) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String name = jsonReq.getString("name");

			int pageSize = jsonReq.getInt("pageSize");

			int pageNum = jsonReq.getInt("pageNum");

			int dbId = jsonReq.getInt("dbId");

			RdNameSelector selector = new RdNameSelector();

			JSONObject data = selector.searchByName(name, pageSize, pageNum, dbId);

			return new ModelAndView("jsonView", success(data));

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}

	/**
	 * road提交 根据所选grid进行road数据的提交
	 * 
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping(value = "/road/base/release")
	public ModelAndView getRoadBaseRelease(HttpServletRequest request) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");
		AccessToken tokenObj = (AccessToken) request.getAttribute("token");
		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);
			int subtaskId = jsonReq.getInt("subtaskId");
			Release release = new Release();
			release.roadRelease(subtaskId);
			return new ModelAndView("jsonView", success());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}
}
