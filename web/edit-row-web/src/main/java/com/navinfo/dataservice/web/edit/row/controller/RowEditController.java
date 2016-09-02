package com.navinfo.dataservice.web.edit.row.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.api.edit.iface.EditApi;
import com.navinfo.dataservice.api.job.iface.JobApi;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.exception.DataNotChangeException;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.DBUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
public class RowEditController extends BaseController {
	private static final Logger logger = Logger.getLogger(RowEditController.class);

	@RequestMapping(value = "/run")
	public ModelAndView run(HttpServletRequest request) throws Exception {

		String parameter = request.getParameter("parameter");
		
		Connection conn = null;
		
		try {

			JSONObject json = JSONObject.fromObject(parameter);
			
			OperType operType = Enum.valueOf(OperType.class, json.getString("command"));
			
			ObjType objType = Enum.valueOf(ObjType.class, json.getString("type"));
			
			int dbId = json.getInt("dbId");
			
			conn = DBConnector.getInstance().getConnectionById(dbId);
			
			EditApi editApi = (EditApi) ApplicationContextUtil.getBean("editApi");

			JSONObject result = editApi.run(json);
			
			StringBuffer buf = new StringBuffer();
			
			int pid = 0;
			
			if(operType !=  OperType.CREATE)
			{
				if(objType == ObjType.IXSAMEPOI)
				{
					String poiPids = JsonUtils.getStringValueFromJSONArray(json.getJSONArray("poiPids"));
					buf.append(poiPids);
				}
				else
				{
					pid = json.getInt("objId");
					
					buf.append(String.valueOf(pid));
				}
			}
			else
			{
				pid = result.getInt("pid");
				buf.append(String.valueOf(pid));
			}
			
//			json.put("objId", pid);
//			BatchProcess batchProcess = new BatchProcess();
//			batchProcess.execute(json, conn);
			
			upatePoiStatus(buf.toString(),conn);
			
			return new ModelAndView("jsonView", success(result));
		} catch (DataNotChangeException e) {
			DbUtils.rollbackAndClose(conn);
			logger.error(e.getMessage(), e);
			return new ModelAndView("jsonView", success(e.getMessage()));
		} catch (Exception e) {
			DbUtils.rollbackAndClose(conn);
			logger.error(e.getMessage(), e);
			return new ModelAndView("jsonView", fail(e.getMessage()));
		} finally {
			DbUtils.close(conn);
		}
	}

	@RequestMapping(value = "/poi/base/list")
	public ModelAndView getPoiList(HttpServletRequest request) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		Connection conn = null;

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			int dbId = jsonReq.getInt("dbId");
			// 项目管理（放开）
			// subtaskId
			int subtaskId = jsonReq.getInt("subtaskId");
			int type = jsonReq.getInt("type");
			ManApi apiService = (ManApi) ApplicationContextUtil.getBean("manApi");
			Subtask subtask = apiService.queryBySubtaskId(subtaskId);
			int pageNum = jsonReq.getInt("pageNum");
			int pageSize = jsonReq.getInt("pageSize");
			int pid = 0;
			String pidName = "";
			if (jsonReq.containsKey("pidName")) {
				pidName = jsonReq.getString("pidName");
			}
			if (jsonReq.containsKey("pid")) {
				pid = jsonReq.getInt("pid");
			}
			conn = DBConnector.getInstance().getConnectionById(dbId);
			IxPoiSelector selector = new IxPoiSelector(conn);
			JSONObject jsonObject = selector.loadPids(false, pid, pidName, type, subtask.getGeometry(), pageSize,
					pageNum);
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
	public ModelAndView getPoiBaseCount(HttpServletRequest request) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");
		Connection conn = null;
		Connection manConn = null;
		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);
			int subtaskId = jsonReq.getInt("subtaskId");
			ManApi apiService = (ManApi) ApplicationContextUtil.getBean("manApi");
			manConn = DBConnector.getInstance().getManConnection();
			Subtask subtaskObj = apiService.queryBySubtaskId(subtaskId);
			String sql = "SELECT E.STATUS, COUNT(1) COUNT_NUM " + "  FROM POI_EDIT_STATUS E, IX_POI P"
					+ " WHERE E.ROW_ID = P.ROW_ID" + "   AND E.STATUS IN (1,2)"
					+ "   AND SDO_RELATE(P.GEOMETRY, SDO_GEOMETRY('" + subtaskObj.getGeometry()
					+ "', 8307), 'MASK=ANYINTERACT') =" + "       'TRUE'" + " GROUP BY E.STATUS";
			int dbId = subtaskObj.getDbId();
			conn = DBConnector.getInstance().getConnectionById(dbId);
			ResultSetHandler<JSONObject> rsHandler = new ResultSetHandler<JSONObject>() {
				public JSONObject handle(ResultSet rs) throws SQLException {
					JSONObject staticsObj = new JSONObject();
					int total = 0;
					while (rs.next()) {
						staticsObj.put(rs.getInt("STATUS"), rs.getInt("COUNT_NUM"));
						total += rs.getInt("COUNT_NUM");
					}
					staticsObj.put(4, total);
					return staticsObj;
				}
			};
			QueryRunner run = new QueryRunner();
			JSONObject resultJson = run.query(conn, sql, rsHandler);
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
	 * poi操作修改poi状态为已作业，鲜度信息为0 zhaokk sourceFlag 0 web 1 Android
	 * 
	 * @param row
	 * @throws Exception
	 */
	public void upatePoiStatus(String pids,Connection conn) throws Exception {
		StringBuilder sb = new StringBuilder(" MERGE INTO poi_edit_status T1 ");
		sb.append(" USING (SELECT row_id as a , 2 AS b,0 AS C FROM ix_poi where pid in ("+pids+")) T2 ");
		sb.append(" ON ( T1.row_id=T2.a) ");
		sb.append(" WHEN MATCHED THEN ");
		sb.append(" UPDATE SET T1.status = T2.b,T1.fresh_verified= T2.c ");
		sb.append(" WHEN NOT MATCHED THEN ");
		sb.append(" INSERT (T1.row_id,T1.status,T1.fresh_verified) VALUES(T2.a,T2.b,T2.c)");
		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.executeUpdate();
		} catch (Exception e) {
			throw e;

		} finally {
			DBUtils.closeStatement(pstmt);
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
