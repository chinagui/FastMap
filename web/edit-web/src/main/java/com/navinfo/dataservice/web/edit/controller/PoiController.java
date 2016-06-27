package com.navinfo.dataservice.web.edit.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.commons.util.Log4jUtils;
import com.navinfo.dataservice.commons.util.ZipUtils;
import com.navinfo.dataservice.engine.edit.edit.operation.obj.poi.android.Operation;
import com.navinfo.dataservice.engine.edit.edit.operation.obj.poi.download.DownloadOperation;
import com.navinfo.dataservice.engine.edit.edit.operation.obj.poi.upload.UploadOperation;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
public class PoiController extends BaseController{
	private static final Logger logger = Logger.getLogger(EditController.class);
	
	/**
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping(value = "/poi/base/download")
	public ModelAndView getPoi(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String parameter = request.getParameter("parameter");
		
		try{
			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			JSONArray gridDateList = new JSONArray();
			gridDateList = jsonReq.getJSONArray("grid");
			
			DownloadOperation operation = new DownloadOperation();
			String url = operation.getPoiUrl(gridDateList);
			
			return new ModelAndView("jsonView", success(url));
		}catch(Exception e){
			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
		
	}
	
	/**
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping(value = "/poi/base/upload")
	public ModelAndView importPoi(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String parameter = request.getParameter("parameter");
		
		try{
			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			JSONObject json = JSONObject.fromObject(parameter);

			int jobId = json.getInt("jobId");
			
			String filePath = unzipByJobId(jobId);

			UploadOperation operation = new UploadOperation();
			JSONObject retArray = operation.importPoi(filePath + "/poi.txt");
			
			return new ModelAndView("jsonView", success(retArray));
		}catch(Exception e){
			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
		
	}
	
	/**
	 * 安卓端检查是否有可下载的POI
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping(value = "/poi/base/download/check")
	public ModelAndView downloadCheck(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String parameter = request.getParameter("parameter");
		try{
			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			JSONArray gridDateList = new JSONArray();
			gridDateList = jsonReq.getJSONArray("grid");
			
			Operation operation = new Operation();
			JSONArray ret = operation.downloadCheck(gridDateList);
			
			return new ModelAndView("jsonView", success(ret));
		} catch (Exception e) {
			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}
	
	
	/**
	 * 根据rowId获取POI（返回名称和分类）
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping(value = "/poi/base/getByRowId")
	public ModelAndView getByRowId(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String parameter = request.getParameter("parameter");
		try{
			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String rowId = jsonReq.getString("rowId");
			double x = jsonReq.getDouble("x");
			double y = jsonReq.getDouble("y");
			
			Operation operation = new Operation();
			
			JSONObject ret = operation.getByRowId(rowId, x, y);
			
			return new ModelAndView("jsonView", success(ret));
		} catch (Exception e) {
			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}
	
	
	
	public String unzipByJobId(int jobId) throws Exception{
		
		JSONObject uploadInfo = getUploadInfo(jobId);

		String fileName = uploadInfo.getString("fileName");

		String filePath = uploadInfo.getString("filePath") + "/" + jobId;

		ZipUtils.unzipFile(filePath + "/" + fileName, filePath);
		
		return filePath;
	}
	
	public JSONObject getUploadInfo(int jobId) throws Exception {

		Connection conn = null;

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();

			JSONObject json = new JSONObject();

			String sql = "select * from dropbox_upload where job_id = :1";

			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, jobId);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				String fileName = resultSet.getString("file_name");

				String filePath = resultSet.getString("file_path");

				String md5 = resultSet.getString("md5");

				json.put("fileName", fileName);

				json.put("filePath", filePath);

				json.put("md5", md5);

			} else {
				throw new Exception("不存在对应的jobid:" + jobId);
			}

			return json;

		} catch (Exception e) {
			throw e;
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (pstmt != null) {
				pstmt.close();
			}
			if (conn != null) {
				conn.close();
			}
		}
	}

}
