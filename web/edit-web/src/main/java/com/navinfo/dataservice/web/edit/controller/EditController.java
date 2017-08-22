package com.navinfo.dataservice.web.edit.controller;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import com.navinfo.dataservice.dao.glm.selector.SearchAllObject;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.commons.exception.DataNotChangeException;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.commons.util.JsonUtils;
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
	public ModelAndView run(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");
		logger.info("parameter====" + parameter);
		AccessToken tokenObj = (AccessToken) request.getAttribute("token");
		// 修改net.sf.JSONObject的bug：string转json对象损失精度问题（解决方案目前有两种:
		// 一种替换新的jar包以及依赖的包，第二种先转fastjson后再转net.sf）
		com.alibaba.fastjson.JSONObject fastJson = com.alibaba.fastjson.JSONObject
				.parseObject(parameter);

		JSONObject paraJson = JsonUtils.fastJson2netJson(fastJson);

		try {
			long beginRunTime = System.currentTimeMillis();
			logger.info("BEGIN EDIT RUN");
			Transaction t = new Transaction(parameter);
			// 加载用户ID
			t.setUserId(tokenObj.getUserId());
			// 加载用户taskId
			if (paraJson.containsKey("subtaskId")) {
				t.setSubTaskId(paraJson.getInt("subtaskId"));
			}
			// 加载数据库类型
			if (paraJson.containsKey("dbType")) {
				t.setDbType(paraJson.getInt("dbType"));
			}
			String msg = t.run();

			String log = t.getLogs();

			JSONObject json = new JSONObject();

			json.put("result", msg);

			json.put("log", log);

			json.put("check", t.getCheckLog());

			json.put("pid", t.getPid());
			long endRunTime = System.currentTimeMillis();
			logger.info("END EDIT RUN");
			logger.info("edit run total use time   "
					+ String.valueOf(endRunTime - beginRunTime));
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
	public ModelAndView getByCondition(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		Connection conn = null;

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String objType = jsonReq.getString("type");

			int dbId = jsonReq.getInt("dbId");

			JSONObject data = jsonReq.getJSONObject("data");

			conn = DBConnector.getInstance().getConnectionById(dbId);

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

	@RequestMapping(value = "/getByElementCondition")
	public ModelAndView getSearchBy(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		Connection conn = null;

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			if (jsonReq.containsKey("uuid")) {

				int dbId = jsonReq.getInt("dbId");

				conn = DBConnector.getInstance().getConnectionById(dbId);

				SearchAllObject p = new SearchAllObject(conn);

				JSONObject jsonObject = p.loadByElementCondition(jsonReq);

				return new ModelAndView("jsonView", success(jsonObject));
			}

			ObjType objType = ObjType.valueOf(jsonReq.getString("type"));
			int pageNum = jsonReq.getInt("pageNum");
			int pageSize = jsonReq.getInt("pageSize");
			int dbId = jsonReq.getInt("dbId");
			JSONObject data = jsonReq.getJSONObject("data");
			conn = DBConnector.getInstance().getConnectionById(dbId);
			SelectorUtils selectorUtils = new SelectorUtils(conn);
			JSONObject jsonObject = selectorUtils.loadByElementCondition(data,
					objType, pageSize, pageNum, false);
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
	public ModelAndView getByPid(HttpServletRequest request)
			throws ServletException, IOException {

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
				IRow row = selector.loadByDetailId(detailId, branchType, rowId,
						false);

				if (row != null) {

					JSONObject obj = row.Serialize(ObjLevel.FULL);
					if (!obj.containsKey("geometry")) {
						int pageNum = 1;
						int pageSize = 1;
						JSONObject data = new JSONObject();
						String primaryKey = "branch_pid";
						if(row instanceof IObj){
							IObj iObj = (IObj)row;
							primaryKey = iObj.primaryKey().toLowerCase();
						}
						data.put(primaryKey, row.parentPKValue());
						SelectorUtils selectorUtils = new SelectorUtils(conn);
						JSONObject jsonObject = selectorUtils
								.loadByElementCondition(data,
										row.objType(), pageSize,
										pageNum, false);
						obj.put("geometry", jsonObject.getJSONArray("rows")
								.getJSONObject(0).getString("geometry"));
					}
					
					obj.put("geoLiveType", objType);
					
					
					return new ModelAndView("jsonView", success(obj));

				} else {
					return new ModelAndView("jsonView", success());
				}

			} else {
				int pid = jsonReq.getInt("pid");

				SearchProcess p = new SearchProcess(conn);

				IObj obj = p.searchDataByPid(ObjType.valueOf(objType), pid);

				if (obj != null) {
					JSONObject json = obj2Json( obj,  pid,  objType,  conn);
					return new ModelAndView("jsonView", success(json));

				} else {
					return new ModelAndView("jsonView", success());
				}
			}
		} catch (Exception e) {
			logger.info(e.getMessage(), e);

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

	@RequestMapping(value = "/getDelByPid")
	public ModelAndView getDelByPid(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		Connection conn = null;

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String objType = jsonReq.getString("type");

			int dbId = jsonReq.getInt("dbId");

			conn = DBConnector.getInstance().getConnectionById(dbId);

			int pid = jsonReq.getInt("pid");

			SearchProcess p = new SearchProcess(conn);

			IObj obj = p.searchDelDataByPid(ObjType.valueOf(objType), pid);

			if (obj != null) {
				JSONObject json = obj2Json( obj,  pid,  objType,  conn);

				return new ModelAndView("jsonView", success(json));

			} else {
				return new ModelAndView("jsonView", success());
			}

		} catch (Exception e) {
			logger.info(e.getMessage(), e);

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

	private JSONObject obj2Json(IObj obj, int pid, String objType, Connection conn) throws Exception {

		JSONObject json = obj.Serialize(ObjLevel.FULL);

		if (!json.containsKey("geometry")) {

			int pageNum = 1;

			int pageSize = 1;

			JSONObject data = new JSONObject();

			data.put(obj.primaryKey().toLowerCase(), pid);

			SelectorUtils selectorUtils = new SelectorUtils(conn);

			JSONObject jsonObject = selectorUtils.loadByElementCondition(data,
					ObjType.valueOf(objType), pageSize, pageNum, false);

			json.put("geometry", jsonObject.getJSONArray("rows").getJSONObject(0).getString("geometry"));
		}
		json.put("geoLiveType", objType);

		return json;
	}

	@RequestMapping(value = "/getByPids")
	public ModelAndView getByPids(HttpServletRequest request)
			throws ServletException, IOException {

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
				IRow row = selector.loadByDetailId(detailId, branchType, rowId,
						false);

				if (row != null) {

					JSONObject json = row.Serialize(ObjLevel.FULL);
					json.put("geoLiveType", objType);
					return new ModelAndView("jsonView", success(json));

				} else {
					return new ModelAndView("jsonView", success());
				}

			} else {
				JSONArray pidArray = jsonReq.getJSONArray("pids");

				SearchProcess p = new SearchProcess(conn);

				List<? extends IRow> objList = p.searchDataByPids(
						ObjType.valueOf(objType), pidArray);

				JSONArray array = new JSONArray();

				if (objList != null) {

					for (IRow obj : objList) {
						JSONObject json = obj.Serialize(ObjLevel.FULL);
						json.put("geoLiveType", objType);
						array.add(json);
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
	public ModelAndView getBySpatial(HttpServletRequest request)
			throws ServletException, IOException {

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
	public ModelAndView applyPid(HttpServletRequest request)
			throws ServletException, IOException {

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
	public ModelAndView searchRdName(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String name = jsonReq.getString("name");

			int pageSize = jsonReq.getInt("pageSize");

			int pageNum = jsonReq.getInt("pageNum");

			int dbId = jsonReq.getInt("dbId");

			RdNameSelector selector = new RdNameSelector();

			JSONObject data = selector.searchByName(name, pageSize, pageNum,
					dbId);

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
	public ModelAndView getRoadBaseRelease(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");
		AccessToken tokenObj = (AccessToken) request.getAttribute("token");
		long userId=tokenObj.getUserId();
		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);
			int subtaskId = jsonReq.getInt("subtaskId");
			Release release = new Release();
			release.roadRelease(subtaskId,userId);
			return new ModelAndView("jsonView", success());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}
	
	
	/**
	 * 查询以渲染格式返回的数据
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping(value = "/getObject")
	public ModelAndView getObject(HttpServletRequest request)
			throws ServletException, IOException {	
		
		String parameter = request.getParameter("parameter");

		Connection conn = null;

		try {
			JSONObject condition = JSONObject.fromObject(parameter);

			int dbId = condition.getInt("dbId");
			
			conn = DBConnector.getInstance().getConnectionById(dbId);

			SearchProcess p = new SearchProcess(conn);

			JSONObject array = p.searchDataByObject(condition);

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
	
	/**
	 * 根据node查询以渲染格式返回的link
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping(value = "/getLinkByNode")
	public ModelAndView getLinkByNode(HttpServletRequest request)
			throws ServletException, IOException {	
		
		String parameter = request.getParameter("parameter");

		Connection conn = null;

		try {
			JSONObject condition = JSONObject.fromObject(parameter);

			int dbId = condition.getInt("dbId");
			
			conn = DBConnector.getInstance().getConnectionById(dbId);

			SearchProcess p = new SearchProcess(conn);

			JSONObject array = p.searchLinkByNode(condition);

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

	/**
	 * 根据node查询以渲染格式返回的link
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping(value = "/getGeoLiveInfo")
	public ModelAndView getGeoLiveInfo(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		Connection conn = null;

		try {
			JSONObject condition = JSONObject.fromObject(parameter);

			SearchAllObject p = new SearchAllObject();

			JSONObject result = p.getGeoLiveInfo(condition);

			return new ModelAndView("jsonView", success(result));

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
