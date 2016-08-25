package com.navinfo.dataservice.web.edit.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.api.job.iface.JobApi;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.exception.DataNotChangeException;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.selector.SelectorUtils;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchSelector;
import com.navinfo.dataservice.dao.pidservice.PidService;
import com.navinfo.dataservice.engine.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.search.SearchProcess;
import com.navinfo.navicommons.database.QueryRunner;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

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
		}
		catch (DataNotChangeException e)
		{
			logger.error(e.getMessage(), e);
			
			return new ModelAndView("jsonView", success(e.getMessage()));
		}
		catch (Exception e) {

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

			String tableName = jsonReq.getString("type");
			int pageNum = jsonReq.getInt("pageNum");
			int pageSize = jsonReq.getInt("pageSize");
			int dbId = jsonReq.getInt("dbId");
			JSONObject data = jsonReq.getJSONObject("data");
			conn = DBConnector.getInstance().getConnectionById(dbId);
			SelectorUtils selectorUtils = new SelectorUtils(conn);
			JSONObject jsonObject = selectorUtils.loadByElementCondition(data,tableName, pageSize, pageNum, false);
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
				String rowId =jsonReq.getString("rowId");
				RdBranchSelector selector = new RdBranchSelector(conn);
				IRow row = selector.loadByDetailId(detailId,branchType,rowId, false);

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

	@RequestMapping(value = "/poi/base/list")
	public ModelAndView getPoiList(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		Connection conn = null;

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			int dbId = jsonReq.getInt("dbId");
			// 项目管理（放开）
			// subtaskId
			int subtaskId = jsonReq.getInt("subtaskId");
			int type = jsonReq.getInt("type");
			ManApi apiService=(ManApi) ApplicationContextUtil.getBean("manApi");
			Subtask subtask = apiService.queryBySubtaskId(subtaskId);
			int pageNum = jsonReq.getInt("pageNum");
			int pageSize = jsonReq.getInt("pageSize");
			int pid =0;
			String pidName = "";
			if (jsonReq.containsKey("pidName")){
				pidName = jsonReq.getString("pidName");
			}if(jsonReq.containsKey("pid")){
				pid =jsonReq.getInt("pid");
			}
			conn = DBConnector.getInstance().getConnectionById(dbId);
			IxPoiSelector selector = new IxPoiSelector(conn);
			JSONObject jsonObject = selector.loadPids(false,pid,pidName,type,subtask.getGeometry(),pageSize, pageNum);
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
	
	@RequestMapping(value = "/poi/base/count")
	public ModelAndView getPoiBaseCount(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");
		Connection conn = null;
		Connection manConn=null;
		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);
			int subtaskId=jsonReq.getInt("subtaskId");
			ManApi apiService=(ManApi) ApplicationContextUtil.getBean("manApi");
			manConn=DBConnector.getInstance().getManConnection();
			Subtask subtaskObj=apiService.queryBySubtaskId(subtaskId);
			String sql="SELECT E.STATUS, COUNT(1) COUNT_NUM "
					+ "  FROM POI_EDIT_STATUS E, IX_POI P"
					+ " WHERE E.ROW_ID = P.ROW_ID"
					+ "   AND E.STATUS IN (1,2)"
					+ "   AND SDO_RELATE(P.GEOMETRY, SDO_GEOMETRY('"+subtaskObj.getGeometry()+"', 8307), 'MASK=ANYINTERACT') ="
					+ "       'TRUE'"
					+ " GROUP BY E.STATUS";
			int dbId=subtaskObj.getDbId();
			conn = DBConnector.getInstance().getConnectionById(dbId);
			ResultSetHandler<JSONObject> rsHandler = new ResultSetHandler<JSONObject>(){
				public JSONObject handle(ResultSet rs) throws SQLException {
					JSONObject staticsObj=new JSONObject();
					int total=0;
					while(rs.next()){
						staticsObj.put(rs.getInt("STATUS"), rs.getInt("COUNT_NUM"));
						total+=rs.getInt("COUNT_NUM");
					}
					staticsObj.put(4, total);
					return staticsObj;
				}	    		
	    	};		
	    	QueryRunner run = new QueryRunner();
	    	JSONObject resultJson=run.query(conn, sql,rsHandler);
			return new ModelAndView("jsonView", success(resultJson));
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
			if (manConn != null) {
				try {
					manConn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	/**
	 * POI提交
	 * 根据所选grid进行POI数据的提交，自动执行检查、批处理
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping(value = "/poi/base/release")
	public ModelAndView getPoiBaseRelease(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");
		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);
			int dbId = jsonReq.getInt("dbId");
			JSONArray gridIds=jsonReq.getJSONArray("gridIds");
			
			JSONObject jobReq=new JSONObject();
			jobReq.put("targetDbId", dbId);
			jobReq.put("gridIds", gridIds);
					
			AccessToken tokenObj=(AccessToken) request.getAttribute("token");
			long userId=tokenObj.getUserId();
			//long userId=2;
			JobApi apiService=(JobApi) ApplicationContextUtil.getBean("jobApi");
			long jobId=apiService.createJob("editPoiBaseRelease", jobReq, userId, "POI行编提交");	
			return new ModelAndView("jsonView", success(jobId));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}
	
}
