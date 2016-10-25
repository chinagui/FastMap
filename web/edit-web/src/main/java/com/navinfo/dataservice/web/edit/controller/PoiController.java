package com.navinfo.dataservice.web.edit.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.commons.util.Log4jUtils;
import com.navinfo.dataservice.commons.util.ZipUtils;
import com.navinfo.dataservice.control.app.download.DownloadOperation;
import com.navinfo.dataservice.control.app.upload.UploadOperation;
import com.navinfo.dataservice.control.app.search.Operation;
import com.navinfo.dataservice.engine.photo.CollectorImport;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
public class PoiController extends BaseController{
	private static final Logger logger = Logger.getLogger(EditController.class);
	
	/**
	 * 安卓下载
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
	 * 安卓上传
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
			
			CollectorImport.importPhoto(filePath);
			
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
	// 此接口采集端不需要，已跟刘妍希确认 20161025
//	@RequestMapping(value = "/poi/base/getByRowId")
//	public ModelAndView getByRowId(HttpServletRequest request,
//			HttpServletResponse response) throws ServletException, IOException {
//		String parameter = request.getParameter("parameter");
//		try{
//			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
//			JSONObject jsonReq = JSONObject.fromObject(parameter);
//
//			String rowId = jsonReq.getString("rowId");
//			double x = jsonReq.getDouble("x");
//			double y = jsonReq.getDouble("y");
//			
//			Operation operation = new Operation();
//			
//			JSONObject ret = operation.getByRowId(rowId, x, y);
//			
//			return new ModelAndView("jsonView", success(ret));
//		} catch (Exception e) {
//			String logid = Log4jUtils.genLogid();
//
//			Log4jUtils.error(logger, logid, parameter, e);
//
//			return new ModelAndView("jsonView", fail(e.getMessage()));
//		}
//	}
//	
	
	
	private String unzipByJobId(int jobId) throws Exception{
		
		UploadOperation operation = new UploadOperation();
		
		JSONObject uploadInfo = operation.getUploadInfo(jobId);

		String fileName = uploadInfo.getString("fileName");

		String filePath = uploadInfo.getString("filePath") + "/" + jobId;

		ZipUtils.unzipFile(filePath + "/" + fileName, filePath);
		
		return filePath;
	}
	

}
